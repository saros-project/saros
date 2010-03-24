package de.fu_berlin.inf.dpp.preferences;

import org.eclipse.core.resources.IProject;

import de.fu_berlin.inf.dpp.util.StateChangeNotifier;

/**
 * A {@link IPreferenceManipulator} is used to change one particular preference
 * which might cause problems during a shared project session.
 * 
 * For example:
 * 
 * - SaveActionPreferenceManipulator is used to disable Save Actions which cause
 * problems on saving.
 * 
 */
public interface IPreferenceManipulator {

    /**
     * Interface used by {@link IPreferenceManipulator#change(IProject)} to give
     * the caller a possibility to restore the state of the preference
     * represented by the manipulator *before* calling
     * {@link IPreferenceManipulator#change(IProject)}.
     */
    public interface IRestorePoint {

        /**
         * Will restore the state of the preferences represented by the
         * PreferenceManipulator which returned this {@link IRestorePoint}
         * before the call to {@link IPreferenceManipulator#change(IProject)}
         * was being made.
         */
        public void restore();

    }

    /**
     * Returns <code>true</code> if the preference setting handled by this
     * manipulator is currently in a state which could be dangerous for the host
     * or client, <code>false</false> otherwise.
     * 
     * If <code>true</code> is returned, a caller might want to call
     * {@link #change(IProject)} to disable this preference setting.
     */
    public boolean isEnabled(IProject project);

    /**
     * Returns a {@link StateChangeNotifier} to use if one is interested in
     * becoming updated when the preference represented by this manipulator
     * changes for the given project OR null, if being notified of state changes
     * is not supported.
     * 
     */
    public StateChangeNotifier<IPreferenceManipulator> getPreferenceStateChangeNotifier(
        IProject project);

    /**
     * Will try to change the preference represented by this
     * {@link IPreferenceManipulator} for the given {@link IProject}. Will
     * return a {@link IRestorePoint} which can be used to restore the state of
     * the preferences before this method was called.
     */
    public IRestorePoint change(IProject project);

    public boolean isDangerousForClient();

    public boolean isDangerousForHost();

}
