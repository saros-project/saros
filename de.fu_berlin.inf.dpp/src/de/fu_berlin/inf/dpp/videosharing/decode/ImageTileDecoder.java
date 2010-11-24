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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.encode.ImageTileEncoder;
import de.fu_berlin.inf.dpp.videosharing.encode.ImageTileEncoder.ImageDone;
import de.fu_berlin.inf.dpp.videosharing.encode.ImageTileEncoder.Tile;
import de.fu_berlin.inf.dpp.videosharing.exceptions.DecoderInitializationException;
import de.fu_berlin.inf.dpp.videosharing.exceptions.DecodingException;

/**
 * 
 */
public class ImageTileDecoder extends Decoder {
    private static Logger log = Logger.getLogger(ImageTileEncoder.class);

    protected ObjectInputStream objectInput;
    protected ImageDecoder imageDecoder;
    protected String imageFormat;
    /**
     * Whole decoded image. Tiles are painted on it, until it is complete.
     */
    protected BufferedImage currentImage;

    public ImageTileDecoder(InputStream input, ObjectOutputStream statisticOut,
        int width, int height, String imageFormat,
        VideoSharingSession videoSharingSession)
        throws DecoderInitializationException {
        super(input, statisticOut, width, height, imageFormat,
            videoSharingSession);
        try {
            objectInput = new ObjectInputStream(input);
        } catch (IOException e) {
            throw new DecoderInitializationException(e);
        }
        this.imageFormat = imageFormat;
        imageDecoder = new ImageDecoder();
        currentImage = new BufferedImage(width, height,
            BufferedImage.TYPE_3BYTE_BGR);
    }

    public void run() {

        while (!Thread.interrupted()) {
            Object read;
            try {
                read = objectInput.readObject();
            } catch (IOException e) {
                if (e instanceof EOFException)
                    // closed
                    break;
                if (!(e instanceof InterruptedIOException))
                    videoSharingSession.reportError(new DecodingException(e));
                break;
            } catch (ClassNotFoundException e) {
                // should not happen
                videoSharingSession.reportError(new DecodingException(e));
                break;
            }

            Tile tile;
            if (read instanceof Tile) {
                tile = (Tile) read;

            } else {
                if (read instanceof ImageDone) {
                    statistic.renderedFrame();
                    updatePlayer(currentImage);
                    currentImage.flush();
                    continue;
                } else {
                    log.warn("Received unknown object " + read.toString());
                    continue;
                }
            }
            BufferedImage tileImage;
            try {
                statistic.dataRead(tile.getImageData().length);
                tileImage = imageDecoder.decode(tile.getImageData());
            } catch (IOException e) {
                videoSharingSession.reportError(new DecodingException(e));
                break;
            }

            if (tileImage != null) {
                Graphics2D graphics2d = currentImage.createGraphics();
                graphics2d.drawImage(tileImage, tile.getX(), tile.getY(), null);
                tileImage.flush();
                graphics2d.dispose();
            }
        }
    }

    protected class ImageDecoder {
        protected ImageReader imageReader;
        protected ImageInputStream imageInput;
        protected int imageIndex = 0;

        public ImageDecoder() throws DecoderInitializationException {

            try {
                imageReader = ImageIO.getImageReadersByFormatName(imageFormat)
                    .next();
            } catch (NoSuchElementException e) {
                throw new DecoderInitializationException(
                    "Imagedecoder not available for format " + imageFormat);
            }
        }

        public BufferedImage decode(byte[] imageData) throws IOException {
            ByteArrayInputStream input = new ByteArrayInputStream(imageData);
            imageInput = ImageIO.createImageInputStream(input);
            imageReader.setInput(imageInput, true, true);
            return imageReader.read(imageReader.getMinIndex());
        }
    }

}
