package de.fu_berlin.inf.dpp.feedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * A collector class that collects local text edit activities and compares them
 * in relation to parallelism or rather concurrency with remote text events.<br>
 * It is measured how many characters the local user wrote in a session
 * (whitespaces are omitted, because Eclipse produces many of them automatically
 * e.g. when a new line is started), how many text edit activities he produced
 * (which can be different to the number of characters he wrote, e.g. when
 * copy&paste or Eclipse's method generation was used) and how concurrent the
 * local user's writing was to remote users using different sample intervals. <br>
 * <br>
 * NOTE: Text edit activities that are triggered by Eclipse (e.g. when restoring
 * an editor) are counted as well. And refactorings can produce quite a large
 * number of characters that are counted. <br>
 * <br>
 * Example:<br>
 * The percent numbers (for all intervals + non-parallel) should add up to
 * (nearly) 100, slight rounding errors are possible. The counted chars for all
 * intervals and non-parallel edits should add up to textedits.chars<br>
 * <br>
 * <code>
 * textedits.chars=5 <br>
 * textedits.count=5 <br>
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
 * textedits.parallel.interval.2.percent=20
 * </code>
 * 
 * 
 * @author Lisa Dohrmann
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

    /** List to contain local text activities */
    protected List<EditEvent> localEvents = Collections
        .synchronizedList(new ArrayList<EditEvent>());
    /** List to contain remote text activities */
    protected List<EditEvent> remoteEvents = Collections
        .synchronizedList(new ArrayList<EditEvent>());

    /** Maps sample interval to chars written in this interval */
    protected Map<Integer, Integer> parallelTextEdits = new HashMap<Integer, Integer>();
    /** Maps sample interval to number of edits in this interval */
    protected Map<Integer, Integer> parallelTextEditsCount = new HashMap<Integer, Integer>();

    protected ISharedEditorListener editorListener = new AbstractSharedEditorListener() {

        @Override
        public void textEditRecieved(User user, IPath editor, String text,
            String replacedText, int offset) {
            /*
             * delete whitespaces from the text because we don't want to count
             * them. that would result in quite a number of counted characters
             * the user actually hasn't written, e.g. when eclipse automatically
             * starts lines with tabs or spaces
             */
            int textLength = StringUtils.deleteWhitespace(text).length();
            EditEvent event = new EditEvent(System.currentTimeMillis(),
                textLength);

            if (log.isTraceEnabled()) {
                log.trace(String.format("Recieved chars written from %s "
                    + "(whitespaces omitted): %s [%s]", user.getJID(),
                    textLength, Util.escapeForLogging(text)));
            }

            if (textLength > 0) {
                if (user.isLocal()) {
                    /*
                     * accumulate the written chars of the local user and store
                     * the time and text length of this activity
                     */
                    addToCharsWritten(textLength);
                    localEvents.add(event);

                    if (log.isTraceEnabled()) {
                        log.trace("Edits=" + localEvents.size() + " Written="
                            + getCharsWritten());
                    }
                } else {
                    // store all remote text edits for future comparison
                    remoteEvents.add(event);
                }
            }
        }

    };

    public TextEditCollector(StatisticManager statisticManager,
        SessionManager sessionManager, EditorManager editorManager) {
        super(statisticManager, sessionManager);

        editorManager.addSharedEditorListener(editorListener);
    }

    protected synchronized void addToCharsWritten(int chars) {
        charsWritten += chars;
    }

    protected synchronized long getCharsWritten() {
        return charsWritten;
    }

    @Override
    protected void processGatheredData() {
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
            data.setNonParallelTextEditsPercent(100);
            return;
        }

        for (Entry<Integer, Integer> e : parallelTextEdits.entrySet()) {
            data.setParallelTextEdits(e.getKey(), e.getValue());
            data.setParallelTextEditsPercent(e.getKey(), getPercentage(e
                .getValue(), getCharsWritten()));
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
    protected void doOnSessionStart(ISharedProject project) {
        // nothing to do here
    }

    @Override
    protected void doOnSessionEnd(ISharedProject project) {
        // nothing to do here
    }

    @Override
    protected synchronized void clearPreviousData() {
        charsWritten = 0;
        localEvents.clear();
        remoteEvents.clear();
        parallelTextEdits.clear();
        parallelTextEditsCount.clear();
        super.clearPreviousData();
    }
}
