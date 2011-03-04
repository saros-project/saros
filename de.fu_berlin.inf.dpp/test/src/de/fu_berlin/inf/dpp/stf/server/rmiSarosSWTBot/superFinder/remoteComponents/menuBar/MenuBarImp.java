package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class MenuBarImp extends Component implements MenuBar {

    private static transient MenuBarImp self;

    private static SarosMImp sarosM;
    private static WindowMImp windowM;

    /**
     * {@link MenuBarImp} is a singleton, but inheritance is possible.
     */
    public static MenuBarImp getInstance() {
        if (self != null)
            return self;
        self = new MenuBarImp();
        sarosM = SarosMImp.getInstance();
        windowM = WindowMImp.getInstance();
        return self;
    }

    public SarosM saros() throws RemoteException {
        return sarosM.setMenu(bot().menu(MENU_SAROS));
    }

    public WindowM window() throws RemoteException {
        return windowM;
    }

}