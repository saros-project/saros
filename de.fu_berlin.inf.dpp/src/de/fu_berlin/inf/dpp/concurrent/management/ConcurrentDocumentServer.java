package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer.QueueItem;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * The ConcurrentDocumentServer is responsible for coordinating all
 * JupiterActivities.
 * 
 * All drivers (also the host!) will send their JupiterActivities to the
 * ConcurrentDocumentServer on the host, which transforms them (using Jupiter)
 * and then sends them to everybody else.
 * 
 * A ConcurrentDocumentServer exists only on the host!
 */
public class ConcurrentDocumentServer implements Disposable {

    private static Logger log = Logger
        .getLogger(ConcurrentDocumentServer.class);

    protected final ISharedProject sharedProject;

    protected JupiterServer server;

    /**
     * Cached since it never changes
     */
    protected final User host;

    protected final ISharedProjectListener projectListener;

    public ConcurrentDocumentServer(ISharedProject sharedProject) {

        if (!sharedProject.isHost())
            throw new IllegalStateException();

        this.sharedProject = sharedProject;
        this.host = sharedProject.getHost();

        this.server = new JupiterServer(sharedProject);
        this.projectListener = new HostSideProjectListener();
        this.sharedProject.addListener(projectListener);
    }

    public void dispose() {
        sharedProject.removeListener(projectListener);
    }

    /**
     * ISharedProjectListener for updating Jupiter documents on the host.
     * 
     * @host
     */
    public class HostSideProjectListener extends AbstractSharedProjectListener {

        @Override
        public void roleChanged(User user) {

            if (user.isHost())
                return;

            if (user.isObserver()) {
                // if driver changed to observer
                server.removeUser(user);
            } else {
                // if user became a driver
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
        public boolean receive(FileActivity fileActivity) {
            if (fileActivity.getType() == FileActivity.Type.Removed) {
                server.removePath(fileActivity.getPath());
            }
            return false;
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
     *              * @notSWT This method may not be called from SWT, otherwise
     *              a deadlock might occur!!
     */
    public TransformationResult transformIncoming(List<IActivity> activities) {

        assert sharedProject.isHost() : "CDS.transformIncoming may not be called on the Client!!";

        assert !Util.isSWT() : "CDS.transformIncoming may not be called from SWT!!";

        TransformationResult result = new TransformationResult(sharedProject
            .getLocalUser());

        for (IActivity activity : activities) {
            try {
                activity.dispatch(hostReceiver);

                if (activity instanceof JupiterActivity) {
                    result.addAll(receive((JupiterActivity) activity));
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

            User to = sharedProject.getUser(entry.getKey());

            if (to == null) {
                log.error("Unknown user in transformation result: " + to);
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
     * {@link ConcurrentDocumentClient#reset(IPath)} should be executed at the
     * same time on the side of the given user.
     * 
     * @host
     */
    public synchronized void reset(JID jid, IPath path) {

        assert sharedProject.isHost();

        log.debug("Resetting jupiter server for [" + jid.getBase() + "]: "
            + path.toOSString());
        this.server.reset(path, jid);
    }
}
