package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;

@Component(module = "action")
public class GiveWriteAccessAction extends ChangeWriteAccessAction {

    public GiveWriteAccessAction() {
        super(Permission.WRITE_ACCESS, Messages.GiveWriteAccessAction_title,
            Messages.GiveWriteAccessAction_tooltip,
            ImageManager.ICON_CONTACT_SAROS_SUPPORT);
    }

}
