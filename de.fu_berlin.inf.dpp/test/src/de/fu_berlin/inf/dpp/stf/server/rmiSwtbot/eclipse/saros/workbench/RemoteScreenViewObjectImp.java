package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.STFController;

public class RemoteScreenViewObjectImp extends EclipseObject implements
    RemoteScreenViewObject {
    // public static RemoteScreenViewObjectImp classVariable;

    private static transient RemoteScreenViewObjectImp self;

    /**
     * {@link ChatViewObjectImp} is a singleton, but inheritance is possible.
     */
    public static RemoteScreenViewObjectImp getInstance() {
        if (self != null)
            return self;
        self = new RemoteScreenViewObjectImp();
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
        viewObject
            .setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    public boolean isRemoteScreenViewOpen() throws RemoteException {
        return viewObject.isViewOpen(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    public void openRemoteScreenView() throws RemoteException {
        if (!isRemoteScreenViewOpen())
            viewObject
                .openViewById("de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView");
    }

    public void closeRemoteScreenView() throws RemoteException {
        viewObject
            .closeViewById("de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView");
    }

    public void changeModeOfImageSource() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_CHANGE_MODE_IMAGE_SOURCE);
    }

    public void stopRunningSession() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_STOP_RUNNING_SESSION);
    }

    public void resume() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_RESUME);
    }

    public void pause() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_PAUSE);
    }
}
