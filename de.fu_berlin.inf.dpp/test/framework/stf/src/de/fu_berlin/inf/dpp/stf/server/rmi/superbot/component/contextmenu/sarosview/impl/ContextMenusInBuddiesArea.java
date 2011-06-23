package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInBuddiesArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.IWorkTogetherOnC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.impl.WorkTogetherOnC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

public final class ContextMenusInBuddiesArea extends ContextMenusInSarosView
    implements IContextMenusInBuddiesArea {

    private static final ContextMenusInBuddiesArea INSTANCE = new ContextMenusInBuddiesArea();

    public static ContextMenusInBuddiesArea getInstance() {
        return INSTANCE;
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
        RemoteWorkbenchBot.getInstance().waitUntilShellIsOpen(CONFIRM_DELETE);
        RemoteWorkbenchBot.getInstance().shell(CONFIRM_DELETE).activate();
        RemoteWorkbenchBot.getInstance().shell(CONFIRM_DELETE).bot()
            .button(YES).click();
    }

    public void rename(String newBuddyName) throws RemoteException {
        treeItem.contextMenus(CM_RENAME).click();
        IRemoteBotShell shell = RemoteWorkbenchBot.getInstance().shell(
            SHELL_SET_NEW_NICKNAME);
        if (!shell.activate()) {
            shell.waitUntilActive();
        }
        shell.bot().text().setText(newBuddyName);
        shell.bot().button(OK).click();
        RemoteWorkbenchBot.getInstance().sleep(500);
    }

    public void addToSarosSession() throws RemoteException {
        if (!treeItem.isEnabled()) {
            throw new RuntimeException(
                "unable to invite this user, he is not conntected");
        }
        treeItem.waitUntilContextMenuExists(CM_ADD_TO_SAROS_SESSION);
        treeItem.contextMenus(CM_ADD_TO_SAROS_SESSION).click();

    }

    public void addBuddy(JID jid) throws RemoteException {
        if (!sarosView.hasBuddy(jid)) {
            treeItem.contextMenus("Add Buddy...").click();
            SuperBot.getInstance().confirmShellAddBuddy(jid);
        }
    }

    public IWorkTogetherOnC workTogetherOn() throws RemoteException {
        WorkTogetherOnC.getInstance().setTreeItem(treeItem);
        return WorkTogetherOnC.getInstance();
    }
}
