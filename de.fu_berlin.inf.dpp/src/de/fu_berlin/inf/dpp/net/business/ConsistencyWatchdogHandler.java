package de.fu_berlin.inf.dpp.net.business;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CancellationException;

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
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.ChecksumErrorActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This component is responsible for handling Consistency Errors on the host
 */
@Component(module = "consistency")
public class ConsistencyWatchdogHandler {

    private static Logger log = Logger
        .getLogger(ConsistencyWatchdogHandler.class);

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected ConsistencyWatchdogClient watchdogClient;

    @Inject
    protected StopManager stopManager;

    protected SarosSessionManager sessionManager;

    protected ISarosSession sarosSession;

    protected IActivityReceiver activityReceiver = new AbstractActivityReceiver() {
        @Override
        public void receive(ChecksumErrorActivity checksumError) {
            startRecovery(checksumError);
        }
    };

    protected IActivityProvider activityProvider = new AbstractActivityProvider() {
        @Override
        public void exec(IActivity activity) {
            activity.dispatch(activityReceiver);
        }
    };

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSharedProject) {
            sarosSession = newSharedProject;
            if (sarosSession.isHost())
                newSharedProject.addActivityProvider(activityProvider);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            if (sarosSession.isHost())
                oldSarosSession.removeActivityProvider(activityProvider);
            sarosSession = null;
        }
    };

    public ConsistencyWatchdogHandler(SarosSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.sessionManager.addSarosSessionListener(sessionListener);
    }

    /**
     * This method creates and opens an error message which informs the user
     * that inconsistencies are handled and he should wait until the
     * inconsistencies are resolved. The Message are saved in a HashMap with a
     * pair of JID of the user and a string representation of the paths of the
     * handled files as key. You can use <code>closeChecksumErrorMessage</code>
     * with the same arguments to close this message again.
     * 
     */
    protected void startRecovery(final ChecksumErrorActivity checksumError) {

        log.debug("Received Checksum Error: " + checksumError);

        Utils.runSafeSWTSync(log, new Runnable() {
            public void run() {

                final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                    EditorAPI.getAWorkbenchWindow().getShell()) {
                    @Override
                    protected Image getImage() {
                        return getWarningImage();
                    }

                    // TODO add some text
                    // "Inconsitent file state has detected. File "
                    // + pathes
                    // + " from buddy "
                    // + from.getBase()
                    // +
                    // " has to be synchronized with project host. Please wait until the inconsistencies are resolved."
                };

                // Run async, so we can continue to receive messages over the
                // network...
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        try {
                            dialog.run(true, true, new IRunnableWithProgress() {
                                public void run(IProgressMonitor monitor) {
                                    runRecovery(checksumError,
                                        SubMonitor.convert(monitor));
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
                            log.error("Code not designed to be interruptable",
                                e);
                        }
                    }
                });
            }
        });
    }

    protected void runRecovery(ChecksumErrorActivity checksumError,
        SubMonitor progress) throws CancellationException {

        List<StartHandle> startHandles = null;

        progress.beginTask("Performing recovery", 1200);
        try {

            startHandles = stopManager.stop(sarosSession.getParticipants(),
                "Consistency recovery", progress.newChild(200));

            progress.subTask("Sending files to client...");
            recoverFiles(checksumError, progress.newChild(700));

            /*
             * We have to start the StartHandle of the inconsistent user first
             * (blocking!) because otherwise the other participants can be
             * started before the inconsistent user completely processed the
             * consistency recovery.
             */
            progress.subTask("Wait for peers...");

            // find the StartHandle of the inconsistent user
            StartHandle inconsistentStartHandle = null;
            for (StartHandle startHandle : startHandles) {
                if (checksumError.getSource().equals(startHandle.getUser())) {
                    inconsistentStartHandle = startHandle;
                    break;
                }
            }
            if (inconsistentStartHandle == null) {
                log.error("Could not find the StartHandle"
                    + " of the inconsistent buddy");
            } else {
                inconsistentStartHandle.startAndAwait(progress.newChild(200));
                startHandles.remove(inconsistentStartHandle);
            }
        } finally {
            if (startHandles != null)
                for (StartHandle startHandle : startHandles)
                    startHandle.start();
            progress.done();
        }
    }

    /**
     * @host This is only called on the host
     * 
     * @nonSWT This method should not be called from the SWT Thread!
     */
    protected void recoverFiles(ChecksumErrorActivity checksumError,
        SubMonitor progress) {

        ISarosSession sarosSession = this.sarosSession;

        progress
            .beginTask("Sending files", checksumError.getPaths().size() + 1);

        try {
            for (SPath path : checksumError.getPaths()) {
                progress.subTask("Recovering file: "
                    + path.getProjectRelativePath().lastSegment());
                recoverFile(checksumError.getSource(), sarosSession, path,
                    progress.newChild(1));
            }

            // Tell the user that we sent all files
            sarosSession.sendActivity(checksumError.getSource(),
                new ChecksumErrorActivity(sarosSession.getLocalUser(), null,
                    checksumError.getRecoveryID()));
        } finally {
            progress.done();
        }
    }

    /**
     * Recover a single file for the given user (that is either send the file or
     * tell the user to remove it).
     */
    protected void recoverFile(User from, final ISarosSession sarosSession,
        final SPath path, SubMonitor progress) {

        progress.beginTask("Handling file: " + path.toString(), 10);

        IFile file = path.getFile();

        // Save document before sending to client
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
        sarosSession.getConcurrentDocumentServer().reset(from.getJID(), path);

        progress.worked(1);
        final User user = sarosSession.getLocalUser();

        if (file.exists()) {

            try {
                // Send the file to client
                sarosSession.sendActivity(from,
                    FileActivity.created(user, path, Purpose.RECOVERY));

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
                    Utils.runSafeSWTSync(log, new Runnable() {
                        public void run() {
                            sarosSession.activityCreated(new ChecksumActivity(
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
            sarosSession.sendActivity(from,
                FileActivity.removed(user, path, Purpose.RECOVERY));
            Utils.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    sarosSession.activityCreated(ChecksumActivity.missing(user,
                        path));
                }
            });

            progress.worked(8);
        }
        progress.done();
    }

}
