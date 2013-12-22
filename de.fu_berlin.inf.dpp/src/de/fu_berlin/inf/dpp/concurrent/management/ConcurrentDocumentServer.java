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
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.internal.ActivityHandler.QueueItem;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

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

    private final ISarosSession sarosSession;

    private final JupiterServer server;

    /**
     * ISharedProjectListener for updating Jupiter documents on the host.
     */
    private final ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void userStartedQueuing(User user) {
            server.addUser(user);
        }

        @Override
        public void userLeft(User user) {
            server.removeUser(user);
        }
    };

    public ConcurrentDocumentServer(ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
        this.server = new JupiterServer(sarosSession);
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
     * Dispatched the activity to the internal ActivityReceiver. The
     * ActivityReceiver will remove FileDocuments when the file has been
     * deleted.
     * 
     * @param activity
     *            Activity to be dispatched
     */
    public void checkFileDeleted(IActivity activity) {

        activity.dispatch(hostReceiver);

    }

    private final IActivityReceiver hostReceiver = new AbstractActivityReceiver() {
        @Override
        public void receive(FileActivity fileActivity) {
            if (fileActivity.getType() == FileActivity.Type.REMOVED) {
                server.removePath(fileActivity.getPath());
            }
        }
    };

    /**
     * Transforms the given activities on the server side and returns a list of
     * QueueItems containing the transformed activities and there receivers.
     * 
     * @host
     * 
     * @sarosThread Must be executed in the Saros dispatch thread.
     * 
     * @notSWT This method may not be called from SWT, otherwise a deadlock
     *         might occur!!
     * 
     * @param activity
     *            Activity to be transformed
     * 
     * @return A list of QueueItems containing the activities and receivers
     */
    public List<QueueItem> transformIncoming(IActivity activity) {

        assert sarosSession.isHost() : "CDS.transformIncoming must not be called on the client";

        assert !SWTUtils.isSWT() : "CDS.transformIncoming must not be called from SWT";

        List<QueueItem> result = new ArrayList<QueueItem>();

        try {
            activity.dispatch(hostReceiver);

            if (activity instanceof JupiterActivity) {
                result.addAll(receive((JupiterActivity) activity));

            } else if (activity instanceof ChecksumActivity) {
                result.addAll(withTimestamp((ChecksumActivity) activity));
            }
        } catch (Exception e) {
            log.error("Error while transforming activity: " + activity, e);
        }

        return result;
    }

    /**
     * Does the actual work of transforming a clients JupiterActivity into
     * specific JupiterActivities for every client.
     */
    private List<QueueItem> receive(JupiterActivity jupiterActivity) {

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
                log.error("unknown user in transformation result: " + jid);
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

        log.debug("Resetting jupiter server for " + jid + ": "
            + path.toString());
        this.server.reset(path, jid);
    }

    /**
     * Does the actual work of transforming a ChecksumActivity.
     */
    private List<QueueItem> withTimestamp(ChecksumActivity checksumActivity) {

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
                log.error("unknown user in transformation result: " + jid);
                continue;
            }

            ChecksumActivity transformed = entry.getValue();

            result.add(new QueueItem(to, transformed));
        }
        return result;
    }
}
