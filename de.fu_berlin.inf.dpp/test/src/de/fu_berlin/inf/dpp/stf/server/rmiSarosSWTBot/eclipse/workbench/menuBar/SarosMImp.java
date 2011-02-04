package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;

public class SarosMImp extends PreferencesImp implements SarosM {

    private static transient SarosMImp self;

    /**
     * {@link SarosMImp} is a singleton, but inheritance is possible.
     */
    public static SarosMImp getInstance() {
        if (self != null)
            return self;
        self = new SarosMImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void creatAccount(JID jid, String password) throws RemoteException {
        precondition();
        menuW.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        confirmShellCreateNewXMPPAccount(jid, password);
    }

    /**********************************************
     * 
     * Inner functions
     * 
     **********************************************/

    protected void precondition() throws RemoteException {
        workbench.activateWorkbench();
    }
}
