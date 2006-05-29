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
package de.fu_berlin.inf.dpp.listeners;

import org.eclipse.jface.text.ITextSelection;

import de.fu_berlin.inf.dpp.SharedEditor;

/**
 * A interface for listening to events from {@link SharedEditor}.
 * 
 * @author rdjemili
 */
public interface ISharedEditorListener {
    // TODO rename to ISharedEditorListener

    void viewportChanged(int topIndex, int bottomIndex);
    
    /**
     * @param selection The new selection.
     */
    void cursorChanged(ITextSelection selection);
    
    /**
     * @param offset The offset at which the text is inserted.
     * @param text The new text.
     * @param replace The length of the replaced text.
     * @param line The line where this event happend.
     */
    void textChanged(int offset, String text, int replace, int line);
}
