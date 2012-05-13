package de.fu_berlin.inf.dpp.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.optional.jdt.JDTFacade;
import de.fu_berlin.inf.dpp.preferences.IPreferenceManipulator.IRestorePoint;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SharedResourcesManager;

/**
 * The Preference Manager is responsible for
 * 
 * a.) changing dangerous preferences on the host or client before a
 * SarosSession session is started<br>
 * haferburg: FIXME What is "dangerous" supposed to mean???
 * 
 * b.) restoring the preference after the end of the session
 * 
 * c.) TODO Tracking changes to the preferences during the session and warning
 * the user
 * 
 * d.) TODO Show proper warnings to the user
 */
/*
 * FIXME This class registers a SessionListener for project specific stuff. It
 * should not rely on the fact that a project is only added when a new session
 * starts. We need a new type of listener to handle the addition of a project to
 * a session.
 */
@Component(module = "prefs")
public class PreferenceManager implements Startable {
    protected List<IPreferenceManipulator> manipulators = new ArrayList<IPreferenceManipulator>();

    protected List<IRestorePoint> restorePoints = new ArrayList<IRestorePoint>();

    private final ISarosSession sarosSession;

    /*
     * The resourceManager is added to add a dependency from PreferenceManager
     * to SharedResourceManager. This will make the picocontainer first call
     * start on the SharedResourceManager and then on this class.
     * 
     * The PreferencesManager relies on the fact that a project is added only
     * when a session is started, and it might create a new file
     * ".settings/org.eclipse.core.resources.prefs" for the project specific
     * settings. Adding PreferencesManager after the SharedResourcesManager
     * makes sure that the file creation is registered by the
     * SharedResourcesManager.
     */
    public PreferenceManager(JDTFacade jdtFacade, ISarosSession sarosSession,
        SharedResourcesManager resourceManager) {
        this.sarosSession = sarosSession;

        // collect PreferenceManipulators
        if (jdtFacade.isJDTAvailable()) {
            manipulators.addAll(jdtFacade.getPreferenceManipulators());
        }
        manipulators.add(new LineDelimiterManipulator());
        manipulators.add(new EncodingManipulator());
    }

    @Override
    public void start() {

        // TODO The user should be told that we are changing options...
        for (IProject project : sarosSession.getProjects()) {
            if (sarosSession.isHost()) {
                for (IPreferenceManipulator manipulator : manipulators) {
                    if (manipulator.isDangerousForHost()
                        && manipulator.isEnabled(project)) {
                        restorePoints.add(manipulator.change(project));
                    }
                }
            } else {
                for (IPreferenceManipulator manipulator : manipulators) {
                    if (manipulator.isDangerousForClient()
                        && manipulator.isEnabled(project)) {
                        restorePoints.add(manipulator.change(project));
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        // TODO we should monitor the preferences for changes during the
        // session
        for (IRestorePoint restorePoint : restorePoints) {
            restorePoint.restore();
        }
        restorePoints.clear();
    }
}
