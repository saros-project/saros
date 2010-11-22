package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.STFController;

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
        viewPart.setFocusOnViewByTitle(VIEWNAME);
    }

    public boolean isRemoteScreenViewOpen() throws RemoteException {
        return viewPart.isViewOpen(VIEWNAME);
    }

    public boolean isRemoteScreenViewActive() throws RemoteException {
        return viewPart.isViewActive(VIEWNAME);
    }

    public void openRemoteScreenView() throws RemoteException {
        if (!isRemoteScreenViewOpen())
            viewPart.openViewById(VIEWID);
    }

    public void closeRemoteScreenView() throws RemoteException {
        viewPart.closeViewById(VIEWID);
    }

    public void changeModeOfImageSource() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewPart.clickToolbarButtonWithTooltipInView(VIEWNAME,
            TB_CHANGE_MODE_IMAGE_SOURCE);
    }

    public void stopRunningSession() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewPart.clickToolbarButtonWithTooltipInView(VIEWNAME,
            TB_STOP_RUNNING_SESSION);
    }

    public void resume() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewPart.clickToolbarButtonWithTooltipInView(VIEWNAME, TB_RESUME);
    }

    public void pause() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewPart.clickToolbarButtonWithTooltipInView(VIEWNAME, TB_PAUSE);
    }

    public void waitUntilRemoteScreenViewIsActive() throws RemoteException {
        viewPart.waitUntilViewActive(VIEWNAME);
    }

    public void IncomingScreensharingSession(String YesOrNot)
        throws RemoteException {
        windowPart
            .confirmWindow(SHELL_INCOMING_SCREENSHARING_SESSION, YesOrNot);
    }

    @Override
    protected void precondition() throws RemoteException {
        // TODO Auto-generated method stub

    }
}
