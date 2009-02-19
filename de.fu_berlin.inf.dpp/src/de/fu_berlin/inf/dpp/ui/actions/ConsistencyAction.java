package de.fu_berlin.inf.dpp.ui.actions;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.IDataReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.BalloonNotification;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.VariableProxy;
import de.fu_berlin.inf.dpp.util.VariableProxyListener;

public class ConsistencyAction extends Action implements ISessionListener {

    private static Logger log = Logger.getLogger(ConsistencyAction.class);

    private boolean executingChecksumErrorHandling;

    private IToolBarManager toolBar;

    private Set<IPath> paths;

    public ConsistencyAction(IToolBarManager toolBar) {
        this.toolBar = toolBar;
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

    VariableProxy<Boolean> proxy;

    VariableProxyListener<Boolean> listener = new VariableProxyListener<Boolean>() {

        public void setVariable(Boolean newValue) {

            ConsistencyAction.this.setEnabled(newValue);

            if (newValue) {
                paths = new CopyOnWriteArraySet<IPath>(Saros.getDefault()
                    .getSessionManager().getSharedProject()
                    .getConcurrentDocumentManager()
                    .getPathesWithWrongChecksums());

                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {

                        // concatenate pathes
                        String pathesOfInconsistencies = "";
                        for (IPath path : paths) {
                            pathesOfInconsistencies += path.toOSString() + " ";
                        }

                        // set tooltip
                        setToolTipText("Inconsistency Detected in file/s "
                            + pathesOfInconsistencies);

                        // show balloon notification
                        BalloonNotification.showNotification(
                            ((ToolBarManager) toolBar).getControl(),
                            "Inconsistency Detected!", "In file/s "
                                + pathesOfInconsistencies
                                + " exists inconsistencies.", 5000);
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

            FileUtil.writeFile(input, file);

            return true;
        }
    };

    public void setChecksumErrorHandling(boolean newState) {

        if (newState != executingChecksumErrorHandling) {

            executingChecksumErrorHandling = newState;

            if (newState) {
                Saros.getDefault().getSessionManager().getTransmitter()
                    .addDataReceiver(receiver);
            } else {

                for (IPath path : paths) {
                    Saros.getDefault().getSessionManager().getTransmitter()
                        .sendFileChecksumErrorMessage(path, true);
                }
                paths.clear();

                Saros.getDefault().getSessionManager().getTransmitter()
                    .removeDataReceiver(receiver);
            }
        }
    }

    @Override
    public void run() {
        super.run();

        setChecksumErrorHandling(true);

        for (final IPath path : paths) {
            // save document
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    Set<IEditorPart> editors = EditorManager.getDefault()
                        .getEditors(path);
                    if (editors != null && editors.size() > 0) {
                        editors.iterator().next().doSave(
                            new NullProgressMonitor());
                    }
                }
            });

            Saros.getDefault().getSessionManager().getTransmitter()
                .sendFileChecksumErrorMessage(path, false);
        }

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
