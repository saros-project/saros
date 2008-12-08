package de.fu_berlin.inf.dpp.ui.actions;

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
            }
        }

    };

    public void sessionStarted(ISharedProject session) {

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

        if (proxy != null) {
            proxy.remove(listener);
            proxy = null;
        }
    }

}
