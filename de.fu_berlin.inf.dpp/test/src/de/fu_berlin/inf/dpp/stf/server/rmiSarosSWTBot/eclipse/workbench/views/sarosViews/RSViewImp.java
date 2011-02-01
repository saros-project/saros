package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

public class RSViewImp extends EclipsePart implements RSView {
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

    /**
     * constructs a remoteScreenViewObject, which would only be created in class
     * {@links StartupSaros} and then exported by {@link STFController} on our
     * local RMI Registry.
     * 
     * @param rmiBot
     *            controls Saros from the GUI perspective and manage all
     *            exported rmi-objects.
     */

    public void activateRemoteScreenView() throws RemoteException {
        viewW.setFocusOnViewByTitle(VIEW_REMOTE_SCREEN);
    }

    public boolean isRemoteScreenViewOpen() throws RemoteException {
        return viewW.isViewOpen(VIEW_REMOTE_SCREEN);
    }

    public boolean isRemoteScreenViewActive() throws RemoteException {
        return viewW.isViewActive(VIEW_REMOTE_SCREEN);
    }

    public void openRemoteScreenView() throws RemoteException {
        if (!isRemoteScreenViewOpen())
            viewW.openViewById(VIEW_REMOTE_SCREEN_ID);
    }

    public void closeRemoteScreenView() throws RemoteException {
        viewW.closeViewById(VIEW_REMOTE_SCREEN_ID);
    }

    public void clickTBChangeModeOfImageSource() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipInView(
            VIEW_REMOTE_SCREEN, TB_CHANGE_MODE_IMAGE_SOURCE);
    }

    public void clickTBStopRunningSession() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipInView(
            VIEW_REMOTE_SCREEN, TB_STOP_RUNNING_SESSION);
    }

    public void clickTBResume() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipInView(
            VIEW_REMOTE_SCREEN, TB_RESUME);
    }

    public void clickTBPause() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipInView(
            VIEW_REMOTE_SCREEN, TB_PAUSE);
    }

    public void waitUntilRemoteScreenViewIsActive() throws RemoteException {
        viewW.waitUntilIsViewActive(VIEW_REMOTE_SCREEN);
    }

    public void confirmShellIncomingScreensharingSession(String YesOrNot)
        throws RemoteException {
        shellC.confirmShell(SHELL_INCOMING_SCREENSHARING_SESSION, YesOrNot);
    }

}
