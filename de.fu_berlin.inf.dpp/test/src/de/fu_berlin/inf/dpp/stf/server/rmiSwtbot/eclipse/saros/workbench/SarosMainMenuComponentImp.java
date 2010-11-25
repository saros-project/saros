package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

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
    public void creatAccount(JID jid, String password) throws RemoteException {
        menuPart.clickMenuWithTexts("Saros", "Create Account");
        rosterVC.confirmWizardCreateXMPPAccount(jid.getDomain(), jid.getName(),
            password);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    private void clickMenuSarosPreferences() throws RemoteException {
        precondition();
        menuPart.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
    }

}
