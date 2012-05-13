package de.fu_berlin.inf.dpp.project.internal;

import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.ProjectsAddedActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.Messages;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This class processes incoming {@link ProjectsAddedActivity
 * ProjectsAddedActivities}
 */
public class ProjectsAddedManager extends AbstractActivityProvider implements
    Startable {

    private static final Logger log = Logger
        .getLogger(ProjectsAddedManager.class);

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

        User user = fileListActivity.getSource();
        if (!user.isInSarosSession()) {
            throw new IllegalArgumentException(MessageFormat.format(
                Messages.ProjectsAddedManager_user_no_participant_of_session,
                user));
        }
        if (!user.hasWriteAccess()) {
            log.warn(Utils.prefix(user.getJID())
                + " send FileListActivity, but has no writing permission in session"); //$NON-NLS-1$
            return;
        }
        JID from = fileListActivity.getSource().getJID();
        sessionManager
            .incomingProjectReceived(from, fileListActivity.getProjectInfos(),
                fileListActivity.getProcessID());
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
