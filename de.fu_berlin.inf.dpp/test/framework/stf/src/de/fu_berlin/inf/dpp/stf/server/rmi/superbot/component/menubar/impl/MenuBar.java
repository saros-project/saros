package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.Component;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.IMenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.ISarosM;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.IWindowM;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl.SarosM;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl.WindowM;

public class MenuBar extends Component implements IMenuBar {

    private static transient MenuBar self;

    private static SarosM sarosM;
    private static WindowM windowM;

    /**
     * {@link MenuBar} is a singleton, but inheritance is possible.
     */
    public static MenuBar getInstance() {
        if (self != null)
            return self;
        self = new MenuBar();
        sarosM = SarosM.getInstance();
        windowM = WindowM.getInstance();
        return self;
    }

    public ISarosM saros() throws RemoteException {
        return sarosM.setMenu(remoteBot().menu(MENU_SAROS));
    }

    public IWindowM window() throws RemoteException {
        return windowM;
    }

}