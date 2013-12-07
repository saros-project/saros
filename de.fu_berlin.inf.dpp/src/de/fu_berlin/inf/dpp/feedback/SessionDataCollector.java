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

import java.util.Date;

import de.fu_berlin.inf.dpp.ISarosContextBindings.PlatformVersion;
import de.fu_berlin.inf.dpp.ISarosContextBindings.SarosVersion;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * Collects some general session data (session time, session ID, session count),
 * platform informations and settings.
 * 
 * @author Lisa Dohrmann
 */
@Component(module = "feedback")
public class SessionDataCollector extends AbstractStatisticCollector {

    protected Saros saros;

    private final String sarosVersion;
    private final String platformVersion;

    protected String currentSessionID;
    protected Date localSessionStart;
    protected Date localSessionEnd;
    protected boolean isHost;

    public SessionDataCollector(StatisticManager statisticManager,
        ISarosSession session, @SarosVersion String sarosVersion,
        @PlatformVersion String platformVersion) {
        super(statisticManager, session);
        this.sarosVersion = sarosVersion;
        this.platformVersion = platformVersion;
    }

    @Override
    protected void processGatheredData() {
        data.setSessionID(currentSessionID);
        data.setLocalSessionStartTime(localSessionStart);
        data.setLocalSessionEndTime(localSessionEnd);
        data.setLocalSessionDuration(StatisticManager.getTimeInMinutes(Math
            .max(0, localSessionEnd.getTime() - localSessionStart.getTime())));
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
        localSessionEnd = new Date();
        isHost = sarosSession.getLocalUser().isHost();
    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        currentSessionID = sarosSession.getID();
        localSessionStart = new Date();
    }

    /**
     * Stores some general platform information and settings, e.g. Saros
     * version, Java version, feedback settings, states of various settings
     * (auto follow mode)
     * 
     */
    protected void storeGeneralInfos() {
        data.setSarosVersion(sarosVersion);
        data.setJavaVersion(System.getProperty("java.version",
            "Unknown Java Version"));
        data.setOSName(System.getProperty("os.name", "Unknown OS"));
        data.setPlatformVersion(platformVersion);
        data.setFeedbackDisabled(FeedbackManager.isFeedbackDisabled());
        data.setFeedbackInterval(FeedbackManager.getSurveyInterval());
        data.setUserID(statisticManager.getUserID());
        data.setAutoFollowModeEnabled(/* no longer used */false);
    }
}