package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInBuddiesArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.IWorkTogetherOnContextMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.impl.WorkTogetherOnContextMenu;
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
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_DELETE);
        SWTBotShell shell = new SWTBot().shell(CONFIRM_DELETE);
        shell.activate();
        shell.bot().button(YES).click();
        shell.bot().waitUntil(Conditions.shellCloses(shell));
        // wait for tree update in saros session view
        new SWTBot().sleep(500);
    }

    public void rename(String newBuddyName) throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_RENAME);

        SWTBotShell shell = new SWTBot().shell(SHELL_SET_NEW_NICKNAME);
        shell.activate();
        shell.bot().text().setText(newBuddyName);
        shell.bot().button(OK).click();
        shell.bot().waitUntil(Conditions.shellCloses(shell));
        // wait for tree update in saros session view
        new SWTBot().sleep(500);
    }

    public void addToSarosSession() throws RemoteException {
        if (!treeItem.isEnabled()) {
            throw new RuntimeException(
                "unable to invite this user, he is not conntected");
        }
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_ADD_TO_SAROS_SESSION);
        // wait for tree update in saros session view
        new SWTBot().sleep(500);
    }

    public void addBuddy(JID jid) throws RemoteException {
        if (!sarosView.hasBuddy(jid)) {
            treeItem.select();
            ContextMenuHelper.clickContextMenu(tree, "Add Buddy...");
            SuperBot.getInstance().confirmShellAddBuddy(jid);
            // wait for tree update in saros session view
            new SWTBot().sleep(500);
        }
    }

    public IWorkTogetherOnContextMenu workTogetherOn() throws RemoteException {
        WorkTogetherOnContextMenu.getInstance().setTree(tree);
        WorkTogetherOnContextMenu.getInstance().setTreeItem(treeItem);
        return WorkTogetherOnContextMenu.getInstance();
    }
}
