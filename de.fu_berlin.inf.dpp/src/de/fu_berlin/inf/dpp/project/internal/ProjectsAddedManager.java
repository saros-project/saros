package de.fu_berlin.inf.dpp.project.internal;

import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.ProjectsAddedActivity;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;

/**
 * This class processes incoming {@link ProjectsAddedActivity
 * ProjectsAddedActivities}
 */
public class ProjectsAddedManager extends AbstractActivityProvider implements
    Startable {

    protected final ISarosSessionManager sessionManager;
    protected final ISarosSession sarosSession;

    public ProjectsAddedManager(ISarosSessionManager sessionManager,
        ISarosSession sarosSession) {
        this.sessionManager = sessionManager;
        this.sarosSession = sarosSession;
    }

    protected AbstractActivityReceiver receiver = new AbstractActivityReceiver() {
        @Override
        public void receive(ProjectsAddedActivity fileListActivity) {
            handleFileListActivity(fileListActivity);
        }
    };

    protected void handleFileListActivity(ProjectsAddedActivity fileListActivity) {
        sessionManager.incomingProjectReceived(fileListActivity.getSource()
            .getJID(), fileListActivity.getProjectInfos(), fileListActivity
            .getProcessID());
    }

    @Override
    public void exec(IActivity activity) {
        activity.dispatch(receiver);
    }

    @Override
    public void start() {
        sarosSession.addActivityProvider(this);
    }

    @Override
    public void stop() {
        sarosSession.removeActivityProvider(this);
    }
}
