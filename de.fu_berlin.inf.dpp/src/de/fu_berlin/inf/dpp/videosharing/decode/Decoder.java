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
package de.fu_berlin.inf.dpp.videosharing.decode;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.Codec;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.exceptions.DecoderInitializationException;
import de.fu_berlin.inf.dpp.videosharing.player.VideoDisplay;

/**
 * Decoding an encoded stream and sending it to player(s) and/or other streams
 * (such as files).
 * 
 * @author s-lau
 * 
 */
public abstract class Decoder implements Runnable, Disposable {

    private static Logger log = Logger.getLogger(Decoder.class);

    protected VideoSharingSession videoSharingSession;

    protected InputStream videoInput;

    /**
     * Where this decoder sends it's decoded images to. It should not be
     * directly accessed implementations. They use
     * {@link #updatePlayer(BufferedImage)}.
     */
    private VideoDisplay videoPlayer;

    protected Thread decoderThread;

    protected Dimension videoDimension;
    protected String encodingFormatName;

    /* statistics */
    protected static final long STATISTICS_EVERY = 3000;
    protected Statistics statistic;
    protected DecodingStatisticPacket lastDecodingStatistic = null;
    protected Timer statisticTimer = new Timer("decodingStatisticTimer");

    /**
     * 
     * @param input
     *            data-stream
     * @param statisticOut
     *            decoding-statistics
     * @param width
     *            incoming video's width
     * @param height
     *            incoming video's height
     * @param encodingFormatName
     *            name of codec if the decoder needs one
     * @throws DecoderInitializationException
     */
    protected Decoder(InputStream input, ObjectOutputStream statisticOut,
        int width, int height, String encodingFormatName,
        VideoSharingSession videoSharingSession)
        throws DecoderInitializationException {
        this.videoInput = input;
        try {
            statistic = new Statistics(statisticOut);
        } catch (IOException e) {
            throw new DecoderInitializationException(e);
        }
        statisticTimer.scheduleAtFixedRate(statistic, STATISTICS_EVERY,
            STATISTICS_EVERY);

        videoDimension = new Dimension(width, height);
        this.encodingFormatName = encodingFormatName;

        this.videoSharingSession = videoSharingSession;
        assert videoSharingSession != null;
    }

    /**
     * Adds a {@link VideoDisplay} where decoded images will be send to. There
     * can only be one player. When one was previous set, it will be
     * overwritten.
     * 
     * @param player
     */
    public void addPlayer(VideoDisplay player) {
        videoPlayer = player;
    }

    /**
     * Updates image displayed in {@link #videoPlayer}
     * 
     * @param image
     */
    protected void updatePlayer(BufferedImage image) {
        if (image == null)
            return;
        if (videoPlayer == null)
            return;
        // check for resampling
        Dimension imageSize = new Dimension(videoDimension);
        Dimension sizeForPlayer = videoPlayer.getImageDimension(imageSize);
        if (sizeForPlayer != null && !sizeForPlayer.equals(imageSize)) {
            image = resample(image, sizeForPlayer);
        }
        videoPlayer.updateImage(image);
    }

    public DecodingStatisticPacket getLastDecodingStatistic() {
        return this.lastDecodingStatistic;
    }

    /**
     * Starts the decoder-thread when it is not started yet
     */
    public void startDecoder() {
        if (decoderThread == null)
            decoderThread = Utils
                .runSafeAsync("VideoSharing-Decoder", log, this);
    }

    public void dispose() {
        statisticTimer.cancel();
        decoderThread.interrupt();
    }

    public static BufferedImage resample(BufferedImage image, Dimension newSize) {
        if (newSize.height <= 0 || newSize.width <= 0)
            return null;
        if (image.getWidth() == newSize.width
            && image.getHeight() == newSize.height)
            // nothing to resample, they're equal
            return image;
        BufferedImage resampledImage = new BufferedImage(newSize.width,
            newSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resampledImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, newSize.width, newSize.height, null);

        return resampledImage;

    }

    public static Decoder getDecoder(Codec codec, InputStream input,
        ObjectOutputStream out, int width, int height,
        String encodingFormatName, VideoSharingSession videoSharingSession)
        throws DecoderInitializationException {
        switch (codec) {
        case XUGGLER:
            return new XugglerDecoder(input, out, width, height,
                encodingFormatName, videoSharingSession);
        case IMAGE:
            return new ImageTileDecoder(input, out, width, height,
                encodingFormatName, videoSharingSession);
        }

        // should not happen
        assert false;
        return null;
    }

    class Statistics extends TimerTask {

        protected long lastBytesRead = 0L;
        protected long lastFrames = 0L;
        protected long lastDelay = 0L;

        ObjectOutputStream out;

        public Statistics(ObjectOutputStream statisticOut) throws IOException {
            out = statisticOut;
            out.flush();
        }

        @Override
        public void run() {
            long bytesRead, frames, delay;
            synchronized (this) {
                bytesRead = lastBytesRead;
                frames = lastFrames;
                delay = lastDelay;
                resetLastStatistics();
            }
            if (videoInput instanceof StreamSession.StreamSessionInputStream) {
                StreamSession.StreamSessionInputStream in = (StreamSession.StreamSessionInputStream) videoInput;
                bytesRead = in.getReadBytes();
            }

            try {
                lastDecodingStatistic = new DecodingStatisticPacket(
                    STATISTICS_EVERY, frames, delay, bytesRead);
                out.writeObject(lastDecodingStatistic);
                out.flush();
            } catch (IOException e) {
                videoSharingSession.reportError(e);
            }
        }

        protected synchronized void dataRead(long bytes) {
            lastBytesRead += bytes;
        }

        protected synchronized void renderedFrame() {
            ++lastFrames;
        }

        protected synchronized void delay(long ms) {
            lastDelay += ms;
        }

        protected synchronized void resetLastStatistics() {
            lastBytesRead = 0;
            lastFrames = 0;
            lastDelay = 0;
        }

    }

}
