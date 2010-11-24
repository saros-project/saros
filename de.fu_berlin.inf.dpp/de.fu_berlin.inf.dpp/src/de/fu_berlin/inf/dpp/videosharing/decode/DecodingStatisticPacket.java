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

import java.io.Serializable;

/**
 * statusobjects which client sends to server during a session
 * 
 * @author lau
 */
public class DecodingStatisticPacket implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 5309937518590287550L;

    double fps;
    int bytes;
    long delay;

    /**
     * change param types
     * 
     * @param period
     * @param frames
     * @param delay
     * @param bytes
     */
    public DecodingStatisticPacket(long period, long frames, long delay,
        long bytes) {
        period = period / 1000; // in sec's
        this.fps = period != 0 ? (double) frames / period : 0;
        this.fps = (int) (this.fps * 10) / (double) 10;
        this.delay = frames != 0 ? delay / frames : 0;
        this.bytes = (int) (period != 0 ? bytes / period : 0);
        // System.out.println("period " + period + " fps " + fps + " bytes " +
        // bytes);
    }

    public double getFps() {
        return fps;
    }

    public int getBytes() {
        return bytes;
    }

    public long getDelay() {
        return delay;
    }

    @Override
    public String toString() {
        return "fps: " + fps + " kb/s: " + (bytes / 1024) + " delay: " + delay;
    }

}
