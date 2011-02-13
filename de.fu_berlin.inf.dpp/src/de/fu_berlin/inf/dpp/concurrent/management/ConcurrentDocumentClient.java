package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.ArrayList;
import java.util.List;

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
import de.fu_berlin.inf.dpp.activities.business.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.internal.SarosSession.QueueItem;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * ConcurrentDocumentClient is responsible for managing the Jupiter interaction
 * on the local side of users with {@link User.Permission#WRITE_ACCESS}.
 * 
 * A client exists for every participant (also the host!) to take local
 * TextEdits and transforms them into JupiterActivities to send to the host.
 * 
 * When JupiterActivities are received from the host they are transformed in the
 * ConcurrentDocumentClient to TextEditActivities to execute locally again as
 * well.
 */
public class ConcurrentDocumentClient implements Disposable {

    private static Logger log = Logger
        .getLogger(ConcurrentDocumentClient.class);

    protected final ISarosSession sarosSession;

    protected final JupiterClient jupiterClient;

    protected final User host; // Cached since it never changes

    protected final ISharedProjectListener projectListener;

    public ConcurrentDocumentClient(ISarosSession sarosSession) {

        this.sarosSession = sarosSession;
        this.host = sarosSession.getHost();
        this.jupiterClient = new JupiterClient(sarosSession);
        this.projectListener = new ClientSideProjectListener();
        sarosSession.addListener(projectListener);
    }

    public void dispose() {
        sarosSession.removeListener(projectListener);
    }

    /**
     * ISharedProjectListener which is used to reset Jupiter on the client side,
     * when the user has no longer {@link User.Permission#WRITE_ACCESS}.
     */
    public class ClientSideProjectListener extends
        AbstractSharedProjectListener {

        @Override
        public void permissionChanged(User user) {

            // Host always keeps his client docs
            if (sarosSession.isHost())
                return;

            // Clear clientdocs
            if (user.isLocal()) {
                jupiterClient.reset();
            }
        }
    }

    /**
     * This is called from the shared project when a local activityDataObject
     * has been caused by user activityDataObject (for instance the user pressed
     * key 'a')
     * 
     * This method transforms the activityDataObject into a list of events to
     * send to individual users.
     * 
     * @swt Must be called on the SWT Thread to ensure proper synchronization
     * 
     * @host and @client This is called whenever activityDataObjects are created
     *       LOCALLY both on the client and on the host
     */
    public List<QueueItem> transformOutgoing(IActivity activity) {

        assert Utils.isSWT() : "CDC.transformOutgoing must be called on the SWT Thread";

        List<QueueItem> result = new ArrayList<QueueItem>();

        final List<User> remoteUsersWithReadOnlyAccess = sarosSession
            .getRemoteUsersWithReadOnlyAccess();
        final List<User> remoteUsers = sarosSession.getRemoteUsers();
        if (activity instanceof TextEditActivity) {
            TextEditActivity textEdit = (TextEditActivity) activity;

            result.add(new QueueItem(host, jupiterClient.generate(textEdit)));

            /**
             * This activityDataObject still needs to be sent to all users with
             * {@link User.Permission#READONLY_ACCESS}, because they are not
             * notified by receiveJupiterActivityHostSide(...).
             */
            if (sarosSession.isHost()
                && remoteUsersWithReadOnlyAccess.size() > 0) {
                result.add(new QueueItem(remoteUsersWithReadOnlyAccess,
                    activity));
            }
        } else if (activity instanceof ChecksumActivity) {
            ChecksumActivity checksumActivityDataObject = (ChecksumActivity) activity;

            /**
             * Only the host can generate Checksums
             */
            assert sarosSession.isHost();

            // Send Jupiter specific checksum to ConcurrentDocumentServer
            result.add(new QueueItem(host, jupiterClient
                .withTimestamp(checksumActivityDataObject)));

            if (remoteUsersWithReadOnlyAccess.size() > 0)
                /**
                 * Send general checksum to all users with
                 * {@link User.Permission#READONLY_ACCESS}
                 */
                result.add(new QueueItem(remoteUsersWithReadOnlyAccess,
                    checksumActivityDataObject));
        } else {
            if (remoteUsers.size() > 0)
                result.add(new QueueItem(remoteUsers, activity));
        }
        return result;
    }

    /**
     * This method is called when activityDataObjects received over the network
     * should be executed locally.
     * 
     * This method will transform them and return a set of results which can be
     * executed locally and also QueueItems which must be sent to other users
     * (which happens mainly on the host).
     * 
     * @swt Must be called on the SWT Thread to ensure proper synchronization
     * 
     * @host and @client This is called whenever activityDataObjects are
     *       received from REMOTELY both on the client and on the host
     */
    public TransformationResult transformIncoming(
        List<IActivity> activityDataObjects) {

        assert Utils.isSWT() : "CDC.transformIncoming must be called on the SWT Thread";

        TransformationResult result = new TransformationResult(
            sarosSession.getLocalUser());

        for (IActivity activityDataObject : activityDataObjects) {
            try {
                activityDataObject.dispatch(clientReceiver);

                if (activityDataObject instanceof JupiterActivity) {
                    result
                        .addAll(receiveActivity((JupiterActivity) activityDataObject));
                } else if (activityDataObject instanceof ChecksumActivity
                    && sarosSession.hasWriteAccess()) {
                    result
                        .addAll(receiveChecksum((ChecksumActivity) activityDataObject));
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
     * Will receive an incoming ChecksumActivity and discard it if it is not
     * valid within the current local Jupiter timestamp
     */
    protected TransformationResult receiveChecksum(ChecksumActivity activity) {

        TransformationResult result = new TransformationResult(
            sarosSession.getLocalUser());

        try {
            if (jupiterClient.isCurrent(activity))
                result.executeLocally.add(activity);
        } catch (TransformationException e) {
            // TODO this should trigger a consistency check
            log.error("Error during transformation of: " + activity, e);
        }

        return result;
    }

    protected final IActivityReceiver clientReceiver = new AbstractActivityReceiver() {
        @Override
        public void receive(FileActivity fileActivity) {
            if (fileActivity.getType() == FileActivity.Type.Removed) {
                jupiterClient.reset(fileActivity.getPath());
            }
        }
    };

    /**
     * @client and @host
     */
    protected TransformationResult receiveActivity(
        JupiterActivity jupiterActivity) {

        TransformationResult result = new TransformationResult(
            sarosSession.getLocalUser());

        Operation op;
        try {
            op = jupiterClient.receive(jupiterActivity);
        } catch (TransformationException e) {
            log.error("Error during transformation of: " + jupiterActivity, e);
            // TODO this should trigger a consistency check
            return result;
        }

        // Transform to TextEdit so it can be executed locally
        for (TextEditActivity textEdit : op.toTextEdit(
            jupiterActivity.getPath(), jupiterActivity.getSource())) {

            result.executeLocally.add(textEdit);
        }

        /**
         * Send text edits to all users with
         * {@link User.Permission#READONLY_ACCESS}
         */
        if (sarosSession.isHost()) {
            List<User> usersWithReadOnlyAccess = sarosSession.getUsersWithReadOnlyAccess();
            usersWithReadOnlyAccess.remove(host);
            if (!usersWithReadOnlyAccess.isEmpty()) {
                for (IActivity activity : result.executeLocally) {
                    result.add(new QueueItem(usersWithReadOnlyAccess, activity));
                }
            }
        }
        return result;
    }

    /**
     * Resets the JupiterClient for the given path.
     * 
     * When this is called on the client (or on the host for one of his
     * JupiterClient), a call to
     * {@link ConcurrentDocumentServer#reset(de.fu_berlin.inf.dpp.net.JID, SPath)}
     * should be executed at the same time on the side of the given user.
     * 
     * @client and @host This can be called on the host as well, if the host
     *         wants to reset his client document (which at the moment never
     *         happens, because the version of the host is the authoritative one
     *         and thus does not need to be reset).
     */
    public synchronized void reset(SPath path) {
        log.debug("Resetting jupiter client: " + path.toString());
        jupiterClient.reset(path);
    }

    public boolean isCurrent(ChecksumActivity checksumActivityDataObject) {
        try {
            return jupiterClient.isCurrent(checksumActivityDataObject);
        } catch (TransformationException e) {
            log.error("Error during transformation of: "
                + checksumActivityDataObject, e);
            // TODO this should trigger a consistency recovery. Difficult :-(
            return false;
        }
    }
}
