package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.internal.ActivityHandler.QueueItem;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

/**
 * ConcurrentDocumentClient is responsible for managing the Jupiter interaction
 * on the local side of the clients.
 * 
 * A client exists for every participant (also the host!) to take local
 * TextEdits and transforms them into JupiterActivities to send to the Server on
 * the host-side.
 * 
 * When JupiterActivities are received from the server they are transformed by
 * the ConcurrentDocumentClient to TextEditActivities which can then be executed
 * locally.
 */
public class ConcurrentDocumentClient {

    private static Logger log = Logger
        .getLogger(ConcurrentDocumentClient.class);

    protected final ISarosSession sarosSession;

    protected final JupiterClient jupiterClient;

    protected final User host; // Cached since it never changes

    public ConcurrentDocumentClient(ISarosSession sarosSession) {

        this.sarosSession = sarosSession;
        this.host = sarosSession.getHost();
        this.jupiterClient = new JupiterClient(sarosSession);
    }

    /**
     * This is called when an activity has been caused by the local user
     * 
     * (Activity is for instance: the user pressed key 'a')
     * 
     * This method transforms the activity into a list of events to send to the
     * server on the host-side.
     * 
     * @swt Must be called on the SWT Thread to ensure proper synchronization
     * 
     * @host and @client This is called whenever activities are created locally
     *       both on the client and on the host
     */
    public List<QueueItem> transformOutgoing(IActivity activity) {

        assert SWTUtils.isSWT() : "CDC.transformOutgoing must be called on the SWT Thread";

        List<QueueItem> result = new ArrayList<QueueItem>();

        if (activity instanceof TextEditActivity) {
            TextEditActivity textEdit = (TextEditActivity) activity;
            result.add(new QueueItem(host, jupiterClient.generate(textEdit)));

        } else if (activity instanceof ChecksumActivity) {
            ChecksumActivity checksumActivityDataObject = (ChecksumActivity) activity;

            /**
             * Only the host can generate Checksums
             */
            assert sarosSession.isHost();

            // Send Jupiter specific checksum to ConcurrentDocumentServer
            result.add(new QueueItem(host, jupiterClient
                .withTimestamp(checksumActivityDataObject)));

        } else {
            /**
             * Send all other activities to the server on the host-side. The
             * server is responsible for distributing them to all receivers
             */
            result.add(new QueueItem(host, activity));
        }
        return result;
    }

    /**
     * This method is called when activities received over the network should be
     * executed locally.
     * 
     * This method will transform them and return a set of results which can be
     * executed locally and also QueueItems which must be sent to other users
     * (which happens mainly on the host).
     * 
     * @swt Must be called on the SWT Thread to ensure proper synchronization
     * 
     * @host and @client This is called whenever activities are received from
     *       REMOTELY both on the client and on the host
     */
    public TransformationResult transformIncoming(List<IActivity> activities) {

        assert SWTUtils.isSWT() : "CDC.transformIncoming must be called on the SWT Thread";

        TransformationResult result = new TransformationResult(
            sarosSession.getLocalUser());

        for (IActivity activity : activities) {
            try {
                activity.dispatch(clientReceiver);

                if (activity instanceof JupiterActivity) {
                    result.addAll(receiveActivity((JupiterActivity) activity));

                } else if (activity instanceof ChecksumActivity) {
                    result.addAll(receiveChecksum((ChecksumActivity) activity));

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
