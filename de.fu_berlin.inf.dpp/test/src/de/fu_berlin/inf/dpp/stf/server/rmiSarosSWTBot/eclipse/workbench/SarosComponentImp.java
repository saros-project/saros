package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Shell;

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
        Shell shell = bot().shell(SHELL_CREATE_NEW_XMPP_ACCOUNT);

        shell.activateAndWait();
        stfText.setTextInTextWithLabel(jid.getDomain(),
            LABEL_XMPP_JABBER_SERVER);
        stfText.setTextInTextWithLabel(jid.getName(), LABEL_USER_NAME);
        stfText.setTextInTextWithLabel(password, LABEL_PASSWORD);
        stfText.setTextInTextWithLabel(password, LABEL_REPEAT_PASSWORD);
        shell.bot_().button(FINISH).click();
        try {
            shell.waitShortUntilIsShellClosed();
        } catch (TimeoutException e) {
            String errorMessage = shell.getErrorMessageInShell();
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
        Shell shell = bot().shell(SHELL_SAROS_CONFIGURATION);
        shell.activateAndWait();
        stfText.setTextInTextWithLabel(jid.getDomain(),
            LABEL_XMPP_JABBER_SERVER);
        stfText.setTextInTextWithLabel(jid.getName(), LABEL_USER_NAME);
        stfText.setTextInTextWithLabel(password, LABEL_PASSWORD);
        shell.bot_().button(NEXT).click();
        shell.bot_().button(FINISH).click();
    }

    public void confirmShellInvitation(String... baseJIDOfinvitees)
        throws RemoteException {
        bot().waitUntilShellOpen(SHELL_INVITATION);
        bot().shell(SHELL_INVITATION).activate();
        bot().shell(SHELL_INVITATION).confirmWindowWithCheckBoxs(FINISH,
            baseJIDOfinvitees);
    }
}
