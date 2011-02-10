package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

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
        shellW.activateShellAndWait(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        textW.setTextInTextWithLabel(jid.getDomain(), LABEL_XMPP_JABBER_SERVER);
        textW.setTextInTextWithLabel(jid.getName(), LABEL_USER_NAME);
        textW.setTextInTextWithLabel(password, LABEL_PASSWORD);
        textW.setTextInTextWithLabel(password, LABEL_REPEAT_PASSWORD);
        buttonW.clickButton(FINISH);
        try {
            shellW.waitShortUntilIsShellClosed(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        } catch (TimeoutException e) {
            String errorMessage = shellW
                .getErrorMessageInShell(SHELL_CREATE_NEW_XMPP_ACCOUNT);
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
        shellW.activateShellAndWait(SHELL_SAROS_CONFIGURATION);
        textW.setTextInTextWithLabel(jid.getDomain(), LABEL_XMPP_JABBER_SERVER);
        textW.setTextInTextWithLabel(jid.getName(), LABEL_USER_NAME);
        textW.setTextInTextWithLabel(password, LABEL_PASSWORD);
        buttonW.clickButton(NEXT);
        buttonW.clickButton(FINISH);
    }

    public void confirmShellInvitation(String... baseJIDOfinvitees)
        throws RemoteException {
        shellW.activateShell(SHELL_INVITATION);
        shellW.confirmWindowWithCheckBoxs(SHELL_INVITATION, FINISH,
            baseJIDOfinvitees);
    }

}
