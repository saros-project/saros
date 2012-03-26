package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.internal.SarosSession.QueueItem;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * The ConcurrentDocumentServer is responsible for coordinating all
 * JupiterActivities.
 * 
 * All users with {@link User.Permission#WRITE_ACCESS} (also the host!) will
 * send their JupiterActivities to the ConcurrentDocumentServer on the host,
 * which transforms them (using Jupiter) and then sends them to everybody else.
 * 
 * A ConcurrentDocumentServer exists only on the host!
 */
public class ConcurrentDocumentServer implements Disposable {

    private static Logger log = Logger
        .getLogger(ConcurrentDocumentServer.class);

    protected final ISarosSession sarosSession;

    protected JupiterServer server;

    /**
     * Cached since it never changes
     */
    protected final User host;

    protected final ISharedProjectListener projectListener;

    public ConcurrentDocumentServer(ISarosSession sarosSession) {

        if (!sarosSession.isHost())
            throw new IllegalStateException();

        this.sarosSession = sarosSession;
        this.host = sarosSession.getHost();

        this.server = new JupiterServer(sarosSession);
        this.projectListener = new HostSideProjectListener();
        this.sarosSession.addListener(projectListener);
    }

    public void dispose() {
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
            if (user.hasWriteAccess()) {
                server.addUser(user);
            }
        }

        @Override
        public void permissionChanged(User user) {

            if (user.isHost())
                return;

            if (user.hasReadOnlyAccess()) {
                // if restricted to read-only
                server.removeUser(user);
            } else {
                // if user got write access
                server.addUser(user);
            }
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
     * Transforms the given activityDataObjects on the server side and returns a
     * list of activityDataObjects to be executed locally and sent to other
     * users.
     * 
     * @host
     * 
     * @sarosThread Must be executed in the Saros dispatch thread.
     * 
     *              * @notSWT This method may not be called from SWT, otherwise
     *              a deadlock might occur!!
     */
    public TransformationResult transformIncoming(
        List<IActivity> activityDataObjects) {

        assert sarosSession.isHost() : "CDS.transformIncoming may not be called on the Client!!";

        assert !Utils.isSWT() : "CDS.transformIncoming may not be called from SWT!!";

        TransformationResult result = new TransformationResult(
            sarosSession.getLocalUser());

        for (IActivity activityDataObject : activityDataObjects) {
            try {
                activityDataObject.dispatch(hostReceiver);

                if (activityDataObject instanceof JupiterActivity) {
                    result
                        .addAll(receive((JupiterActivity) activityDataObject));
                } else if (activityDataObject instanceof ChecksumActivity) {
                    result
                        .addAll(withTimestamp((ChecksumActivity) activityDataObject));
                } else {
                    result.executeLocally.add(activityDataObject);
                }
            } catch (Exception e) {
                log.error("Error while receiving activityDataObject: "
                    + activityDataObject, e);
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
                log.error("Unknown buddy in transformation result: "
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
                log.error("Unknown buddy in transformation result: "
                    + Utils.prefix(jid));
                continue;
            }

            ChecksumActivity transformed = entry.getValue();

            result.add(new QueueItem(to, transformed));
        }
        return result;
    }
}
