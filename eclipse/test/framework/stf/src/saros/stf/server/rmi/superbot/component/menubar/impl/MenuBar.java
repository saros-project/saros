package saros.stf.server.rmi.superbot.component.menubar.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.SWTBot;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.superbot.component.menubar.IMenuBar;
import saros.stf.server.rmi.superbot.component.menubar.menu.ISarosMenu;
import saros.stf.server.rmi.superbot.component.menubar.menu.IWindowMenu;
import saros.stf.server.rmi.superbot.component.menubar.menu.impl.SarosMenu;
import saros.stf.server.rmi.superbot.component.menubar.menu.impl.WindowMenu;

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
