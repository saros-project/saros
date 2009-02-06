package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class GiveExclusiveDriverRoleAction extends GiveDriverRoleAction {

    public GiveExclusiveDriverRoleAction(ISelectionProvider provider,
        String text) {
        super(provider, text);
        setImageDescriptor(SarosUI.getImageDescriptor("icons/user_edit.png"));
        setToolTipText("Give the driver role exclusive to this user");
    }

    @Override
    public void run() {
        super.run();
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        for (User user : project.getParticipants()) {

            if ((user.getUserRole() == UserRole.DRIVER)
                && !this.selectedUser.equals(user)) {
                project.removeDriver(user, false);
            }
        }
    }

}
