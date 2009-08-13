package de.fu_berlin.inf.dpp.project.internal;

import java.util.LinkedList;
import java.util.List;

import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SessionView;

/**
 * This manager is responsible for handling driver changes.
 * 
 * @author rdjemili
 */
@Component(module = "core")
public class RoleManager implements IActivityProvider {

    private final List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

    private ISharedProject sharedProject;

    private ISharedProjectListener sharedProjectListener = new ISharedProjectListener() {

        public void roleChanged(User user) {

            /*
             * Not nice to have GUI stuff here, but it can't be handled in
             * SessionView because it is not guaranteed there actually is a
             * session view open.
             */
            SessionView.showNotification("Role changed", String.format(
                "%s %s now %s of this session.", user.getHumanReadableName(),
                user.isLocal() ? "are" : "is", user.isDriver() ? "a driver"
                    : "an observer"));
        }

        public void userJoined(User user) {
            SessionView.showNotification("User joined", user
                .getHumanReadableName()
                + " joined the session.");
        }

        public void userLeft(User user) {
            SessionView.showNotification("User left", user
                .getHumanReadableName()
                + " left the session.");
        }
    };

    @Inject
    protected Saros saros;

    public RoleManager(SessionManager sessionManager) {
        sessionManager.addSessionListener(sessionListener);
    }

    public final ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            sharedProject = project;
            sharedProject.addListener(sharedProjectListener);
            sharedProject.addActivityProvider(RoleManager.this);
        }

        @Override
        public void sessionEnded(ISharedProject project) {
            assert sharedProject == project;
            sharedProject.removeListener(sharedProjectListener);
            sharedProject.removeActivityProvider(RoleManager.this);
            sharedProject = null;
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void addActivityListener(IActivityListener listener) {
        this.activityListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void removeActivityListener(IActivityListener listener) {
        this.activityListeners.remove(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void exec(IActivity activity) {
        if (activity instanceof RoleActivity) {
            RoleActivity roleActivity = (RoleActivity) activity;
            User user = this.sharedProject.getUser(roleActivity
                .getAffectedUser());
            if (user == null) {
                throw new IllegalArgumentException("User "
                    + roleActivity.getAffectedUser()
                    + " is not a participant in this shared project");
            }
            UserRole role = roleActivity.getRole();
            this.sharedProject.setUserRole(user, role);
        }
    }
}
