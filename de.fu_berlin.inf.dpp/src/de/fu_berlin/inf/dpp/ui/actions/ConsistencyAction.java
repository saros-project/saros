package de.fu_berlin.inf.dpp.ui.actions;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.annotations.Inject;

import bmsi.util.Diff;
import bmsi.util.DiffPrint;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.IDataReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * TODO This UI Class knows too much about the business logic of running a
 * consistency check
 */
public class ConsistencyAction extends Action {

    private static Logger log = Logger.getLogger(ConsistencyAction.class);

    protected boolean executingChecksumErrorHandling;

    protected Set<IPath> pathsOfHandledFiles;

    @Inject
    SessionManager sessionManager;

    @Inject
    DataTransferManager dataTransferManager;

    @Inject
    ITransmitter transmitter;

    ISharedProject sharedProject;

    public ConsistencyAction() {
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_WARN_TSK));
        setToolTipText("No inconsistencies");

        Saros.getDefault().reinject(this);

        sessionManager.addSessionListener(new AbstractSessionListener() {
            @Override
            public void sessionStarted(ISharedProject session) {
                setSharedProject(session);
            }

            @Override
            public void sessionEnded(ISharedProject session) {
                setSharedProject(null);
            }
        });

        setSharedProject(sessionManager.getSharedProject());
    }

    private void setSharedProject(ISharedProject newSharedProject) {

        // Unregister from previous project
        if (sharedProject != null) {
            this.pathsOfHandledFiles.clear();
            this.proxy.remove(listener);
            this.proxy = null;
        }

        sharedProject = newSharedProject;

        // Register to new project
        if (sharedProject != null) {
            this.pathsOfHandledFiles = new CopyOnWriteArraySet<IPath>();
            this.proxy = sharedProject.getConcurrentDocumentManager()
                .getConsistencyToResolve();
            proxy.addAndNotify(listener);
        } else {
            setEnabled(false);
        }

    }

    // TODO Name is to generic
    ObservableValue<Boolean> proxy;

    ValueChangeListener<Boolean> listener = new ValueChangeListener<Boolean>() {

        public void setValue(Boolean newValue) {

            ConsistencyAction.this.setEnabled(newValue);

            if (newValue) {
                final Set<IPath> paths = new CopyOnWriteArraySet<IPath>(Saros
                    .getDefault().getSessionManager().getSharedProject()
                    .getConcurrentDocumentManager()
                    .getPathsWithWrongChecksums());

                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {

                        // Concatenate paths
                        StringBuilder sb = new StringBuilder();
                        for (IPath path : paths) {
                            if (sb.length() > 0)
                                sb.append(", ");

                            sb.append(path.toOSString());
                        }
                        String pathsOfInconsistencies = sb.toString();

                        // set tooltip
                        setToolTipText("Inconsistency Detected in file/s "
                            + pathsOfInconsistencies);

                        // TODO Balloon is too aggressive at the moment, when
                        // the host is slow in sending changes (for instance
                        // when refactoring)

                        // show balloon notification
                        /*
                         * BalloonNotification.showNotification(
                         * ((ToolBarManager) toolBar).getControl(),
                         * "Inconsistency Detected!",
                         * "Inconsistencies detected in: " +
                         * pathsOfInconsistencies, 5000);
                         */
                    }
                });

            } else {
                setToolTipText("No inconsistencies");
                log.debug("All Inconsistencies are resolved");
                setChecksumErrorHandling(false);
            }
        }

    };

    IDataReceiver receiver = new IDataReceiver() {

        public boolean receivedArchive(TransferDescription data,
            InputStream input) {
            return false;
        }

        public boolean receivedFileList(TransferDescription data,
            InputStream input) {
            return false;
        }

        public boolean receivedResource(JID from, Path path, InputStream input,
            int time) {

            log.debug("Received consistency file [" + from.getName() + "] "
                + path.toString());

            ISharedProject project = sessionManager.getSharedProject();

            // Project might have ended in between
            if (project == null)
                return false;

            final IFile file = project.getProject().getFile(path);

            if (log.isDebugEnabled()) {
                input = logDiff(log, from, path, input, file);
            }

            final InputStream toWrite = input;
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    FileUtil.writeFile(toWrite, file);
                }
            });

            ConcurrentDocumentManager concurrentManager = project
                .getConcurrentDocumentManager();

            // The file contents has been replaced, now reset Jupiter
            if (concurrentManager.isManagedByJupiter(path))
                concurrentManager.resetJupiterClient(path);

            // Trigger a new consistency check, so we don't have to wait for new
            // checksums from the host
            concurrentManager.checkConsistency();

            return true;
        }

    };

    public static InputStream logDiff(Logger log, JID from, Path path,
        InputStream input, IFile file) {
        try {
            // save input in a byte[] for later
            byte[] inputBytes = IOUtils.toByteArray(input);

            // reset input
            input = new ByteArrayInputStream(inputBytes);

            // get stream from old file
            InputStream oldStream = file.getContents();
            InputStream newStream = new ByteArrayInputStream(inputBytes);

            // read Lines from
            Object[] oldContent = IOUtils.readLines(oldStream).toArray();
            Object[] newContent = IOUtils.readLines(newStream).toArray();

            // Calculate diff of the two files
            Diff diff = new Diff(oldContent, newContent);
            Diff.Change script = diff.diff_2(false);

            // log diff
            DiffPrint.UnifiedPrint print = new DiffPrint.UnifiedPrint(
                oldContent, newContent);
            Writer writer = new StringWriter();
            print.setOutput(writer);
            print.print_script(script);

            String diffAsString = writer.toString();

            if (diffAsString == null || diffAsString.trim().length() == 0) {
                log.error("No inconsistency found in file [" + from.getName()
                    + "] " + path.toString());
            } else {
                log.debug("Diff of inconsistency: \n" + writer);
            }
        } catch (CoreException e) {
            log.error("Can't read file content", e);
        } catch (IOException e) {
            log.error("Can't convert file content to String", e);
        }
        return input;
    }

    // TODO: move this business logic into ConsistencyWatchdog
    public void setChecksumErrorHandling(boolean newState) {

        if (newState != executingChecksumErrorHandling) {

            executingChecksumErrorHandling = newState;

            if (newState) {

                // Register as a receiver of incoming files...
                dataTransferManager.addDataReceiver(receiver);

                for (final IPath path : pathsOfHandledFiles) {

                    // Save document before asking host to resend
                    try {
                        EditorManager.getDefault().saveLazy(path);
                    } catch (FileNotFoundException e) {
                        // Sending the checksum error message should recreate
                        // this file
                    }
                }

                // Send checksumErrorMessage to host
                transmitter.sendFileChecksumErrorMessage(pathsOfHandledFiles,
                    false);

            } else {

                // Unregister from dataTransferManager
                dataTransferManager.removeDataReceiver(receiver);

                // Send message to host that all inconsistencies are resolved
                transmitter.sendFileChecksumErrorMessage(pathsOfHandledFiles,
                    true);
                pathsOfHandledFiles.clear();
            }
        }
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {

        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                // TODO: move this business logic into new ConsistencyWatchdog
                // class
                pathsOfHandledFiles = new CopyOnWriteArraySet<IPath>(
                    sessionManager.getSharedProject()
                        .getConcurrentDocumentManager()
                        .getPathsWithWrongChecksums());

                if (executingChecksumErrorHandling) {
                    log.warn("Restarting Checksum Error Handling"
                        + " while another operation is running");
                    // HACK If we programmed correctly this should not happen
                    executingChecksumErrorHandling = false;
                }

                setChecksumErrorHandling(true);

            }
        });
    }

}
