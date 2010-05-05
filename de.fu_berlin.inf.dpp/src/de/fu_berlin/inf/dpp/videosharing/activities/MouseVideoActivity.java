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
package de.fu_berlin.inf.dpp.videosharing.activities;

/**
 * @author s-lau
 */
public class MouseVideoActivity extends VideoActivity {
    private static final long serialVersionUID = -7701446680571577001L;

    int x = -1;
    int y = -1;
    int width = -1;
    int height = -1;

    public MouseVideoActivity(int x, int y, int width, int height, Type type) {
        super(type);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getWidth(int screenWidth) {
        if (width < 0)
            return -1;
        return (int) (((double) x / width) * screenWidth);
    }

    public int getHeight(int screenHeight) {
        if (height < 0)
            return -1;
        return (int) (((double) y / height) * screenHeight);
    }

    @Override
    public String toString() {
        String position = " ";
        if (width > 0)
            position = " @" + x + "x" + y + " (" + width + "x" + height + ") ";
        return type.name() + position;
    }

}
