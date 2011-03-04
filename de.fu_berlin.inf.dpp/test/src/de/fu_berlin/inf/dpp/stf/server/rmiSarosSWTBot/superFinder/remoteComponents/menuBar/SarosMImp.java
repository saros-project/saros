package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class SarosMImp extends Component implements SarosM {

    private static transient SarosMImp self;

    private static SarosPreferencesImp pref;

    /**
     * {@link SarosMImp} is a singleton, but inheritance is possible.
     */
    public static SarosMImp getInstance() {
        if (self != null)
            return self;
        self = new SarosMImp();
        pref = SarosPreferencesImp.getInstance();
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
        bot().menu(MENU_SAROS).menu(MENU_CREATE_ACCOUNT).click();
        sarosBot().confirmShellCreateNewXMPPAccount(jid, password);
    }

    public void addBuddy(JID jid) throws RemoteException {
        bot().menu(MENU_SAROS).menu(MENU_ADD_BUDDY).click();
        sarosBot().confirmShellAddBuddy(jid);
    }

    public void shareProjects(String projectName, JID... jids)
        throws RemoteException {
        bot().menu(MENU_SAROS).menu(MENU_SHARE_PROJECTS).click();
        sarosBot().confirmWizardShareProject(projectName, jids);
    }

    public SarosPreferences preferences() throws RemoteException {
        return pref;
    }

    /**********************************************
     * 
     * Inner functions
     * 
     **********************************************/

    protected void precondition() throws RemoteException {
        bot().activateWorkbench();
    }
}
