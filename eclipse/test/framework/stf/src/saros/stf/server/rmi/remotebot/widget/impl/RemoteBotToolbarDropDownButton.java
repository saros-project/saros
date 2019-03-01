package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.hamcrest.Matcher;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotToolbarDropDownButton;

public final class RemoteBotToolbarDropDownButton extends StfRemoteObject
    implements IRemoteBotToolbarDropDownButton {

  private static final RemoteBotToolbarDropDownButton INSTANCE =
      new RemoteBotToolbarDropDownButton();

  private SWTBotToolbarDropDownButton widget;

  public static RemoteBotToolbarDropDownButton getInstance() {
    return INSTANCE;
  }

  public IRemoteBotToolbarDropDownButton setWidget(
      SWTBotToolbarDropDownButton toolbarDropDownButton) {
    this.widget = toolbarDropDownButton;
    return this;
  }

  public List<? extends SWTBotMenu> menuItems(Matcher<MenuItem> matcher) {
    return widget.menuItems(matcher);
  }

  @Override
  public IRemoteBotMenu menuItem(String menuItem) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.menuItem(menuItem));
  }

  @Override
  public IRemoteBotMenu menuItem(Matcher<MenuItem> matcher) throws RemoteException {

    return RemoteBotMenu.getInstance().setWidget(widget.menuItem(matcher));
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  @Override
  public void click() throws RemoteException {
    widget.click();
  }

  @Override
  public void clickAndWait() throws RemoteException {
    waitUntilIsEnabled();
    click();
  }

  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  @Override
  public void pressShortcut(KeyStroke... keys) throws RemoteException {
    widget.pressShortcut(keys);
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
  public boolean isActive() throws RemoteException {
    return widget.isActive();
  }

  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public String getToolTipText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public void waitUntilIsEnabled() throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitUntil(Conditions.widgetIsEnabled(widget));
  }
}
