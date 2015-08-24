package de.fu_berlin.inf.dpp.concurrent.watchdog;

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
import org.eclipse.swt.graphics.Image;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.ChecksumErrorActivity;
import de.fu_berlin.inf.dpp.activities.RecoveryFileActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
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
public final class ConsistencyWatchdogHandler extends AbstractActivityProducer
    implements Startable {

    private static Logger LOG = Logger
        .getLogger(ConsistencyWatchdogHandler.class);

    private final EditorManager editorManager;

    private final ISarosSession session;

    private final IEditorAPI editorAPI;

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void receive(ChecksumErrorActivity checksumError) {
            if (session.isHost())
                triggerRecovery(checksumError);
        }
    };

    @Override
    public void start() {
        session.addActivityConsumer(consumer);
        session.addActivityProducer(this);
    }

    @Override
    public void stop() {
        session.removeActivityConsumer(consumer);
        session.removeActivityProducer(this);
    }

    public ConsistencyWatchdogHandler(final ISarosSession session,
        final EditorManager editorManager, final IEditorAPI editorAPI) {
        this.session = session;
        this.editorManager = editorManager;
        this.editorAPI = editorAPI;
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
    private void triggerRecovery(final ChecksumErrorActivity checksumError) {

        LOG.debug("received Checksum Error: " + checksumError);

        final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
            SWTUtils.getShell()) {
            @Override
            protected Image getImage() {
                return getWarningImage();
            }

            // TODO add some text
            // "Inconsistent file state has detected. File "
            // + paths
            // + " from user "
            // + from.getBase()
            // +
            // " has to be synchronized with project host. Please wait until the inconsistencies are resolved."
        };

        // execute async so outstanding activities could be dispatched
        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
            @Override
            public void run() {
                try {
                    /*
                     * run in a modal context otherwise we would block again the
                     * dispatching of activities
                     */
                    dialog.run(true, true, new IRunnableWithProgress() {
                        @Override
                        public void run(IProgressMonitor monitor) {
                            runRecovery(checksumError, monitor);
                        }
                    });
                } catch (InvocationTargetException e) {
                    try {
                        throw e.getCause();
                    } catch (CancellationException c) {
                        LOG.info("Recovery was canceled by local user");
                    } catch (Throwable t) {
                        LOG.error("Internal Error: ", t);
                    }
                } catch (InterruptedException e) {
                    LOG.error("Code not designed to be interruptible", e);
                }
            }
        });
    }

    private void runRecovery(final ChecksumErrorActivity checksumError,
        final IProgressMonitor monitor) throws CancellationException {

        List<StartHandle> startHandles = null;

        final SubMonitor progress = SubMonitor.convert(monitor,
            "Performing recovery...", 1000);

        try {

            progress.subTask("locking session");

            startHandles = session.getStopManager().stop(session.getUsers(),
                "Consistency recovery");

            progress.worked(100);

            recoverFiles(checksumError, progress.newChild(800));

            /*
             * We have to start the StartHandle of the inconsistent user first
             * (blocking!) because otherwise the other participants can be
             * started before the inconsistent user completely processed the
             * consistency recovery.
             */
            progress.subTask("unlocking session");

            // find the StartHandle of the inconsistent user
            StartHandle inconsistentStartHandle = null;
            for (StartHandle startHandle : startHandles) {
                if (checksumError.getSource().equals(startHandle.getUser())) {
                    inconsistentStartHandle = startHandle;
                    break;
                }
            }
            if (inconsistentStartHandle == null) {
                LOG.error("could not find start handle"
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

    private void recoverFiles(final ChecksumErrorActivity checksumError,
        final IProgressMonitor monitor) {

        final List<SPath> inconsistentPaths = checksumError.getPaths();

        monitor.beginTask("Performing recovery...", inconsistentPaths.size());

        try {
            for (final SPath path : inconsistentPaths) {
                monitor.subTask("recovering file: " + path.getFullPath());

                SWTUtils.runSafeSWTSync(LOG, new Runnable() {
                    @Override
                    public void run() {
                        recoverFile(checksumError.getSource(), path);
                    }
                });

                monitor.worked(1);
            }

            // Tell the user that we sent all files
            fireActivity(new ChecksumErrorActivity(session.getLocalUser(),
                checksumError.getSource(), null, checksumError.getRecoveryID()));
        } finally {
            monitor.done();
        }
    }

    /**
     * Recover a single file for the given user (that is either send the file or
     * tell the user to remove it).
     */
    private void recoverFile(final User from, final SPath path) {

        final IFile file = ((EclipseFileImpl) path.getFile()).getDelegate();

        // Reset jupiter
        session.getConcurrentDocumentServer().reset(from, path);

        final User user = session.getLocalUser();

        if (!file.exists()) {
            // TODO Warn the user...
            // Tell the client to delete the file
            fireActivity(RecoveryFileActivity.removed(user, path, from, null));
            fireActivity(ChecksumActivity.missing(user, path));
            return;
        }

        /*
         * save the editor the dirty contents are flushed to the underlying
         * storage
         */
        editorManager.saveLazy(path);

        String charset = null;

        try {
            charset = file.getCharset();
        } catch (CoreException e) {
            LOG.warn("could not determine encoding for file: " + file, e);
        }

        byte[] content = FileUtils.getLocalFileContent(file);

        if (content == null) {
            LOG.error("could not read file: " + file);
            return;
        }

        fireActivity(RecoveryFileActivity.created(user, path, content, from,
            charset));

        /*
         * Immediately follow up with a new checksum activity so that the remote
         * side can verify the recovered file.
         */

        DocumentChecksum checksum = new DocumentChecksum(path);
        checksum.update(editorManager.getContent(path));

        fireActivity(new ChecksumActivity(user, path, checksum.getHash(),
            checksum.getLength(), null));
    }
}
