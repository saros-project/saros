/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
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
package de.fu_berlin.inf.dpp.activities;

/**
 * A simple immutable text activity. 
 * 
 * @author rdjemili
 */
public class TextEditActivity implements IActivity {
    public final int    offset;
    public final String text;
    public final int    replace;
    
    /**
     * @param offset the offset inside the document where this activity happend.
     * @param text the text that was inserted.
     * @param replace the length of text that was replaced by this activity.
     */
    public TextEditActivity(int offset, String text, int replace) {
        this.offset = offset;
        this.text = text;
        this.replace = replace;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextEditActivity) {
            TextEditActivity other = (TextEditActivity)obj;
            return offset == other.offset && text.equals(other.text) && replace == other.replace;
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        return "TextEditActivity(offset:"+offset+",text:"+text+",replace:"+replace+")";
    }
}
