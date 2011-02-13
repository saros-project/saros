package de.fu_berlin.inf.dpp.project.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.FileListActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Utils;

public class FileListManager implements IActivityProvider {

    private static Logger log = Logger.getLogger(FileListManager.class);

    protected ISarosSession sarosSession;
    protected SarosSessionManager sessionManager;
    protected final List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();
    @Inject
    protected SarosUI sarosUI;
    @Inject
    protected SessionIDObservable sessionIDObservable;

    public FileListManager(SarosSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        sessionManager.addSarosSessionListener(sessionListener);
    }

    protected AbstractActivityReceiver receiver = new AbstractActivityReceiver() {
        @Override
        public void receive(FileListActivity fileListActivity) {
            handleFileListActivity(fileListActivity);
        }
    };

    protected void handleFileListActivity(FileListActivity fileListActivity) {

        User user = fileListActivity.getSource();
        if (!user.isInSarosSession()) {
            throw new IllegalArgumentException("User " + user
                + " is not a participant in this Saros session");
        }
        if (!user.hasWriteAccess()) {
            log.warn(Utils.prefix(user.getJID())
                + " send FileListActivity, but has no writing permission in session");
            return;
        }
        List<FileList> fileLists = new ArrayList<FileList>();
        fileLists.add(fileListActivity.getFileList());
        JID from = fileListActivity.getSource().getJID();
        String description = fileListActivity.getDescription();
        String projectID = fileListActivity.getProjectID();
        sessionManager.incomingProjectReceived(from, fileLists, sarosUI,
            description, projectID);
    }

    public void exec(IActivity activity) {
        activity.dispatch(receiver);
    }

    public void addActivityListener(IActivityListener listener) {
        this.activityListeners.add(listener);
    }

    public void removeActivityListener(IActivityListener listener) {
        this.activityListeners.remove(listener);
    }

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
            session.addActivityProvider(FileListManager.this);
            sarosSession = session;
        }

        @Override
        public void sessionEnded(ISarosSession project) {
            project.removeActivityProvider(FileListManager.this);
            sarosSession = null;
        }
    };

}
