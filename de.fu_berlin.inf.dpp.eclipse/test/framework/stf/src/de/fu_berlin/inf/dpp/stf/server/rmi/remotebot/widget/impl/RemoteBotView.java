package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarDropDownButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarPushButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarRadioButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarToggleButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotViewMenu;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarPushButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarRadioButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarToggleButton;

public final class RemoteBotView extends StfRemoteObject implements IRemoteBotView {

  private static final RemoteBotView INSTANCE = new RemoteBotView();

  private SWTBotView widget;

  public static RemoteBotView getInstance() {
    return INSTANCE;
  }

  public IRemoteBotView setWidget(SWTBotView view) {
    widget = view;
    return this;
  }

  @Override
  public IRemoteBot bot() {
    RemoteWorkbenchBot.getInstance().setBot(widget.bot());
    return RemoteWorkbenchBot.getInstance();
  }

  // menu
  @Override
  public IRemoteBotViewMenu menu(String label) throws RemoteException {
    return RemoteBotViewMenu.getInstance().setWidget(widget.menu(label));
  }

  @Override
  public IRemoteBotViewMenu menu(String label, int index) throws RemoteException {
    return RemoteBotViewMenu.getInstance().setWidget(widget.menu(label, index));
  }

  // toolbarButton
  @Override
  public IRemoteBotToolbarButton toolbarButton(String tooltip) throws RemoteException {
    SWTBotToolbarButton toolbarButton = widget.toolbarButton(tooltip);
    return RemoteBotToolbarButton.getInstance().setWidget(toolbarButton);
  }

  @Override
  public IRemoteBotToolbarButton toolbarButtonWithRegex(String regex) throws RemoteException {
    for (String tooltip : getToolTipTextOfToolbarButtons()) {
      if (tooltip.matches(regex)) {
        SWTBotToolbarButton toolbarButton = widget.toolbarButton(tooltip);
        return RemoteBotToolbarButton.getInstance().setWidget(toolbarButton);
      }
    }
    throw new WidgetNotFoundException("unable to find toolbar button with regex: " + regex);
  }

  // toolbarDropDownButton
  @Override
  public IRemoteBotToolbarDropDownButton toolbarDropDownButton(String tooltip)
      throws RemoteException {
    SWTBotToolbarDropDownButton toolbarButton = widget.toolbarDropDownButton(tooltip);
    return RemoteBotToolbarDropDownButton.getInstance().setWidget(toolbarButton);
  }

  // toolbarRadioButton
  @Override
  public IRemoteBotToolbarRadioButton toolbarRadioButton(String tooltip) throws RemoteException {
    SWTBotToolbarRadioButton toolbarButton = widget.toolbarRadioButton(tooltip);
    return RemoteBotToolbarRadioButton.getInstance().setWidget(toolbarButton);
  }

  // toolbarPushButton
  @Override
  public IRemoteBotToolbarPushButton toolbarPushButton(String tooltip) throws RemoteException {
    SWTBotToolbarPushButton toolbarButton = widget.toolbarPushButton(tooltip);
    return RemoteBotToolbarPushButton.getInstance().setWidget(toolbarButton);
  }

  // toolbarToggleButton
  @Override
  public IRemoteBotToolbarToggleButton toolbarToggleButton(String tooltip) throws RemoteException {
    SWTBotToolbarToggleButton toolbarButton = widget.toolbarToggleButton(tooltip);
    return RemoteBotToolbarToggleButton.getInstance().setWidget(toolbarButton);
  }

  @Override
  public void close() throws RemoteException {
    widget.close();
  }

  @Override
  public void show() throws RemoteException {
    widget.show();
  }

  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  @Override
  public List<String> getToolTipOfAllToolbarbuttons() throws RemoteException {
    List<String> tooltips = new ArrayList<String>();
    for (SWTBotToolbarButton button : widget.getToolbarButtons()) {
      tooltips.add(button.getToolTipText());
    }
    return tooltips;
  }

  @Override
  public boolean existsToolbarButton(String tooltip) throws RemoteException {
    return getToolTipOfAllToolbarbuttons().contains(tooltip);
  }

  @Override
  public boolean isActive() throws RemoteException {
    return widget.isActive();
  }

  @Override
  public String getTitle() throws RemoteException {
    return widget.getTitle();
  }

  @Override
  public List<String> getToolTipTextOfToolbarButtons() throws RemoteException {
    List<String> toolbarButtons = new ArrayList<String>();
    for (SWTBotToolbarButton toolbarButton : widget.getToolbarButtons()) {
      toolbarButtons.add(toolbarButton.getToolTipText());
    }
    return toolbarButtons;
  }

  @Override
  public void waitUntilIsActive() throws RemoteException {

    RemoteWorkbenchBot.getInstance()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return isActive();
              }

              @Override
              public String getFailureMessage() {
                return "unable to activate the view: " + widget.getTitle();
              }
            });
  }
}
