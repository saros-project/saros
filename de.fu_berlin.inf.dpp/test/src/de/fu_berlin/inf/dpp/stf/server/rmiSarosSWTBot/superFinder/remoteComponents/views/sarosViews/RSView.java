package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class RSView extends Component implements IRSView {

    private static transient RSView self;
    private IRemoteBotView view;

    /**
     * {@link RSView} is a singleton, but inheritance is possible.
     */
    public static RSView getInstance() {
        if (self != null)
            return self;
        self = new RSView();
        return self;
    }

    public IRSView setView(IRemoteBotView view) {
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
