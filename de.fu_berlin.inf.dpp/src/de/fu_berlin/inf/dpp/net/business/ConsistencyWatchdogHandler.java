package de.fu_berlin.inf.dpp.net.business;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumErrorDataObject;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumErrorDataObject.ChecksumErrorExtensionProvider;
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

    public ConsistencyWatchdogHandler(final SessionManager sessionManager,
        final ChecksumErrorExtensionProvider extensionProvider,
        XMPPReceiver receiver, SessionIDObservable sessionID) {

        this.sessionIDObservable = sessionID;
        this.sessionManager = sessionManager;

        receiver.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                try {
                    ChecksumErrorDataObject payload = extensionProvider
                        .getPayload(packet);
                    if (payload == null) {
                        log.warn("Invalid ChecksumErrorExtensionPacket"
                            + " does not contain a payload: " + packet);
                        return;
                    }
                    JID from = new JID(packet.getFrom());

                    if (!ObjectUtils.equals(sessionIDObservable.getValue(),
                        payload.getSessionID())) {
                        log.warn("Rcvd ("
                            + String.format("%03d", payload.getPaths().size())
                            + ") " + Util.prefix(from)
                            + "from an old/unknown session: "
                            + payload.getPaths());
                        return;
                    }

                    Set<SPath> paths = new HashSet<SPath>();
                    ISharedProject project = sessionManager.getSharedProject();
                    for (SPathDataObject dataObject : payload.getPaths()) {
                        paths.add(dataObject.toSPath(project));
                    }

                    String checksumErrors = Util.toOSString(paths);

                    if (payload.isResolved()) {
                        log.info("Inconsistency resolved for "
                            + Util.prefix(from) + "for " + checksumErrors);
                        closeChecksumErrorMessage(checksumErrors, from);
                        return;
                    }

                    log.debug("Received Checksum Error from "
                        + Util.prefix(from) + "for " + checksumErrors);

                    startRecovery(checksumErrors, from, paths);
                } catch (Exception e) {
                    log.error(
                        "An internal error occurred while processing packets",
                        e);
                }
            }
        }, extensionProvider.getPacketFilter());
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
    protected void startRecovery(final String pathsOfInconsistencies,
        final JID from, final Set<SPath> paths) {

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
        final Set<SPath> paths) {

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
            try {
                throw e.getCause();
            } catch (CancellationException c) {
                log.info("Recovery was cancelled by local user");
            } catch (Throwable t) {
                log.error("Internal Error: ", t);
            }
        } catch (InterruptedException e) {
            log.error("Code not designed to be interruptable", e);
        }
    }

    protected void runRecovery(final String pathsOfInconsistencies,
        final JID from, final Set<SPath> paths, SubMonitor progress)
        throws CancellationException {
        SubMonitor waiting;

        List<StartHandle> startHandles = null;

        try {
            if (sessionManager.getSharedProject().isHost()) {
                progress.beginTask("Performing recovery", 1200);
                startHandles = stopManager.stop(sessionManager
                    .getSharedProject().getParticipants(),
                    "Consistency recovery", progress.newChild(200));
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
                 * We have to start the StartHandle of the inconsistent user
                 * first (blocking!) because otherwise the other participants
                 * can be started before the inconsistent user completely
                 * processed the consistency recovery.
                 */
                // find the StartHandle of the inconsistent user
                StartHandle inconsistentStartHandle = null;
                for (StartHandle startHandle : startHandles) {
                    if (from.equals(startHandle.getUser().getJID())) {
                        inconsistentStartHandle = startHandle;
                        break;
                    }
                }
                if (inconsistentStartHandle == null)
                    log.error("Could not find the StartHandle"
                        + " of the inconsistent user");
                else {
                    inconsistentStartHandle.startAndAwait(progress);
                    startHandles.remove(inconsistentStartHandle);
                }
            }

        } finally {
            if (startHandles != null)
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
    protected void performConsistencyRecovery(JID from, Set<SPath> paths,
        SubMonitor progress) {

        progress.beginTask("Performing Consistency Recovery", paths.size());
        progress.subTask("Sending files");

        recoverFiles(from, paths, progress.newChild(paths.size()));

        progress.done();
    }

    protected void recoverFiles(JID from, Set<SPath> paths, SubMonitor progress) {

        ISharedProject sharedProject = sessionManager.getSharedProject();
        progress.beginTask("Sending files", paths.size());

        for (SPath path : paths) {
            progress.subTask("Recovering file: "
                + path.getProjectRelativePath().lastSegment());
            recoverFile(from, sharedProject, path, progress.newChild(1));
        }
        progress.done();
    }

    protected void recoverFile(JID from, final ISharedProject sharedProject,
        final SPath path, SubMonitor progress) {

        User fromUser = sharedProject.getUser(from);

        progress.beginTask("Handling file: " + path.toString(), 10);

        IFile file = path.getFile();

        // Save document before sending to clients
        if (file.exists()) {
            try {
                editorManager.saveLazy(path);
            } catch (FileNotFoundException e) {
                log.error("File could not be found, despite existing: " + path,
                    e);
            }
        }
        progress.worked(1);

        // Reset jupiter
        sharedProject.getConcurrentDocumentServer().reset(from, path);

        progress.worked(1);
        final User user = sharedProject.getLocalUser();

        if (file.exists()) {

            try {
                // Send the file to client
                sharedProject.sendActivity(fromUser, FileActivity.created(user,
                    path, Purpose.RECOVERY));

                // Immediately follow up with a new checksum
                IDocument doc;
                FileEditorInput input = new FileEditorInput(file);
                IDocumentProvider provider = EditorManager
                    .getDocumentProvider(input);
                try {
                    provider.connect(input);
                    doc = provider.getDocument(input);

                    final DocumentChecksum checksum = new DocumentChecksum(path);
                    checksum.bind(doc);
                    checksum.update();
                    Util.runSafeSWTSync(log, new Runnable() {
                        public void run() {
                            sharedProject.activityCreated(new ChecksumActivity(
                                user, path, checksum.getHash(), checksum
                                    .getLength()));
                        }
                    });
                } catch (CoreException e) {
                    log.warn("Could not check checksum of file "
                        + path.toString());
                } finally {
                    provider.disconnect(input);
                }

            } catch (IOException e) {
                log.error("File could not be read, despite existing: " + path,
                    e);
            }
        } else {
            // TODO Warn the user...
            // Tell the client to delete the file
            sharedProject.sendActivity(fromUser, FileActivity.removed(user,
                path, Purpose.RECOVERY));
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    sharedProject.activityCreated(ChecksumActivity.missing(
                        user, path));
                }
            });

            progress.worked(8);
        }
        progress.done();
    }

    /**
     * Closes the ChecksumError message identified by the JID from the user who
     * had the inconsistencies and a string representation of the handled files.
     * The string representation must be the same which are used to show the
     * message with <code>showChecksumErrorMessage</code>.
     * 
     * @see #startRecovery(String, JID, Set)
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

    /**
     * Runs a consistency recovery for the given SPath and the given JID
     * 
     * @param inconsistentJID
     *            JID of the user having inconsistencies
     * @param path
     *            of the inconsistent resource
     * 
     * @host
     */
    public void runRecoveryForPath(final JID inconsistentJID, final SPath path) {

        Set<SPath> paths = Collections.singleton(path);
        final String pathsOfInconsistencies = Util.toOSString(paths);

        startRecovery(pathsOfInconsistencies, inconsistentJID, paths);
    }
}
