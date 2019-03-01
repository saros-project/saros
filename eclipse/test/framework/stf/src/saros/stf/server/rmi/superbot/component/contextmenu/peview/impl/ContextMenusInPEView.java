package saros.stf.server.rmi.superbot.component.contextmenu.peview.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.bot.SarosSWTBotPreferences;
import saros.stf.server.bot.widget.ContextMenuHelper;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.IContextMenusInPEView;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.INewC;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IRefactorC;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IRunAsContextMenu;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IShareWithC;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.NewC;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.RefactorC;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.RunAsContextMenu;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.ShareWithC;

public final class ContextMenusInPEView extends StfRemoteObject implements IContextMenusInPEView {

  private static final Logger log = Logger.getLogger(ContextMenusInPEView.class);
  private static final ContextMenusInPEView INSTANCE = new ContextMenusInPEView();

  private SWTBotTreeItem treeItem;
  private SWTBotTree tree;
  private TreeItemType type;

  public static ContextMenusInPEView getInstance() {
    return INSTANCE;
  }

  public void setTreeItem(SWTBotTreeItem treeItem) {
    this.treeItem = treeItem;
  }

  public void setTreeItemType(TreeItemType type) {
    this.type = type;
  }

  public void setTree(SWTBotTree tree) {
    this.tree = tree;
  }

  @Override
  public IShareWithC shareWith() throws RemoteException {
    ShareWithC.getInstance().setTree(tree);
    ShareWithC.getInstance().setTreeItem(treeItem);
    return ShareWithC.getInstance();
  }

  @Override
  public INewC newC() throws RemoteException {
    NewC.getInstance().setTree(tree);
    return NewC.getInstance();
  }

  @Override
  public IRunAsContextMenu runAs() throws RemoteException {
    RunAsContextMenu.getInstance().setTree(tree);
    RunAsContextMenu.getInstance().setTreeItem(treeItem);
    return RunAsContextMenu.getInstance();
  }

  @Override
  public IRefactorC refactor() throws RemoteException {
    RefactorC.getInstance().setTree(tree);
    RefactorC.getInstance().setTreeItem(treeItem);
    RefactorC.getInstance().setTreeItemType(type);
    return RefactorC.getInstance();
  }

  @Override
  public void open() throws RemoteException {
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, CM_OPEN);
  }

  @Override
  public void copy() throws RemoteException {
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, MENU_COPY);
  }

  @Override
  public void refresh() throws RemoteException {
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, MENU_REFRESH);
  }

  @Override
  public void paste(String target) throws RemoteException {
    ContextMenuHelper.clickContextMenu(tree, MENU_PASTE);
    SWTBotShell shell = new SWTBot().shell(SHELL_COPY_PROJECT);

    shell.activate();
    shell.bot().textWithLabel("Project name:").setText(target);
    shell.bot().button(OK).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell), SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);

    new SWTBot().sleep(1000);
    new SWTBot()
        .waitWhile(
            Conditions.shellIsActive("Progess Information"),
            SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
  }

  @Override
  public void openWith(String editorType) throws RemoteException {
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, CM_OPEN_WITH, CM_OTHER);

    SWTBotShell shell = new SWTBot().shell(SHELL_EDITOR_SELECTION);

    shell.activate();
    shell.bot().table().getTableItem(editorType).select();
    shell.bot().button(OK).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void delete() throws RemoteException {
    treeItem.select();
    ContextMenuHelper.clickContextMenu(tree, CM_DELETE);

    SWTBotShell shell;

    switch (type) {
      case PROJECT:
      case JAVA_PROJECT:
        shell = new SWTBot().shell(SHELL_DELETE_RESOURCE);
        shell.activate();
        if (!shell.bot().checkBox().isChecked()) shell.bot().checkBox().click();

        shell.bot().button(OK).click();

        break;
      default:
        shell = new SWTBot().shell(DELETE);
        shell.activate();
        shell.bot().button(OK).click();
        break;
    }

    shell.bot().waitUntil(Conditions.shellCloses(shell));
    shell
        .bot()
        .waitWhile(
            new DefaultCondition() {

              @Override
              public boolean test() throws Exception {

                for (SWTBotTreeItem item : tree.getAllItems())
                  if (item.getText().equals(treeItem.getText())) return true;

                return false;
              }

              @Override
              public String getFailureMessage() {
                // TODO Auto-generated method stub
                return "tree item '"
                    + treeItem.getText()
                    + "' still exists in tree '"
                    + tree.getText()
                    + "'";
              }
            });
  }

  @Override
  public boolean existsWithRegex(String regex) throws RemoteException {
    try {
      List<String> items;

      if (treeItem == null) items = getTextOfItems(tree);
      else items = getTextOfItems(treeItem);

      for (String item : items) if (item.matches(regex)) return true;

      return false;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean exists(String name) throws RemoteException {
    try {
      if (treeItem == null) return getTextOfItems(tree).contains(name);
      else return getTextOfItems(treeItem).contains(name);

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  private List<String> getTextOfItems(SWTBotTreeItem treeItem) {
    List<String> allItemTexts = new ArrayList<String>();
    for (SWTBotTreeItem item : treeItem.getItems()) {
      allItemTexts.add(item.getText());
    }
    return allItemTexts;
  }

  private List<String> getTextOfItems(SWTBotTree tree) {
    List<String> allItemTexts = new ArrayList<String>();
    for (SWTBotTreeItem item : tree.getAllItems()) {
      allItemTexts.add(item.getText());
    }
    return allItemTexts;
  }
}
