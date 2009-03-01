package de.fu_berlin.inf.dpp.ui.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import bmsi.util.Diff;
import bmsi.util.DiffPrint;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.IDataReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

public class ConsistencyAction extends Action implements ISessionListener {

    private static Logger log = Logger.getLogger(ConsistencyAction.class);

    protected boolean executingChecksumErrorHandling;

    protected Set<IPath> paths;

    public ConsistencyAction() {
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_WARN_TSK));
        setToolTipText("No inconsistencies");

        // add ConsistencyListener if already in a session
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();
        if (project != null) {
            this.proxy = project.getConcurrentDocumentManager()
                .getConsistencyToResolve();
            proxy.addAndNotify(listener);
        } else
            setEnabled(false);

        Saros.getDefault().getSessionManager().addSessionListener(this);
    }

    ObservableValue<Boolean> proxy;

    ValueChangeListener<Boolean> listener = new ValueChangeListener<Boolean>() {

        public void setValue(Boolean newValue) {

            ConsistencyAction.this.setEnabled(newValue);

            if (newValue) {
                paths = new CopyOnWriteArraySet<IPath>(Saros.getDefault()
                    .getSessionManager().getSharedProject()
                    .getConcurrentDocumentManager()
                    .getPathsWithWrongChecksums());

                Display.getDefault().asyncExec(new Runnable() {
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

            ISharedProject project = Saros.getDefault().getSessionManager()
                .getSharedProject();
            if (project == null)
                return false;

            IFile file = project.getProject().getFile(path);

            if (log.isDebugEnabled()) {
                try {
                    // save input in a byte[] for later
                    byte[] inputBytes = IOUtils.toByteArray(input);

                    // reset input
                    input = new ByteArrayInputStream(inputBytes);

                    // get stream from old file
                    InputStream oldStream = file.getContents();
                    InputStream newStream = new ByteArrayInputStream(inputBytes);

                    // read Lines from
                    Object[] oldContent = IOUtils.readLines(oldStream)
                        .toArray();
                    Object[] newContent = IOUtils.readLines(newStream)
                        .toArray();

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

                    if (diffAsString == null
                        || diffAsString.trim().length() == 0) {
                        log.error("No inconsistency found in file ["
                            + from.getName() + "] " + path.toString());
                    } else {
                        log.debug("Diff of inconsistency: \n" + writer);
                    }
                } catch (CoreException e) {
                    log.error("Can't read file content", e);
                } catch (IOException e) {
                    log.error("Can't convert file content to String", e);
                }
            }

            FileUtil.writeFile(input, file);

            Saros.getDefault().getSessionManager().getSharedProject()
                .getConcurrentDocumentManager().checkConsistency();

            return true;
        }
    };

    public void setChecksumErrorHandling(boolean newState) {

        if (newState != executingChecksumErrorHandling) {

            executingChecksumErrorHandling = newState;

            if (newState) {
                Saros.getDefault().getContainer().getComponent(
                    DataTransferManager.class).addDataReceiver(receiver);
            } else {

                for (IPath path : paths) {
                    Saros.getDefault().getSessionManager().getTransmitter()
                        .sendFileChecksumErrorMessage(path, true);
                }
                paths.clear();

                Saros.getDefault().getContainer().getComponent(
                    DataTransferManager.class).removeDataReceiver(receiver);
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
                executeConsistencyHandling();
            }
        });
    }

    public void executeConsistencyHandling() {
        setChecksumErrorHandling(true);

        for (final IPath path : paths) {
            // Save document
            for (IEditorPart editor : EditorManager.getDefault().getEditors(
                path)) {
                if (!saveEditor(editor)) {
                    log
                        .info("Consistency Check canceled by user! Diff might be inaccurate");
                }
            }

            // Send checksumErrorMessage to Host
            Saros.getDefault().getSessionManager().getTransmitter()
                .sendFileChecksumErrorMessage(path, false);
        }
    }

    /**
     * @return true when the editor was successfully saved
     */
    public static boolean saveEditor(final IEditorPart editor) {

        final CountDownLatch latch = new CountDownLatch(1);

        final IProgressMonitor monitor = new NullProgressMonitor() {
            @Override
            public void done() {
                latch.countDown();
            }
        };

        // save document
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                try {
                    editor.doSave(monitor);
                } catch (RuntimeException e) {
                    log.error("Internal error: ", e);
                }
            }
        });

        // Wait for saving to be done
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return !monitor.isCanceled();

    }

    public void sessionStarted(ISharedProject session) {

        paths = new CopyOnWriteArraySet<IPath>();
        if (proxy != null) {
            proxy.remove(listener);
        }

        proxy = Saros.getDefault().getSessionManager().getSharedProject()
            .getConcurrentDocumentManager().getConsistencyToResolve();

        proxy.add(listener);
    }

    public void invitationReceived(IIncomingInvitationProcess invitation) {
        // ignore
    }

    public void sessionEnded(ISharedProject session) {

        if (paths != null) {
            paths.clear();
        }

        if (proxy != null) {
            proxy.remove(listener);
            proxy = null;
        }
    }

}
