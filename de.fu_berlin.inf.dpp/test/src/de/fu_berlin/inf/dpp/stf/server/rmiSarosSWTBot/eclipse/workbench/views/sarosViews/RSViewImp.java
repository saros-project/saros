package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

public class RSViewImp extends EclipsePart implements RSView {
    // public static RemoteScreenViewObjectImp classVariable;

    private static transient RSViewImp self;

    // View infos
    private final static String VIEWNAME = "Remote Screen";
    private final static String VIEWID = "de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView";

    public final static String TB_CHANGE_MODE_IMAGE_SOURCE = "Change mode of image source";
    public final static String SHELL_INCOMING_SCREENSHARING_SESSION = "Incoming screensharing session";
    public final static String TB_STOP_RUNNING_SESSION = "Stop running session";
    public final static String TB_RESUME = "Resume";
    public final static String TB_PAUSE = "Pause";

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
        viewW.setFocusOnViewByTitle(VIEWNAME);
    }

    public boolean isRemoteScreenViewOpen() throws RemoteException {
        return viewW.isViewOpen(VIEWNAME);
    }

    public boolean isRemoteScreenViewActive() throws RemoteException {
        return viewW.isViewActive(VIEWNAME);
    }

    public void openRemoteScreenView() throws RemoteException {
        if (!isRemoteScreenViewOpen())
            viewW.openViewById(VIEWID);
    }

    public void closeRemoteScreenView() throws RemoteException {
        viewW.closeViewById(VIEWID);
    }

    public void clickTBChangeModeOfImageSource() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipInView(VIEWNAME,
            TB_CHANGE_MODE_IMAGE_SOURCE);
    }

    public void clickTBStopRunningSession() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipInView(VIEWNAME,
            TB_STOP_RUNNING_SESSION);
    }

    public void clickTBResume() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipInView(VIEWNAME,
            TB_RESUME);
    }

    public void clickTBPause() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        toolbarButtonW.clickToolbarButtonWithRegexTooltipInView(VIEWNAME,
            TB_PAUSE);
    }

    public void waitUntilRemoteScreenViewIsActive() throws RemoteException {
        viewW.waitUntilIsViewActive(VIEWNAME);
    }

    public void confirmShellIncomingScreensharingSession(String YesOrNot)
        throws RemoteException {
        shellC.confirmShell(SHELL_INCOMING_SCREENSHARING_SESSION, YesOrNot);
    }

}
