package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.ISarosM;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.ISarosPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.impl.SarosPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

public final class SarosM extends StfRemoteObject implements ISarosM {

    private static final SarosM INSTANCE = new SarosM();

    private IRemoteBotMenu menu;

    public static SarosM getInstance() {
        return INSTANCE;
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

    public void createAccount(JID jid, String password) throws RemoteException {
        precondition();
        menu.menu(MENU_CREATE_ACCOUNT).click();
        SuperBot.getInstance().confirmShellCreateNewXMPPJabberAccount(jid,
            password);
    }

    public void addBuddy(JID jid) throws RemoteException {
        menu.menu(MENU_ADD_BUDDY).click();
        SuperBot.getInstance().confirmShellAddBuddy(jid);
    }

    public void addBuddies(String... jidOfInvitees) throws RemoteException {
        menu.menu(ADD_BUDDIES).click();
        SuperBot.getInstance().confirmShellAddBuddyToSession(jidOfInvitees);
    }

    public void shareProjects(String projectName, JID... jids)
        throws RemoteException {
        menu.menu(SHARE_PROJECTS).click();
        SuperBot.getInstance().confirmShellShareProjects(projectName, jids);
    }

    public void shareProjects(String[] projectNames, JID... jids)
        throws RemoteException {
        menu.menu(SHARE_PROJECTS).click();
        SuperBot.getInstance().confirmShellShareProjects(projectNames, jids);
    }

    public void addProjects(String... projectNames) throws RemoteException {
        menu.menu(ADD_PROJECTS).click();
        SuperBot.getInstance().confirmShellAddProjectsToSession(projectNames);
    }

    public void stopSession() throws RemoteException {
        menu.menu(CM_STOP_SAROS_SESSION).click();
        SuperBot.getInstance().confirmShellLeavingClosingSession();
    }

    public ISarosPreferences preferences() throws RemoteException {
        return SarosPreferences.getInstance();
    }

    /**********************************************
     * 
     * Inner functions
     * 
     **********************************************/

    protected void precondition() throws RemoteException {
        RemoteWorkbenchBot.getInstance().activateWorkbench();
    }
}
