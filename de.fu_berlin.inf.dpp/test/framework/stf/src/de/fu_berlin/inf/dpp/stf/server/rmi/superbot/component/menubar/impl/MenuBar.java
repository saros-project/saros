package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.IMenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.ISarosMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.IWindowMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl.SarosMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl.WindowMenu;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.SWTBot;

public final class MenuBar extends StfRemoteObject implements IMenuBar {

  private static final MenuBar INSTANCE = new MenuBar();

  public static MenuBar getInstance() {
    return INSTANCE;
  }

  @Override
  public ISarosMenu saros() throws RemoteException {
    return SarosMenu.getInstance().setMenu(new SWTBot().menu(MENU_SAROS));
  }

  @Override
  public IWindowMenu window() throws RemoteException {
    return WindowMenu.getInstance();
  }
}
