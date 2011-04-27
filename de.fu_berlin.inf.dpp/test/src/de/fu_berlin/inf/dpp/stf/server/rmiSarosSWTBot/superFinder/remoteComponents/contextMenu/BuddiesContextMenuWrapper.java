package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.submenus.IWorkTogetherOnC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.submenus.WorkTogetherOnC;

public class BuddiesContextMenuWrapper extends SarosContextMenuWrapper
    implements IBuddiesContextMenuWrapper {
    private static transient BuddiesContextMenuWrapper self;
    private static WorkTogetherOnC workTogetherOn;

    /**
     * {@link BuddiesContextMenuWrapper} is a singleton, but inheritance is
     * possible.
     */
    public static BuddiesContextMenuWrapper getInstance() {
        if (self != null)
            return self;
        self = new BuddiesContextMenuWrapper();
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
     * contextMenus showed in buddies View
     * 
     **********************************************/

    public void delete() throws RemoteException {
        treeItem.contextMenus(CM_DELETE).click();
        bot().waitUntilShellIsOpen(CONFIRM_DELETE);
        bot().shell(CONFIRM_DELETE).activate();
        bot().shell(CONFIRM_DELETE).bot().button(YES).click();
    }

    public void rename(String newBuddyName) throws RemoteException {
        treeItem.contextMenus(CM_RENAME).click();
        IRemoteBotShell shell = bot().shell(SHELL_SET_NEW_NICKNAME);
        if (!shell.activate()) {
            shell.waitUntilActive();
        }
        shell.bot().text().setText(newBuddyName);
        shell.bot().button(OK).click();
        bot().sleep(500);
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
            sarosBot().confirmShellAddBuddy(jid);
        }
    }

    public IWorkTogetherOnC workTogetherOn() throws RemoteException {
        workTogetherOn.setTreeItem(treeItem);
        return workTogetherOn;
    }
}
