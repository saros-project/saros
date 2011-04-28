package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus.submenus.ISarosPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus.submenus.SarosPreferences;

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
        superBot().confirmShellCreateNewXMPPJabberAccount(jid, password);
    }

    public void addBuddy(JID jid) throws RemoteException {
        menu.menu(MENU_ADD_BUDDY).click();
        superBot().confirmShellAddBuddy(jid);
    }

    public void addBuddies(String... jidOfInvitees) throws RemoteException {
        menu.menu(ADD_BUDDIES).click();
        superBot().confirmShellAddBuddyToSession(jidOfInvitees);
    }

    public void shareProjects(String projectName, JID... jids)
        throws RemoteException {
        menu.menu(SHARE_PROJECTS).click();
        superBot().confirmShellShareProject(projectName, jids);
    }

    public void addProjects(String... projectNames) throws RemoteException {
        menu.menu(ADD_PROJECTS).click();
        superBot().confirmShellAddProjectsToSession(projectNames);
    }

    public void stopSession() throws RemoteException {
        menu.menu(CM_STOP_SAROS_SESSION).click();
        superBot().confirmShellLeavingClosingSession();
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
        remoteBot().activateWorkbench();
    }
}
