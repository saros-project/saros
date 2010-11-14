package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.STFController;

public class ExRemoteScreenViewObjectImp extends EclipseObject implements
    ExRemoteScreenViewObject {
    // public static RemoteScreenViewObjectImp classVariable;

    private static transient ExRemoteScreenViewObjectImp self;

    /**
     * {@link ExChatViewObjectImp} is a singleton, but inheritance is possible.
     */
    public static ExRemoteScreenViewObjectImp getInstance() {
        if (self != null)
            return self;
        self = new ExRemoteScreenViewObjectImp();
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
        viewO
            .setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    public boolean isRemoteScreenViewOpen() throws RemoteException {
        return viewO.isViewOpen(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    public void openRemoteScreenView() throws RemoteException {
        if (!isRemoteScreenViewOpen())
            viewO
                .openViewById("de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView");
    }

    public void closeRemoteScreenView() throws RemoteException {
        viewO
            .closeViewById("de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView");
    }

    public void changeModeOfImageSource() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewO.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_CHANGE_MODE_IMAGE_SOURCE);
    }

    public void stopRunningSession() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewO.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_STOP_RUNNING_SESSION);
    }

    public void resume() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewO.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_RESUME);
    }

    public void pause() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewO.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_PAUSE);
    }
}
