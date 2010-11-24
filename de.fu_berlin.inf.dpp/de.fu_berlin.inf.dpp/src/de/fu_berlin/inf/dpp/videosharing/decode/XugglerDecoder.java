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

import java.io.InputStream;
import java.io.ObjectOutputStream;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.encode.XugglerEncoder;
import de.fu_berlin.inf.dpp.videosharing.exceptions.DecoderInitializationException;
import de.fu_berlin.inf.dpp.videosharing.exceptions.DecodingException;
import de.fu_berlin.inf.dpp.videosharing.exceptions.xuggler.XugglerNotInstalledException;

/**
 * @author s-lau
 * 
 */
public class XugglerDecoder extends Decoder {

    /* settings */
    IContainerFormat containerFormat;
    protected static IPixelFormat.Type pixelformat;

    /* video */
    protected IContainer container;
    protected IStreamCoder coder = null;
    protected IConverter converter = null;
    protected IVideoResampler resampler = null;

    protected long videoStreamIndex = -1;
    protected long firstTimestamp;
    protected long firstSystemTime = 0;

    public XugglerDecoder(InputStream input, ObjectOutputStream statistics,
        int width, int height, String containerFormatName,
        VideoSharingSession videoSharingSession)
        throws DecoderInitializationException {
        super(input, statistics, width, height, containerFormatName,
            videoSharingSession);

        if (!XugglerEncoder.isInstalled())
            throw new DecoderInitializationException(
                new XugglerNotInstalledException());
        pixelformat = IPixelFormat.Type.YUV420P;
        container = IContainer.make();
        firstTimestamp = Global.NO_PTS;

        containerFormat = createContainerFormat(containerFormatName);
    }

    protected void updatePlayer(IVideoPicture image) {
        if (image.getPixelType() != IPixelFormat.Type.RGB24)
            image = resample(image);
        if (converter == null)
            converter = ConverterFactory.createConverter("XUGGLER-BGR-24",
                image);
        super.updatePlayer(converter.toImage(image));
    }

    protected static IContainerFormat createContainerFormat(
        String containerFormatName) throws DecoderInitializationException {
        int errorNumber;
        IContainerFormat format = IContainerFormat.make();
        if ((errorNumber = format.setOutputFormat(containerFormatName, null,
            null)) < 0)
            throw new DecoderInitializationException(IError.make(errorNumber)
                .getDescription());
        return format;
    }

    public void run() {
        int errorNumber;
        container.open(videoInput, containerFormat, true, false);
        IPacket packet = IPacket.make();
        while (container.readNextPacket(packet) >= 0) {
            if (Thread.interrupted())
                return;

            statistic.dataRead(packet.getSize());

            if (coder == null) {
                int streamIndex = packet.getStreamIndex();
                IStreamCoder newCoder = container.getStream(streamIndex)
                    .getStreamCoder();
                if (newCoder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                    videoStreamIndex = streamIndex;
                    coder = newCoder;
                    if ((errorNumber = coder.open()) < 0) {
                        videoSharingSession.reportError(new DecodingException(
                            IError.make(errorNumber).getDescription()));
                        return;
                    }
                }
                if (coder == null)
                    // coder not properly set up yet
                    continue;
            }

            if (packet.getStreamIndex() == videoStreamIndex) {
                // one of the pictures in this packet
                IVideoPicture picture = IVideoPicture.make(
                    coder.getPixelType(), coder.getWidth(), coder.getHeight());

                // offset for already processed data in package
                int offset = 0;
                // fetch every frame from the packet
                while (offset < packet.getSize()) {
                    int bytesDecoded = coder.decodeVideo(picture, packet,
                        offset);

                    if (bytesDecoded < 0) {
                        videoSharingSession.reportError(new DecodingException(
                            IError.make(bytesDecoded).getDescription()));
                        return;
                    }

                    offset += bytesDecoded;

                    if (picture.isComplete()) {
                        sleepUntilRenderFrame(picture);
                        updatePlayer(picture);
                        statistic.renderedFrame();
                    }
                }
            }
        }
    }

    protected void sleepUntilRenderFrame(IVideoPicture frame) {
        if (firstTimestamp == Global.NO_PTS) {
            firstTimestamp = frame.getTimeStamp();
            firstSystemTime = System.currentTimeMillis();
        } else {
            long systemClockCurrentTime = System.currentTimeMillis();
            long millisecondsClockTimeSinceStartofVideo = systemClockCurrentTime
                - firstSystemTime;
            // compute how long for this frame since the first frame in the
            // stream.
            // remember that IVideoPicture and IAudioSamples timestamps are
            // always in MICROSECONDS,
            // so we divide by 1000 to get milliseconds.
            long millisecondsStreamTimeSinceStartOfVideo = (frame
                .getTimeStamp() - firstTimestamp) / 1000;
            final long millisecondsTolerance = 50; // and we give ourselfs 50
            // ms
            // of tolerance
            final long millisecondsToSleep = (millisecondsStreamTimeSinceStartOfVideo - (millisecondsClockTimeSinceStartofVideo + millisecondsTolerance));
            if (millisecondsToSleep > 0) {
                try {
                    Thread.sleep(millisecondsToSleep);
                } catch (InterruptedException e) {
                    // we might get this when the user closes the dialog box, so
                    // just return from the method.
                    return;
                }
            }
            statistic.delay(millisecondsToSleep);
        }
    }

    protected IVideoPicture resample(IVideoPicture picture) {
        if (resampler == null) {
            // create new resampler
            resampler = IVideoResampler.make(coder.getWidth(), coder
                .getHeight(), pixelformat, coder.getWidth(), coder.getHeight(),
                coder.getPixelType());
            if (resampler == null)
                throw new RuntimeException("resampler not available");
        }

        IVideoPicture newPicture = IVideoPicture.make(pixelformat, coder
            .getWidth(), coder.getHeight());

        if (resampler.resample(newPicture, picture) < 0)
            throw new RuntimeException("could not resample the picture");

        return newPicture;
    }
}
