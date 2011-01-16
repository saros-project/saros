package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.sarosViewComponents;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.STFController;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class RSViewComponentImp extends EclipseComponent implements
    RSViewComponent {
    // public static RemoteScreenViewObjectImp classVariable;

    private static transient RSViewComponentImp self;

    // View infos
    private final static String VIEWNAME = "Remote Screen";
    private final static String VIEWID = "de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView";

    public final static String TB_CHANGE_MODE_IMAGE_SOURCE = "Change mode of image source";
    public final static String SHELL_INCOMING_SCREENSHARING_SESSION = "Incoming screensharing session";
    public final static String TB_STOP_RUNNING_SESSION = "Stop running session";
    public final static String TB_RESUME = "Resume";
    public final static String TB_PAUSE = "Pause";

    /**
     * {@link ChatViewComponentImp} is a singleton, but inheritance is possible.
     */
    public static RSViewComponentImp getInstance() {
        if (self != null)
            return self;
        self = new RSViewComponentImp();
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
        basic.setFocusOnViewByTitle(VIEWNAME);
    }

    public boolean isRemoteScreenViewOpen() throws RemoteException {
        return basic.isViewOpen(VIEWNAME);
    }

    public boolean isRemoteScreenViewActive() throws RemoteException {
        return basic.isViewActive(VIEWNAME);
    }

    public void openRemoteScreenView() throws RemoteException {
        if (!isRemoteScreenViewOpen())
            basic.openViewById(VIEWID);
    }

    public void closeRemoteScreenView() throws RemoteException {
        basic.closeViewById(VIEWID);
    }

    public void clickTBChangeModeOfImageSource() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        basic.clickToolbarButtonWithRegexTooltipInView(VIEWNAME,
            TB_CHANGE_MODE_IMAGE_SOURCE);
    }

    public void clickTBStopRunningSession() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        basic.clickToolbarButtonWithRegexTooltipInView(VIEWNAME,
            TB_STOP_RUNNING_SESSION);
    }

    public void clickTBResume() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        basic.clickToolbarButtonWithRegexTooltipInView(VIEWNAME, TB_RESUME);
    }

    public void clickTBPause() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        basic.clickToolbarButtonWithRegexTooltipInView(VIEWNAME, TB_PAUSE);
    }

    public void waitUntilRemoteScreenViewIsActive() throws RemoteException {
        basic.waitUntilViewActive(VIEWNAME);
    }

    public void confirmShellIncomingScreensharingSession(String YesOrNot)
        throws RemoteException {
        shellC.confirmShell(SHELL_INCOMING_SCREENSHARING_SESSION, YesOrNot);
    }

}
