package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.STFController;

public class RSViewComponentImp extends EclipseComponent implements
    RSViewComponent {
    // public static RemoteScreenViewObjectImp classVariable;

    private static transient RSViewComponentImp self;

    // View infos
    private final static String VIEWNAME = SarosConstant.VIEW_TITLE_REMOTE_SCREEN;
    private final static String VIEWID = SarosConstant.ID_REMOTE_SCREEN_VIEW;

    /*
     * title of shells which are pop up by performing the actions on the session
     * view.
     */
    private final static String CONFIRMLEAVINGSESSION = SarosConstant.SHELL_TITLE_CONFIRM_LEAVING_SESSION;
    private final static String CONFIRMCLOSINGSESSION = "Confirm Closing Session";
    private final static String INCOMINGSCREENSHARINGSESSION = "Incoming screensharing session";

    /*
     * Tool tip text of toolbar buttons on the session view
     */
    private final static String SHARESCREENWITHUSER = SarosConstant.TOOL_TIP_TEXT_SHARE_SCREEN_WITH_USER;
    private final static String STOPSESSIONWITHUSER = "Stop session with user";
    private final static String SENDAFILETOSELECTEDUSER = SarosConstant.TOOL_TIP_TEXT_SEND_FILE_TO_SELECTED_USER;
    private final static String STARTVOIPSESSION = SarosConstant.TOOL_TIP_TEXT_START_VOIP_SESSION;
    private final static String NOINCONSISTENCIES = SarosConstant.TOOL_TIP_TEXT_INCONSISTENCY_DETECTED;
    private final static String OPENINVITATIONINTERFACE = SarosConstant.TOOL_TIP_TEXT_OPEN_INVITATION_INTERFACE;
    private final static String REMOVEALLDRIVERROLES = SarosConstant.TOOL_TIP_TEXT_REMOVE_ALL_DRIVER_ROLES;
    private final static String ENABLEDISABLEFOLLOWMODE = SarosConstant.TOOL_TIP_TEXT_ENABLE_DISABLE_FOLLOW_MODE;
    private final static String LEAVETHESESSION = SarosConstant.TOOL_TIP_TEXT_LEAVE_THE_SESSION;

    // Context menu of the table on the view
    private final static String GIVEEXCLUSIVEDRIVERROLE = SarosConstant.CONTEXT_MENU_GIVE_EXCLUSIVE_DRIVER_ROLE;
    private final static String GIVEDRIVERROLE = SarosConstant.CONTEXT_MENU_GIVE_DRIVER_ROLE;
    private final static String REMOVEDRIVERROLE = SarosConstant.CONTEXT_MENU_REMOVE_DRIVER_ROLE;
    private final static String FOLLOWTHISUSER = SarosConstant.CONTEXT_MENU_FOLLOW_THIS_USER;
    private final static String STOPFOLLOWINGTHISUSER = SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER;
    private final static String JUMPTOPOSITIONSELECTEDUSER = SarosConstant.CONTEXT_MENU_JUMP_TO_POSITION_SELECTED_USER;
    private final static String CHANGECOLOR = "Change Color";

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
        viewPart.setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    public boolean isRemoteScreenViewOpen() throws RemoteException {
        return viewPart.isViewOpen(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    public boolean isRemoteScreenViewActive() throws RemoteException {
        return viewPart.isViewActive(VIEWNAME);
    }

    public void openRemoteScreenView() throws RemoteException {
        if (!isRemoteScreenViewOpen())
            viewPart
                .openViewById("de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView");
    }

    public void closeRemoteScreenView() throws RemoteException {
        viewPart
            .closeViewById("de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView");
    }

    public void changeModeOfImageSource() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewPart.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_CHANGE_MODE_IMAGE_SOURCE);
    }

    public void stopRunningSession() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewPart.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_STOP_RUNNING_SESSION);
    }

    public void resume() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewPart.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_RESUME);
    }

    public void pause() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewPart.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_PAUSE);
    }

    public void waitUntilRemoteScreenViewIsActive() throws RemoteException {
        viewPart.waitUntilViewActive(VIEWNAME);
    }

    public void IncomingScreensharingSession(String YesOrNot)
        throws RemoteException {
        windowPart.confirmWindow(
            SarosConstant.SHELL_TITLE_INCOMING_SCREENSHARING_SESSION, YesOrNot);
    }

    @Override
    protected void precondition() throws RemoteException {
        // TODO Auto-generated method stub

    }
}
