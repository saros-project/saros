/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.core.editor;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.intellij.editor.text.LineRange;
import de.fu_berlin.inf.dpp.session.User;

/**
 * Empty abstract implementation of the ISharedEditorListener interface.
 */
public abstract class AbstractSharedEditorListener
    implements ISharedEditorListener {

    @Override
    public void activeEditorChanged(User user, SPath path) {
        // does nothing
    }

    @Override
    public void editorRemoved(User user, SPath path) {
        // does nothing
    }

    @Override
    public void userWithWriteAccessEditorSaved(SPath path, boolean replicated) {
        // does nothing
    }

    @Override
    public void followModeChanged(User user, boolean isFollowed) {
        // does nothing
    }

    @Override
    public void textEditRecieved(User user, SPath editor, String text,
        String replacedText, int offset) {
        // does nothing
    }

    @Override
    public void textSelectionMade(TextSelectionActivity selection) {
        // does nothing
    }

    @Override
    public void viewportChanged(ViewportActivity viewport) {
        // does nothing
    }

    @Override
    public void jumpedToUser(User jumpedTo) {
        // does nothing
    }

    @Override
    public void viewportGenerated(LineRange viewport, SPath path) {
        //does nothing
    }

    @Override
    public void colorChanged() {
        // does nothing
    }
}
