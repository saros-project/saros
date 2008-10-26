package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class JumpToDriverPositionAction extends Action implements
	ISessionListener {

    public JumpToDriverPositionAction() {
	setToolTipText("Jump to position of driver.");
	setImageDescriptor(SarosUI.getImageDescriptor("icons/table_edit.png"));

	Saros.getDefault().getSessionManager().addSessionListener(this);
	updateEnablement();
    }

    private ISharedProject getSharedProject() {
	return Saros.getDefault().getSessionManager().getSharedProject();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess process) {
	// ignore
    }

    @Override
    public void run() {
	EditorManager.getDefault().openDriverEditor();
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
    public void sessionStarted(ISharedProject session) {
	updateEnablement();
    }

    private void updateEnablement() {
	setEnabled(getSharedProject() != null);
    }
}
