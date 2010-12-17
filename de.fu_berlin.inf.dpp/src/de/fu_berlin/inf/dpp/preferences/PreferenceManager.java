package de.fu_berlin.inf.dpp.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.optional.jdt.JDTFacade;
import de.fu_berlin.inf.dpp.preferences.IPreferenceManipulator.IRestorePoint;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

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
public class PreferenceManager extends AbstractSarosSessionListener {
    protected List<IPreferenceManipulator> manipulators = new ArrayList<IPreferenceManipulator>();

    protected List<IRestorePoint> restorePoints = new ArrayList<IRestorePoint>();

    public PreferenceManager(JDTFacade jdtFacade,
        SarosSessionManager sessionManager) {

        // collect PreferenceManipulators
        if (jdtFacade.isJDTAvailable()) {
            manipulators.addAll(jdtFacade.getPreferenceManipulators());
        }
        manipulators.add(new LineDelimiterManipulator());
        manipulators.add(new EncodingManipulator());

        sessionManager.addSarosSessionListener(this);
    }

    @Override
    public void sessionStarted(ISarosSession newSarosSession) {

        // TODO The user should be told that we are changing options...
        for (IProject project : newSarosSession.getProjects()) {
            if (newSarosSession.isHost()) {
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
    public void sessionEnded(ISarosSession oldSarosSession) {
        // TODO we should monitor the preferences for changes during the
        // session
        for (IRestorePoint restorePoint : restorePoints) {
            restorePoint.restore();
        }
        restorePoints.clear();
    }
}
