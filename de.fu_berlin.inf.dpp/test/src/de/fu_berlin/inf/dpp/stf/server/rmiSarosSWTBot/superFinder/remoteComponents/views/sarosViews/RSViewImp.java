package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class RSViewImp extends Component implements RSView {

    private static transient RSViewImp self;
    private STFBotView view;

    /**
     * {@link ChatViewImp} is a singleton, but inheritance is possible.
     */
    public static RSViewImp getInstance() {
        if (self != null)
            return self;
        self = new RSViewImp();
        return self;
    }

    public RSView setView(STFBotView view) {
        this.view = view;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void clickTBChangeModeOfImageSource() throws RemoteException {
        view.toolbarButton(TB_CHANGE_MODE_IMAGE_SOURCE).click();
    }

    public void clickTBStopRunningSession() throws RemoteException {
        view.toolbarButton(TB_STOP_RUNNING_SESSION).click();
    }

    public void clickTBResume() throws RemoteException {
        view.toolbarButton(TB_RESUME).click();
    }

    public void clickTBPause() throws RemoteException {
        view.toolbarButton(TB_PAUSE).click();
    }

}
