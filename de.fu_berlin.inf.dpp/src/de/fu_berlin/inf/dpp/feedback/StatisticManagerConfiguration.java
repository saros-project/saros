package de.fu_berlin.inf.dpp.feedback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

public class StatisticManagerConfiguration {

    private static final Random RANDOM = new Random();

    public static String getStatisticsPseudonymID(Saros saros) {

        // First look in the Preferences
        String userID = saros.getPreferenceStore().getString(
            PreferenceConstants.STATISTICS_PSEUDONYM_ID);

        if (userID != null && userID.trim().length() > 0)
            return userID;

        // Fall back to the Configuration
        return saros.getGlobalPreferences().get(
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

        return saros.getGlobalPreferences().getBoolean(
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
        int status = saros.getGlobalPreferences().getInt(
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
        saros.getGlobalPreferences().putBoolean(
            PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM, isPseudonymAllowed);
        saros.saveGlobalPreferences();
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
        saros.getGlobalPreferences().putInt(
            PreferenceConstants.STATISTIC_ALLOW_SUBMISSION, submission);
        saros.saveGlobalPreferences();
        saros.getPreferenceStore().setValue(
            PreferenceConstants.STATISTIC_ALLOW_SUBMISSION, submission);
    }

    public static void setStatisticsPseudonymID(Saros saros, String userID) {
        // store in configuration and preference scope
        saros.getGlobalPreferences().put(PreferenceConstants.STATISTICS_PSEUDONYM_ID,
            userID);
        saros.saveGlobalPreferences();
        saros.getPreferenceStore().setValue(
            PreferenceConstants.STATISTICS_PSEUDONYM_ID, userID);
    }

    /**
     * Returns the random user ID from the global preferences, if one was
     * created and saved yet. If none was found, a newly generated ID is stored
     * and returned.
     * 
     * @see StatisticManagerConfiguration#generateUserID()
     * 
     * @return the random user ID for this eclipse installation
     */
    public static String getUserID(Saros saros) {
        String userID = saros.getGlobalPreferences().get(
            PreferenceConstants.RANDOM_USER_ID, null);
        if (userID == null) {
            userID = generateUserID();
            // save ID in the global preferences
            saros.getGlobalPreferences().put(PreferenceConstants.RANDOM_USER_ID,
                userID);
            saros.saveGlobalPreferences();
        }
        // HACK if we are a developer, add this info to our user ID
        if (saros.getVersion().endsWith("DEVEL"))
            userID = "sarosTeam-" + userID;
        if (isPseudonymSubmissionAllowed(saros))
            userID += "-"
                + StatisticManagerConfiguration.getStatisticsPseudonymID(saros);
        return userID;
    }

    /**
     * Generates a random user ID. The ID consists of the current date and time
     * plus a random positive Integer. Thus identical IDs for different users
     * should be very unlikely.
     * 
     * @return a random user ID e.g. 2009-06-11_14-53-59_1043704453
     */
    public static String generateUserID() {
        int randInt = RANDOM.nextInt(Integer.MAX_VALUE);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss");
        String userID = dateFormat.format(new Date()) + "_" + randInt;

        return userID;
    }
}
