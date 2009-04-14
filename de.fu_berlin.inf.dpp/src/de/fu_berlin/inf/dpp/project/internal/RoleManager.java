package de.fu_berlin.inf.dpp.project.internal;

import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.AbstractActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.util.xstream.XppReader;

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
                IActivity activity = new RoleActivity(Saros.getDefault()
                    .getMyJID().toString(), user.getJID().toString(), user
                    .getUserRole());
                for (IActivityListener listener : RoleManager.this.activityListeners) {
                    listener.activityCreated(activity);
                }
            }
        }
    };

    public RoleManager() {
        Saros.getDefault().getSessionManager().addSessionListener(
            sessionListener);
    }

    public final ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            sharedProject = project;
            sharedProject.addListener(sharedProjectListener);
            sharedProject.getActivityManager().addProvider(RoleManager.this);
        }

        @Override
        public void sessionEnded(ISharedProject project) {
            assert sharedProject == project;
            sharedProject.removeListener(sharedProjectListener);
            sharedProject.getActivityManager().removeProvider(RoleManager.this);
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
            User user = this.sharedProject.getParticipant(roleActivity
                .getAffectedUser());
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
            return (IActivity) AbstractActivity.xstream
                .unmarshal(new XppReader(parser));
        } else {
            return null;
        }
    }
}
