/*
 * DPP - Serious Distributed Pair Programming
 * (c) Moritz v. Hoffen, Freie Universität Berlin 2010
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

package de.fu_berlin.inf.dpp.feedback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;

/**
 * This collector collects information about selections made by users with
 * {@link Permission#READONLY_ACCESS} that are witnessed by the local user. The
 * total selections are counted and it is checked, if each selection was within
 * the file the local user was viewing. He might either see that selection
 * directly within his viewport or through viewport annotation.
 * 
 * Selection made by users with {@link Permission#WRITE_ACCESS} are not analyzed
 * as those will be misleading.
 * 
 * Furthermore, if a user with {@link Permission#READONLY_ACCESS} selects some
 * text and a change to that specific text is done later on, this is stored as a
 * (probable) successful gesture.
 */
@Component(module = "feedback")
public class SelectionCollector extends AbstractStatisticCollector {

    /**
     * A SelectionEvent encapsulates information about a selection made. This
     * information includes:
     * <ul>
     * <li>time of the event</li>
     * <li>{@link SPath} of the event</li>
     * <li>{@link User} who made the selection</li>
     * <li>offset of the selection</li>
     * <li>length of the selection</li>
     * <li><code>boolean</code> representing if selection was within scope of
     * local user
     * <li><code>boolean</code> flag if a text edit occurred within that
     * selection
     * </ul>
     */
    protected static class SelectionEvent {
        long time;
        SPath path;
        int offset;
        int length;
        boolean withinFile;
        boolean gestured;

        public SelectionEvent(long time, SPath path, int offset, int length,
            boolean withinFile, boolean gestured) {
            this.time = time;
            this.path = path;
            this.offset = offset;
            this.length = length;
            this.withinFile = withinFile;
            this.gestured = gestured;
        }
    }

    /** Represents the current active editor of the local {@link User} */
    protected SPath localPath = null;

    /**
     * A map where the latest selections information for each each user as
     * {@link JID} are stored.
     */
    protected Map<User, SelectionEvent> activeSelections = new HashMap<User, SelectionEvent>();

    /**
     * A list where all selection events made by a remote user with
     * {@link Permission#READONLY_ACCESS} are being stored<br>
     */
    protected List<SelectionEvent> userWithReadOnlyAccessSelectionEvents = new ArrayList<SelectionEvent>();

    private final EditorManager editorManager;

    protected ISharedEditorListener editorListener = new AbstractSharedEditorListener() {

        @Override
        public void textEditRecieved(User user, SPath editor, String text,
            String replacedText, int offset) {
            // the JID of the user who made the text edit

            /*
             * for each edit check if it is made within the range of a current
             * active selection
             */
            for (Map.Entry<User, SelectionEvent> entry : activeSelections
                .entrySet()) {
                User currentUser = entry.getKey();
                SelectionEvent selection = entry.getValue();

                /*
                 * only proceed if selection was not made by the editor himself
                 * and the edit occurs within this selection (path and range).
                 * If so, increment the gesture count and break. To prevent
                 * multiple indicated gestures set the flag gestured to true.
                 * This will prevent multiple possible gestures from just a
                 * single selection - i.e. when the edit consists of more than
                 * one character.
                 */
                if (!currentUser.equals(user) && selection.offset <= offset
                    && (selection.offset + selection.length) >= offset
                    && (selection.path).equals(editor) && !selection.gestured) {
                    selection.gestured = true;
                    break;
                }
            }
        }

        @Override
        public void viewportGenerated(ViewportActivity viewport) {
            // refresh the SPath of the local active editor
            localPath = viewport.getPath();

        }

        @Override
        public void textSelectionMade(TextSelectionActivity selection) {
            // details of the occurred selection
            long time = System.currentTimeMillis();
            SPath path = selection.getPath();
            int offset = selection.getOffset();
            int length = selection.getLength();

            boolean withinFile = false;
            boolean gestured = false;

            // get user who made the selection
            User source = selection.getSource();

            /*
             * check if selection was made within the file the local user is
             * currently viewing
             */
            if (path.equals(localPath)) {
                withinFile = true;
            }

            // create an event for the current selection
            SelectionEvent currentSelection = new SelectionEvent(time, path,
                offset, length, withinFile, gestured);

            /**
             * check if the selection was made by a user with
             * {@link User.Permission#READONLY_ACCESS} and has a length of more
             * than 0
             */
            if (length > 0 && source.hasReadOnlyAccess()) {
                // store the selection event in the array list
                userWithReadOnlyAccessSelectionEvents.add(currentSelection);
                /*
                 * check if there is already a selection stored for this user
                 * and replace it in case or just store the selection if not
                 */
                if (activeSelections.containsKey(source)) {
                    activeSelections.remove(source);
                    activeSelections.put(source, currentSelection);
                } else {
                    activeSelections.put(source, currentSelection);
                }
            }
        }
    };

    public SelectionCollector(StatisticManager statisticManager,
        ISarosSession session, EditorManager editorManager) {
        super(statisticManager, session);

        this.editorManager = editorManager;
    }

    @Override
    protected void processGatheredData() {
        // count for selections witnessed by the local user
        int numberOfWitnessedUserWithReadOnlyAccessSelections = 0;
        int numberOfGestures = 0;

        /**
         * iterate through SelectionEvens caused by users with
         * {@link User.Permission#READONLY_ACCESS} and check if they occurred
         * within the file the local user was viewing and check if the gestured
         * flag was set for this selection.
         */
        for (SelectionEvent currentEntry : userWithReadOnlyAccessSelectionEvents) {
            if (currentEntry.withinFile) {
                numberOfWitnessedUserWithReadOnlyAccessSelections++;
            }
            if (currentEntry.gestured) {
                numberOfGestures++;
            }
        }

        data.setTotalOberserverSelectionCount((userWithReadOnlyAccessSelectionEvents
            .size()));
        data.setGestureCount(numberOfGestures);
        data.setWitnessedUserWithReadOnlyAccessSelections(numberOfWitnessedUserWithReadOnlyAccessSelections);

    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        editorManager.addSharedEditorListener(editorListener);
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        editorManager.removeSharedEditorListener(editorListener);
    }
}
