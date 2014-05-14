package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;

public class RestrictToReadOnlyAccessAction extends ChangeWriteAccessAction {

    public RestrictToReadOnlyAccessAction() {
        super(Permission.READONLY_ACCESS,
            Messages.RestrictToReadOnlyAccessAction_title,
            Messages.RestrictToReadOnlyAccessAction_tooltip,
            ImageManager.ICON_USER_SAROS_READONLY);
    }

}
