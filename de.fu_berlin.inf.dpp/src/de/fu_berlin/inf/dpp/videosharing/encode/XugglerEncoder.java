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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.decode.Decoder;
import de.fu_berlin.inf.dpp.videosharing.exceptions.EncoderInitializationException;
import de.fu_berlin.inf.dpp.videosharing.exceptions.EncodingException;
import de.fu_berlin.inf.dpp.videosharing.exceptions.xuggler.XugglerNotInstalledException;
import de.fu_berlin.inf.dpp.videosharing.source.ImageSource;

/**
 * @author s-lau
 * 
 */
public class XugglerEncoder extends Encoder {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(XugglerEncoder.class);

    /**
     * Throw error when {@link #slowEncoding} bigger than this
     */
    public final static int SLOW_ENCODING_ERROR = 10;

    /**
     * Incremented when encoding is too slow (can't reach given FPS).
     */
    protected int slowEncoding = 0;

    /* ffmpeg settings */
    protected boolean useVBV = false;
    protected double bitrateVariation = 0.65;
    protected double keyframeRate = 1;

    /* xuggler settings */
    protected IContainerFormat containerFormat;
    protected ICodec codec;
    protected static IPixelFormat.Type pixelformat;

    static {
        if (isInstalled())
            pixelformat = IPixelFormat.Type.YUV420P;
    }

    /* used for encoding */
    protected IContainer container;
    protected IStreamCoder coder;
    protected IConverter converter;

    /* statuses */
    private long firstTimestamp = -1;
    /**
     * size of last send picture.
     */
    private Dimension lastRenderedPictureSize = null;

    public XugglerEncoder(OutputStream out, ImageSource source,
        VideoSharingSession videoSharingSession)
        throws EncoderInitializationException {
        super(out, source, videoSharingSession);
        int errorNumber;

        if (!isInstalled())
            throw new EncoderInitializationException(
                new XugglerNotInstalledException());

        container = IContainer.make();

        initializeContainer(preferences
            .getString(PreferenceConstants.XUGGLER_CONTAINER_FORMAT),
            preferences.getString(PreferenceConstants.XUGGLER_CODEC));
        useVBV = preferences.getBoolean(PreferenceConstants.XUGGLER_USE_VBV);

        if ((errorNumber = container.open(out, containerFormat)) < 0)
            throw new EncoderInitializationException(IError.make(errorNumber)
                .getDescription());

        IStream stream = container.addNewStream(0);

        coder = createCoder(stream, codec, height, width, framerate);
        setBitrate(maxBitrate);
    }

    protected void initializeContainer(String containerFormatName,
        String codecFormatName) {
        containerFormat = IContainerFormat.make();
        containerFormat.setOutputFormat(containerFormatName, null, null);
        codec = ICodec.findEncodingCodecByName(codecFormatName);
    }

    public void run() {
        int errorNumber;
        if ((errorNumber = coder.open()) < 0) {
            videoSharingSession.reportError(new EncoderInitializationException(
                IError.make(errorNumber).getDescription()));
            return;
        }
        if ((errorNumber = container.writeHeader()) < 0) {
            videoSharingSession.reportError(new EncoderInitializationException(
                IError.make(errorNumber).getDescription()));
            return;
        }
        while (isEncoding) {
            isPaused();
            if (Thread.interrupted())
                stopEncoding();
            long trace = System.currentTimeMillis();
            encodeImage(imageSource.toImage());
            trace = System.currentTimeMillis() - trace;
            long frameIntervall = (long) (1000 / (double) framerate);

            if (trace < frameIntervall) {
                try {
                    Thread.sleep(frameIntervall - trace);
                } catch (InterruptedException e) {
                    stopEncoding();
                    return;
                }
                slowEncoding = 0;
            } else {
                // log.warn("Encoding too slow for framerate. " + trace
                // + " ms behind. " + (SLOW_ENCODING_ERROR - slowEncoding)
                // + " warnings in row remaining.");
                if (++slowEncoding > SLOW_ENCODING_ERROR) {
                    videoSharingSession
                        .reportError(new EncodingException(
                            "Can't encode that fast. Please choose lower frames per second."));
                    return;
                }
            }
        }
    }

    protected synchronized void encodeImage(BufferedImage image) {
        if (!isEncoding)
            return;

        int errorNumber;
        IPacket packet = IPacket.make();

        long now = System.currentTimeMillis();
        if (firstTimestamp == -1)
            firstTimestamp = now;

        if (converter == null || lastRenderedPictureSize == null
            || image.getWidth() != lastRenderedPictureSize.width
            || image.getHeight() != lastRenderedPictureSize.height) {
            lastRenderedPictureSize = new Dimension(image.getWidth(), image
                .getHeight());
            // close old converter if necessary
            if (converter != null)
                converter.delete();
            try {
                // converter =
                // ConverterFactory.createConverter("XUGGLER-BGR-24",
                // PIXELFORMAT, width, height, bgrImage.getWidth(), bgrImage
                // .getHeight());
                converter = ConverterFactory.createConverter("XUGGLER-BGR-24",
                    pixelformat, width, height, width, height);
            } catch (UnsupportedOperationException e) {
                videoSharingSession.reportError(new EncodingException(e));
                return;
            }
        }

        image = Decoder.resample(image, new Dimension(width, height));

        long timeStamp = (now - firstTimestamp) * 1000; // convert to
        // microseconds
        com.xuggle.xuggler.IVideoPicture outFrame = converter.toPicture(
            convertToType(image, BufferedImage.TYPE_3BYTE_BGR), timeStamp);

        if ((errorNumber = coder.encodeVideo(packet, outFrame, 0)) < 0) {
            videoSharingSession.reportError(new EncodingException(IError.make(
                errorNumber).getDescription()));
            return;
        }

        if (packet.isComplete()) {
            if ((errorNumber = container.writePacket(packet)) < 0) {
                videoSharingSession.reportError(new EncodingException(IError
                    .make(errorNumber).getDescription()));
                return;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu_berlin.inf.dpp.videosharing.encode.Encoder#stopEncodingInternal()
     */
    @Override
    public synchronized void stopEncodingInternal() {
        container.writeTrailer();
        container.close();
    }

    public void setBitrate(int maxBitrate) {
        this.maxBitrate = maxBitrate;
        int averageBitrate = (int) (bitrateVariation * maxBitrate);
        coder.setBitRate(averageBitrate);
        coder.setBitRateTolerance(maxBitrate - averageBitrate);
        // coder.setProperty("b", averageBitrate);
        // coder.setProperty("bt", maxBitrate - averageBitrate);
        updateVBVBuffer(coder);
    }

    public int getBitrate() {
        return this.maxBitrate;
    }

    /**
     * updates settings for prebuffer (vbv-buffer) of ffmpeg. should be called
     * when targeted bitrate or fps change.
     */
    private void updateVBVBuffer(IStreamCoder coder) {
        int maxBitsPerFrame = (maxBitrate / framerate);
        coder.setProperty("bufsize", useVBV ? maxBitsPerFrame : 0);
        coder.setProperty("maxrate", useVBV ? this.maxBitrate : 0);
    }

    protected static IContainerFormat createContainerFormat(
        String containerFormatName) throws EncoderInitializationException {
        int errorNumber;
        IContainerFormat format = IContainerFormat.make();
        if ((errorNumber = format.setOutputFormat(containerFormatName, null,
            null)) < 0)
            throw new EncoderInitializationException(IError.make(errorNumber)
                .getDescription());
        return format;
    }

    protected static ICodec createVideoFormat(String videoFormatName) {
        return ICodec.findEncodingCodecByName(videoFormatName);
    }

    protected static IStreamCoder createCoder(IStream stream,
        ICodec videoFormat, int height, int width, int framerate) {
        // get streams coder
        IStreamCoder coder = stream.getStreamCoder();
        // set codec for the stream
        coder.setCodec(videoFormat);
        // framerate
        IRational fps = IRational.make(framerate);
        // timebase = 1/fps
        coder.setTimeBase(IRational.make(fps.getDenominator(), fps
            .getNumerator()));
        coder.setPixelType(pixelformat);
        // set resolution
        coder.setHeight(height);
        coder.setWidth(width);
        // set some flags
        coder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
        coder.setFlag(IStreamCoder.Flags.FLAG_GLOBAL_HEADER, true);

        if (coder.getCodecID() == ICodec.ID.CODEC_ID_H264)
            applyX264Settings(coder);

        return coder;
    }

    private static void applyX264Settings(IStreamCoder coder) {
        coder.setProperty("coder", "0");
        coder.setProperty("bf", "0");
        coder.setProperty("flags2", "-wpred-dct8x8+fastpskip");
        coder.setProperty("flags", "+loop");
        coder.setProperty("qcomp", "0.4");
        coder.setProperty("qmin", "10");
        coder.setProperty("qmax", "51");
        coder.setProperty("qdiff", "4");

        coder.setProperty("b_strategy", "0");
        coder.setProperty("subq", "1");
        coder.setProperty("sc_threshold", "-1");
    }

    @Override
    public void setBandwidth(int newBandwidth) {
        // not supported
    }

    @Override
    public void setFps(int newFps) {
        this.framerate = newFps;

    }

    @Override
    public void setQuality(int newQuality) {
        // not supported
    }

    @Override
    public boolean supportSetBandwidth() {
        return false;
    }

    @Override
    public boolean supportSetFps() {
        return true;
    }

    @Override
    public boolean supportSetQuality() {
        return false;
    }

    public static boolean isInstalled() {
        try {
            IContainer.make();
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }
}
