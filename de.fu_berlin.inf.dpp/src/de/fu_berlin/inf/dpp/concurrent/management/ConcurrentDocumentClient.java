package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer.QueueItem;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * ConcurrentDocumentClient is responsible for managing the Jupiter interaction
 * on the local side of drivers.
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

    protected final ISharedProject sharedProject;

    protected final JupiterClient jupiterClient;

    protected final User host; // Cached since it never changes

    protected final ISharedProjectListener projectListener;

    public ConcurrentDocumentClient(ISharedProject sharedProject) {

        this.sharedProject = sharedProject;
        this.host = sharedProject.getHost();
        this.jupiterClient = new JupiterClient(sharedProject);
        this.projectListener = new ClientSideProjectListener();
        sharedProject.addListener(projectListener);
    }

    public void dispose() {
        sharedProject.removeListener(projectListener);
    }

    /**
     * ISharedProjectListener which is used to reset Jupiter on the client side,
     * when the user is no longer a driver.
     */
    public class ClientSideProjectListener extends
        AbstractSharedProjectListener {

        @Override
        public void roleChanged(User user) {

            // Host always keeps his client docs
            if (sharedProject.isHost())
                return;

            // Clear clientdocs
            if (user.isLocal()) {
                jupiterClient.reset();
            }
        }
    }

    /**
     * This is called from the shared project when a local activity has been
     * caused by user activity (for instance the user pressed key 'a')
     * 
     * This method transforms the activity into a list of events to send to
     * individual users.
     * 
     * @swt Must be called on the SWT Thread to ensure proper synchronization
     * 
     * @host and @client This is called whenever activities are created LOCALLY
     *       both on the client and on the host
     */
    public List<QueueItem> transformOutgoing(IActivity activity) {

        assert Util.isSWT() : "CDC.transformOutgoing must be called on the SWT Thread";

        List<QueueItem> result = new ArrayList<QueueItem>();

        if (activity instanceof TextEditActivity) {
            TextEditActivity textEdit = (TextEditActivity) activity;

            result.add(new QueueItem(host, jupiterClient.generate(textEdit)));

            /*
             * This activity still needs to be sent to all observers, because
             * they are not notified by receiveJupiterActivityHostSide(...).
             */
            if (sharedProject.isHost()) {
                result.add(new QueueItem(sharedProject.getRemoteObservers(),
                    activity));
            }
        } else if (activity instanceof ChecksumActivity) {
            ChecksumActivity checksumActivity = (ChecksumActivity) activity;

            /**
             * Only the host can generate Checksums
             */
            assert sharedProject.isHost();

            // Send Jupiter specific checksum to ConcurrentDocumentServer
            result.add(new QueueItem(host, jupiterClient
                .withTimestamp(checksumActivity)));

            // Send general checksum to all observers
            result.add(new QueueItem(sharedProject.getRemoteObservers(),
                checksumActivity));

        } else {
            result.add(new QueueItem(sharedProject.getRemoteUsers(), activity));
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

        assert Util.isSWT() : "CDC.transformIncoming must be called on the SWT Thread";

        TransformationResult result = new TransformationResult(sharedProject
            .getLocalUser());

        for (IActivity activity : activities) {
            try {
                activity.dispatch(clientReceiver);

                if (activity instanceof JupiterActivity) {
                    result.addAll(receiveActivity((JupiterActivity) activity));
                } else if (activity instanceof ChecksumActivity
                    && sharedProject.isDriver()) {
                    result.addAll(receiveChecksum((ChecksumActivity) activity));
                } else {
                    result.executeLocally.add(activity);
                }
            } catch (Exception e) {
                log.error("Error while receiving activity: " + activity, e);
            }
        }
        return result;
    }

    /**
     * Will receive an incoming ChecksumActivity and discard it if it is not
     * valid within the current local Jupiter timestamp
     */
    protected TransformationResult receiveChecksum(ChecksumActivity activity) {

        TransformationResult result = new TransformationResult(sharedProject
            .getLocalUser());

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

        TransformationResult result = new TransformationResult(sharedProject
            .getLocalUser());

        Operation op;
        try {
            op = jupiterClient.receive(jupiterActivity);
        } catch (TransformationException e) {
            log.error("Error during transformation of: " + jupiterActivity, e);
            // TODO this should trigger a consistency check
            return result;
        }

        // Transform to TextEdit so it can be executed locally
        for (TextEditActivity textEdit : op.toTextEdit(jupiterActivity
            .getEditorPath(), jupiterActivity.getSource())) {

            result.executeLocally.add(textEdit);
        }

        // Send text edits to all observers
        if (sharedProject.isHost()) {
            List<User> observers = sharedProject.getObservers();
            observers.remove(host);
            for (IActivity activity : result.executeLocally) {
                result.add(new QueueItem(observers, activity));
            }
        }
        return result;
    }

    /**
     * Resets the JupiterClient for the given path.
     * 
     * When this is called on the client (or on the host for one of his
     * JupiterClient), a call to
     * {@link ConcurrentDocumentServer#reset(de.fu_berlin.inf.dpp.net.JID, IPath)}
     * should be executed at the same time on the side of the given user.
     * 
     * @client and @host This can be called on the host as well, if the host
     *         wants to reset his client document (which at the moment never
     *         happens, because the version of the host is the authoritative one
     *         and thus does not need to be reset).
     */
    public synchronized void reset(IPath path) {
        log.debug("Resetting jupiter client: " + path.toOSString());
        jupiterClient.reset(path);
    }

    public boolean isCurrent(ChecksumActivity checksumActivity) {
        try {
            return jupiterClient.isCurrent(checksumActivity);
        } catch (TransformationException e) {
            log.error("Error during transformation of: " + checksumActivity, e);
            // TODO this should trigger a consistency recovery. Difficult :-(
            return false;
        }
    }
}
