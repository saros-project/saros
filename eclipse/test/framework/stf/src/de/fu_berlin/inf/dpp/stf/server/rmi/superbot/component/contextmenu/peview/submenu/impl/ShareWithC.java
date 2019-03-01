package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IShareWithC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public final class ShareWithC extends StfRemoteObject implements IShareWithC {

  private static final ShareWithC INSTANCE = new ShareWithC();

  private SWTBotTree tree;
  private SWTBotTreeItem treeItem;

  public static ShareWithC getInstance() {
    return INSTANCE;
  }

  public void setTree(SWTBotTree tree) {
    this.tree = tree;
  }

  public void setTreeItem(SWTBotTreeItem treeItem) {
    this.treeItem = treeItem;
  }

  // FIXME can not click the context menu.
  @Override
  public void multipleContacts(String projectName, JID... baseJIDOfInvitees)
      throws RemoteException {
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, CM_SHARE_WITH, CM_MULTIPLE_CONTACTS);
    SuperBot.getInstance().confirmShellShareProjects(projectName, baseJIDOfInvitees);
  }

  @Override
  public void contact(JID jid) throws RemoteException {
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, CM_SHARE_WITH, jid.getBase());
  }

  @Override
  public void addToSarosSession() throws RemoteException {
    /*
     * The menu is only activated if there are project existed in the
     * package explorer view, which is not in the session.
     */
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, CM_ADD_TO_SAROS_SESSION);
  }

  public void stopToSarosSession() {
    //
  }
}
