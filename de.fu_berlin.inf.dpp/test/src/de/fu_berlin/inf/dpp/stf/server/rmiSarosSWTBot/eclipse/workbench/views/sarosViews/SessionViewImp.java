package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

/**
 * This implementation of {@link SessionView}
 * 
 * @author Lin
 */
public class SessionViewImp extends EclipsePart implements
    SessionView {

    private static transient SessionViewImp self;

    /*
     * View infos
     */
    private final static String VIEWNAME = "Shared Project Session";
    private final static String VIEWID = "de.fu_berlin.inf.dpp.ui.SessionView";

    /*
     * title of shells which are pop up by performing the actions on the view.
     */
    private final static String SHELL_CONFIRM_CLOSING_SESSION = "Confirm Closing Session";
    private final static String SHELL_INCOMING_SCREENSHARING_SESSION = "Incoming screensharing session";
    private final static String SHELL_SCREENSHARING_ERROR_OCCURED = "Screensharing: An error occured";
    private final static String SHELL_INVITATION = "Invitation";
    private final static String SHELL_ERROR_IN_SAROS_PLUGIN = "Error in Saros-Plugin";
    private final static String CLOSING_THE_SESSION = "Closing the Session";
    protected final static String CONFIRM_LEAVING_SESSION = "Confirm Leaving Session";

    /*
     * Tool tip text of all the toolbar buttons on the view
     */
    private final static String TB_SHARE_SCREEN_WITH_USER = "Share your screen with selected user";
    private final static String TB_STOP_SESSION_WITH_USER = "Stop session with user";
    private final static String TB_SEND_A_FILE_TO_SELECTED_USER = "Send a file to selected user";
    private final static String TB_START_VOIP_SESSION = "Start a VoIP Session...";
    private final static String TB_INCONSISTENCY_DETECTED = "Inconsistency Detected in";
    private final static String TB_OPEN_INVITATION_INTERFACE = "Open invitation interface";
    private final static String TB_REMOVE_ALL_DRIVER_ROLES = "Remove all driver roles";
    private final static String TB_ENABLE_DISABLE_FOLLOW_MODE = "Enable/Disable follow mode";
    private final static String TB_LEAVE_THE_SESSION = "Leave the session";

    // Context menu's name of the table on the view
    private final static String CM_GIVE_EXCLUSIVE_DRIVER_ROLE = "Give exclusive driver role";
    private final static String CM_GIVE_DRIVER_ROLE = "Give driver role";
    private final static String CM_REMOVE_DRIVER_ROLE = "Remove driver role";
    private final static String CM_FOLLOW_THIS_USER = "Follow this user";
    private final static String CM_STOP_FOLLOWING_THIS_USER = "Stop following this user";
    private final static String CM_JUMP_TO_POSITION_SELECTED_USER = "Jump to position of selected user";
    private final static String CM_CHANGE_COLOR = "Change Color";

    /**
     * {@link SessionViewImp} is a singleton, but inheritance is
     * possible.
     */
    public static SessionViewImp getInstance() {
        if (self != null)
            return self;
        self = new SessionViewImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * open/close/activate the view
     * 
     **********************************************/
    public void openSessionView() throws RemoteException {
        if (!isSessionViewOpen())
            basic.openViewById(VIEWID);
    }

    public boolean isSessionViewOpen() throws RemoteException {
        return basic.isViewOpen(VIEWNAME);
    }

    public void closeSessionView() throws RemoteException {
        if (isSessionViewOpen())
            basic.closeViewById(VIEWID);
    }

    public void setFocusOnSessionView() throws RemoteException {
        basic.setFocusOnViewByTitle(VIEWNAME);
        workbenchC.captureScreenshot(workbenchC.getPathToScreenShot()
            + "/focusOnsessionView.png");
        basic.waitUntilViewActive(VIEWNAME);
    }

    public boolean isSessionViewActive() throws RemoteException {
        return basic.isViewActive(VIEWNAME);
    }

    /**********************************************
     * 
     * is Session open/close?
     * 
     **********************************************/
    public boolean isInSession() {
        log.debug("isInSession() == " + sessionManager.getSarosSession() != null);
        return sessionManager.getSarosSession() != null;
    }

    public boolean isInSessionGUI() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_LEAVE_THE_SESSION);
    }

    public void waitUntilSessionOpen() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isInSession();
            }

            public String getFailureMessage() {
                return "can't open the session.";
            }
        });
    }

    public void waitUntilSessionOpenBy(final SessionView sessionV)
        throws RemoteException {
        sessionV.waitUntilSessionOpen();
    }

    public void waitUntilSessionClosed() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isInSession();
            }

            public String getFailureMessage() {
                return "can't close the session.";
            }
        });
    }

    public void waitUntilSessionClosedBy(SessionView sessionV)
        throws RemoteException {
        sessionV.waitUntilSessionClosed();
    }

    /**********************************************
     * 
     * informations in the session view's field
     * 
     **********************************************/
    public boolean isContactInSessionViewGUI(JID contactJID)
        throws RemoteException {
        precondition();
        String contactLabel = getContactStatusInSessionView(contactJID);
        SWTBotTable table = basic.getTableInView(VIEWNAME);
        for (int i = 0; i < table.rowCount(); i++) {
            if (table.getTableItem(i).getText().equals(contactLabel))
                return true;
        }
        return false;
    }

    public void selectContactInSessionViewGUI(JID contactJID)
        throws RemoteException {
        precondition();
        if (isContactInSessionViewGUI(contactJID)) {
            String contactLabel = getContactStatusInSessionView(contactJID);
            SWTBotTable table = basic.getTableInView(VIEWNAME);
            table.getTableItem(contactLabel).select();
        }
    }

    public String getContactStatusInSessionView(JID contactJID)
        throws RemoteException {
        String contactLabel;
        if (localJID.equals(contactJID)) {
            if (isDriver())
                contactLabel = OWN_CONTACT_NAME + ROLENAME;
            else
                contactLabel = OWN_CONTACT_NAME;
        } else if (rosterV.hasBuddyNickName(contactJID)) {
            if (isDriver(contactJID))
                contactLabel = rosterV.getBuddyNickName(contactJID) + " ("
                    + contactJID.getBase() + ")" + ROLENAME;
            else
                contactLabel = rosterV.getBuddyNickName(contactJID) + " ("
                    + contactJID.getBase() + ")";
        } else {
            if (isDriver(contactJID))
                contactLabel = contactJID.getBase() + ROLENAME;
            else
                contactLabel = contactJID.getBase();
        }
        return contactLabel;
    }

    public boolean existsLabelTextInSessionView() throws RemoteException {
        precondition();
        return basic.existsLabelInView(VIEWNAME);
    }

    public String getFirstLabelTextInSessionview() throws RemoteException {
        if (existsLabelTextInSessionView())
            return basic.getView(VIEWNAME).bot().label().getText();
        return null;
    }

    /**********************************************
     * 
     * context menu of a contact on the view: give/remove driver role
     * 
     **********************************************/
    public void giveDriverRole(final JID jidOfPeer) throws RemoteException {
        // TODO add the correct implementation
    }

    public void giveDriverRoleGUI(final SessionView sessionV)
        throws RemoteException {
        final JID jidOfPeer = sessionV.getJID();
        if (isDriver(jidOfPeer)) {
            throw new RuntimeException(
                "User \""
                    + jidOfPeer.getBase()
                    + "\" is already a driver! Please pass a correct Musician Object to the method.");
        }
        precondition();
        String contactLabel = getContactStatusInSessionView(jidOfPeer);
        basic.clickContextMenuOfTableInView(VIEWNAME, contactLabel,
            CM_GIVE_DRIVER_ROLE);
        sessionV.waitUntilIsDriver();
    }

    public void giveExclusiveDriverRole(final JID jidOfPeer)
        throws RemoteException {
        // TODO add the correct implementation
    }

    public void giveExclusiveDriverRoleGUI(final SessionView sessionV)
        throws RemoteException {
        final JID jidOfPeer = sessionV.getJID();
        if (isDriver(jidOfPeer)) {
            throw new RuntimeException(
                "User \""
                    + jidOfPeer.getBase()
                    + "\" is already a driver! Please pass a correct Musician Object to the method.");
        }
        precondition();
        String contactLabel = getContactStatusInSessionView(jidOfPeer);
        basic.clickContextMenuOfTableInView(VIEWNAME, contactLabel,
            CM_GIVE_EXCLUSIVE_DRIVER_ROLE);
        sessionV.waitUntilIsDriver();
    }

    public void removeDriverRole(final SessionView sessionV)
        throws RemoteException {
        // TODO add the correct implementation
    }

    public void removeDriverRoleGUI(final SessionView sessionV)
        throws RemoteException {
        final JID jidOfPeer = sessionV.getJID();
        if (!sessionV.isDriver()) {
            throw new RuntimeException(
                "User \""
                    + jidOfPeer.getBase()
                    + "\" is  no driver! Please pass a correct Musician Object to the method.");
        }
        precondition();
        String contactLabel = getContactStatusInSessionView(jidOfPeer);
        basic.clickContextMenuOfTableInView(VIEWNAME, contactLabel,
            CM_REMOVE_DRIVER_ROLE);
        sessionV.waitUntilIsNoDriver();
    }

    public void waitUntilIsDriver() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isDriver();
            }

            public String getFailureMessage() {
                return localJID.getBase() + " is not a driver.";
            }
        });
    }

    public void waitUntilIsDriver(final JID jid) throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isDriver(jid);
            }

            public String getFailureMessage() {
                return jid.getBase() + " is not a driver.";
            }
        });
    }

    public void waitUntilIsNoDriver() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isDriver();
            }

            public String getFailureMessage() {
                return localJID.getBase() + " is still a driver.";
            }
        });
    }

    public boolean isDriver() throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        return sarosSession.isDriver();
    }

    public boolean isDriver(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        log.debug("isDriver(" + jid.toString() + ") == "
            + sarosSession.getDrivers().contains(user));
        return sarosSession.getDrivers().contains(user);
    }

    public boolean areDrivers(List<JID> jids) {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                User user = sarosSession.getUser(jid);
                result &= sarosSession.getDrivers().contains(user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isExclusiveDriver() throws RemoteException {
        try {
            ISarosSession sarosSession = sessionManager.getSarosSession();
            if (sarosSession == null)
                return false;
            return sarosSession.isExclusiveDriver();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExclusiveDriver(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        if (isDriver(jid) && sarosSession.getDrivers().size() == 1)
            return true;
        return false;
    }

    public boolean isHost() throws RemoteException {
        return isHost(getJID());
    }

    public boolean isHost(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        final boolean result = user == sarosSession.getHost();
        log.debug("isHost(" + jid.toString() + ") == " + result);
        return result;
    }

    public boolean isObserver() throws RemoteException {
        return isObserver(getJID());
    }

    public boolean isObserver(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        log.debug("isObserver(" + jid.toString() + ") == "
            + sarosSession.getObservers().contains(user));
        return sarosSession.getObservers().contains(user);
    }

    public boolean areObservers(List<JID> jids) {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                User user = sarosSession.getUser(jid);
                result &= sarosSession.getObservers().contains(user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isParticipant() throws RemoteException {
        return isParticipant(getJID());
    }

    public boolean isParticipant(JID jid) throws RemoteException {
        try {
            ISarosSession sarosSession = sessionManager.getSarosSession();
            if (sarosSession == null)
                return false;
            User user = sarosSession.getUser(jid);
            if (user == null)
                return false;
            log.debug("isParticipant(" + jid.toString() + ") == "
                + sarosSession.getParticipants().contains(user));
            return sarosSession.getParticipants().contains(user);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areParticipants(List<JID> jids) throws RemoteException {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                result &= sarosSession.getParticipants().contains(
                    sarosSession.getUser(jid));
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    /**********************************************
     * 
     * context menu of a contact on the view: follow/stop following user
     * 
     **********************************************/
    public void followThisUser(JID jidOfFollowedUser) throws RemoteException {
        // TODO add the implementation.
    }

    public void followThisUserGUI(JID jidOfFollowedUser) throws RemoteException {
        precondition();
        if (isInFollowMode()) {
            log.debug(jidOfFollowedUser.getBase()
                + " is already followed by you.");
            return;
        }
        clickContextMenuOfSelectedUser(
            jidOfFollowedUser,
            CM_FOLLOW_THIS_USER,
            "Hi guy, you can't follow youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    public boolean isInFollowMode() throws RemoteException {
        return editorManager.isFollowing();
    }

    public boolean isInFollowModeGUI() throws RemoteException {
        try {
            precondition();
            SWTBotTable table = basic.getTableInView(VIEWNAME);
            for (int i = 0; i < table.rowCount(); i++) {
                try {
                    return table.getTableItem(i)
                        .contextMenu(CM_STOP_FOLLOWING_THIS_USER).isEnabled();
                } catch (WidgetNotFoundException e) {
                    continue;
                }
            }
        } catch (WidgetNotFoundException e) {
            return false;
        }
        return false;
    }

    public void waitUntilIsFollowingUser(final String baseJIDOfFollowedUser)
        throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isFollowingUser(baseJIDOfFollowedUser);
            }

            public String getFailureMessage() {
                return localJID.getBase() + " is not folloing the user "
                    + baseJIDOfFollowedUser;
            }
        });

    }

    public boolean isFollowingUser(String baseJID) throws RemoteException {
        if (getFollowedUserJID() == null)
            return false;
        else
            return getFollowedUserJID().getBase().equals(baseJID);
    }

    public JID getFollowedUserJID() throws RemoteException {
        if (editorManager.getFollowedUser() != null)
            return editorManager.getFollowedUser().getJID();
        else
            return null;
    }

    public void stopFollowing() throws RemoteException {
        // TODO add the implementation
    }

    public void stopFollowingGUI() throws RemoteException {
        final JID followedUserJID = getFollowedUserJID();
        if (followedUserJID == null) {
            log.debug(" You are not in follow mode, so you don't need to perform thhe function.");
            return;
        }
        log.debug(" JID of the followed user: " + followedUserJID.getBase());
        precondition();
        String contactLabel = getContactStatusInSessionView(followedUserJID);
        basic.clickContextMenuOfTableInView(VIEWNAME, contactLabel,
            CM_STOP_FOLLOWING_THIS_USER);
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isInFollowMode();
            }

            public String getFailureMessage() {
                return followedUserJID.getBase() + " is still followed.";
            }
        });
    }

    public void stopFollowingThisUserGUI(JID jidOfFollowedUser)
        throws RemoteException {
        if (!isInFollowMode()) {
            log.debug(" You are not in follow mode, so you don't need to perform thhe function.");
            return;
        }
        precondition();
        clickContextMenuOfSelectedUser(
            jidOfFollowedUser,
            CM_STOP_FOLLOWING_THIS_USER,
            "Hi guy, you can't stop following youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    public boolean isCMStopFollowingThisUserVisible(String contactName)
        throws RemoteException {
        precondition();
        return basic.isContextMenuOfTableVisibleInView(VIEWNAME, contactName,
            CM_STOP_FOLLOWING_THIS_USER);
    }

    public boolean isCMStopFollowingThisUserEnabled(String contactName)
        throws RemoteException {
        precondition();
        return basic.isContextMenuOfTableEnabledInView(VIEWNAME, contactName,
            CM_STOP_FOLLOWING_THIS_USER);
    }

    /**********************************************
     * 
     * context menu of a contact on the view: jump to position of selected user
     * 
     **********************************************/
    public void jumpToPositionOfSelectedUserGUI(JID jidOfselectedUser)
        throws RemoteException {
        clickContextMenuOfSelectedUser(
            jidOfselectedUser,
            CM_JUMP_TO_POSITION_SELECTED_USER,
            "Hi guy, you can't jump to the position of youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    /**********************************************
     * 
     * toolbar button on the view: share your screen with selected user
     * 
     **********************************************/
    public void shareYourScreenWithSelectedUserGUI(JID jidOfPeer)
        throws RemoteException {
        selectUser(
            jidOfPeer,
            "Hi guy, you can't share screen with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_SHARE_SCREEN_WITH_USER);
    }

    public void stopSessionWithUserGUI(JID jidOfPeer) throws RemoteException {
        selectUser(
            jidOfPeer,
            "Hi guy, you can't stop screen session with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_STOP_SESSION_WITH_USER);
    }

    public void confirmIncomingScreensharingSesionWindow()
        throws RemoteException {
        shellC.waitUntilShellActive(SHELL_INCOMING_SCREENSHARING_SESSION);
        shellC.confirmShell(SHELL_INCOMING_SCREENSHARING_SESSION, YES);
    }

    public void confirmWindowScreensharingAErrorOccured()
        throws RemoteException {
        shellC.waitUntilShellActive(SHELL_SCREENSHARING_ERROR_OCCURED);
        shellC.confirmShell(SHELL_SCREENSHARING_ERROR_OCCURED, OK);
    }

    /**********************************************
     * 
     * toolbar button on the view: send a file to selected user
     * 
     **********************************************/
    public void sendAFileToSelectedUserGUI(JID jidOfPeer)
        throws RemoteException {
        selectUser(
            jidOfPeer,
            "Hi guy, you can't send a file to youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_SEND_A_FILE_TO_SELECTED_USER);
    }

    /**********************************************
     * 
     * toolbar button on the view: start a VoIP session
     * 
     **********************************************/
    public void startAVoIPSessionGUI(JID jidOfPeer) throws RemoteException {
        selectUser(
            jidOfPeer,
            "Hi guy, you can't start a VoIP session with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_START_VOIP_SESSION);
        if (shellC.isShellActive(SHELL_ERROR_IN_SAROS_PLUGIN)) {
            confirmErrorInSarosPluginWindow();
        }
    }

    public void confirmErrorInSarosPluginWindow() throws RemoteException {
        shellC.confirmShell(SHELL_ERROR_IN_SAROS_PLUGIN, OK);
    }

    /**********************************************
     * 
     * toolbar button on the view: inconsistence detected
     * 
     **********************************************/
    public void inconsistencyDetectedGUI() throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_INCONSISTENCY_DETECTED);
        shellC.waitUntilShellClosed(SHELL_PROGRESS_INFORMATION);
    }

    public boolean isInconsistencyDetectedEnabled() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_INCONSISTENCY_DETECTED);
    }

    public void waitUntilInconsistencyDetected() throws RemoteException {
        precondition();
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isToolbarButtonEnabled(TB_INCONSISTENCY_DETECTED);
            }

            public String getFailureMessage() {
                return "The toolbar button " + TB_INCONSISTENCY_DETECTED
                    + " isn't enabled.";
            }
        });
    }

    /**********************************************
     * 
     * toolbar button on the view: remove all river role
     * 
     **********************************************/
    public void removeAllRriverRolesGUI() throws RemoteException {
        precondition();
        if (isRemoveAllRiverEnabled()) {
            clickToolbarButtonWithTooltip(TB_REMOVE_ALL_DRIVER_ROLES);
            shellC.waitUntilShellClosed(SHELL_PROGRESS_INFORMATION);
        }
    }

    public boolean isRemoveAllRiverEnabled() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_REMOVE_ALL_DRIVER_ROLES);
    }

    /**********************************************
     * 
     * toolbar button on the view: enable/disable follow mode
     * 
     **********************************************/
    public void enableDisableFollowModeGUI() throws RemoteException {
        precondition();
        if (isEnableDisableFollowModeEnabled())
            clickToolbarButtonWithTooltip(TB_ENABLE_DISABLE_FOLLOW_MODE);
    }

    public boolean isEnableDisableFollowModeEnabled() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_ENABLE_DISABLE_FOLLOW_MODE);
    }

    /**********************************************
     * 
     * toolbar button on the view: leave the session
     * 
     **********************************************/
    public void clickTBleaveTheSession() throws RemoteException {
        clickToolbarButtonWithTooltip(TB_LEAVE_THE_SESSION);
    }

    public void waitUntilAllPeersLeaveSession(
        final List<JID> jidsOfAllParticipants) throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                for (JID jid : jidsOfAllParticipants) {
                    if (isParticipant(jid))
                        return false;
                }
                return true;
            }

            public String getFailureMessage() {
                return "There are someone, who still not leave the session.";
            }
        });
    }

    public void leaveTheSessionByPeer() throws RemoteException {
        precondition();
        clickTBleaveTheSession();
        if (!shellC.activateShellWithText(CONFIRM_LEAVING_SESSION))
            shellC.waitUntilShellActive(CONFIRM_LEAVING_SESSION);
        shellC.confirmShell(CONFIRM_LEAVING_SESSION, YES);
        waitUntilSessionClosed();
    }

    public void leaveTheSessionByHost() throws RemoteException {
        precondition();
        clickTBleaveTheSession();
        // Util.runSafeAsync(log, new Runnable() {
        // public void run() {
        // try {
        // exWindowO.confirmWindow("Confirm Closing Session",
        // SarosConstant.BUTTON_YES);
        // } catch (RemoteException e) {
        // // no popup
        // }
        // }
        // });
        if (!shellC.activateShellWithText(SHELL_CONFIRM_CLOSING_SESSION))
            shellC.waitUntilShellActive(SHELL_CONFIRM_CLOSING_SESSION);
        shellC.confirmShell(SHELL_CONFIRM_CLOSING_SESSION, YES);
        waitUntilSessionClosed();
    }

    public void confirmClosingTheSessionWindow() throws RemoteException {
        shellC.waitUntilShellOpen(CLOSING_THE_SESSION);
        shellC.activateShellWithText(CLOSING_THE_SESSION);
        shellC.confirmShell(CLOSING_THE_SESSION, OK);
        shellC.waitUntilShellClosed(CLOSING_THE_SESSION);
    }

    /**********************************************
     * 
     * toolbar button on the view: open invitation interface
     * 
     **********************************************/
    public void openInvitationInterface(String... jidOfInvitees)
        throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_OPEN_INVITATION_INTERFACE);
        pEV.confirmWindowInvitation(jidOfInvitees);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    public void setJID(JID jid) throws RemoteException {
        this.localJID = jid;
    }

    public JID getJID() throws RemoteException {
        return localJID;
    }

    /**
     * 
     * Define the precondition which should be guaranteed when you want to
     * perform actions within the session view.
     * 
     * @throws RemoteException
     */
    protected void precondition() throws RemoteException {
        openSessionView();
        setFocusOnSessionView();
    }

    /**
     * 
     * It get all contact names listed in the session view and would be used by
     * {@link SessionViewImp#isInFollowMode}.
     * 
     * @return list, which contain all the contact names.
     * @throws RemoteException
     */
    private List<String> getAllContactsInSessionView() throws RemoteException {
        precondition();
        List<String> allContactsName = new ArrayList<String>();
        SWTBotTable table = basic.getTableInView(VIEWNAME);
        for (int i = 0; i < table.rowCount(); i++) {
            allContactsName.add(table.getTableItem(i).getText());
        }
        return allContactsName;
    }

    private void clickContextMenuOfSelectedUser(JID jidOfSelectedUser,
        String context, String message) throws RemoteException {
        if (localJID.equals(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        workbenchC.activateEclipseShell();
        precondition();
        String contactLabel = getContactStatusInSessionView(jidOfSelectedUser);
        workbenchC.captureScreenshot(workbenchC.getPathToScreenShot()
            + "/serverside_vor_jump_to_position.png");
        basic.clickContextMenuOfTableInView(VIEWNAME, contactLabel, context);

    }

    private void selectUser(JID jidOfSelectedUser, String message)
        throws RemoteException {
        if (localJID.equals(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        precondition();
        String contactLabel = getContactStatusInSessionView(jidOfSelectedUser);
        basic.getTableItemInView(VIEWNAME, contactLabel);
    }

    private boolean isToolbarButtonEnabled(String tooltip)
        throws RemoteException {
        return basic.isToolbarButtonInViewEnabled(VIEWNAME, tooltip);
    }

    private void clickToolbarButtonWithTooltip(String tooltipText)
        throws RemoteException {
        basic.clickToolbarButtonWithRegexTooltipInView(VIEWNAME, tooltipText);
    }

    private List<SWTBotToolbarButton> getToolbarButtons()
        throws RemoteException {
        return basic.getAllToolbarButtonsOnView(VIEWNAME);
    }

}
