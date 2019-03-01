package saros.stf.server.rmi.superbot.component.contextmenu.sarosview.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import saros.net.xmpp.JID;
import saros.stf.server.bot.widget.ContextMenuHelper;
import saros.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInContactListArea;
import saros.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.IWorkTogetherOnContextMenu;
import saros.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.impl.WorkTogetherOnContextMenu;
import saros.stf.server.rmi.superbot.impl.SuperBot;

public final class ContextMenusInContactListArea extends ContextMenusInSarosView
    implements IContextMenusInContactListArea {

  private static final ContextMenusInContactListArea INSTANCE = new ContextMenusInContactListArea();

  public static ContextMenusInContactListArea getInstance() {
    return INSTANCE;
  }

  @Override
  public void delete() throws RemoteException {
    getTreeItem().select();
    ContextMenuHelper.clickContextMenu(tree, CM_DELETE);
    SWTBotShell shell = new SWTBot().shell(CONFIRM_DELETE);
    shell.activate();
    shell.bot().button(YES).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell));
    // wait for tree update in saros session view
    new SWTBot().sleep(500);
  }

  @Override
  public void rename(String nickname) throws RemoteException {
    getTreeItem().select();
    ContextMenuHelper.clickContextMenu(tree, CM_RENAME);

    SWTBotShell shell = new SWTBot().shell(SHELL_SET_NEW_NICKNAME);
    shell.activate();
    shell.bot().text().setText(nickname);
    shell.bot().button(OK).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell));
    // wait for tree update in saros session view
    new SWTBot().sleep(500);
  }

  @Override
  public void addToSarosSession() throws RemoteException {
    SWTBotTreeItem treeItem = getTreeItem();

    if (!treeItem.isEnabled()) {
      throw new RuntimeException("unable to invite this user, he is not conntected");
    }
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, CM_ADD_TO_SAROS_SESSION);
    // wait for tree update in saros session view
    new SWTBot().sleep(500);
  }

  @Override
  public void addContact(JID jid) throws RemoteException {
    if (!sarosView.isInContactList(jid)) {
      getTreeItem().select();
      ContextMenuHelper.clickContextMenu(tree, CM_ADD_CONTACT);
      SuperBot.getInstance().confirmShellAddContact(jid);
      // wait for tree update in saros session view
      new SWTBot().sleep(500);
    }
  }

  @Override
  public IWorkTogetherOnContextMenu workTogetherOn() throws RemoteException {
    WorkTogetherOnContextMenu.getInstance().setTree(tree);
    WorkTogetherOnContextMenu.getInstance().setTreeItem(getTreeItem());
    return WorkTogetherOnContextMenu.getInstance();
  }
}
