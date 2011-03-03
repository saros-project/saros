/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 * (c) Stephan Lau - 2010
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.videosharing.encode;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.Codec;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.exceptions.EncoderInitializationException;
import de.fu_berlin.inf.dpp.videosharing.source.ImageSource;

/**
 * Encodes images ({@code ImageSource}) into one (and later maybe many) stream.
 * 
 * @author s-lau
 * 
 */
public abstract class Encoder implements Runnable {

    private static Logger log = Logger.getLogger(Encoder.class);

    @Inject
    protected Saros saros;

    protected VideoSharingSession videoSharingSession;

    protected IPreferenceStore preferences;

    protected Integer width = 320;
    protected Integer height = 240;
    protected Integer maxBitrate = 512 * 1024;
    protected int framerate = 10;

    /**
     * Pause-state of encoder. Subclasses should check the state via
     * {@link #isPaused()}.
     */
    private volatile boolean paused = false;
    /**
     * <code>true</code> when encoder was encoding and is stopped.
     */
    protected boolean stopped = false;
    protected Thread encoderThread = null;

    protected OutputStream out;
    protected ImageSource imageSource;
    protected volatile boolean isEncoding = false;
    protected CountDownLatch encodingLatch = new CountDownLatch(1);

    protected Encoder(OutputStream out, ImageSource source,
        VideoSharingSession videoSharingSession) {
        this.out = out;
        this.imageSource = source;

        this.videoSharingSession = videoSharingSession;
        assert videoSharingSession != null;

        SarosPluginContext.initComponent(this);

        preferences = saros.getPreferenceStore();
        width = preferences.getInt(PreferenceConstants.ENCODING_VIDEO_WIDTH);
        height = preferences.getInt(PreferenceConstants.ENCODING_VIDEO_HEIGHT);
        maxBitrate = preferences
            .getInt(PreferenceConstants.ENCODING_MAX_BITRATE);
        framerate = preferences
            .getInt(PreferenceConstants.ENCODING_VIDEO_FRAMERATE);
    }

    /**
     * Make encoder ready for start encoding. Any thread in
     * {@link #waitForStartEncoding()} will be released.
     * 
     * @return the Thread in which this encoder is running
     * @throws IllegalStateException
     *             when encoding already started
     */
    public final synchronized Thread startEncoding()
        throws IllegalStateException {
        if (isEncoding)
            throw new IllegalStateException();
        isEncoding = true;

        encoderThread = Utils.runSafeAsync("Videosharing-Encoder", log, this);

        encodingLatch.countDown();

        return encoderThread;
    }

    /**
     * Any caller will be blocked until encoding is started. When encoding
     * already started it returns immediately.
     * 
     * @blocking until {@link #startEncoding()} is called
     * @throws InterruptedException
     *             when Thread interrupted
     */
    public final void waitForStartEncoding() throws InterruptedException {
        encodingLatch.await();
    }

    /**
     * Toggles pause-state. If {@link #isPaused()} the {@link #encoderThread}
     * will be paused until this method is called again.
     * 
     * @throws IllegalStateException
     *             when not encoding
     */
    public final synchronized void pause() {
        if (!isEncoding)
            throw new IllegalStateException("Can't paused, not encoding!");
        paused = !paused;
        if (!paused) {
            LockSupport.unpark(encoderThread);
        }

    }

    /**
     * Get the pause-state of encoder. If a started encoder calls this method
     * and encoder is paused, it is blocked until it is unpaused.
     * Implementations should call this method frequently to realize pause.
     * 
     * @return pause-state of encoder
     */
    public final boolean isPaused() {
        Thread current = Thread.currentThread();

        if (current.equals(encoderThread) && paused) {
            LockSupport.park();
            if (Thread.interrupted())
                current.interrupt();
        }

        return paused;
    }

    /**
     * Finishes encoding.
     * 
     * @throws IllegalStateException
     */
    public final synchronized void stopEncoding() throws IllegalStateException {
        if (stopped)
            return;
        stopped = true;
        if (!isEncoding)
            throw new IllegalStateException(
                "Can't stop encoding when not started.");

        if (paused)
            // to unpark encoder-thread
            pause();

        isEncoding = false;

        stopEncodingInternal();
        try {
            out.close();
        } catch (IOException e) {
            log.error("Could not close stream: ", e);
        }
    }

    public Thread getEncoderThread() {
        return encoderThread;
    }

    /**
     * Finishes encoding, will be called after {@link #stopEncoding()}. This is
     * to make sure the encoder-thread can finish when {@link #isPaused()}.
     */
    protected abstract void stopEncodingInternal();

    public synchronized boolean isEncoding() {
        return isEncoding;
    }

    /**
     * Adjust bandwidth used by the encoded image-sequence.
     * 
     * @param newBandwidth
     *            in bytes
     */
    public abstract void setBandwidth(int newBandwidth);

    /**
     * 
     * @return <code>true</code> when bandwidth can be changed on fly
     */
    public abstract boolean supportSetBandwidth();

    /**
     * Change encoded frames per second during encoding.
     * 
     * @param newFps
     */
    public abstract void setFps(int newFps);

    /**
     * 
     * @return <code>true</code> when changing FPS during encoding is supported
     */
    public abstract boolean supportSetFps();

    /**
     * Adjust quality of encoded image-sequence during encoding. A lower quality
     * should result in lower bandwidth usage.
     * 
     * @param newQuality
     *            0..100, where 100 is default (when encoding started) and 0 is
     *            worst
     */
    public abstract void setQuality(int newQuality);

    /**
     * 
     * @return <code>true</code> when change quality during encoding is
     *         supported
     */
    public abstract boolean supportSetQuality();

    public static Encoder getEncoder(Codec codec, OutputStream out,
        ImageSource imageSource, VideoSharingSession videoSharingSession)
        throws EncoderInitializationException {
        switch (codec) {
        case XUGGLER:
            return new XugglerEncoder(out, imageSource, videoSharingSession);
        case IMAGE:
            return new ImageTileEncoder(out, imageSource, videoSharingSession);
        }

        // should not happen
        assert false;
        return null;
    }

    public static BufferedImage convertToType(BufferedImage sourceImage,
        int targetType) {
        BufferedImage image;

        // if the source image is already the target type, return the source
        // image

        if (sourceImage.getType() == targetType)
            image = sourceImage;

        // otherwise create a new image of the target type and draw the new
        // image

        else {
            image = new BufferedImage(sourceImage.getWidth(), sourceImage
                .getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }

        return image;
    }
}
