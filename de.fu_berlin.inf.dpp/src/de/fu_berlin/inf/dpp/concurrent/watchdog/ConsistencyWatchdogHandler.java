package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.io.FileNotFoundException;
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
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.ChecksumErrorActivity;
import de.fu_berlin.inf.dpp.activities.RecoveryFileActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.EclipseFileImpl;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.util.FileUtils;

/**
 * This component is responsible for handling Consistency Errors on the host. It
 * both produces and consumes activities.
 */
@Component(module = "consistency")
public class ConsistencyWatchdogHandler extends AbstractActivityProducer
    implements Startable {

    private static Logger log = Logger
        .getLogger(ConsistencyWatchdogHandler.class);

    protected final EditorManager editorManager;

    protected final ConsistencyWatchdogClient watchdogClient;

    protected final ISarosSession sarosSession;

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void receive(ChecksumErrorActivity checksumError) {
            if (sarosSession.isHost())
                startRecovery(checksumError);
        }
    };

    @Override
    public void start() {
        sarosSession.addActivityConsumer(consumer);
        sarosSession.addActivityProducer(this);
    }

    @Override
    public void stop() {
        sarosSession.removeActivityConsumer(consumer);
        sarosSession.removeActivityProducer(this);
    }

    public ConsistencyWatchdogHandler(ISarosSession sarosSession,
        EditorManager editorManager, ConsistencyWatchdogClient watchdogClient) {
        this.sarosSession = sarosSession;
        this.editorManager = editorManager;
        this.watchdogClient = watchdogClient;
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

        // execute async so outstanding activities could be dispatched
        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {

                final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                    SWTUtils.getShell()) {
                    @Override
                    protected Image getImage() {
                        return getWarningImage();
                    }

                    // TODO add some text
                    // "Inconsitent file state has detected. File "
                    // + pathes
                    // + " from user "
                    // + from.getBase()
                    // +
                    // " has to be synchronized with project host. Please wait until the inconsistencies are resolved."
                };

                try {
                    /*
                     * run in a modal context otherwise we would block again the
                     * dispatching of activities
                     */
                    dialog.run(true, true, new IRunnableWithProgress() {
                        @Override
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
                    log.error("Code not designed to be interruptable", e);
                }
            }
        });
    }

    protected void runRecovery(ChecksumErrorActivity checksumError,
        SubMonitor progress) throws CancellationException {

        List<StartHandle> startHandles = null;

        progress.beginTask("Performing recovery", 1200);
        try {

            startHandles = sarosSession.getStopManager().stop(
                sarosSession.getUsers(), "Consistency recovery");

            progress.subTask("Sending files to client...");
            recoverFiles(checksumError, progress.newChild(900));

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
                log.error("could not find start handle"
                    + " of the inconsistent user");
            } else {
                // FIXME evaluate the return value
                inconsistentStartHandle.startAndAwait();
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

        progress
            .beginTask("Sending files", checksumError.getPaths().size() + 1);

        try {
            for (SPath path : checksumError.getPaths()) {
                progress.subTask("Recovering file: "
                    + path.getProjectRelativePath());
                recoverFile(checksumError.getSource(), sarosSession, path);

                progress.worked(1);
            }

            // Tell the user that we sent all files
            fireActivity(new ChecksumErrorActivity(sarosSession.getLocalUser(),
                checksumError.getSource(), null, checksumError.getRecoveryID()));
        } finally {
            progress.done();
        }
    }

    /**
     * Recover a single file for the given user (that is either send the file or
     * tell the user to remove it).
     */
    protected void recoverFile(final User from,
        final ISarosSession sarosSession, final SPath path) {

        IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();

        // Save document before sending to client
        if (file.exists()) {
            try {
                editorManager.saveLazy(path);
            } catch (FileNotFoundException e) {
                log.error("File could not be found, despite existing: " + path,
                    e);
            }
        }

        // Reset jupiter
        sarosSession.getConcurrentDocumentServer().reset(from, path);

        final User user = sarosSession.getLocalUser();

        if (!file.exists()) {
            // TODO Warn the user...
            // Tell the client to delete the file
            fireActivity(RecoveryFileActivity.removed(user, path, from, null));
            fireActivity(ChecksumActivity.missing(user, path));
            return;
        }

        String charset = null;

        try {
            charset = file.getCharset();
        } catch (CoreException e) {
            log.error("could not determine encoding for file: " + file, e);
        }

        byte[] content = FileUtils.getLocalFileContent(file);

        if (content == null) {
            log.error("could not read file: " + file);
            return;
        }

        fireActivity(RecoveryFileActivity.created(user, path, content, from,
            charset));

        /*
         * immediately follow up with a new checksum to the remote side can
         * verify the recovered file
         */
        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = EditorManager.getDocumentProvider(input);

        try {
            provider.connect(input);
            IDocument doc = provider.getDocument(input);

            final DocumentChecksum checksum = new DocumentChecksum(path);
            checksum.bind(doc);
            checksum.update();

            fireActivity(new ChecksumActivity(user, path, checksum.getHash(),
                checksum.getLength(), null));
        } catch (CoreException e) {
            log.warn("could not check checksum of file: " + file, e);
        } finally {
            provider.disconnect(input);
        }
    }
}
