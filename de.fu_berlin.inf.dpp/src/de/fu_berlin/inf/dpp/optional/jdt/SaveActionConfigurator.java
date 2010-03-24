package de.fu_berlin.inf.dpp.optional.jdt;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.internal.corext.fix.CleanUpPostSaveListener;
import org.eclipse.jdt.internal.corext.fix.CleanUpPreferenceUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.fix.CleanUpSaveParticipantPreferenceConfiguration;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.AbstractSaveParticipantPreferenceConfiguration;
import org.eclipse.jdt.internal.ui.javaeditor.saveparticipant.SaveParticipantRegistry;
import org.eclipse.jdt.ui.JavaUI;

import de.fu_berlin.inf.dpp.preferences.IPreferenceManipulator;
import de.fu_berlin.inf.dpp.util.StateChangeNotifier;

/**
 * The responsibility of the SaveActionConfigurator is to provide methods for:
 * 
 * - Checking whether SaveActions are enabled
 * 
 * - Methods for disabling SaveActions and restoring them.
 * 
 * - TODO Provide method for monitoring SaveActions enabled state.
 * 
 * Important classes for this are:
 * 
 * {@link SaveParticipantRegistry} - This is the class that JDT classes use to
 * find out what to do on save
 * 
 * {@link CleanUpPostSaveListener} - This is the only PostSaveListener currently
 * available
 * 
 * {@link CleanUpPreferenceUtil} - Utility class for saving options of a
 * PostSaveListener (but not whether the options are enabled or not)
 * 
 * {@link AbstractSaveParticipantPreferenceConfiguration} - This class contains
 * the prefix used for the configuration key
 * 
 * {@link CleanUpSaveParticipantPreferenceConfiguration } - This is the actual
 * Save Action Preference Page
 */
@SuppressWarnings( { "restriction" })
public class SaveActionConfigurator implements IPreferenceManipulator {

    private static final Logger log = Logger
        .getLogger(SaveActionConfigurator.class.getName());

    public boolean isEnabled(IProject project) {

        // We could access the preferences directly as in {@link
        // AbstractSaveParticipantPreferenceConfiguration
        // #isEnabled(IScopeContext)}
        return JavaPlugin.getDefault().getSaveParticipantRegistry()
            .getEnabledPostSaveListeners(project).length > 0;
    }

    protected String getSaveActionPreferenceKey() {

        // Copied from AbstractSaveParticipantPreferenceConfiguration because
        // the field is private
        String EDITOR_SAVE_PARTICIPANT_PREFIX = "editor_save_participant_";

        // Currently the JDT only has this one PostSaveListener
        String POSTSAVELISTENER_ID = CleanUpPostSaveListener.POSTSAVELISTENER_ID;

        return EDITOR_SAVE_PARTICIPANT_PREFIX + POSTSAVELISTENER_ID;
    }

    public IRestorePoint change(final IProject project) {

        IEclipsePreferences prefs = new ProjectScope(project)
            .getNode(JavaUI.ID_PLUGIN);

        if (prefs == null) {
            log.error("No preference store in project scope available");
            return null;
        }

        final String oldValue = prefs.get(getSaveActionPreferenceKey(), null);

        prefs.putBoolean(getSaveActionPreferenceKey(), false);

        return new IRestorePoint() {

            public void restore() {
                IEclipsePreferences prefs = new ProjectScope(project)
                    .getNode(JavaUI.ID_PLUGIN);

                if (oldValue == null) {
                    prefs.remove(getSaveActionPreferenceKey());
                } else {
                    prefs.put(getSaveActionPreferenceKey(), oldValue);
                }

            }

        };
    }

    public StateChangeNotifier<IPreferenceManipulator> getPreferenceStateChangeNotifier(
        IProject project) {
        return null;
    }

    public boolean isDangerousForClient() {
        return true;
    }

    public boolean isDangerousForHost() {
        return true;
    }
}
