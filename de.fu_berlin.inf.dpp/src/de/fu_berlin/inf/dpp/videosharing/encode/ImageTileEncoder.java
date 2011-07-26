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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.exceptions.EncoderInitializationException;
import de.fu_berlin.inf.dpp.videosharing.exceptions.EncodingException;
import de.fu_berlin.inf.dpp.videosharing.source.ImageSource;

/**
 * <p>
 * This class handles image compression for video streaming be using only pure
 * Java components
 * </p>
 * 
 * <p>
 * Algorithm:
 * </p>
 * <ol>
 * <li>Capture an image and possible scale it down</li>
 * <li>If this is the first captured image or the image that was captured is not
 * a 24 or 32 bit RGB images then compress the whole image and send it to the
 * receiver</li>
 * <li>Otherwise the content of the images are subdivided into 8x8 tiles (pixel
 * squares) and compared against each other
 * <li>After comparison a matrix has been created which will look like this:
 * 
 * <pre>
 * [x] [ ] [ ] [ ]
 * [x] [x] [ ] [x]
 * [x] [x] [ ] [x]
 * [x] [x] [ ] [x]
 * </pre>
 * 
 * <p>
 * Where each X represents a dirty tile. Now all dirty tiles are aggregated to
 * maximum square blocks (this is currently not implemented, only the tiles per
 * row are aggregated)
 * </p>
 * 
 * <p>
 * Which should result in:
 * </p>
 * 
 * <pre>
 * |---|
 * |[x]| [ ] [ ] [ ]
 * |---|
 * 
 * |-------|     |---|
 * |[x] [x]| [ ] |[x]|
 * |[x] [x]| [ ] |[x]|
 * |[x] [x]| [ ] |[x]|
 * |-------|     |---|
 * </pre>
 * 
 * </li>
 * <li>Each of these maximum square block is now compressed and then send to the
 * receiver</li>
 * <li>Continue with step 1</li>
 * </ol>
 * 
 * @author s-lau
 * @author Stefan Rossbach
 */
public final class ImageTileEncoder extends Encoder {

    public static class Tile implements Serializable {
        private static final long serialVersionUID = 2961263497738302562L;

        /** image date of the tile */
        public byte[] imageData;

        /** the x offset of the tile in the original image */
        public int x;
        /** the y offset of the tile in the original image */
        public int y;

        /** the width of the tile */
        public int w;

        /** the height of the tile */
        public int h;

        /** the width of the original image */
        public int iw;

        /** the height of the original image */
        public int ih;

        public Tile(byte[] imageData, int x, int y, int w, int h, int iw, int ih) {
            super();
            this.imageData = imageData;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.iw = iw;
            this.ih = ih;
        }

        @Override
        public String toString() {
            return "[" + imageData.length + ":" + x + ":" + y + ":" + w + ":"
                + h + ":" + iw + ":" + ih + ":" + "]";
        }
    }

    private static Logger log = Logger.getLogger(ImageTileEncoder.class);

    private final static int MAX_ALLOWED_FRAMES_TO_DROP = 20;

    /** Threshold in percent if an image should be completely encoded */
    // currently set to 30% because our aggregation algorithm is bad
    private static final int DIRTY_TILES_THRESHOLD = 30;

    private int droppedFrames = 0;

    private int maxBandwidthUsed = 0;

    private ObjectOutputStream objectOut;

    private ByteArrayOutputStream imageOutput = new ByteArrayOutputStream();

    private BufferedImage lastImage = null;

    public ImageTileEncoder(OutputStream out, ImageSource source,
        VideoSharingSession videoSharingSession)
        throws EncoderInitializationException {
        super(out, source, videoSharingSession);

        if ((width & 7) != 0 || (height & 7) != 0 || width <= 0 || height <= 0)
            throw new EncoderInitializationException(
                "The screen capture resolution must be a multiple of 8 for width and height. Current resolution is:"
                    + width + "x" + height);
        try {
            objectOut = new ObjectOutputStream(out);
        } catch (IOException e) {
            throw new EncoderInitializationException(e);
        }
    }

    /* main encoder loop */

    public void run() {

        while (isEncoding) {
            isPaused();

            if (Thread.interrupted()) {
                stopEncodingInternal();
                return;
            }

            long startTime = System.currentTimeMillis();

            imageOutput.reset();
            BufferedImage image = imageSource.toImage();

            if (image.getWidth() != width || image.getHeight() != height) {
                BufferedImage scaledImage = new BufferedImage(width, height,
                    image.getType());
                Graphics2D g2 = scaledImage.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);
                g2.drawImage(image, 0, 0, width, height, null);
                g2.dispose();
                image = scaledImage;
            }

            try {

                if (lastImage == null
                    || image.getRaster().getDataBuffer().getDataType() != DataBuffer.TYPE_INT) {
                    lastImage = image;
                    imageOutput.reset();
                    ImageIO.write(image, "jpeg",
                        new MemoryCacheImageOutputStream(imageOutput));
                    maxBandwidthUsed = Math.max(maxBandwidthUsed,
                        imageOutput.size());
                    objectOut.writeObject(new Tile(imageOutput.toByteArray(),
                        0, 0, image.getWidth(), image.getHeight(), image
                            .getWidth(), image.getHeight()));
                    objectOut.flush();

                } else {

                    int[] rasterOld = ((DataBufferInt) lastImage.getRaster()
                        .getDataBuffer()).getData();
                    int[] rasterNew = ((DataBufferInt) image.getRaster()
                        .getDataBuffer()).getData();

                    int bytesWritten = 0;

                    for (DirtyTile dirtyTile : getDirtyTiles(rasterOld,
                        rasterNew)) {

                        BufferedImage dirtySubImage = image.getSubimage(
                            dirtyTile.x, dirtyTile.y, dirtyTile.w, dirtyTile.h);

                        imageOutput.reset();
                        ImageIO.write(dirtySubImage, "jpeg",
                            new MemoryCacheImageOutputStream(imageOutput));
                        bytesWritten += imageOutput.size();
                        objectOut.writeObject(new Tile(imageOutput
                            .toByteArray(), dirtyTile.x, dirtyTile.y,
                            dirtyTile.w, dirtyTile.h, image.getWidth(), image
                                .getHeight()));
                        objectOut.flush();
                    }

                    maxBandwidthUsed = Math.max(maxBandwidthUsed, bytesWritten);

                    lastImage = image;

                }

                /*
                 * send final tile as empty tile to update the picture and / or
                 * the fps of the receiver (could be the case that no tile was
                 * send at all
                 */
                objectOut.writeObject(new Tile(new byte[0], 0, 0, 0, 0, 0, 0));

            } catch (IOException e) {
                log.error(e.getMessage(), e);
                stopEncodingInternal();
                if (!(e instanceof EOFException))
                    videoSharingSession.reportError(new EncodingException(e));
                return;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                videoSharingSession.reportError(new EncodingException(e));
                return;
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            long frameIntervall = (1000L / framerate);

            if (elapsedTime < frameIntervall) {
                try {
                    Thread.sleep(frameIntervall - elapsedTime);
                } catch (InterruptedException e) {
                    stopEncodingInternal();
                    return;
                }
                droppedFrames = 0;
            } else {
                if (++droppedFrames > MAX_ALLOWED_FRAMES_TO_DROP) {
                    reportMaxDroppedFramesReached();
                    return;
                }
            }
        }
    }

    // WTF ?!!!!

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

    /**
     * Return a list of dirty 8x8 tiles from the difference of two images. The
     * two arrays <b>must</b> have the same length.
     * 
     * @param bufferOld
     *            image data from the old image
     * @param bufferNew
     *            image data from the old image
     * @return a list of 8x8 tiles which differs from the old picture
     */
    private List<DirtyTile> getDirtyTiles(int[] bufferOld, int[] bufferNew) {

        List<DirtyTile> dirtyTiles = new ArrayList<DirtyTile>();

        int tilesX = width >>> 3;
        int tilesY = height >>> 3;

        int offset = 0;
        int offsetY = 0;

        int dirtyTilesFound = 0;
        List<List<Integer>> dirtyXTilesPerRow = new ArrayList<List<Integer>>(
            tilesY);

        for (int y = 0, ty = 0; y < height; y += 8, ty++) {
            offset = offsetY;

            List<Integer> dirtyXTiles = new ArrayList<Integer>(tilesX);
            for (int x = 0; x < width; x += 8) {
                if (isTileDirty8x8(bufferOld, bufferNew, offset, width)) {
                    dirtyXTiles.add(x);
                    dirtyTilesFound++;
                }
                offset += 8;
            }
            dirtyXTilesPerRow.add(dirtyXTiles);
            offsetY += (width << 3);
        }

        if ((dirtyTilesFound * 100) / (tilesX * tilesY) >= DIRTY_TILES_THRESHOLD) {
            dirtyTiles.add(new DirtyTile(0, 0, width, height));
            return dirtyTiles;
        }

        /*
         * aggregate the dirty tiles to greater blocks if possible. currently it
         * only aggregates the blocks per row use a better algorithm here eg. An
         * Efficient Algorithm for Finding all Maximal Square Blocks in a Matrix
         * from Heinz Breu
         */
        int y = 0;
        for (List<Integer> dirtyXTiles : dirtyXTilesPerRow) {

            int len = dirtyXTiles.size();
            if (len == 0) {
                y += 8;
                continue;
            }

            int start;
            int current;
            start = current = dirtyXTiles.get(0);
            for (int i = 1; i < len; i++) {
                if (dirtyXTiles.get(i) - 8 == current) {
                    current += 8;
                } else {
                    dirtyTiles.add(new DirtyTile(start, y, current - start + 8,
                        8));
                    start = current = dirtyXTiles.get(i);
                }
            }
            dirtyTiles.add(new DirtyTile(start, y, current - start + 8, 8));

            y += 8;

        }

        return dirtyTiles;
    }

    /**
     * @param oldBuffer
     *            image data from the old image
     * @param newBuffer
     *            image data from the new image
     * @param offset
     *            the offset to start comparison
     * @param delta
     *            the width of the image in pixels
     * @return true if the 8x8 tile changed in the new image
     */
    private boolean isTileDirty8x8(int[] oldBuffer, int[] newBuffer,
        int offset, int delta) {
        /*
         * DO NOT REFACTOR THIS CODE ! THIS IS AN UNROLLED LOOP THAT AVOIDS
         * PIPELINE STALLS AND BRANCH PREDICTION
         */
        int y0 = (oldBuffer[offset + 0] ^ newBuffer[offset + 0])
            | (oldBuffer[offset + 1] ^ newBuffer[offset + 1])
            | (oldBuffer[offset + 2] ^ newBuffer[offset + 2])
            | (oldBuffer[offset + 3] ^ newBuffer[offset + 3])
            | (oldBuffer[offset + 4] ^ newBuffer[offset + 4])
            | (oldBuffer[offset + 5] ^ newBuffer[offset + 5])
            | (oldBuffer[offset + 6] ^ newBuffer[offset + 6])
            | (oldBuffer[offset + 7] ^ newBuffer[offset + 7]);
        offset += delta;
        int y1 = (oldBuffer[offset + 0] ^ newBuffer[offset + 0])
            | (oldBuffer[offset + 1] ^ newBuffer[offset + 1])
            | (oldBuffer[offset + 2] ^ newBuffer[offset + 2])
            | (oldBuffer[offset + 3] ^ newBuffer[offset + 3])
            | (oldBuffer[offset + 4] ^ newBuffer[offset + 4])
            | (oldBuffer[offset + 5] ^ newBuffer[offset + 5])
            | (oldBuffer[offset + 6] ^ newBuffer[offset + 6])
            | (oldBuffer[offset + 7] ^ newBuffer[offset + 7]);
        offset += delta;
        int y2 = (oldBuffer[offset + 0] ^ newBuffer[offset + 0])
            | (oldBuffer[offset + 1] ^ newBuffer[offset + 1])
            | (oldBuffer[offset + 2] ^ newBuffer[offset + 2])
            | (oldBuffer[offset + 3] ^ newBuffer[offset + 3])
            | (oldBuffer[offset + 4] ^ newBuffer[offset + 4])
            | (oldBuffer[offset + 5] ^ newBuffer[offset + 5])
            | (oldBuffer[offset + 6] ^ newBuffer[offset + 6])
            | (oldBuffer[offset + 7] ^ newBuffer[offset + 7]);
        offset += delta;
        int y3 = (oldBuffer[offset + 0] ^ newBuffer[offset + 0])
            | (oldBuffer[offset + 1] ^ newBuffer[offset + 1])
            | (oldBuffer[offset + 2] ^ newBuffer[offset + 2])
            | (oldBuffer[offset + 3] ^ newBuffer[offset + 3])
            | (oldBuffer[offset + 4] ^ newBuffer[offset + 4])
            | (oldBuffer[offset + 5] ^ newBuffer[offset + 5])
            | (oldBuffer[offset + 6] ^ newBuffer[offset + 6])
            | (oldBuffer[offset + 7] ^ newBuffer[offset + 7]);
        offset += delta;
        int y4 = (oldBuffer[offset + 0] ^ newBuffer[offset + 0])
            | (oldBuffer[offset + 1] ^ newBuffer[offset + 1])
            | (oldBuffer[offset + 2] ^ newBuffer[offset + 2])
            | (oldBuffer[offset + 3] ^ newBuffer[offset + 3])
            | (oldBuffer[offset + 4] ^ newBuffer[offset + 4])
            | (oldBuffer[offset + 5] ^ newBuffer[offset + 5])
            | (oldBuffer[offset + 6] ^ newBuffer[offset + 6])
            | (oldBuffer[offset + 7] ^ newBuffer[offset + 7]);
        offset += delta;
        int y5 = (oldBuffer[offset + 0] ^ newBuffer[offset + 0])
            | (oldBuffer[offset + 1] ^ newBuffer[offset + 1])
            | (oldBuffer[offset + 2] ^ newBuffer[offset + 2])
            | (oldBuffer[offset + 3] ^ newBuffer[offset + 3])
            | (oldBuffer[offset + 4] ^ newBuffer[offset + 4])
            | (oldBuffer[offset + 5] ^ newBuffer[offset + 5])
            | (oldBuffer[offset + 6] ^ newBuffer[offset + 6])
            | (oldBuffer[offset + 7] ^ newBuffer[offset + 7]);
        offset += delta;
        int y6 = (oldBuffer[offset + 0] ^ newBuffer[offset + 0])
            | (oldBuffer[offset + 1] ^ newBuffer[offset + 1])
            | (oldBuffer[offset + 2] ^ newBuffer[offset + 2])
            | (oldBuffer[offset + 3] ^ newBuffer[offset + 3])
            | (oldBuffer[offset + 4] ^ newBuffer[offset + 4])
            | (oldBuffer[offset + 5] ^ newBuffer[offset + 5])
            | (oldBuffer[offset + 6] ^ newBuffer[offset + 6])
            | (oldBuffer[offset + 7] ^ newBuffer[offset + 7]);
        offset += delta;
        int y7 = (oldBuffer[offset + 0] ^ newBuffer[offset + 0])
            | (oldBuffer[offset + 1] ^ newBuffer[offset + 1])
            | (oldBuffer[offset + 2] ^ newBuffer[offset + 2])
            | (oldBuffer[offset + 3] ^ newBuffer[offset + 3])
            | (oldBuffer[offset + 4] ^ newBuffer[offset + 4])
            | (oldBuffer[offset + 5] ^ newBuffer[offset + 5])
            | (oldBuffer[offset + 6] ^ newBuffer[offset + 6])
            | (oldBuffer[offset + 7] ^ newBuffer[offset + 7]);

        int s = y0 | y1 | y2 | y3 | y4 | y5 | y6 | y7;
        return s != 0;
    }

    private static class DirtyTile {
        int x;
        int y;
        int w;
        int h;

        public DirtyTile(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override
        public String toString() {
            return "[" + x + ":" + y + ":" + w + ":" + h + "]";
        }
    }

    private void reportMaxDroppedFramesReached() {
        videoSharingSession
            .reportError(new EncodingException(
                "Your PC cannot handle the current frame rate. Maximum bandwidth used was "
                    + (maxBandwidthUsed / 1024)
                    + " KiB per frame and can result in "
                    + ((maxBandwidthUsed / 1024) * framerate)
                    + " KiB/s at the current frame rate("
                    + framerate
                    + " FPS)."
                    + " Please choose a lower frames per second rate or switch to"
                    + " a lower resolution. Make also sure that your network connection"
                    + " supports the current used bandwidth."));
    }
}
