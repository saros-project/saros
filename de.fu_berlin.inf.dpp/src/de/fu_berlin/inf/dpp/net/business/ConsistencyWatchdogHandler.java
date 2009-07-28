package de.fu_berlin.inf.dpp.net.business;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumErrorExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.SharedProjectObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.util.Pair;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This component is responsible for handling Consistency Errors and Checksums.
 * 
 * TODO Cancellation not handeled
 */
@Component(module = "consistency")
public class ConsistencyWatchdogHandler {

    private static Logger log = Logger
        .getLogger(ConsistencyWatchdogHandler.class.getName());

    protected HashMap<Pair<String, JID>, ProgressMonitorDialog> actualChecksumErrorDialogs = new HashMap<Pair<String, JID>, ProgressMonitorDialog>();

    @Inject
    protected ITransmitter transmitter;

    @Inject
    protected SharedProjectObservable project;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected ConsistencyWatchdogClient watchdogClient;

    @Inject
    protected StopManager stopManager;

    protected SessionIDObservable sessionIDObservable;

    protected SessionManager sessionManager;

    protected Handler handler;

    public ConsistencyWatchdogHandler(SessionManager sessionManager,
        XMPPChatReceiver receiver, SessionIDObservable sessionID) {

        this.sessionIDObservable = sessionID;
        this.sessionManager = sessionManager;

        this.handler = new Handler(sessionID);

        receiver.addPacketListener(listener, new AndFilter(
            new MessageTypeFilter(Message.Type.chat), PacketExtensionUtils
                .getInSessionFilter(sessionManager), Util.orFilter(handler
                .getFilter(), ActivitiesPacketExtension.getFilter())));
    }

    protected class Handler extends ChecksumErrorExtension {

        public Handler(SessionIDObservable sessionID) {
            super(sessionID);
        }

        @Override
        public PacketFilter getFilter() {
            return new AndFilter(super.getFilter(), PacketExtensionUtils
                .getInSessionFilter(sessionManager));
        }

        @Override
        public void checksumErrorReceived(final JID from,
            final Set<IPath> paths, boolean resolved) {

            // Concatenate paths
            final String pathsOfInconsistencies = Util.toOSString(paths);

            if (resolved) {
                log.info("Inconsistency resolved for [" + from + "]");
                closeChecksumErrorMessage(pathsOfInconsistencies, from);
                return;
            }

            log.debug("Received Checksum Error from [" + from + "] for "
                + pathsOfInconsistencies);

            showChecksumErrorMessage(pathsOfInconsistencies, from, paths);
        }
    }

    protected boolean inProgress(String pathsOfInconsistencies, JID from) {
        return actualChecksumErrorDialogs.containsKey(new Pair<String, JID>(
            pathsOfInconsistencies, from));
    }

    /**
     * This method creates and opens an error message which informs the user
     * that inconsistencies are handled and he should wait until the
     * inconsistencies are resolved. The Message are saved in a HashMap with a
     * pair of JID of the user and a string representation of the paths of the
     * handled files as key. You can use <code>closeChecksumErrorMessage</code>
     * with the same arguments to close this message again.
     * 
     * @see #closeChecksumErrorMessage(String, JID)
     * 
     */
    protected void showChecksumErrorMessage(
        final String pathsOfInconsistencies, final JID from,
        final Set<IPath> paths) {

        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                createRecoveryProgressDialog(pathsOfInconsistencies, from);
            }
        });

        // Run async, so we can continue to receive messages over the network...
        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                runRecoveryInProgressDialog(pathsOfInconsistencies, from, paths);
            }
        });
    }

    protected void createRecoveryProgressDialog(
        final String pathsOfInconsistencies, final JID from) {

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(EditorAPI
            .getAWorkbenchWindow().getShell()) {
            @Override
            protected Image getImage() {
                return getWarningImage();
            }
        };

        //
        // "Inconsitent file state has detected. File "
        // + pathes
        // + " from user "
        // + from.getBase()
        // +
        // " has to be synchronized with project host. Please wait until the inconsistencies are resolved."
        actualChecksumErrorDialogs.put(new Pair<String, JID>(
            pathsOfInconsistencies, from), dialog);
    }

    protected void runRecoveryInProgressDialog(
        final String pathsOfInconsistencies, final JID from,
        final Set<IPath> paths) {

        ProgressMonitorDialog md = actualChecksumErrorDialogs
            .get(new Pair<String, JID>(pathsOfInconsistencies, from));

        if (md == null)
            return;

        try {
            md.run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) {

                    SubMonitor progress = SubMonitor.convert(monitor);

                    runRecovery(pathsOfInconsistencies, from, paths, progress);
                }
            });
        } catch (InvocationTargetException e) {
            log.error("Internal Error: ", e);
        } catch (InterruptedException e) {
            log.error("Code not designed to be interruptable", e);
        }
    }

    protected void runRecovery(final String pathsOfInconsistencies,
        final JID from, final Set<IPath> paths, SubMonitor progress) {
        SubMonitor waiting;

        List<StartHandle> startHandles = null;

        if (sessionManager.getSharedProject().isHost()) {
            progress.beginTask("Performing recovery", 1000);
            startHandles = stopManager.stop(sessionManager.getSharedProject()
                .getParticipants(), "Consistency recovery", progress);
            progress.subTask("Send files to clients");
            performConsistencyRecovery(from, paths, progress.newChild(700));

            progress.subTask("Wait for peers");
            waiting = progress.newChild(300);
        } else {
            waiting = progress;
        }
        waiting.beginTask("Waiting for peers", 500);

        if (startHandles != null) {
            /*
             * We have to start the StartHandle of the inconsistent user first
             * (blocking!) because otherwise the other participants can be
             * started before the inconsistent user completely processed the
             * consistency recovery.
             */
            // find the StartHandle of the inconsistent user
            StartHandle inconsistentStartHandle = null;
            for (StartHandle startHandle : startHandles)
                if (from.equals(startHandle.getUser().getJID())) {
                    inconsistentStartHandle = startHandle;
                    break;
                }
            if (inconsistentStartHandle == null)
                log
                    .error("Could not find the StartHandle of the inconsistent user");

            // starting all StartHandles
            startHandles.remove(inconsistentStartHandle);
            inconsistentStartHandle.startAndAwait(progress);
            for (StartHandle startHandle : startHandles)
                startHandle.start();

            closeChecksumErrorMessage(pathsOfInconsistencies, from);
        }
    }

    /**
     * @host This is only called on the host
     * 
     * @nonSWT This method should not be called from the SWT Thread!
     */
    protected void performConsistencyRecovery(JID from, Set<IPath> paths,
        SubMonitor progress) {

        progress.beginTask("Performing Consistency Recovery", paths.size());
        progress.subTask("Sending files");

        recoverFiles(from, paths, progress.newChild(paths.size()));

        progress.done();
    }

    protected void recoverFiles(JID from, Set<IPath> paths, SubMonitor progress) {

        ISharedProject project = sessionManager.getSharedProject();
        JID myJID = project.getLocalUser().getJID();

        progress.beginTask("Sending files", paths.size());

        for (IPath path : paths) {
            progress.subTask("Recovering file: " + path.lastSegment());
            recoverFile(from, project, myJID, path, progress.newChild(1));
        }
        progress.done();
    }

    protected void recoverFile(JID from, ISharedProject project, JID myJID,
        final IPath path, SubMonitor progress) {

        User fromUser = project.getUser(from);

        progress.beginTask("Handling file: " + path.toOSString(), 10);

        IFile file = project.getProject().getFile(path);

        // Save document before sending to clients
        if (file.exists())
            try {
                editorManager.saveLazy(path);
            } catch (FileNotFoundException e) {
                log.error("File could not be found, despite existing: ", e);
            }
        progress.worked(1);

        // Reset jupiter
        ConcurrentDocumentManager concurrentManager = project
            .getConcurrentDocumentManager();
        if (concurrentManager.isManagedByJupiterServer(from, path))
            concurrentManager.resetJupiterServer(from, path);

        progress.worked(1);

        if (file.exists()) {
            // Send the file to client
            sendFile(from, project, path, progress.newChild(8));
        } else {
            // TODO Warn the user...
            // Tell the client to delete the file
            project.getSequencer().sendActivity(
                fromUser,
                new FileActivity(myJID.toString(), FileActivity.Type.Removed,
                    path));
            progress.worked(8);
        }
        progress.done();
    }

    protected void sendFile(JID from, ISharedProject project, final IPath path,
        SubMonitor progress) {
        try {
            transmitter.sendFile(from, project.getProject(), path,
                TimedActivity.NO_SEQUENCE_NR, progress);
        } catch (IOException e) {
            // TODO This means we were really unable to send
            // this file. No more falling back.
            log.error("Could not sent file for consistency resolution", e);
        }
    }

    /**
     * Closes the ChecksumError message identified by the JID from the user who
     * had the inconsistencies and a string representation of the handled files.
     * The string representation must be the same which are used to show the
     * message with <code>showChecksumErrorMessage</code>.
     * 
     * @see #showChecksumErrorMessage(String, JID, Set)
     * 
     * @param from
     *            JID of user who had the inconsistencies
     * @param paths
     *            a string representation of the paths of handled files
     * 
     */
    protected void closeChecksumErrorMessage(final String paths, final JID from) {
        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                actualChecksumErrorDialogs.remove(new Pair<String, JID>(paths,
                    from));
            }
        });
    }

    PacketListener listener = new PacketListener() {

        public void processPacket(Packet packet) {
            handler.processPacket(packet);
        }
    };

}
