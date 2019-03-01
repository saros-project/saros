package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.Map;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.StringResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.bot.condition.SarosConditions;
import saros.stf.server.rmi.remotebot.IRemoteBot;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotShell;

public final class RemoteBotShell extends StfRemoteObject implements IRemoteBotShell {

  private static final RemoteBotShell INSTANCE = new RemoteBotShell();

  private static final String TEXT_FIELD_TYPE_FILTER_TEXT = "type filter text";

  private SWTBotShell widget;

  public static RemoteBotShell getInstance() {
    return INSTANCE;
  }

  public IRemoteBotShell setWidget(SWTBotShell shell) {
    this.widget = shell;
    return this;
  }

  @Override
  public IRemoteBot bot() {
    RemoteWorkbenchBot.getInstance().setBot(widget.bot());
    return RemoteWorkbenchBot.getInstance();
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  @Override
  public boolean activate() throws RemoteException {
    widget.activate();
    return true;
  }

  @Override
  public void close() throws RemoteException {
    widget.close();
  }

  @Override
  public void confirm() throws RemoteException {
    activate();
    bot().button().click();
  }

  @Override
  public void confirm(String buttonText) throws RemoteException {
    activate();
    bot().button(buttonText).click();
  }

  @Override
  public void confirmWithTree(String buttonText, String... nodes) throws RemoteException {
    activate();
    bot().tree().selectTreeItem(nodes);
    bot().button(buttonText).waitUntilIsEnabled();
    bot().button(buttonText).click();
  }

  @Override
  public void confirmWithTextField(String textLabel, String text, String buttonText)
      throws RemoteException {
    activate();
    bot().textWithLabel(textLabel).setText(text);

    bot().button(buttonText).waitUntilIsEnabled();
    bot().button(buttonText).click();
  }

  @Override
  public void confirmWithTextFieldAndWait(Map<String, String> labelsAndTexts, String buttonText)
      throws RemoteException {
    activate();

    for (Map.Entry<String, String> entry : labelsAndTexts.entrySet())
      bot().textWithLabel(/* label */ entry.getKey()).setText(/* text */ entry.getValue());

    bot().button(buttonText).waitUntilIsEnabled();
    bot().button(buttonText).click();
  }

  @Override
  public void confirmWithTreeWithWaitingExpand(String buttonText, String... nodes)
      throws RemoteException {
    bot().tree().selectTreeItemAndWait(nodes);
    bot().button(buttonText).click();
  }

  @Override
  public void confirmWithCheckBox(String buttonText, boolean isChecked) throws RemoteException {
    activate();
    if (isChecked) bot().checkBox().click();
    bot().button(buttonText).click();
  }

  @Override
  public void confirmWithCheckBoxs(String buttonText, String... itemNames) throws RemoteException {
    waitUntilActive();
    for (String itemName : itemNames) {
      bot().tree().selectTreeItem(itemName).check();
    }
    bot().button(buttonText).waitUntilIsEnabled();
    bot().button(buttonText).click();
  }

  @Override
  public void confirmWithTable(String itemName, String buttonText) throws RemoteException {
    waitUntilActive();

    bot().table().select(itemName);
    bot().button(buttonText).waitUntilIsEnabled();
    bot().button(buttonText).click();
  }

  @Override
  public void confirmWithTreeWithFilterText(
      String rootOfTreeNode, String treeNode, String buttonText) throws RemoteException {
    waitUntilActive();
    bot().text(TEXT_FIELD_TYPE_FILTER_TEXT).setText(treeNode);
    bot().tree().waitUntilItemExists(rootOfTreeNode);
    bot().tree().selectTreeItem(rootOfTreeNode, treeNode);
    bot().button(buttonText).waitUntilIsEnabled();
    bot().button(buttonText).click();
  }

  @Override
  public boolean isActive() throws RemoteException {
    return widget.isActive();
  }

  @Override
  public boolean isEnabled() throws RemoteException {
    return widget.isEnabled();
  }

  @Override
  public boolean isVisible() throws RemoteException {
    return widget.isVisible();
  }

  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public String getToolTipText() throws RemoteException {
    return widget.getToolTipText();
  }

  @Override
  public String getErrorMessage() throws RemoteException {
    activate();

    String errorMessage =
        UIThreadRunnable.syncExec(
            new StringResult() {
              @Override
              public String run() {
                WizardDialog dialog = (WizardDialog) widget.widget.getData();
                return dialog.getErrorMessage();
              }
            });

    if (errorMessage != null) return errorMessage;

    /*
     * public void setMessage(String newMessage, int newType)
     *
     * Note that for backward compatibility, a message of type ERROR is
     * different than an error message (set using setErrorMessage). An error
     * message overrides the current message until the error message is
     * cleared. This method replaces the current message and does not affect
     * the error message.
     */

    errorMessage =
        UIThreadRunnable.syncExec(
            new StringResult() {
              @Override
              public String run() {
                WizardDialog dialog = (WizardDialog) widget.widget.getData();
                return dialog.getMessage();
              }
            });

    if (errorMessage == null)
      throw new WidgetNotFoundException(
          "current wizard dialog does not display any message at all");

    return errorMessage;
  }

  @Override
  public String getMessage() throws RemoteException {
    activate();
    final String message =
        UIThreadRunnable.syncExec(
            new StringResult() {
              @Override
              public String run() {
                WizardDialog dialog = (WizardDialog) widget.widget.getData();
                return dialog.getMessage();
              }
            });
    if (message == null) {
      throw new WidgetNotFoundException("could not find message!");
    }
    return message;
  }

  @Override
  public boolean existsTableItem(String label) throws RemoteException {
    activate();
    return bot().table().containsItem(label);
  }

  @Override
  public void waitUntilActive() throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitUntil(SarosConditions.isShellActive(widget));
  }

  @Override
  public void waitShortUntilIsClosed() throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitShortUntil(SarosConditions.isShellClosed(widget));
  }

  @Override
  public void waitLongUntilIsClosed() throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitLongUntil(SarosConditions.isShellClosed(widget));
  }
}
