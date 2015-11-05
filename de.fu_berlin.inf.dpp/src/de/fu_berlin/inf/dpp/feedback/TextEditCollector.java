/*
 * DPP - Serious Distributed Pair Programming
 * (c) Lisa Dohrmann, Freie Universität Berlin 2009
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * <p>
 * A collector class that collects local TextEditActivitys and compares them in
 * relation to parallelism or rather concurrency with remote text events.
 * </p>
 * <p>
 * It is measured how many characters the local user wrote in a session
 * (whitespaces are omitted, because Eclipse produces many of them automatically
 * e.g. when a new line is started), how many TextEditActivitys he produced
 * (which can be different to the number of characters he wrote, e.g. when
 * copy&paste or Eclipse's method generation was used) and how concurrent the
 * local user's writing was to remote users using different sample intervals.
 * </p>
 * <p>
 * Furthermore, it accumulates the characters edited for all remote
 * participants.
 * </p>
 * <p>
 * A little addition was made to track possible paste / auto generations. If a
 * single text edit activity produces more than a certain number
 * (pasteThreshold) of characters, it will be counted as possible paste action.
 * The threshold was chosen to be 16 due to some testing. The characters within
 * a textEditActivity heavily depends on the connection speed. When a the local
 * user fires several textEdits within a short interval he might see those edits
 * as several single edits with a few characters but a user will most likely get
 * less textEditActivities containing a bunch of characters. The manual tests
 * results were: When "typing" (rather thrashing the keyboard) as fast as one
 * could, the user received text edits with up to 20 characters. As this typing
 * speed is beyond human capabilities, I've decided to set the threshold to 16
 * which will most likely prevent getting false positives (e.g. a really fast
 * typer is typing instead of a paste). Unfortunately, this relatively high
 * threshold will cause the collector to slip some true pastes as well. (E.g. an
 * auto completion of a comment block)
 * </p>
 * <p>
 * NOTE: TextEditActivitys that are triggered by Eclipse (e.g. when restoring an
 * editor) are counted as well. And refactorings can produce quite a large
 * number of characters that are counted. <br>
 * </p>
 * <p>
 * Fixed: The number of non parallel edits was set to 100% if there are no
 * concurrent edits, which is fine. But if there are no edits at all, the non
 * parallel edits are still set to 100% even though this should rather be 0%.
 * </p>
 * <p>
 * Example:<br>
 * The percent numbers (for all intervals + non-parallel) should add up to
 * (nearly) 100, slight rounding errors are possible. The counted chars for all
 * intervals and non-parallel edits should add up to textedits.chars
 * </p>
 * <code>
 * textedits.chars=5 <br>
 * textedits.count=5 <br>
 * textedits.pastes.chars=0
 * textedits.pastes=0
 * textedits.nonparallel.chars=1 <br>
 * textedits.nonparallel.percent=20 <br>
 * textedits.parallel.interval.1.chars=1 <br>
 * textedits.parallel.interval.1.count=1 <br>
 * textedits.parallel.interval.1.percent=20 <br>
 * textedits.parallel.interval.10.chars=2 <br>
 * textedits.parallel.interval.10.count=2 <br>
 * textedits.parallel.interval.10.percent=40 <br>
 * textedits.parallel.interval.2.chars=1 <br>
 * textedits.parallel.interval.2.count=1 <br>
 * textedits.parallel.interval.2.percent=20<br>
 * textedits.remote.user.1.chars=1<br>
 * textedits.remote.user.1.pastes.chars=0<br>
 * textedits.remote.user.1.pastes=0<br>
 * </code>
 * 
 * TODO: replace the HashMaps by AutoHashMaps which initializes missing values
 * with a default. This will lead to easier code and help to get rid of these
 * constructs:
 * 
 * Integer currentPasteCount = possiblePastes.get(id); if (currentPasteCount ==
 * null) { currentPasteCount = 0; } possiblePastes.put(id, currentPasteCount +
 * 1);
 * 
 * TODO: synchronize the numbers the users get with the other collectors (E.g.
 * Alice as user should appear as user.2 in all statistic fields.
 * 
 * @author Lisa Dohrmann, Moritz von Hoffen
 */
@Component(module = "feedback")
public class TextEditCollector extends AbstractStatisticCollector {

    protected static class EditEvent {
        long time;
        int chars;

        public EditEvent(long time, int chars) {
            this.time = time;
            this.chars = chars;
        }
    }

    protected static final Logger log = Logger
        .getLogger(TextEditCollector.class.getName());

    /**
     * Different sample intervals (in milliseconds) for measuring parallel text
     * edits. It is determined for each local text edit if there occurred a
     * remote text edit X seconds before or after
     */
    protected static final int[] sampleIntervals = { 1000, 2000, 5000, 10000,
        15000 };

    protected long charsWritten = 0;

    /**
     * This threshold is the upper limit that is believed to have been produced
     * by "hand". If a single text edit activity contains more characters than
     * specified by this threshold it indicates that a paste action has
     * occurred. That paste action could either mean an auto completion /
     * generation using eclipse or a simple paste from the clip board.
     */
    protected int pasteThreshold = 16;

    protected User localUser = null;

    /** List to contain local {@link EditEvent}s */
    protected List<EditEvent> localEvents = Collections
        .synchronizedList(new ArrayList<EditEvent>());
    /** List to contain remote {@link EditEvent}s */
    protected List<EditEvent> remoteEvents = Collections
        .synchronizedList(new ArrayList<EditEvent>());

    /** Maps sample interval to chars written in this interval */
    protected Map<Integer, Integer> parallelTextEdits = new HashMap<Integer, Integer>();
    /** Maps sample interval to number of edits in this interval */
    protected Map<Integer, Integer> parallelTextEditsCount = new HashMap<Integer, Integer>();

    /** A map which should possible detect auto generation and paste actions */
    protected Map<User, Integer> pastes = new HashMap<User, Integer>();

    /** A map which accumulates the characters produced within paste actions */
    protected Map<User, Integer> pastesCharCount = new HashMap<User, Integer>();

    /**
     * for each key {@link User} an Integer is stored which represents the total
     * chars edited.
     */
    protected Map<User, Integer> remoteCharCount = new HashMap<User, Integer>();

    private final IEditorManager editorManager;

    protected ISharedEditorListener editorListener = new AbstractSharedEditorListener() {

        @Override
        public void textEdited(User user, SPath filePath, int offset,
            String replacedText, String text) {
            /*
             * delete whitespaces from the text because we don't want to count
             * them. that would result in quite a number of counted characters
             * the user actually hasn't written, e.g. when eclipse automatically
             * starts lines with tabs or spaces
             */
            int textLength = StringUtils.deleteWhitespace(text).length();
            // get the JID for the current edit

            EditEvent event = new EditEvent(System.currentTimeMillis(),
                textLength);

            /*
             * if the edit activity text length exceeds the threshold for
             * possible pastes store this as a possible paste and file it under
             * the according JID for the user who made that possible paste.
             * Moreover, store the number of characters that were "pasted" or
             * auto generated.
             */
            if (textLength > pasteThreshold) {
                Integer currentPasteCount = pastes.get(user);
                if (currentPasteCount == null) {
                    currentPasteCount = 0;
                }
                pastes.put(user, currentPasteCount + 1);

                Integer currentPasteChars = pastesCharCount.get(user);
                if (currentPasteChars == null) {
                    currentPasteChars = 0;
                }
                pastesCharCount.put(user, currentPasteChars + textLength);

            }

            if (log.isTraceEnabled()) {
                log.trace(String.format("Received chars written from %s "
                    + "(whitespaces omitted): %s [%s]", user, textLength,
                    StringEscapeUtils.escapeJava(text)));
            }

            if (textLength > 0) {
                if (user.isLocal()) {
                    /*
                     * accumulate the written chars of the local user and store
                     * the time and text length of this Activity
                     */
                    addToCharsWritten(textLength);
                    localEvents.add(event);

                    if (log.isTraceEnabled()) {
                        log.trace("Edits=" + localEvents.size() + " Written="
                            + getCharsWritten());
                    }
                } else {
                    /*
                     * store all remote text edits for future comparison. As
                     * those text edits are remote it needs to be determined,
                     * who made the edit and to increase the appropriate edited
                     * character count. The total text edit count is increased
                     * by one for each TextEditActivity received.
                     */
                    remoteEvents.add(event);
                    Integer currentCharCount = remoteCharCount.get(user);
                    if (currentCharCount == null) {
                        currentCharCount = 0;
                    }
                    remoteCharCount.put(user, currentCharCount + textLength);

                }

            }
        }

    };

    public TextEditCollector(StatisticManager statisticManager,
        ISarosSession session, IEditorManager editorManager) {
        super(statisticManager, session);

        // TODO: remove
        this.editorManager = editorManager;
    }

    protected synchronized void addToCharsWritten(int chars) {
        charsWritten += chars;
    }

    protected synchronized long getCharsWritten() {
        return charsWritten;
    }

    @Override
    protected void processGatheredData() {

        /*
         * a variable to distinguish between users and assign a number to each
         * of these users.
         */
        int userNumber = 1;

        // iterate through the map and write written char count to session data
        for (Map.Entry<User, Integer> entry : remoteCharCount.entrySet()) {
            User currentId = entry.getKey();
            // get character count for this user
            Integer charCount = entry.getValue();
            // write the count to the session data
            data.setRemoteUserCharCount(userNumber, charCount);

            /*
             * check, whether pastes have been recorded for this user. if so,
             * write those to the sessions statistics and if not, set the total
             * pastes for this user to null.
             */

            if (pastes.get(currentId) == null) {
                data.setRemoteUserPastes(userNumber, 0);
                data.setRemoteUserPasteChars(userNumber, 0);
            } else {
                Integer pasteCount = pastes.get(currentId);
                Integer pasteChars = pastesCharCount.get(currentId);
                data.setRemoteUserPastes(userNumber, pasteCount);
                data.setRemoteUserPasteChars(userNumber, pasteChars);
            }
            // increment n (so that next remote peer gets a different number)
            userNumber++;
        }

        // determine the possible pastes for the local user
        if (pastes.get(localUser) == null) {
            data.setLocalUserPastes(0);
            data.setLocalUserPasteChars(0);
        } else {
            Integer pasteCount = pastes.get(localUser);
            Integer pasteChars = pastesCharCount.get(localUser);
            data.setLocalUserPastes(pasteCount);
            data.setLocalUserPasteChars(pasteChars);
        }

        data.setTextEditsCount(localEvents.size());
        data.setTextEditChars(getCharsWritten());

        long start = System.currentTimeMillis();

        /*
         * see if we can find a local edit that was parallel to a remote one for
         * one of the given sample intervals
         */
        for (int interval : sampleIntervals) {
            process(interval);
        }

        /* store the results in the data map */
        if (parallelTextEditsCount.isEmpty()) {
            /*
             * there were no parallel text edits i.e. every edit was
             * non-parallel
             */
            data.setNonParallelTextEdits(getCharsWritten());
            if (!(localEvents.isEmpty()) || !(remoteEvents.isEmpty())) {
                data.setNonParallelTextEditsPercent(100);
            } else {
                data.setNonParallelTextEditsPercent(0);
            }

            return;
        }

        for (Entry<Integer, Integer> e : parallelTextEdits.entrySet()) {
            data.setParallelTextEdits(e.getKey(), e.getValue());
            data.setParallelTextEditsPercent(e.getKey(),
                getPercentage(e.getValue(), getCharsWritten()));
        }

        for (Entry<Integer, Integer> e : parallelTextEditsCount.entrySet()) {
            data.setParallelTextEditsCount(e.getKey(), e.getValue());
        }

        // all in the localEvents list remaining events were non-parallel
        long nonParallelTextEdits = 0;
        for (EditEvent local : localEvents) {
            nonParallelTextEdits += local.chars;
        }

        data.setNonParallelTextEdits(nonParallelTextEdits);
        data.setNonParallelTextEditsPercent(getPercentage(nonParallelTextEdits,
            getCharsWritten()));

        log.debug("Processing text edits took "
            + (System.currentTimeMillis() - start) / 1000.0 + " s");

    }

    /**
     * Processes the given interval width, i.e. the local and remote edit events
     * are iterated until a pair is found that fulfills the condition: <br>
     * <code>local.time is element of [lastRemote.time - intervalWidth,
     * lastRemote.time + intervalWidth].</code><br>
     * <br>
     * This local edit is than counted as a parallel one.
     * 
     * @param intervalWidth
     *            the width of the interval to be considered
     */
    public void process(int intervalWidth) {

        Iterator<EditEvent> remote = remoteEvents.iterator();
        EditEvent lastRemote = (remote.hasNext() ? remote.next() : null);

        for (Iterator<EditEvent> localIterator = localEvents.iterator(); localIterator
            .hasNext();) {
            EditEvent local = localIterator.next();

            // Skip all remote events too far in the past
            while (lastRemote != null
                && local.time > lastRemote.time + intervalWidth) {
                lastRemote = (remote.hasNext() ? remote.next() : null);
            }

            if (lastRemote != null
                && local.time >= lastRemote.time - intervalWidth) {
                /*
                 * This local edit occurred inside the time frame
                 * [lastRemote.time - intervalWidth, lastRemote.time +
                 * intervalWidth]. Therefore count it as a parallel text edit
                 * and remove it from the list, because we are done with it.
                 */
                int intervalSeconds = (int) Math.round(intervalWidth / 1000.0);
                addToMap(parallelTextEdits, intervalSeconds, local.chars);
                addToMap(parallelTextEditsCount, intervalSeconds, 1);
                localIterator.remove();
            }
            /*
             * Else: This local edit is non-parallel for the current
             * intervalWidth but it might be parallel to a larger interval. Just
             * leave it in the map and accumulate all remaining events as
             * non-parallel at the end.
             */
        }
    }

    /**
     * Stores the given value for the given key in the map. If there is a
     * previous value for this key, the old value is added to the new one and
     * the result is stored in the map.
     * 
     * @param map
     * @param key
     * @param value
     */
    public static void addToMap(Map<Integer, Integer> map, Integer key,
        Integer value) {
        Integer oldValue = map.get(key);

        if (oldValue != null) {
            value += oldValue;
        }
        map.put(key, value);
    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        editorManager.addSharedEditorListener(editorListener);
        // set local users JID at the beginning of the session
        localUser = sarosSession.getLocalUser();
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        editorManager.removeSharedEditorListener(editorListener);
    }
}
