package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

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
        return sarosM.setMenu(bot().menu(MENU_SAROS));
    }

    public IWindowM window() throws RemoteException {
        return windowM;
    }

}