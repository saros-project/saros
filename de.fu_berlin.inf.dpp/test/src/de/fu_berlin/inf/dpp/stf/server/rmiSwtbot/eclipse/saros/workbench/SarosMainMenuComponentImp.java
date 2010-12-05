package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
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

    /* All infos about the shell "Create XMPP account" */
    private static final String SHELL_CREATE_XMPP_ACCOUNT = "Create XMPP account";
    private static final String LABEL_SERVER = "Server";
    private static final String LABEL_USERNAME = "Username:";
    private static final String LABEL_PASSWORD = "Password:";
    private static final String LABEL_CONFIRM = "Confirm:";

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

    public void createAccount(String username, String password, String server)
        throws RemoteException {
        xmppAccountStore.createNewAccount(username, password, server);
    }

    public void creatAccountGUI(JID jid, String password)
        throws RemoteException {
        menuPart.clickMenuWithTexts("Saros", "Create Account");
        rosterVC.confirmWizardCreateXMPPAccount(jid.getDomain(), jid.getName(),
            password);
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
        menuPart.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
    }

}
