package de.fu_berlin.inf.dpp.project.internal;

import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

/**
 * This manager is responsible for handling driver changes.
 * 
 * @author rdjemili
 */
public class RoleManager implements IActivityProvider {
    private final List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

    private ISharedProject sharedProject;

    private ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void roleChanged(User user, boolean replicated) {
            if (!replicated) {
                IActivity activity = new RoleActivity(user.getJID(), user
                    .getUserRole());
                for (IActivityListener listener : RoleManager.this.activityListeners) {
                    listener.activityCreated(activity);
                }
            }
        }
    };

    public RoleManager() {
        Saros.getDefault().getSessionManager().addSessionListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionStarted(ISharedProject project) {
        this.sharedProject = project;
        this.sharedProject.addListener(this.sharedProjectListener);
        this.sharedProject.getActivityManager().addProvider(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionEnded(ISharedProject project) {
        assert this.sharedProject == project;
        this.sharedProject.removeListener(this.sharedProjectListener);
        this.sharedProject.getActivityManager().removeProvider(this);
        this.sharedProject = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess invitation) {
        // ignore
    }

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
            User user = this.sharedProject.getParticipant(roleActivity
                .getUser());
            UserRole role = roleActivity.getRole();
            this.sharedProject.setUserRole(user, role, true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.IActivityProvider
     */
    public IActivity fromXML(XmlPullParser parser) {
        if (parser.getName().equals("user")) {
            JID user = new JID(parser.getAttributeValue(null, "id"));
            UserRole role = UserRole.valueOf(parser.getAttributeValue(null,
                "role"));
            return new RoleActivity(user, role);
        }

        return null;
    }
}
