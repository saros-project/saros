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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class TextLoadActivity implements IActivity {
    private final IPath path;
    
    public TextLoadActivity(IPath path) {
        this.path = path;
    }
    
    public TextLoadActivity(String path) {
        this.path = new Path(path);
    }
    
    public IPath getPath() {
        return path;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextLoadActivity) {
            TextLoadActivity textLoad = (TextLoadActivity)obj;
            return path.equals(textLoad.path);
        }
        
        return false;
    }

    @Override
    public String toString() {
        return "TextLoad(path:"+path+")";
    }
}
