package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.impl;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.IWorkTogetherOnContextMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public final class WorkTogetherOnContextMenu extends StfRemoteObject
    implements IWorkTogetherOnContextMenu {

  private static final WorkTogetherOnContextMenu INSTANCE = new WorkTogetherOnContextMenu();

  private SWTBotTreeItem treeItem;
  private SWTBotTree tree;

  public static WorkTogetherOnContextMenu getInstance() {
    return INSTANCE;
  }

  public void setTree(SWTBotTree tree) {
    this.tree = tree;
  }

  public void setTreeItem(SWTBotTreeItem treeItem) {
    this.treeItem = treeItem;
  }

  @Override
  public void multipleProjects(String projectName, JID... baseJIDOfInvitees)
      throws RemoteException {
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, CM_WORK_TOGETHER_ON, CM_MULTIPLE_PROJECTS);
    SuperBot.getInstance().confirmShellShareProjects(projectName, baseJIDOfInvitees);
  }

  @Override
  public void project(String projectName) throws RemoteException {
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, CM_WORK_TOGETHER_ON, projectName);
  }
}
