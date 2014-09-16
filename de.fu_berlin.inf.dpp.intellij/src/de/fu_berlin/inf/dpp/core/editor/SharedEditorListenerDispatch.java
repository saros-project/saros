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

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link de.fu_berlin.inf.dpp.core.editor.ISharedEditorListener} which can dispatch to a changing set of
 * {@link de.fu_berlin.inf.dpp.core.editor.ISharedEditorListener}s.
 */
public class SharedEditorListenerDispatch implements ISharedEditorListener {

    private CopyOnWriteArrayList<ISharedEditorListener> editorListeners = new CopyOnWriteArrayList<ISharedEditorListener>();

    /**
     * Adds the listener if it was not added before.
     *
     * @param editorListener
     */
    public void add(ISharedEditorListener editorListener) {
        editorListeners.addIfAbsent(editorListener);
    }

    /**
     * Removes the listener.
     * @param editorListener
     */
    public void remove(ISharedEditorListener editorListener) {
        editorListeners.remove(editorListener);
    }

    @Override
    public void activeEditorChanged(User user, SPath path) {
        for (ISharedEditorListener listener : editorListeners)
            listener.activeEditorChanged(user, path);
    }

    @Override
    public void editorRemoved(User user, SPath path) {
        for (ISharedEditorListener listener : editorListeners)
            listener.editorRemoved(user, path);
    }

    @Override
    public void userWithWriteAccessEditorSaved(SPath path, boolean replicated) {
        for (ISharedEditorListener listener : editorListeners)
            listener.userWithWriteAccessEditorSaved(path, replicated);
    }

    @Override
    public void followModeChanged(User user, boolean isFollowed) {
        for (ISharedEditorListener listener : editorListeners)
            listener.followModeChanged(user, isFollowed);
    }

    @Override
    public void textEditRecieved(User user, SPath editor, String text,
        String replacedText, int offset) {

        for (ISharedEditorListener listener : editorListeners)
            listener.textEditRecieved(user, editor, text, replacedText, offset);
    }

    @Override
    public void textSelectionMade(TextSelectionActivity selection) {
        for (ISharedEditorListener listener : editorListeners)
            listener.textSelectionMade(selection);
    }

    @Override
    public void viewportChanged(ViewportActivity viewport) {
        for (ISharedEditorListener listener : editorListeners)
            listener.viewportChanged(viewport);
    }

    @Override
    public void jumpedToUser(User jumpedTo) {
        for (ISharedEditorListener listener : editorListeners)
            listener.jumpedToUser(jumpedTo);
    }

    @Override
    public void colorChanged() {
        for (ISharedEditorListener listener : editorListeners)
            listener.colorChanged();
    }

    @Override
    public void viewportGenerated(LineRange viewport, SPath path) {
        for (ISharedEditorListener listener : editorListeners)
            listener.viewportGenerated(viewport, path);
    }
}
