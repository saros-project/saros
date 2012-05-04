package de.fu_berlin.inf.dpp.feedback;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

public class StatisticManagerConfiguration {

    public static String getStatisticsPseudonymID(Saros saros) {

        // First look in the Preferences
        String userID = saros.getPreferenceStore().getString(
            PreferenceConstants.STATISTICS_PSEUDONYM_ID);

        if (userID != null && userID.trim().length() > 0)
            return userID;

        // Fall back to the Configuration
        return saros.getConfigPrefs().get(
            PreferenceConstants.STATISTICS_PSEUDONYM_ID, "");
    }

    public static boolean isStatisticSubmissionAllowed(Saros saros) {
        return getStatisticSubmissionStatus(saros) == AbstractFeedbackManager.ALLOW;
    }

    public static boolean hasStatisticAgreement(Saros saros) {
        return getStatisticSubmissionStatus(saros) != AbstractFeedbackManager.UNKNOWN;
    }

    public static boolean isPseudonymSubmissionAllowed(Saros saros) {

        String result = saros.getPreferenceStore().getString(
            PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM);

        if (result != null && result.trim().length() > 0) {
            return saros.getPreferenceStore().getBoolean(
                PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM);
        }

        return saros.getConfigPrefs().getBoolean(
            PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM, false);
    }

    /**
     * Returns whether the submission of statistic is allowed, forbidden or
     * unknown. The global preferences have priority but if the value wasn't
     * found there the value from the PreferenceStore (with fall back to the
     * default) is used.
     * 
     * @return 0 = unknown, 1 = allowed, 2 = forbidden
     */
    public static int getStatisticSubmissionStatus(Saros saros) {
        int status = saros.getConfigPrefs().getInt(
            PreferenceConstants.STATISTIC_ALLOW_SUBMISSION,
            AbstractFeedbackManager.UNDEFINED);

        if (status == AbstractFeedbackManager.UNDEFINED)
            status = saros.getPreferenceStore().getInt(
                PreferenceConstants.STATISTIC_ALLOW_SUBMISSION);
        return status;
    }

    public static void setPseudonymSubmissionAllowed(Saros saros,
        boolean isPseudonymAllowed) {
        // store in configuration and preference scope
        saros.getConfigPrefs().putBoolean(
            PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM, isPseudonymAllowed);
        saros.saveConfigPrefs();
        saros.getPreferenceStore().setValue(
            PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM, isPseudonymAllowed);

    }

    /**
     * Saves in the workspace and globally if the user wants to submit statistic
     * data.
     */
    public static void setStatisticSubmissionAllowed(Saros saros, boolean allow) {
        int submission = allow ? AbstractFeedbackManager.ALLOW
            : AbstractFeedbackManager.FORBID;
        setStatisticSubmission(saros, submission);
    }

    /**
     * Saves in the workspace and globally if the user wants to submit statistic
     * data.<br>
     * <br>
     * Note: It must be set globally first, so the PropertyChangeListener for
     * the local setting is working with latest global data.
     * 
     * @param saros
     *            The preferences to be used
     * @param submission
     *            (see constants of {@link AbstractFeedbackManager})
     */
    protected static void setStatisticSubmission(Saros saros, int submission) {
        // store in configuration and preference scope
        saros.getConfigPrefs().putInt(
            PreferenceConstants.STATISTIC_ALLOW_SUBMISSION, submission);
        saros.saveConfigPrefs();
        saros.getPreferenceStore().setValue(
            PreferenceConstants.STATISTIC_ALLOW_SUBMISSION, submission);
    }

    public static void setStatisticsPseudonymID(Saros saros, String userID) {
        // store in configuration and preference scope
        saros.getConfigPrefs().put(PreferenceConstants.STATISTICS_PSEUDONYM_ID,
            userID);
        saros.saveConfigPrefs();
        saros.getPreferenceStore().setValue(
            PreferenceConstants.STATISTICS_PSEUDONYM_ID, userID);
    }
}
