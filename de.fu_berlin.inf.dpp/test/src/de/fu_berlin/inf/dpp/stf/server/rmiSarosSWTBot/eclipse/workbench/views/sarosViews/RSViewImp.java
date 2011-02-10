package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.SarosComponentImp;

public class RSViewImp extends SarosComponentImp implements RSView {
    // public static RemoteScreenViewObjectImp classVariable;

    private static transient RSViewImp self;

    /**
     * {@link ChatViewImp} is a singleton, but inheritance is possible.
     */
    public static RSViewImp getInstance() {
        if (self != null)
            return self;
        self = new RSViewImp();
        return self;
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
        preCondition();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipOnView(
            VIEW_REMOTE_SCREEN, TB_CHANGE_MODE_IMAGE_SOURCE);
    }

    public void clickTBStopRunningSession() throws RemoteException {
        preCondition();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipOnView(
            VIEW_REMOTE_SCREEN, TB_STOP_RUNNING_SESSION);
    }

    public void clickTBResume() throws RemoteException {
        preCondition();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipOnView(
            VIEW_REMOTE_SCREEN, TB_RESUME);
    }

    public void clickTBPause() throws RemoteException {
        preCondition();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipOnView(
            VIEW_REMOTE_SCREEN, TB_PAUSE);
    }

    public void confirmShellIncomingScreensharingSession(String YesOrNot)
        throws RemoteException {
        shellW.confirmShell(SHELL_INCOMING_SCREENSHARING_SESSION, YesOrNot);
    }

    /**********************************************
     * 
     * States
     * 
     **********************************************/
    public boolean isRemoteScreenViewOpen() throws RemoteException {
        return viewW.isViewOpen(VIEW_REMOTE_SCREEN);
    }

    public boolean isRemoteScreenViewActive() throws RemoteException {
        return viewW.isViewActive(VIEW_REMOTE_SCREEN);
    }

    /**********************************************
     * 
     * Waits until
     * 
     **********************************************/
    public void waitUntilRemoteScreenViewIsActive() throws RemoteException {
        viewW.waitUntilIsViewActive(VIEW_REMOTE_SCREEN);
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

    private void preCondition() throws RemoteException {
        viewW.openViewById(VIEW_REMOTE_SCREEN_ID);
        viewW.activateViewByTitle(VIEW_REMOTE_SCREEN);
    }

}
