package de.fu_berlin.inf.dpp.feedback;

import de.fu_berlin.inf.dpp.Saros;

/**
 * Parent class for FeedbackManager and StatisticManager
 */
public abstract class AbstractFeedbackManager {

    public static final int UNDEFINED = -1;

    protected Saros saros;

    public AbstractFeedbackManager(Saros saros) {
        this.saros = saros;
    }

    /**
     * Sets the global value for the given preference key if it is undefined to
     * the local value from the workspace. If a global value exists and it is
     * different from the local value, then the local value is overwritten.<br>
     * <br>
     * NOTE: Use this method only with a preference key that represents an
     * Integer value.
     * 
     * @param preferenceKey
     */
    protected void makePrefConsistent(String preferenceKey) {
        int globalValue = saros.getConfigPrefs().getInt(preferenceKey,
            UNDEFINED);
        int localValue = saros.getPreferenceStore().getInt(preferenceKey);

        if (globalValue == UNDEFINED) {
            saros.getConfigPrefs().putInt(preferenceKey, localValue);
            saros.saveConfigPrefs();
        } else if (globalValue != localValue) {
            saros.getPreferenceStore().setValue(preferenceKey, globalValue);
        }
    }
}
