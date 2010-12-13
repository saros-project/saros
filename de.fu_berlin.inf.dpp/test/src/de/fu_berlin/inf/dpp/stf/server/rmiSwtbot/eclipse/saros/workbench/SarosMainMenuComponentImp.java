package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.MainMenuComponentImp;

public class SarosMainMenuComponentImp extends MainMenuComponentImp implements
    SarosMainMenuComponent {

    private static transient SarosMainMenuComponentImp self;

    /* name of all the main menus */
    private static final String MENU_SAROS = "Saros";
    private static final String MENU_CREATE_ACCOUNT = "Create Account";
    private static final String MENU_PREFERENCES = "Preferences";

    /* title of shells which are pop up by clicking the main menus */
    private static final String SHELL_PREFERNCES = "Preferences";

    /**
     * {@link SarosMainMenuComponentImp} is a singleton, but inheritance is
     * possible.
     */
    public static SarosMainMenuComponentImp getInstance() {
        if (self != null)
            return self;
        self = new SarosMainMenuComponentImp();
        return self;
    }

    /***********************************************************************
     * 
     * exported functions
     * 
     ***********************************************************************/

    /**********************************************
     * 
     * actions about Account.
     * 
     **********************************************/
    public void createAccount(String username, String password, String server)
        throws RemoteException {
        xmppAccountStore.createNewAccount(username, password, server);
    }

    public void creatAccountGUI(JID jid, String password,
        boolean usesThisAccountNow) throws RemoteException {
        mainMenuC.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        rosterVC.confirmWindowCreateXMPPAccount(jid.getDomain(), jid.getName(),
            password, usesThisAccountNow);
    }

    public boolean isAccountExist(JID jid, String password)
        throws RemoteException {
        ArrayList<XMPPAccount> allAccounts = xmppAccountStore.getAllAccounts();
        for (XMPPAccount account : allAccounts) {
            log.debug("account id: " + account.getId());
            log.debug("account username: " + account.getUsername());
            log.debug("account password: " + account.getPassword());
            log.debug("account server: " + account.getServer());
            if (jid.getName().equals(account.getUsername())
                && jid.getDomain().equals(account.getServer())
                && password.equals(account.getPassword())) {
                return true;
            }
        }
        return false;
    }

    public boolean isAccountExistGUI(JID jid, String password)
        throws RemoteException {
        /*
         * TODO add the implementation
         */
        return false;
    }

    public boolean isAccountActive(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        return account.isActive();
    }

    public boolean isAccountActiveGUI(JID jid) throws RemoteException {
        /*
         * TODO add the implementation
         */
        return false;
    }

    public void activateAccount(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        xmppAccountStore.setAccountActive(account);
    }

    public void activateAccountGUI(JID jid) throws RemoteException {
        /*
         * TODO add the implementation
         */
    }

    public void changeAccount(JID jid, String newUserName, String newPassword,
        String newServer) throws RemoteException {
        xmppAccountStore.changeAccountData(getXMPPAccount(jid).getId(),
            newUserName, newPassword, newServer);
    }

    public void changeAccountGUI(JID jid, String newUserName,
        String newPassword, String newServer) throws RemoteException {
        /*
         * TODO add the implementation
         */
    }

    public void deleteAccount(JID jid) throws RemoteException {
        xmppAccountStore.deleteAccount(getXMPPAccount(jid));
    }

    public void deleteAccountGUI(JID jid) throws RemoteException {
        /*
         * TODO add the implementation
         */
    }

    /**********************************************
     * 
     * setting for screensharing
     * 
     **********************************************/

    public void setupSettingForScreensharing(int encoder, int videoResolution,
        int bandWidth, int capturedArea) throws RemoteException {
        clickMenuSarosPreferences();
        SWTBotTree tree = bot.tree();
        tree.expandNode("Saros").select("Screensharing");
        bot.ccomboBox(0).setSelection(encoder);
        bot.ccomboBox(1).setSelection(videoResolution);
        bot.button("Apply").click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    /**********************************************
     * 
     * setting for Feedback
     * 
     **********************************************/
    public void disableAutomaticReminder() throws RemoteException {
        if (feedbackManager.isFeedbackDisabled()) {
            feedbackManager.setFeedbackDisabled(true);
            // clickMenuSarosPreferences();
            // SWTBotTree tree = bot.tree();
            // tree.expandNode("Saros").select("Feedback");
            // bot.radioInGroup(Messages.getString("feedback.page.radio.disable"),
            // Messages.getString("feedback.page.group.interval")).click();
            // bot.button("Apply").click();
            // bot.button(OK).click();
            // windowPart.waitUntilShellClosed(SHELL_PREFERNCES);
        }
    }

    public void disableAutomaticReminderGUI() throws RemoteException {
        if (feedbackManager.isFeedbackDisabled()) {
            clickMenuSarosPreferences();
            SWTBotTree tree = bot.tree();
            tree.expandNode("Saros").select("Feedback");
            bot.radioInGroup(Messages.getString("feedback.page.radio.disable"),
                Messages.getString("feedback.page.group.interval")).click();
            bot.button("Apply").click();
            bot.button(OK).click();
            shellC.waitUntilShellClosed(SHELL_PREFERNCES);
        }
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    private XMPPAccount getXMPPAccount(JID id) {
        ArrayList<XMPPAccount> allAccounts = xmppAccountStore.getAllAccounts();
        for (XMPPAccount account : allAccounts) {
            if (localJID.getName().equals(account.getUsername())
                && localJID.getDomain().equals(account.getServer())) {
                return account;
            }
        }
        return null;
    }

    private void clickMenuSarosPreferences() throws RemoteException {
        precondition();
        mainMenuC.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
    }

}
