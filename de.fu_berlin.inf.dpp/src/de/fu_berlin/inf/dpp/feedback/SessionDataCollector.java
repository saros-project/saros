/*
 * DPP - Serious Distributed Pair Programming
 * (c) Lisa Dohrmann, Freie Universitaet Berlin 2009
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

import org.joda.time.DateTime;
import org.joda.time.Duration;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Collects some general session data (session time, session ID, session count),
 * platform informations and settings.
 * 
 * @author Lisa Dohrmann
 */
@Component(module = "feedback")
public class SessionDataCollector extends AbstractStatisticCollector {

    protected SessionIDObservable sessionID;
    protected FeedbackManager feedbackManager;
    protected Saros saros;

    protected String currentSessionID;
    protected DateTime localSessionStart;
    protected DateTime localSessionEnd;
    protected boolean isHost;

    public SessionDataCollector(StatisticManager statisticManager,
        SarosSessionManager sessionManager, SessionIDObservable sessionID,
        Saros saros, FeedbackManager feedbackManager) {
        super(statisticManager, sessionManager);
        this.sessionID = sessionID;
        this.saros = saros;
        this.feedbackManager = feedbackManager;
    }

    @Override
    protected void processGatheredData() {
        data.setSessionID(currentSessionID);
        data.setLocalSessionStartTime(localSessionStart);
        data.setLocalSessionEndTime(localSessionEnd);
        data.setLocalSessionDuration(StatisticManager
            .getTimeInMinutes(new Duration(localSessionStart, localSessionEnd)
                .getMillis()));
        data.setSessionCount(statisticManager.getSessionCount());
        data.setIsHost(isHost);

        if (statisticManager.isPseudonymSubmissionAllowed()) {
            String pseudonym = statisticManager.getStatisticsPseudonymID()
                .trim();
            if (pseudonym.length() > 0) {
                data.setPseudonym(pseudonym);
            }
        }
        storeGeneralInfos();
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        localSessionEnd = new DateTime();
        isHost = sarosSession.getLocalUser().isHost();
    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        currentSessionID = sessionID.getValue();
        localSessionStart = new DateTime();
    }

    /**
     * Stores some general platform information and settings, e.g. Saros
     * version, Java version, feedback settings, states of various settings
     * (auto follow mode)
     * 
     */
    protected void storeGeneralInfos() {
        data.setSarosVersion(saros.getVersion());
        data.setJavaVersion(System.getProperty("java.version",
            "Unknown Java Version"));
        data.setOSName(System.getProperty("os.name", "Unknown OS"));
        data.setEclipseVersion(Utils.getEclipsePlatformInfo());
        data.setFeedbackDisabled(feedbackManager.isFeedbackDisabled());
        data.setFeedbackInterval(feedbackManager.getSurveyInterval());
        data.setUserID(statisticManager.getUserID());
        data.setAutoFollowModeEnabled(saros.getAutoFollowEnabled());
    }
}