package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

public class JumpToDriverPositionAction extends Action implements
    ISessionListener {

    private static final Logger log = Logger
        .getLogger(JumpToDriverPositionAction.class.getName());

    public JumpToDriverPositionAction() {
        setToolTipText("Jump to position of driver.");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/table_edit.png"));

        Saros.getDefault().getSessionManager().addSessionListener(this);
        updateEnablement();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                EditorManager.getDefault().openDriverEditor();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
        updateEnablement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
        updateEnablement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess process) {
        // ignore
    }

    private void updateEnablement() {
        setEnabled(getSharedProject() != null);
    }

    private ISharedProject getSharedProject() {
        return Saros.getDefault().getSessionManager().getSharedProject();
    }
}
