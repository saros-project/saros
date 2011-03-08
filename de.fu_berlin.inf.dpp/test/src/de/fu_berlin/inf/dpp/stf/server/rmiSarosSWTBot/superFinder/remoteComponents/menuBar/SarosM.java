package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class SarosM extends Component implements ISarosM {

    private static transient SarosM self;

    private static SarosPreferences pref;

    private IRemoteBotMenu menu;

    /**
     * {@link SarosM} is a singleton, but inheritance is possible.
     */
    public static SarosM getInstance() {
        if (self != null)
            return self;
        self = new SarosM();
        pref = SarosPreferences.getInstance();
        return self;
    }

    public ISarosM setMenu(IRemoteBotMenu menu) {
        this.menu = menu;
        return this;
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
        menu.menu(MENU_CREATE_ACCOUNT).click();
        sarosBot().confirmShellCreateNewXMPPJabberAccount(jid, password);
    }

    public void addBuddy(JID jid) throws RemoteException {
        menu.menu(MENU_ADD_BUDDY).click();
        sarosBot().confirmShellAddBuddy(jid);
    }

    public void shareProjects(String projectName, JID... jids)
        throws RemoteException {
        menu.menu(MENU_SHARE_PROJECTS).click();
        sarosBot().confirmShellShareProject(projectName, jids);
    }

    public ISarosPreferences preferences() throws RemoteException {
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
