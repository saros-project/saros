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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.videosharing.VideoSharing.VideoSharingSession;
import de.fu_berlin.inf.dpp.videosharing.encode.ImageTileEncoder;
import de.fu_berlin.inf.dpp.videosharing.encode.ImageTileEncoder.Tile;
import de.fu_berlin.inf.dpp.videosharing.exceptions.DecoderInitializationException;
import de.fu_berlin.inf.dpp.videosharing.exceptions.DecodingException;

/**
 * This class is responsible for decoding the received tiles from the
 * {@link ImageTileEncoder}.
 * 
 * @author Stefan Rossbach
 */
public class ImageTileDecoder extends Decoder {
    private static final Logger log = Logger.getLogger(ImageTileEncoder.class);

    private ObjectInputStream objectInput;

    /** the buffered image which is updated with tiles */
    private BufferedImage image;

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
        // currently not used
        // this.imageFormat = imageFormat;

    }

    // Main encoder loop

    @Override
    public void run() {

        while (!Thread.interrupted()) {
            try {
                Tile tile = (Tile) objectInput.readObject();

                if (tile.imageData.length != 0) {
                    BufferedImage subImage = ImageIO
                        .read(new ByteArrayInputStream(tile.imageData));

                    if (image == null || image.getWidth() != tile.iw
                        || image.getHeight() != tile.ih)
                        image = new BufferedImage(tile.iw, tile.ih,
                            subImage.getType());

                    image.getRaster().setRect(tile.x, tile.y,
                        subImage.getRaster());

                    statistic.dataRead(tile.imageData.length);
                } else {
                    statistic.renderedFrame();
                    updatePlayer(image);
                }

            } catch (IOException e) {
                if (e instanceof EOFException)
                    // closed
                    break;
                if (!(e instanceof InterruptedIOException)) {
                    log.error(e.getMessage(), e);
                    videoSharingSession.reportError(new DecodingException(e));
                }
                break;
            } catch (Exception e) {
                // should not happen
                log.error(e.getMessage(), e);
                videoSharingSession.reportError(new DecodingException(e));
                break;
            }
        }
    }
}
