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
        shell(SHELL_INCOMING_SCREENSHARING_SESSION).confirmShell(
            SHELL_INCOMING_SCREENSHARING_SESSION, YesOrNot);
    }

    /**********************************************
     * 
     * Waits until
     * 
     **********************************************/
    public void waitUntilRemoteScreenViewIsActive() throws RemoteException {
        view(VIEW_REMOTE_SCREEN).waitUntilIsActive();
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

    private void preCondition() throws RemoteException {
        view(VIEW_REMOTE_SCREEN).openById();
        view(VIEW_REMOTE_SCREEN).setViewTitle(VIEW_REMOTE_SCREEN);
        view(VIEW_REMOTE_SCREEN).setFocus();

    }
}
