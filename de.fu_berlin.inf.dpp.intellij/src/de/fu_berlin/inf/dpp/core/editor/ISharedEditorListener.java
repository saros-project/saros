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
import de.fu_berlin.inf.dpp.session.User.Permission;

public interface ISharedEditorListener {

    /**
     * The editor that the given user is currently editing has changed.
     * <p/>
     * This method implies that the editor is being opened.
     *
     * @param path the project-relative path of the resource that is the new user
     *             with {@link Permission#WRITE_ACCESS} resource.
     * @param user the user which removed an editor (can be the local user)
     */
    public void activeEditorChanged(User user, SPath path);

    /**
     * Is fired when the given editor is removed from the list of editors that
     * the given user has currently open.
     *
     * @param path the path to the resource that the user with
     *             {@link Permission#WRITE_ACCESS} was editing.
     * @param user the user which removed an editor (can be the local user)
     */
    public void editorRemoved(User user, SPath path);

    /**
     * Is fired when the user with {@link Permission#WRITE_ACCESS} editor is
     * saved.
     *
     * @param path       the project-relative path of the resource that is the new user
     *                   with {@link Permission#WRITE_ACCESS} resource.
     * @param replicated <code>false</code> if this action originates on this client.
     *                   <code>false</code> if it is a replication of an action from
     *                   another participant of the shared project.
     */
    public void userWithWriteAccessEditorSaved(SPath path, boolean replicated);

    /**
     * Is fired when the follow mode is changed.
     *
     * @param user       which is/is not being followed
     * @param isFollowed true if the {@link User} is followed
     */
    public void followModeChanged(User user, boolean isFollowed);

    /**
     * Is fired after a text edit has occurred locally and sent to remote peers
     * (in which case the user is the local user) or has been received from a
     * remote peer and applied locally.<br>
     * <br>
     * <p/>
     * In both cases does this event occur AFTER the change has been applied
     * locally and should only be treated as a notification.
     *
     * @param user         the user who performed the text edit, may be local or remote
     * @param editor       the path of the file which is altered
     * @param text         the text which was inserted at the given offset, after the
     *                     replacedText had been removed
     * @param replacedText the text which will be removed at the given offset before the
     *                     given text will be inserted
     * @param offset       the character based offset inside the document where this edit
     *                     happened
     */
    public void textEditRecieved(User user, SPath editor, String text,
        String replacedText, int offset);

    /**
     * Is fired after a text selection is executed locally when received by a
     * user.
     *
     * @param selection The text selection made as a {@link TextSelectionActivity}
     */
    public void textSelectionMade(TextSelectionActivity selection);

    /**
     * Is fired after a change in the viewport has occurred.
     *
     * @param viewport The viewport to apply to the local editor as a
     *                 {@link ViewportActivity}
     */
    public void viewportChanged(ViewportActivity viewport);

    /**
     * Is fired after a user used the jumpToFeature
     *
     * @param jumpedTo the user being jumped to
     */
    public void jumpedToUser(User jumpedTo);

    /**
     * Is fired when the viewport for the local user gets changed
     *
     * @param viewport
     * @param path
     */
    public void viewportGenerated(LineRange viewport, SPath path);

    /**
     * Is called when the color is changed.
     *
     */
    public void colorChanged();
}
