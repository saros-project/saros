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
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.decode.Decoder;
import de.fu_berlin.inf.dpp.videosharing.encode.tools.QuantizeFilter;
import de.fu_berlin.inf.dpp.videosharing.exceptions.EncoderInitializationException;
import de.fu_berlin.inf.dpp.videosharing.exceptions.EncodingException;
import de.fu_berlin.inf.dpp.videosharing.source.ImageSource;

/**
 * @author s-lau
 */
public class ImageTileEncoder extends Encoder {
    private static Logger log = Logger.getLogger(ImageTileEncoder.class);

    public final static int TILE_SIZE = 20;
    public final static int KEYFRAME = 50;
    /**
     * Throw error when {@link #slowEncoding} bigger than this
     */
    public final static int SLOW_ENCODING_ERROR = 5;

    /**
     * All tiles from last input picture. Used to compute dirty tiles.
     */
    protected int tiles[][][];

    /**
     * Number of tiles in x-direction.
     */
    protected int tilesX;

    /**
     * Number of tiles in y-direction.
     */
    protected int tilesY;

    /**
     * Frames since last key-frame.
     */
    protected int keyframe = 0;

    /**
     * Incremented when encoding is too slow (can't reach given FPS).
     */
    protected int slowEncoding = 0;

    protected ObjectOutputStream objectOut;
    protected QuantizeFilter filter;
    protected ImageEncoder coder;

    public ImageTileEncoder(OutputStream out, ImageSource source,
        VideoSharingSession videoSharingSession)
        throws EncoderInitializationException {
        super(out, source, videoSharingSession);

        try {
            objectOut = new ObjectOutputStream(out);
        } catch (IOException e) {
            throw new EncoderInitializationException(e);
        }

        tilesX = (int) Math.ceil((double) width / TILE_SIZE);
        tilesY = (int) Math.ceil((double) height / TILE_SIZE);

        tiles = new int[tilesX][tilesY][TILE_SIZE * TILE_SIZE];

        filter = new QuantizeFilter();
        filter.setNumColors(preferences
            .getInt(PreferenceConstants.IMAGE_TILE_COLORS));
        filter.setDither(preferences
            .getBoolean(PreferenceConstants.IMAGE_TILE_DITHER));
        filter.setSerpentine(preferences
            .getBoolean(PreferenceConstants.IMAGE_TILE_SERPENTINE));
        coder = new ImageEncoder();
    }

    public void run() {
        while (isEncoding) {
            isPaused();
            if (Thread.interrupted()) {
                stopEncodingInternal();
                return;
            }
            long trace = System.currentTimeMillis();
            encodeImage(imageSource.toImage());
            trace = System.currentTimeMillis() - trace;
            long frameIntervall = (long) (1000 / (double) framerate);

            if (trace < frameIntervall) {
                try {
                    Thread.sleep(frameIntervall - trace);
                } catch (InterruptedException e) {
                    stopEncodingInternal();
                    return;
                }
                slowEncoding = 0;
            } else {
                if (++slowEncoding > SLOW_ENCODING_ERROR) {
                    videoSharingSession
                        .reportError(new EncodingException(
                            "Can't encode that fast. Please choose lower frames per second."));
                    return;
                }
            }
        }
    }

    protected void encodeImage(BufferedImage capture) {
        if (capture.getWidth() != width || capture.getHeight() != height)
            capture = Decoder.resample(capture, new Dimension(width, height));

        capture = filter.filter(capture, null);
        /*
         * TODO reduce color in buffered for a real compression (use a smaller
         * ColorModel)
         */
        // capture = new ColorConvertOp(
        // ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(
        // convertToType(capture, BufferedImage.TYPE_BYTE_GRAY), null);

        if (++keyframe > KEYFRAME) {
            keyframe = 0;
        }

        for (int i = 0; i < tilesX; i++) {
            for (int j = 0; j < tilesY; j++) {

                if (Thread.interrupted()) {
                    stopEncodingInternal();
                    return;
                }

                int offsetX = i * TILE_SIZE;
                int offsetY = j * TILE_SIZE;
                int lengthX = Math.min(TILE_SIZE, width - offsetX);
                int lengthY = Math.min(TILE_SIZE, height - offsetY);

                final BufferedImage currentTile = capture.getSubimage(offsetX,
                    offsetY, lengthX, lengthY);

                int pixels[] = new int[TILE_SIZE * TILE_SIZE];

                PixelGrabber pg = new PixelGrabber(currentTile, 0, 0,
                    TILE_SIZE, TILE_SIZE, pixels, 0, TILE_SIZE);

                try {
                    if (pg.grabPixels()) {

                        if (keyframe == KEYFRAME
                            || !Arrays.equals(tiles[i][j], pixels)) {

                            byte[] imageData = coder.encode(currentTile);
                            currentTile.flush();

                            if (imageData != null && imageData.length > 0) {

                                try {
                                    objectOut.writeObject(new Tile(imageData, i
                                        * TILE_SIZE, j * TILE_SIZE));
                                } catch (IOException e) {
                                    log.error("Sending tile: ", e);
                                }

                                tiles[i][j] = pixels;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    stopEncoding();
                    return;
                } catch (IOException e) {
                    videoSharingSession.reportError(new EncodingException(e));
                    return;
                }
            }
        }

        try {
            objectOut.writeObject(ImageDone.INSTANCE);
        } catch (IOException e) {
            videoSharingSession.reportError(new EncodingException(e));
            return;
        }

    }

    @Override
    public void stopEncodingInternal() {
        try {
            objectOut.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public void setBandwidth(int newBandwidth) {
        // not supported
    }

    @Override
    public void setFps(int newFps) {
        framerate = newFps;
    }

    @Override
    public void setQuality(int newQuality) {
        // TODO can be implemented, but FPS should be enough
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

    public static String[][] getSupportedImageFormats() {
        String[] values = ImageIO.getWriterFormatNames();
        Set<String> filteredValues = new HashSet<String>();
        // remove doublettes
        for (String val : values) {
            if (filteredValues.contains(val.toLowerCase()))
                continue;
            filteredValues.add(val.toLowerCase());
        }
        values = filteredValues.toArray(new String[filteredValues.size()]);
        String[][] namesAndValues = new String[values.length][];
        int i = 0;
        for (String val : values) {
            namesAndValues[i] = new String[] { val, val };
            ++i;
        }
        return namesAndValues;
    }

    public static class Tile implements Serializable {
        private static final long serialVersionUID = 2961263497738302562L;

        protected byte[] imageData;
        protected int x;
        protected int y;

        /**
         * @param imageData
         * @param x
         * @param y
         */
        public Tile(byte[] imageData, int x, int y) {
            super();
            this.imageData = imageData;
            this.x = x;
            this.y = y;
        }

        public byte[] getImageData() {
            return imageData;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

    }

    /**
     * Just a marker object signaling image is done.
     */
    public static class ImageDone implements Serializable {
        private static final long serialVersionUID = 6930705009545988323L;

        public static final ImageDone INSTANCE = new ImageDone();
    }

    protected class ImageEncoder {
        protected ImageWriter imageWriter;
        protected String imageFormat;
        protected ImageOutputStream imageOut;
        protected ByteArrayOutputStream byteOut;
        protected ImageWriteParam imageWriteParam;

        public ImageEncoder() throws EncoderInitializationException {
            byteOut = new ByteArrayOutputStream();
            try {
                imageOut = ImageIO.createImageOutputStream(byteOut);
            } catch (IOException e) {
                throw new EncoderInitializationException(e);
            }
            imageFormat = preferences
                .getString(PreferenceConstants.IMAGE_TILE_CODEC);
            try {
                imageWriter = ImageIO.getImageWriters(
                    new ImageTypeSpecifier(filter.filter(imageSource.toImage(),
                        null)), imageFormat).next();
            } catch (NoSuchElementException e) {
                throw new EncoderInitializationException(
                    "No writer for imageformat " + imageFormat + " available");
            }
            imageWriter.setOutput(imageOut);
            imageWriteParam = imageWriter.getDefaultWriteParam();

            try {
                imageWriteParam
                    .setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                imageWriteParam.setCompressionQuality((float) (preferences
                    .getInt(PreferenceConstants.IMAGE_TILE_QUALITY) / 100.0));
            } catch (UnsupportedOperationException e) {
                imageWriteParam = null;
            }
        }

        public byte[] encode(BufferedImage bufferedImage) throws IOException {
            if (imageWriteParam != null) {
                imageWriter.write(null,
                    new IIOImage(bufferedImage, null, null), imageWriteParam);
            } else {
                imageWriter.write(bufferedImage);
            }
            byte[] imageData = byteOut.toByteArray();
            byteOut.reset();
            return imageData;
        }
    }

}
