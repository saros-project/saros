package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus.ISarosM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus.IWindowM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus.SarosM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus.WindowM;

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