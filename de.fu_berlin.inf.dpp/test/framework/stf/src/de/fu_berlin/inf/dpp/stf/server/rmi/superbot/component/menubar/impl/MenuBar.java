package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.IMenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.ISarosM;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.IWindowM;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl.SarosM;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl.WindowM;

public final class MenuBar extends StfRemoteObject implements IMenuBar {

    private static final MenuBar INSTANCE = new MenuBar();

    public static MenuBar getInstance() {
        return INSTANCE;
    }

    public ISarosM saros() throws RemoteException {
        return SarosM.getInstance().setMenu(
            RemoteWorkbenchBot.getInstance().menu(MENU_SAROS));
    }

    public IWindowM window() throws RemoteException {
        return WindowM.getInstance();
    }

}