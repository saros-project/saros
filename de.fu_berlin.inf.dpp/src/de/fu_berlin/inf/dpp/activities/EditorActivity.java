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

/**
 * A text load activity activates a new resource. If the path is
 * <code>null</code> no resouce is currently active.
 * 
 * @author rdjemili
 */
public class EditorActivity implements IActivity {
    public static enum Type{Activated, Closed, Saved};
    
    private Type  type;
    private IPath path;
    
    /**
     * @param path a valid project-relative path or <code>null</code> if
     * former resource should be deactivated.
     */
    public EditorActivity(Type type, IPath path) {
        if (type != Type.Activated && path == null)
            throw new IllegalArgumentException(
                "Null path for non-activation type editor activity given.");
        
        this.type = type;
        this.path = path;
    }
    
    /**
     * @return the project-relative path to the resource that should be
     * activated.
     */
    public IPath getPath() {
        return path;
    }
    
    public Type getType() {
        return type;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EditorActivity)) {
            return false;
        }
        
        EditorActivity other = (EditorActivity)obj;
        return ((path == null && other.path == null) || path.equals(other.path)) 
            && type.equals(other.type);
    }

    @Override
    public String toString() {
        return "EditorActivity(type:"+type+", path:"+path+")";
    }
}
