package de.fu_berlin.inf.dpp.ui.actions;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.VariableProxy;
import de.fu_berlin.inf.dpp.util.VariableProxyListener;

public class ConsistencyAction extends Action implements ISessionListener {

    private static Logger logger = Logger.getLogger(ConsistencyAction.class);

    private boolean executingChecksumErrorHandling;

    private static Set<IPath> pathes;

    public ConsistencyAction() {
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJS_WARN_TSK));
        setEnabled(false);
        Saros.getDefault().getSessionManager().addSessionListener(this);
    }

    VariableProxy<Boolean> proxy;

    VariableProxyListener<Boolean> listener = new VariableProxyListener<Boolean>() {

        public void setVariable(Boolean newValue) {

            ConsistencyAction.this.setEnabled(newValue);

            if (newValue) {
                setToolTipText("Inconsistency Detected!");
            } else {
                setToolTipText("");
                logger.debug("All Inconsistencies are resolved");
                if (executingChecksumErrorHandling) {
                    for (IPath path : pathes) {
                        Saros.getDefault().getSessionManager().getTransmitter()
                                .sendFileChecksumErrorMessage(path, true);
                    }
                    pathes.clear();
                    executingChecksumErrorHandling = false;
                }
            }
        }

    };

    @Override
    public void run() {
        super.run();

        executingChecksumErrorHandling = true;

        pathes = new CopyOnWriteArraySet<IPath>(Saros.getDefault()
                .getSessionManager().getSharedProject()
                .getConcurrentDocumentManager().getPathesWithWrongChecksums());

        for (IPath path : pathes) {
            Saros.getDefault().getSessionManager().getTransmitter()
                    .sendFileChecksumErrorMessage(path, false);
        }

    }

    public void sessionStarted(ISharedProject session) {

        ConsistencyAction.pathes = new CopyOnWriteArraySet<IPath>();
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

        if (pathes != null) {
            ConsistencyAction.pathes.clear();
        }

        if (proxy != null) {
            proxy.remove(listener);
            proxy = null;
        }
    }

}
