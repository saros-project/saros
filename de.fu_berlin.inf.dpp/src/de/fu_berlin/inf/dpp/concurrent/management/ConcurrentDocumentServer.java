package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.ITargetedActivity;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.business.ProjectsAddedActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.internal.ActivityHandler.QueueItem;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * The ConcurrentDocumentServer is responsible for coordinating all
 * JupiterActivities.
 * 
 * All clients (including the host!) will send their JupiterActivities to the
 * ConcurrentDocumentServer on the host, which transforms them (using Jupiter)
 * and then sends them to everybody else.
 * 
 * A ConcurrentDocumentServer exists only on the host!
 */
public class ConcurrentDocumentServer implements Startable {

    private static Logger log = Logger
        .getLogger(ConcurrentDocumentServer.class);

    protected final ISarosSession sarosSession;

    protected JupiterServer server;

    protected final ISharedProjectListener projectListener;

    public ConcurrentDocumentServer(ISarosSession sarosSession) {

        this.sarosSession = sarosSession;
        this.server = new JupiterServer(sarosSession);
        this.projectListener = new HostSideProjectListener();
    }

    @Override
    public void start() {
        sarosSession.addListener(projectListener);
    }

    @Override
    public void stop() {
        sarosSession.removeListener(projectListener);
    }

    /**
     * ISharedProjectListener for updating Jupiter documents on the host.
     * 
     * @host
     */
    public class HostSideProjectListener extends AbstractSharedProjectListener {

        @Override
        public void userJoined(User user) {
            server.addUser(user);
        }

        @Override
        public void userLeft(User user) {
            server.removeUser(user);
        }
    }

    protected final IActivityReceiver hostReceiver = new AbstractActivityReceiver() {
        @Override
        public void receive(FileActivity fileActivity) {
            if (fileActivity.getType() == FileActivity.Type.Removed) {
                server.removePath(fileActivity.getPath());
            }
        }
    };

    /**
     * Transforms the given activities on the server side and returns a list of
     * activities to be executed locally and sent to other users.
     * 
     * @host
     * 
     * @sarosThread Must be executed in the Saros dispatch thread.
     * 
     * @notSWT This method may not be called from SWT, otherwise a deadlock
     *         might occur!!
     */
    public TransformationResult transformIncoming(List<IActivity> activities) {

        assert sarosSession.isHost() : "CDS.transformIncoming must not be called on the client";

        assert !SWTUtils.isSWT() : "CDS.transformIncoming must not be called from SWT";

        TransformationResult result = new TransformationResult(
            sarosSession.getLocalUser());

        final List<User> remoteUsers = sarosSession.getRemoteUsers();
        final List<User> allUsers = sarosSession.getUsers();

        for (IActivity activity : activities) {
            try {
                activity.dispatch(hostReceiver);

                if (activity instanceof JupiterActivity) {
                    result.addAll(receive((JupiterActivity) activity));

                } else if (activity instanceof ChecksumActivity) {
                    result.addAll(withTimestamp((ChecksumActivity) activity));

                } else if (activity instanceof ITargetedActivity) {
                    ITargetedActivity target = (ITargetedActivity) activity;
                    result.add(new QueueItem(target.getRecipients(), activity));

                } else if (remoteUsers.size() > 0
                    && !(activity instanceof ProjectsAddedActivity)) {
                    /**
                     * ProjectsAddedActivities currently break the
                     * Client-Server-Architecture and therefore must not be send
                     * to clients as they already have them.
                     */

                    // We must not send the activity back to the sender
                    List<User> receivers = new ArrayList<User>();
                    for (User user : allUsers) {
                        if (!user.equals(activity.getSource())) {
                            receivers.add(user);
                        }
                    }
                    result.add(new QueueItem(receivers, activity));

                } else {
                    result.executeLocally.add(activity);
                }
            } catch (Exception e) {
                log.error("Error while transforming activity: " + activity, e);
            }
        }
        return result;
    }

    /**
     * Does the actual work of transforming a JupiterActivity.
     */
    protected List<QueueItem> receive(JupiterActivity jupiterActivity) {

        List<QueueItem> result = new ArrayList<QueueItem>();

        // Sync jupiterActivity with jupiter document server
        Map<JID, JupiterActivity> outgoing;
        try {
            outgoing = server.transform(jupiterActivity);
        } catch (TransformationException e) {
            log.error("Error during transformation of: " + jupiterActivity, e);
            // TODO this should trigger a consistency check
            return result;
        }

        for (Entry<JID, JupiterActivity> entry : outgoing.entrySet()) {

            JID jid = entry.getKey();
            User to = sarosSession.getUser(jid);

            if (to == null) {
                log.error("unknown user in transformation result: "
                    + Utils.prefix(jid));
                continue;
            }

            JupiterActivity transformed = entry.getValue();

            result.add(new QueueItem(to, transformed));
        }
        return result;
    }

    /**
     * Resets the JupiterServer for the given combination and path and user.
     * 
     * When this is called on the host, a call to
     * {@link ConcurrentDocumentClient#reset(SPath)} should be executed at the
     * same time on the side of the given user.
     * 
     * @host
     */
    public synchronized void reset(JID jid, SPath path) {

        assert sarosSession.isHost();

        log.debug("Resetting jupiter server for " + Utils.prefix(jid) + ": "
            + path.toString());
        this.server.reset(path, jid);
    }

    /**
     * Does the actual work of transforming a JupiterActivity.
     */
    protected List<QueueItem> withTimestamp(ChecksumActivity checksumActivity) {

        List<QueueItem> result = new ArrayList<QueueItem>();

        // Timestamp checksumActivity with jupiter document server
        Map<JID, ChecksumActivity> outgoing;
        try {
            outgoing = server.withTimestamp(checksumActivity);
        } catch (TransformationException e) {
            log.error("Error during transformation of: " + checksumActivity, e);
            // TODO this should trigger a consistency check
            return result;
        }

        for (Entry<JID, ChecksumActivity> entry : outgoing.entrySet()) {

            JID jid = entry.getKey();
            User to = sarosSession.getUser(jid);

            if (to == null) {
                log.error("unknown user in transformation result: "
                    + Utils.prefix(jid));
                continue;
            }

            ChecksumActivity transformed = entry.getValue();

            result.add(new QueueItem(to, transformed));
        }
        return result;
    }
}
