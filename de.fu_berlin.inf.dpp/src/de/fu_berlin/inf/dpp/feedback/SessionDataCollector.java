package de.fu_berlin.inf.dpp.feedback;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.Util;

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
    protected long sessionStart;
    protected long sessionTime;
    protected boolean isHost;

    public SessionDataCollector(StatisticManager statisticManager,
        SessionManager sessionManager, SessionIDObservable sessionID,
        Saros saros, FeedbackManager feedbackManager) {
        super(statisticManager, sessionManager);
        this.sessionID = sessionID;
        this.saros = saros;
        this.feedbackManager = feedbackManager;
    }

    @Override
    protected void processGatheredData() {
        data.setSessionID(currentSessionID);
        data.setSessionTime(StatisticManager.getTimeInMinutes(sessionTime));
        data.setSessionCount(statisticManager.getSessionCount());
        data.setIsHost(isHost);

        storeGeneralInfos();
    }

    @Override
    protected void doOnSessionEnd(ISharedProject project) {
        sessionTime = System.currentTimeMillis() - sessionStart;
        isHost = project.getLocalUser().isHost();
    }

    @Override
    protected void doOnSessionStart(ISharedProject project) {
        currentSessionID = sessionID.getValue();
        sessionStart = System.currentTimeMillis();
    }

    /**
     * Stores some general platform information and settings, e.g. Saros
     * version, Java version, feedback settings, etc.
     */
    protected void storeGeneralInfos() {
        data.setSarosVersion(saros.getVersion());
        data.setJavaVersion(System.getProperty("java.version",
            "Unknown Java Version"));
        data.setOSName(System.getProperty("os.name", "Unknown OS"));
        data.setEclipseVersion(Util.getEclipsePlatformInfo());

        data.setFeedbackDisabled(feedbackManager.isFeedbackDisabled());
        data.setFeedbackInterval(feedbackManager.getSurveyInterval());

        data.setUserID(statisticManager.getUserID());
    }
}