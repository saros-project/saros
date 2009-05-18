/**
 * 
 */
package de.fu_berlin.inf.dpp.feedback;

import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.FeedbackDialog;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * The FeedbackManager registers himself as a listener with the
 * {@link SessionManager} to show a {@link FeedbackDialog} at the end of a
 * session. But before he actually shows anything, it is determined from the
 * global preferences if the user wants to participate in general and in which
 * interval.
 * 
 * @author Lisa Dohrmann
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class FeedbackManager extends AbstractFeedbackManager {
    /** the URL to the website that contains our survey */
    public static final String SURVEY_URL = "https://www.inf.fu-berlin.de/w/SE/DPPSurveys"; //$NON-NLS-1$

    /** the text to show in the first FeedbackDialog */
    public static final String FEEDBACK_REQUEST = Messages
        .getString("feedback.dialog.request.general"); //$NON-NLS-1$

    public static final String FEEDBACK_REQUEST_SHORT = Messages
        .getString("feedback.dialog.request.short"); //$NON-NLS-1$

    public static final long MIN_SESSION_TIME = 5 * 60; // 5 min.

    public static final int FEEDBACK_UNDEFINED = 0;
    public static final int FEEDBACK_ENABLED = 1;
    public static final int FEEDBACK_DISABLED = 2;

    protected static final Logger log = Logger.getLogger(FeedbackManager.class
        .getName());

    protected ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject session) {
            startTime = new Date();
        }

        @Override
        public void sessionEnded(ISharedProject session) {
            long sessionTime = (new Date().getTime() - startTime.getTime()) / 1000;
            log.info(String.format("Session lasted %s min %s s",
                sessionTime / 60, sessionTime % 60));

            // don't show the survey if session was very short
            if (sessionTime < MIN_SESSION_TIME)
                return;

            int sessionsUntilNext = getSessionsUntilNext() - 1;
            setSessionsUntilNext(sessionsUntilNext);
            log.debug("Sessions until next survey: " + sessionsUntilNext);

            if (!showNow())
                return;

            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    // ask the user for feedback
                    // TODO show different messages
                    Dialog dialog = new FeedbackDialog(EditorAPI.getShell(),
                        saros, FeedbackManager.this, FEEDBACK_REQUEST);
                    int exitCode = dialog.open();
                    if (exitCode == Window.OK) {
                        showSurvey();
                    }
                }
            });
        }

    };

    protected IPropertyChangeListener propertyListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(
                PreferenceConstants.FEEDBACK_SURVEY_INTERVAL)) {
                // each time the interval changes, reset the number of
                // sessions until the next request is shown
                resetSessionsUntilNext();
            } else if (event.getProperty().equals(
                PreferenceConstants.FEEDBACK_SURVEY_DISABLED)) {
                Object value = event.getNewValue();
                int disabled = ((Integer) value).intValue();
                // if it changed to enabled, reset
                // interval as well
                if (disabled == FEEDBACK_ENABLED) {
                    resetSessionsUntilNext();
                }
            }
        }

    };

    protected Date startTime;

    public FeedbackManager(final Saros saros, SessionManager sessionManager) {
        super(saros);

        ensureConsistentPreferences();

        // listen for start and end of a session
        sessionManager.addSessionListener(sessionListener);
        // listen for feedback preference changes
        saros.getPreferenceStore().addPropertyChangeListener(propertyListener);
    }

    /**
     * Ensures that the preferences the FeedbackManager manages are consistent
     * after plugin start, i.e. if they are not existing in the global scope,
     * the value from the workspace (might be the default) is globally set. If
     * there exists a different value in the workspace than in the global scope,
     * then the local value is overwritten. <br>
     * <br>
     * 
     * This must be done for all values kept both globally and per workspace.
     */
    protected void ensureConsistentPreferences() {
        makePrefConsistent(PreferenceConstants.FEEDBACK_SURVEY_DISABLED);
        makePrefConsistent(PreferenceConstants.FEEDBACK_SURVEY_INTERVAL);
    }

    /**
     * Number of sessions until the {@link FeedbackDialog} is shown.
     * 
     * @return a positive number if the value exists in the preferences or the
     *         default value -1
     */
    public int getSessionsUntilNext() {
        return saros.getConfigPrefs().getInt(
            PreferenceConstants.SESSIONS_UNTIL_NEXT, -1);
    }

    /**
     * Saves the number of session until the next {@link FeedbackDialog} is
     * shown in the global preferences.
     * 
     * @param untilNext
     */
    public void setSessionsUntilNext(int untilNext) {
        saros.getConfigPrefs().putInt(PreferenceConstants.SESSIONS_UNTIL_NEXT,
            untilNext);
        saros.saveConfigPrefs();
    }

    /**
     * Returns whether the feedback is disabled or enabled by the user. The
     * global preferences have priority but if the value wasn't found there the
     * value from the PreferenceStore (with fall back to the default) is used.
     * 
     * @return 0 - undefined, 1 - enabled, 2 - disabled
     */
    public int getFeedbackStatus() {
        int disabled = saros.getConfigPrefs().getInt(
            PreferenceConstants.FEEDBACK_SURVEY_DISABLED, -1);

        if (disabled == -1)
            disabled = saros.getPreferenceStore().getInt(
                PreferenceConstants.FEEDBACK_SURVEY_DISABLED);
        return disabled;
    }

    /**
     * Returns if the feedback is disabled as a boolean.
     * 
     * @return true if it is disabled
     */
    public boolean isFeedbackDisabled() {
        return getFeedbackStatus() != FEEDBACK_ENABLED;
    }

    /**
     * Saves in the global preferences and in the workspace if the feedback is
     * disabled or not.
     * 
     * @param disabled
     */
    public void setFeedbackDisabled(boolean disabled) {
        int status = disabled ? FEEDBACK_DISABLED : FEEDBACK_ENABLED;

        saros.getPreferenceStore().setValue(
            PreferenceConstants.FEEDBACK_SURVEY_DISABLED, status);
        saros.getConfigPrefs().putInt(
            PreferenceConstants.FEEDBACK_SURVEY_DISABLED, status);
        saros.saveConfigPrefs();
    }

    /**
     * Returns the interval in which the survey should be shown. The global
     * preferences have priority but if the value wasn't found there the value
     * from the PreferenceStore (with fall back to the default) is used.
     * 
     * @return
     */
    public int getSurveyInterval() {
        int interval = saros.getPreferenceStore().getInt(
            PreferenceConstants.FEEDBACK_SURVEY_INTERVAL);
        return saros.getConfigPrefs().getInt(
            PreferenceConstants.FEEDBACK_SURVEY_INTERVAL, interval);
    }

    public void setSurveyInterval(int interval) {
        saros.getPreferenceStore().setValue(
            PreferenceConstants.FEEDBACK_SURVEY_INTERVAL, interval);
        saros.getConfigPrefs().putInt(
            PreferenceConstants.FEEDBACK_SURVEY_INTERVAL, interval);
        saros.saveConfigPrefs();
    }

    /**
     * Resets the counter of sessions until the survey request is shown the next
     * time. It is reset to 1, which means that the survey is shown after the
     * next session. Only then the counter is set to the current interval
     * length.<br>
     * This ensures that the survey is shown at the beginning of the interval
     * rather than at the end.
     * 
     */
    public void resetSessionsUntilNext() {
        setSessionsUntilNext(1);
    }

    /**
     * Resets the counter of sessions until the survey request is shown the next
     * time to the current interval length.
     */
    public void resetSessionsUntilNextToInterval() {
        setSessionsUntilNext(getSurveyInterval());
    }

    /**
     * Tries to open the survey in the default external browser. If this method
     * fails Eclipse's internal browser is tried to use. If both methods failed,
     * a message dialog that contains the survey URL is shown.<br>
     * <br>
     * The number of sessions until the next reminder is shown is reset to the
     * current interval length on every call.
     */
    public void showSurvey() {

        if (!Util.openExternalBrowser(SURVEY_URL)) {
            if (!Util.openInternalBrowser(SURVEY_URL, Messages
                .getString("feedback.dialog.title"))) {
                // last resort: present a link to the survey
                // TODO user should be able to copy&paste the link easily
                MessageDialog.openWarning(EditorAPI.getShell(),
                    "Opening survey failed",
                    "Your browser couldn't be opend. Please visit "
                        + SURVEY_URL + " yourself.");
            }
        }
    }

    /**
     * Determines from the users preferences if the survey should be shown now.
     * 
     * @return true if the survey should be shown now, false otherwise
     */
    public boolean showNow() {
        if (isFeedbackDisabled()) {
            return false;
        }
        if (getSessionsUntilNext() > 0) {
            return false;
        }
        resetSessionsUntilNextToInterval();
        return true;
    }

}
