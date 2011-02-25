package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;

public class SarosComponentImp extends EclipseComponentImp implements
    SarosComponent {

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/
    /**********************************************
     * 
     * action
     * 
     **********************************************/
    public void confirmShellCreateNewXMPPAccount(JID jid, String password)
        throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        STFBotShell shell = bot().shell(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        shell.activate();
        shell.bot().textWithLabel(LABEL_XMPP_JABBER_SERVER)
            .setText(jid.getDomain());
        shell.bot().textWithLabel(LABEL_USER_NAME).setText(jid.getName());
        shell.bot().textWithLabel(LABEL_PASSWORD).setText(password);
        shell.bot().textWithLabel(LABEL_REPEAT_PASSWORD).setText(password);

        shell.bot().button(FINISH).click();
        try {
            shell.waitShortUntilIsClosed();
        } catch (TimeoutException e) {
            String errorMessage = shell.getErrorMessage();
            if (errorMessage.matches(ERROR_MESSAGE_TOO_FAST_REGISTER_ACCOUNTS
                + ".*"))
                throw new RuntimeException(
                    "You are not allowed to register accounts so fast!");
            else if (errorMessage.matches(ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS
                + ".*\n*.*"))
                throw new RuntimeException("The Account " + jid.getBase()
                    + " is already existed!");
        }
    }

    public void confirmWizardSarosConfiguration(JID jid, String password)
        throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_SAROS_CONFIGURATION);
        STFBotShell shell = bot().shell(SHELL_SAROS_CONFIGURATION);
        shell.activate();
        shell.bot().textWithLabel(LABEL_XMPP_JABBER_SERVER)
            .setText(jid.getDomain());
        shell.bot().textWithLabel(LABEL_USER_NAME).setText(jid.getName());
        shell.bot().textWithLabel(LABEL_PASSWORD).setText(password);

        shell.bot().button(NEXT).click();
        shell.bot().button(FINISH).click();
    }

    public void confirmShellInvitation(String... baseJIDOfinvitees)
        throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_INVITATION);
        STFBotShell shell = bot().shell(SHELL_INVITATION);
        shell.activate();
        shell.confirmWithCheckBoxs(FINISH, baseJIDOfinvitees);
    }
}
