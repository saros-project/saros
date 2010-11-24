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
package de.fu_berlin.inf.dpp.videosharing.player;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ObjectOutputStream;

/**
 * @author s-lau
 * 
 */
public interface VideoDisplay {

    /**
     * Updates image shown by this component
     * 
     * @param image
     */
    public void updateImage(BufferedImage image);

    /**
     * Gets preferred image-size to which the image can be resized before
     * updating. This is just a hint, not a must.
     * 
     * @param currentImageDimension
     *            actual size of image
     * @return preferred size for next update or <code>null</code> when doesn't
     *         matter
     */
    public Dimension getImageDimension(Dimension currentImageDimension);

    public void setActivityOutput(ObjectOutputStream out);

    /**
     * Resets display to it's initial state (i.e. no pictures will be displayed
     * anymore)
     */
    public void reset();

    /**
     * Makes display ready for displaying images.
     */
    public void initialize();

}
