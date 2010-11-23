/*
 * DPP - Serious Distributed Pair Programming
 * (c) Moritz v. Hoffen, Freie Universitaet Berlin 2010
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
import java.util.List;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.audio.AbstractAudioServiceListener;
import de.fu_berlin.inf.dpp.communication.audio.AudioServiceManager;
import de.fu_berlin.inf.dpp.communication.audio.IAudioServiceListener;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * A Collector which collects information about VoIP usage. It calculates the
 * time spent in a VoIP session (in respect to session length as well). Apart
 * from that, the total count of established VoIP sessions is stored.
 * <p>
 * <code>
 * voip.session.count=1<br>
 * voip.time.percent=17<br>
 * voip.time.total=0.19<br>
 * </code> <br>
 */
@Component(module = "feedback")
public class VoIPCollector extends AbstractStatisticCollector {

    /**
     * A VoIPEvent has a <code>boolean</code> attribute enabled for the state of
     * the VoIP session and a <code>long</code> which declares the time when the
     * state changed
     */
    protected static class VoIPEvent {
        long time;
        boolean enabled;

        public VoIPEvent(long time, boolean enabled) {
            this.time = time;
            this.enabled = enabled;
        }
    }

    protected static final Logger log = Logger
        .getLogger(FollowModeCollector.class.getName());

    /** starting time of the session */
    protected long sessionStart = 0;

    /** ending time of the session */
    protected long sessionEnd = 0;

    /** accumulated time spent in follow mode */
    protected long timeInVoIPSession = 0;

    /** length of the session */
    protected long sessionDuration = 0;

    /** number of total sessions started */
    protected int numberVoIPSessions = 0;

    /** Structure to store local VoIP events */
    protected List<VoIPEvent> VoIPEvents = Collections
        .synchronizedList(new ArrayList<VoIPEvent>());

    protected IAudioServiceListener audioListener = new AbstractAudioServiceListener() {
        @Override
        public void startSession(StreamSession newSession) {
            VoIPEvent event = new VoIPEvent(System.currentTimeMillis(), true);
            /*
             * store the event in an array list
             */
            VoIPEvents.add(event);
            numberVoIPSessions++;
        }

        @Override
        public void sessionStopped(StreamSession session) {
            VoIPEvent event = new VoIPEvent(System.currentTimeMillis(), false);
            /*
             * store the event in an array list
             */
            VoIPEvents.add(event);
        }
    };

    public VoIPCollector(StatisticManager statisticManager,
        SarosSessionManager sessionManager, AudioServiceManager audioManager) {
        super(statisticManager, sessionManager);

        audioManager.addAudioListener(audioListener);
    }

    /** Process the collected data */
    @Override
    protected void processGatheredData() {
        /*
         * local variable where total time of active VoIP sessions is
         * accumulated
         */
        long timeVoIPSessionStarted = 0;

        // calculate duration of the session
        sessionDuration = getDiffTime(sessionStart, sessionEnd);

        /*
         * iterate through the array list and calculate time spent in VoIP
         */
        for (VoIPEvent iterator : VoIPEvents) {
            VoIPEvent currentEntry = iterator;
            // if VoIP session got started, set time stamp
            if (currentEntry.enabled == true) {
                timeVoIPSessionStarted = currentEntry.time;
            }

            /*
             * if VoIP session got terminated, calculate the time spent VoIP
             * session and add that time to the total time spent in VoIP. In
             * order to do so, use getDiffTime.
             */
            else {
                timeInVoIPSession += getDiffTime(timeVoIPSessionStarted,
                    currentEntry.time);
            }
        }
        /*
         * set the total time spent in VoIP sessions
         */
        data.setVoIPTime(StatisticManager.getTimeInMinutes(timeInVoIPSession));

        /*
         * call to getPercentage to store percentage of time spent in VoIP
         * sessions in respect to the total session time.
         */
        data.setVoIPPercentage(getPercentage(timeInVoIPSession, sessionDuration));

        /*
         * store the total number of established VoIP sessions in session data.
         */
        data.setVoIPSessionCount(numberVoIPSessions);
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
    protected void clearPreviousData() {
        // reset previous data
        VoIPEvents.clear();
        timeInVoIPSession = 0;
        sessionStart = 0;
        sessionEnd = 0;
        sessionDuration = 0;
        numberVoIPSessions = 0;

        super.clearPreviousData();
    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        // get starting time of session
        sessionStart = System.currentTimeMillis();
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        // get the time the session ended
        sessionEnd = System.currentTimeMillis();
    }
}