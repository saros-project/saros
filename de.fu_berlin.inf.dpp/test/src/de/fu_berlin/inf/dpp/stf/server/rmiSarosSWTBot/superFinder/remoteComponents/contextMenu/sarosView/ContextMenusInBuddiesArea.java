package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.sarosView;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.sarosView.submenus.IWorkTogetherOnC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.sarosView.submenus.WorkTogetherOnC;

public class ContextMenusInBuddiesArea extends ContextMenusInSarosView
    implements IContextMenusInBuddiesArea {
    private static transient ContextMenusInBuddiesArea self;
    private static WorkTogetherOnC workTogetherOn;

    /**
     * {@link ContextMenusInBuddiesArea} is a singleton, but inheritance is
     * possible.
     */
    public static ContextMenusInBuddiesArea getInstance() {
        if (self != null)
            return self;
        self = new ContextMenusInBuddiesArea();
        workTogetherOn = WorkTogetherOnC.getInstance();

        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * contextMenus showed in buddies-area
     * 
     **********************************************/

    public void delete() throws RemoteException {
        treeItem.contextMenus(CM_DELETE).click();
        remoteBot().waitUntilShellIsOpen(CONFIRM_DELETE);
        remoteBot().shell(CONFIRM_DELETE).activate();
        remoteBot().shell(CONFIRM_DELETE).bot().button(YES).click();
    }

    public void rename(String newBuddyName) throws RemoteException {
        treeItem.contextMenus(CM_RENAME).click();
        IRemoteBotShell shell = remoteBot().shell(SHELL_SET_NEW_NICKNAME);
        if (!shell.activate()) {
            shell.waitUntilActive();
        }
        shell.bot().text().setText(newBuddyName);
        shell.bot().button(OK).click();
        remoteBot().sleep(500);
    }

    public void addToSarosSession() throws RemoteException {
        if (!treeItem.isEnabled()) {
            throw new RuntimeException(
                "You can't invite this user, he isn't conntected yet");
        }
        treeItem.waitUntilContextMenuExists(CM_ADD_TO_SAROS_SESSION);
        treeItem.contextMenus(CM_ADD_TO_SAROS_SESSION).click();

    }

    public void addBuddy(JID jid) throws RemoteException {
        if (!sarosView.hasBuddy(jid)) {
            treeItem.contextMenus("Add Buddy...").click();
            superBot().confirmShellAddBuddy(jid);
        }
    }

    public IWorkTogetherOnC workTogetherOn() throws RemoteException {
        workTogetherOn.setTreeItem(treeItem);
        return workTogetherOn;
    }
}
