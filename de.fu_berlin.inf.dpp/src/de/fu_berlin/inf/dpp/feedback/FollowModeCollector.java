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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * A Collector class that collects the number of local follow-mode toggles
 * executed by a user and the time spent in follow-mode (total time as well as a
 * percentage of total session time).
 * <p>
 * <code>
 * session.followmode.toggles=7         <br>
 * session.followmode.time.percent=25   <br>
 * session.followmode.time.total=12
 * </code> <br>
 */

@Component(module = "feedback")
public class FollowModeCollector extends AbstractStatisticCollector {

    /**
     * A FollowModeEvent has a <code>boolean</code> attribute enabled for the
     * state of follow mode and a <code>long</code> which declares the time when
     * the toggle occurred
     */
    protected static class FollowModeToggleEvent {
        long time;
        boolean enabled;

        public FollowModeToggleEvent(long time, boolean enabled) {
            this.time = time;
            this.enabled = enabled;
        }
    }

    protected static final Logger log = Logger
        .getLogger(FollowModeCollector.class.getName());

    /** reflects the current state of the follow mode */
    protected boolean followModeEnabled = false;

    /** starting time of the session */
    protected long sessionStart = 0;

    /** ending time of the session */
    protected long sessionEnd = 0;

    /** accumulated time spent in follow mode */
    protected long timeInFollowMode = 0;

    /** length of the session */
    protected long sessionDuration = 0;

    /** total count of the follow mode toggles */
    protected int countFollowModeChanges = 0;

    /** Structure to store local follow mode toggle events */
    protected List<FollowModeToggleEvent> followModeChangeEvents = Collections
        .synchronizedList(new ArrayList<FollowModeToggleEvent>());

    private final EditorManager editorManager;

    protected ISharedEditorListener editorListener = new AbstractSharedEditorListener() {

        @Override
        public void followModeChanged(User target, boolean isFollowed) {
            /*
             * set the appropriate mode of follow mode
             */
            followModeEnabled = isFollowed;

            // do some logging
            if (log.isTraceEnabled()) {
                if (!followModeEnabled) {
                    log.trace(String.format("Follow Mode was deactivated"));
                } else {
                    log.trace(String.format("Now following " + target));
                }
            }

            /*
             * when followModeChanged was fired, create a new followModeToggle
             * event
             */
            FollowModeToggleEvent event = new FollowModeToggleEvent(
                System.currentTimeMillis(), followModeEnabled);
            /*
             * store the event in an array list
             */
            followModeChangeEvents.add(event);
            ++countFollowModeChanges;
        }
    };

    public FollowModeCollector(StatisticManager statisticManager,
        ISarosSession session, EditorManager editorManager) {
        super(statisticManager, session);
        this.editorManager = editorManager;
    }

    /** Process the collected data */
    @Override
    protected void processGatheredData() {
        // get starting time of processing
        long start = System.currentTimeMillis();
        /*
         * local variable where total time where follow mode was enabled is
         * accumulated
         */
        long timeFollowModeEnabled = 0;

        // set the number of toggles
        data.setFollowModeTogglesCount(countFollowModeChanges);

        // calculate duration of the session
        sessionDuration = getDiffTime(sessionStart, sessionEnd);

        /*
         * iterate through the array list and calculate time spent in follow
         */
        for (Iterator<FollowModeToggleEvent> iterator = followModeChangeEvents
            .iterator(); iterator.hasNext();) {
            FollowModeToggleEvent currentEntry = iterator.next();
            // if follow mode got enabled, set time stamp
            if (currentEntry.enabled == true) {
                timeFollowModeEnabled = currentEntry.time;
            }
            /*
             * if follow mode got disabled, calculate the time spent in follow
             * mode and add that time to the total time spent in follow mode. In
             * order to do so, use getDiffTime.
             */
            else {
                timeInFollowMode += getDiffTime(timeFollowModeEnabled,
                    currentEntry.time);
            }
        }
        data.setFollowModeTimeTotal(StatisticManager
            .getTimeInMinutes(timeInFollowMode));

        /*
         * call to getPercentage to store percentage of time spent in follow
         * mode in respect to the total session time.
         */
        data.setFollowModeTimePercentage(getPercentage(timeInFollowMode,
            sessionDuration));
        log.debug("Processing follow mode changes took "
            + (System.currentTimeMillis() - start) / 1000.0 + " s");
    }

    /**
     * auxiliary function for calculating duration between
     * <code>start and <end>end</code>
     */
    protected long getDiffTime(long start, long end) {
        long diffTime = end - start;
        if (diffTime < 0) {
            log.warn("Time was negative " + diffTime
                + "ms. The absolute value is being used.");
            diffTime = Math.abs(diffTime);
        }
        return diffTime;
    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        // get starting time of session
        sessionStart = System.currentTimeMillis();
        editorManager.addSharedEditorListener(editorListener);
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        // get the time the session ended
        sessionEnd = System.currentTimeMillis();
        editorManager.removeSharedEditorListener(editorListener);
    }

}
