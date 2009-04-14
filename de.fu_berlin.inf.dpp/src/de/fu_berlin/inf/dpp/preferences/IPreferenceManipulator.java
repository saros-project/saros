package de.fu_berlin.inf.dpp.preferences;

import org.eclipse.core.resources.IProject;

import de.fu_berlin.inf.dpp.util.StateChangeNotifier;

/**
 * A {@link IPreferenceManipulator} is used to disable one particular preference
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
     * Interface used by {@link IPreferenceManipulator#disable(IProject)} to
     * give the caller a possibility to restore the state of the preference
     * represented by the manipulator *before* calling
     * {@link IPreferenceManipulator#disable(IProject)}.
     */
    public interface IRestorePoint {

        /**
         * Will restore the state of the preferences represented by the
         * PreferenceManipulator which returned this {@link IRestorePoint}
         * before the call to {@link IPreferenceManipulator#disable(IProject)}
         * was being made.
         */
        public void restore();

    }

    /**
     * Returns whether the preference represented by this manipulator is current
     * enabled in the scope of the given project
     * 
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
     * Will try to disable the preference represented by this
     * {@link IPreferenceManipulator} for the given {@link IProject}. Will
     * return a {@link IRestorePoint} which can be used to restore the state of
     * the preferences before this method was called.
     */
    public IRestorePoint disable(IProject project);

    public boolean isDangerousForClient();

    public boolean isDangerousForHost();

}
