package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class TakeDriverRoleAction extends SelectionProviderAction implements
        ISharedProjectListener, ISessionListener {
    private User selectedUser;

    public TakeDriverRoleAction(ISelectionProvider provider) {
        super(provider, "Take driver role");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/user.png"));
        setToolTipText("Take the driver role from this user.");

        Saros.getDefault().getSessionManager().addSessionListener(this);
        updateEnablemnet();
    }

    @Override
    public void run() {
        ISharedProject project = Saros.getDefault().getSessionManager()
                .getSharedProject();
        // project.setDriver(selectedUser, false);
        project.removeDriver(this.selectedUser, false);
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.selectedUser = (selection.size() == 1) ? (User) selection
                .getFirstElement() : null;

        updateEnablemnet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
        session.addListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
        session.removeListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess process) {
        // ignore
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void driverChanged(JID driver, boolean replicated) {
        updateEnablemnet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userJoined(JID user) {
        // ignore
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userLeft(JID user) {
        // ignore
    }

    private void updateEnablemnet() {
        ISharedProject project = Saros.getDefault().getSessionManager()
                .getSharedProject();

        boolean enabled = ((project != null) && (this.selectedUser != null)
                && project.isHost()
                && !project.getHost().equals(this.selectedUser) && project
                .isDriver(this.selectedUser));
        setEnabled(enabled);
        // setEnabled(project != null && (project.isDriver() /*||
        // project.isHost()*/)
        // && selectedUser != null &&
        // !project.getDriver().equals(selectedUser));
    }
}
