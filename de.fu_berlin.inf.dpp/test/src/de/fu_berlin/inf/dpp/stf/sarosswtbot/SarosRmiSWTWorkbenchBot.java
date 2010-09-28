package de.fu_berlin.inf.dpp.stf.sarosswtbot;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.ui.RosterView;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * SarosRmiSWTWorkbenchBot controls Eclipse Saros from the GUI perspective. It
 * exports {@link ISarosState} via RMI. You should not use this within tests.
 * Have a look at {@link Musician} if you want to write tests.
 */
public class SarosRmiSWTWorkbenchBot extends RmiSWTWorkbenchBot implements
    ISarosRmiSWTWorkbenchBot {
    private static final transient Logger log = Logger
        .getLogger(SarosRmiSWTWorkbenchBot.class);

    public final static transient String TEMPDIR = System
        .getProperty("java.io.tmpdir");

    private static transient SarosRmiSWTWorkbenchBot self;

    /** RMI exported Saros state object */
    private ISarosState state;

    /** SarosRmiSWTWorkbenchBot is a singleton */
    public static SarosRmiSWTWorkbenchBot getInstance() {
        if (delegate != null && self != null)
            return self;

        SarosSWTWorkbenchBot swtwbb = new SarosSWTWorkbenchBot();
        self = new SarosRmiSWTWorkbenchBot(swtwbb);
        return self;
    }

    /** RmiSWTWorkbenchBot is a singleton, but inheritance is possible */
    protected SarosRmiSWTWorkbenchBot(SarosSWTWorkbenchBot bot) {
        super(bot);
    }

    /**
     * Export given state object by given name on our local RMI Registry.
     */
    public void exportState(SarosState state, String exportName) {
        try {
            this.state = (ISarosState) UnicastRemoteObject.exportObject(state,
                0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.state);
        } catch (RemoteException e) {
            log.error("Could not export stat object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind stat object, because it is bound already.", e);
        }
    }

    /***************** confirm ****************/

    /*************** Saros-specific-highlevel RMI exported Methods ******************/

    public void confirmContact() throws RemoteException {
        // bot.ackContactAdded(questioner.getPlainJid());
        waitUntilShellActive(SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED);
        confirmWindow(
            SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED,
            SarosConstant.BUTTON_OK);
    }

    public void confirmInvitationWindow(String... invitees)
        throws RemoteException {
        windowObject
            .activateShellWithText(SarosConstant.SHELL_TITLE_INVITATION);
        confirmWindowWithCheckBox(SarosConstant.SHELL_TITLE_INVITATION,
            SarosConstant.BUTTON_FINISH, invitees);
    }

    public void confirmSessionInvitationWizard(String inviter,
        String projectname) throws RemoteException {
        waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1(inviter);
        confirmSessionInvitationWindowStep2UsingNewproject(projectname);
    }

    public void confirmSessionInvitationWizardUsingExistProject(String inviter,
        String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1(inviter);
        confirmSessionInvitationWindowStep2UsingExistProject(projectName);
    }

    public void confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
        String inviter, String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1(inviter);
        confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(projectName);
    }

    public void confirmSessionInvitationWizardUsingExistProjectWithCopy(
        String inviter, String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1(inviter);
        confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(projectName);
    }

    public void confirmCreateNewUserAccountWindow(String server,
        String username, String password) throws RemoteException {
        try {
            windowObject.activateShellWithText("Create New User Account");
            delegate.textWithLabel("Jabber Server").setText(server);
            delegate.textWithLabel("Username").setText(username);
            delegate.textWithLabel("Password").setText(password);
            delegate.textWithLabel("Repeat Password").setText(password);
            delegate.button(SarosConstant.BUTTON_FINISH).click();
        } catch (WidgetNotFoundException e) {
            log.error("widget not found while accountBySarosMenu", e);
        }
    }

    /**
     * First step: invitee acknowledge session to given inviter
     * 
     * This method captures two screenshots as side effect.
     */
    public void confirmSessionInvitationWindowStep1(String inviter)
        throws RemoteException {
        if (!isTextWithLabelEqualWithText(SarosConstant.TEXT_LABEL_INVITER,
            inviter))
            log.warn("inviter does not match: " + inviter);
        captureScreenshot(TEMPDIR + "/acknowledge_project1.png");
        waitUntilButtonEnabled(SarosConstant.BUTTON_NEXT);
        delegate.button(SarosConstant.BUTTON_NEXT).click();
        captureScreenshot(TEMPDIR + "/acknowledge_project2.png");
        waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
    }

    /**
     * Second step: invitee acknowledge a new project
     * 
     * This method captures two screenshots as side effect.
     */
    public void confirmSessionInvitationWindowStep2UsingNewproject(
        String projectName) throws RemoteException {
        delegate.radio(SarosConstant.RADIO_LABEL_CREATE_NEW_PROJECT).click();
        captureScreenshot(TEMPDIR + "/acknowledge_project3.png");
        delegate.button(SarosConstant.BUTTON_FINISH).click();
        captureScreenshot(TEMPDIR + "/acknowledge_project4.png");
        waitUntilShellCloses(delegate
            .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
    }

    public void confirmSessionInvitationWindowStep2UsingExistProject(
        String projectName) throws RemoteException {
        delegate.radio("Use existing project").click();
        delegate.button("Browse").click();
        confirmWindowWithTree("Folder Selection", SarosConstant.BUTTON_OK,
            projectName);

        delegate.button(SarosConstant.BUTTON_FINISH).click();

        confirmWindow("Warning: Local changes will be deleted",
            SarosConstant.BUTTON_YES);
        waitUntilShellCloses(delegate
            .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
    }

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(
        String projectName) throws RemoteException {
        delegate.radio("Use existing project").click();
        delegate.button("Browse").click();
        confirmWindowWithTree("Folder Selection", SarosConstant.BUTTON_OK,
            projectName);
        delegate.button(SarosConstant.BUTTON_FINISH).click();
        confirmWindow("Warning: Local changes will be deleted",
            SarosConstant.BUTTON_NO);
    }

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(
        String projectName) throws RemoteException {
        delegate.radio("Use existing project").click();
        delegate.button("Browse").click();
        confirmWindowWithTree("Folder Selection", SarosConstant.BUTTON_OK,
            projectName);
        delegate.checkBox(
            "Create copy for working distributed. New project name:").click();
        delegate.button(SarosConstant.BUTTON_FINISH).click();
        waitUntilShellCloses(delegate
            .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
    }

    /**
     * Fill up the configuration wizard with title "Saros Configuration".
     */

    public void confirmSarosConfigurationWindow(String xmppServer, String jid,
        String password) throws RemoteException {
        windowObject
            .activateShellWithText(SarosConstant.SAROS_CONFI_SHELL_TITLE);
        delegate.textWithLabel(SarosConstant.TEXT_LABEL_JABBER_SERVER).setText(
            xmppServer);
        delegate.sleep(sleepTime);
        delegate.textWithLabel(SarosConstant.TEXT_LABEL_USER_NAME).setText(jid);
        delegate.sleep(sleepTime);
        delegate.textWithLabel(SarosConstant.TEXT_LABEL_PASSWORD).setText(
            password);
        delegate.sleep(sleepTime);

        while (delegate.button("Next >").isEnabled()) {
            delegate.button("Next >").click();
            log.debug("click Next > Button.");
            delegate.sleep(sleepTime);
        }

        if (delegate.button(SarosConstant.BUTTON_FINISH).isEnabled()) {
            delegate.button(SarosConstant.BUTTON_FINISH).click();
            return;
        } else {
            System.out.println("can't click finish button");
        }
        throw new NotImplementedException(
            "only set text fields and click Finish is implemented.");
    }

    // public void addNewContact(String name) throws RemoteException {
    // if (!isRosterViewOpen())
    // addSarosSessionView();
    // clickToolbarButtonWithTooltipInViewWithTitle(
    // SarosConstant.VIEW_TITLE_ROSTER,
    // SarosConstant.TOOL_TIP_TEXT_ADD_A_NEW_CONTACT);
    // activateShellWithText(SarosConstant.SHELL_TITLE_NEW_CONTACT);
    // setTextWithLabel(SarosConstant.TEXT_LABEL_JABBER_ID, name);
    // waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
    // clickButton(SarosConstant.BUTTON_FINISH);
    // delegate.sleep(sleepTime);
    //
    // // // server respond with failure code 503, service unavailable, add
    // // // contact anyway
    // // try {
    // // delegate.shell("Contact look-up failed").activate();
    // // delegate.button("Yes").click();
    // // } catch (WidgetNotFoundException e) {
    // // // ignore, server responds
    // // }
    // }

    /****************** click widget *********************/

    public void clickTBAddANewContactInRosterView() throws RemoteException {
        openRosterView();
        activateRosterView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_ADD_A_NEW_CONTACT);
    }

    public void clickSendAFileToSelectedUserInSPSView(String inviteeJID)
        throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.selectTableItemWithLabelInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION, inviteeJID);
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_SEND_FILE_TO_SELECTED_USER);
    }

    public void clickTBOpenInvitationInterfaceInSPSView()
        throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarPushButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_OPEN_INVITATION_INTERFACE);
    }

    public void clickStartAVoIPSessionInSPSView() throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_START_VOIP_SESSION);
    }

    public void clickNoInconsistenciesInSPSView() throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_NO_INCONSISTENCIES);
    }

    public void clickRemoveAllRriverRolesInSPSView() throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_REMOVE_ALL_DRIVER_ROLES);
    }

    public void clickEnableDisableFollowModeInSPSView() throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_ENABLE_DISABLE_FOLLOW_MODE);
    }

    public void clickTBLeaveTheSessionInSPSView() throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_LEAVE_THE_SESSION);
    }

    public void clickCMJumpToPositionOfSelectedUserInSPSView(
        String participantJID, String sufix) throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, participantJID + sufix,
            SarosConstant.CONTEXT_MENU_JUMP_TO_POSITION_SELECTED_USER);
    }

    public void clickCMStopFollowingThisUserInSPSView(ISarosState state, JID jid)
        throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        if (state.isDriver(jid))
            viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW,
                jid.getBase() + " (Driver)",
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
        else
            viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW, jid.getBase() + "",
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);

    }

    public void clickCMgiveExclusiveDriverRoleInSPSView(String inviteeJID)
        throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, inviteeJID,
            SarosConstant.CONTEXT_MENU_REMOVE_DRIVER_ROLE);
    }

    // public void clickCMgiveDriverRoleInSPSView(String inviteeJID)
    // throws RemoteException {
    //
    // }

    public void clickCMRemoveDriverRoleInSPSView(String inviteeJID)
        throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, inviteeJID,
            SarosConstant.CONTEXT_MENU_REMOVE_DRIVER_ROLE);
    }

    /**
     * Roster must be open
     */
    public void clickTBConnectInRosterView() throws RemoteException {
        openRosterView();
        activateRosterView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_CONNECT);
    }

    /**
     * Roster must be open
     */
    public boolean clickTBDisconnectInRosterView() throws RemoteException {
        openRosterView();
        activateRosterView();
        return viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_DISCONNECT) != null;

    }

    /**
     * This method captures two screenshots as side effect.
     */
    public void clickCMShareProjectInPEView(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        treeObject.clickContextMenuOfTreeInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, projectName);

    }

    public void clickCMShareprojectWithVCSSupportInPEView(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        treeObject.clickContextMenuOfTreeInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, projectName);
    }

    public void clickCMShareProjectParticallyInPEView(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        viewObject.clickContextMenuOfTableInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY, projectName);
    }

    public void clickCMAddToSessionInPEView(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        viewObject.clickContextMenuOfTableInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
            SarosConstant.CONTEXT_MENU_ADD_TO_SESSION, projectName);
    }

    public void clickShareYourScreenWithSelectedUserInSPSView()
        throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_SHARE_SCREEN_WITH_USER);
    }

    public void clickStopSessionWithUserInSPSView(String name)
        throws RemoteException {
        // selectTableItemWithLabelInViewWithTitle(
        // SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION, name);
        openSessionView();
        activateSharedSessionView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_STOP_SESSION_WITH_USER + " " + name);
    }

    public void clickChangeModeOfImageSourceInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_CHANGE_MODE_IMAGE_SOURCE);
    }

    public void clickStopRunningSessionInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_STOP_RUNNING_SESSION);
    }

    public void clickResumeInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_RESUME);
    }

    public void clickPauseInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_PAUSE);
    }

    // public void ackContactAdded(String name) {
    // try {
    // delegate.shell("Request of subscription received").activate();
    // delegate.sleep(750);
    // delegate.button("OK").click();
    // delegate.sleep(750);
    // } catch (WidgetNotFoundException e) {
    // // ignore
    // }
    // }

    /************ open **************/

    protected void hideViewById(final String viewId) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                IWorkbenchPage page = win.getActivePage();
                final IViewPart view = page.findView(viewId);
                if (view != null) {
                    page.hideView(view);
                }
            }
        });
    }

    protected void showViewById(final String viewId) {
        try {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    final IWorkbench wb = PlatformUI.getWorkbench();
                    final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                    IWorkbenchPage page = win.getActivePage();
                    try {
                        page.showView(viewId);
                    } catch (PartInitException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });
        } catch (IllegalArgumentException e) {
            log.debug("Couldn't initialize " + viewId, e.getCause());
        }
    }

    public void openRosterView() throws RemoteException {
        showViewById("de.fu_berlin.inf.dpp.ui.RosterView");
    }

    public void closeRosterView() throws RemoteException {
        hideViewById("de.fu_berlin.inf.dpp.ui.RosterView");
    }

    public void openSessionView() throws RemoteException {
        showViewById("de.fu_berlin.inf.dpp.ui.SessionView");
    }

    public void closeSessionView() throws RemoteException {
        hideViewById("de.fu_berlin.inf.dpp.ui.SessionView");
    }

    public void openChatView() throws RemoteException {
        showViewById("de.fu_berlin.inf.dpp.ui.ChatView");
    }

    public void closeChatView() throws RemoteException {
        hideViewById("de.fu_berlin.inf.dpp.ui.ChatView");
    }

    public void openRemoteScreenView() throws RemoteException {
        showViewById("de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView");
    }

    public void closeRemoteScreenView() throws RemoteException {
        hideViewById("de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView");
    }

    // public void addToSharedProject(String invitee) throws RemoteException {
    // activeViewWithTitle("Shared Project Session");
    // delegate.viewByTitle("Shared Project Session")
    // .toolbarButton("Open invitation interface").click();
    // selectCheckBoxWithText(invitee);
    // }

    // public boolean isConfigShellPoppedUp() throws RemoteException {
    // try {
    // delegate.shell("Saros Configuration");
    // return true;
    // } catch (WidgetNotFoundException e) {
    // // ignore
    // }
    // return false;
    // }

    /*************** is... ******************/

    public boolean isBuddyExist(String contact) throws RemoteException {
        SWTBotTree tree = delegate.viewByTitle(SarosConstant.VIEW_TITLE_ROSTER)
            .bot().tree();
        return treeObject.isTreeItemWithMatchTextExist(tree,
            SarosConstant.BUDDIES, contact + ".*");
    }

    /**
     * This method returns true if {@link SarosState} and the GUI
     * {@link RosterView} having the connected state.
     */
    public boolean isConnectedByXMPP() throws RemoteException {
        return state.isConnectedByXMPP() && isConnectedByXmppGuiCheck();
    }

    public boolean isFollowing() throws RemoteException {
        return state.isFollowing();
    }

    public boolean isConnectedByXmppGuiCheck() throws RemoteException {
        try {
            openRosterView();
            activateRosterView();
            SWTBotToolbarButton toolbarButton = viewObject
                .getToolbarButtonWithTooltipInView(
                    SarosConstant.VIEW_TITLE_ROSTER,
                    SarosConstant.TOOL_TIP_TEXT_DISCONNECT);
            return (toolbarButton != null && toolbarButton.isVisible());
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    // public boolean isConnectedByXMPP() throws RemoteException {
    // return state.isConnectedByXMPP() && isConnectedByXmppGuiCheck();
    // }

    public boolean isContactInSessionView(String Contact)
        throws RemoteException {
        activateSharedSessionView();
        SWTBotTable table = viewObject
            .getTableInView(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
        for (int i = 0; i < table.rowCount(); i++) {
            if (table.getTableItem(i).getText().matches(".*" + Contact + ".*"))
                return true;
        }
        return false;

    }

    public boolean hasContactWith(String contact) throws RemoteException {
        if (!isConnectedByXmppGuiCheck())
            return false;
        return isBuddyExist(contact);

        // SWTBotTreeItem contact_added = selectBuddy(contact);
        // return contact_added != null &&
        // contact_added.getText().equals(contact);
        // try {
        // SWTBotTree tree = delegate.viewByTitle("Roster").bot().tree();
        // if (tree != null) {
        // SWTBotTreeItem buddy = tree.getTreeItem("Buddies");
        // SWTBotTreeItem contact_added = buddy.getNode(contact).select();
        // delegate.sleep(1000);
        // return contact_added != null
        // && contact_added.getText().equals(contact);
        // }
        // } catch (WidgetNotFoundException e) {
        // log.warn("Contact not found: " + contact, e);
        // }
        // return false;
    }

    /**
     * "Shared Project Session" View must be open
     */
    public boolean isInSession() throws RemoteException {
        viewObject.activateViewWithTitle("Shared Project Session");
        return delegate.viewByTitle("Shared Project Session")
            .toolbarButton("Leave the session").isEnabled();
    }

    public boolean isContactOnline(String contact) {
        throw new NotImplementedException(
            "Can not be implemented, because no information is visible by swtbot. Enhance information with a tooltip or toher stuff.");
    }

    /**
     * Returns true if the given jid was found in Shared Project Session View.
     */
    public boolean isInSharedProject(String jid) {
        SWTBotView sessionView = delegate.viewByTitle("Shared Project Session");
        SWTBot bot = sessionView.bot();

        try {
            SWTBotTable table = bot.table();
            SWTBotTableItem item = table.getTableItem(jid);
            return item != null;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean isRosterViewOpen() throws RemoteException {
        return viewObject.isViewOpen(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public boolean isChatViewOpen() throws RemoteException {
        return viewObject.isViewOpen(SarosConstant.VIEW_TITLE_CHAT_VIEW);
    }

    public boolean isRemoteScreenViewOpen() throws RemoteException {
        return viewObject.isViewOpen(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    public boolean isSharedSessionViewOpen() throws RemoteException {
        return viewObject
            .isViewOpen(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
    }

    public void renameContact(String contact, String newName)
        throws RemoteException {
        SWTBotTree tree = delegate.viewByTitle(SarosConstant.VIEW_TITLE_ROSTER)
            .bot().tree();
        SWTBotTreeItem item = treeObject.getTreeItemWithMatchText(tree,
            SarosConstant.BUDDIES + ".*", contact + ".*");
        item.contextMenu("Rename...").click();

        waitUntilShellActive("Set new nickname");
        delegate.text(contact).setText(newName);
        delegate.button(SarosConstant.BUTTON_OK).click();
    }

    /*********************** delete ************************/
    /**
     * Remove given contact from Roster, if contact was added before.
     */
    public void deleteContact(String contact) throws RemoteException {
        if (!hasContactWith(contact))
            return;
        try {
            treeObject.clickContextMenuOfTreeInView(
                SarosConstant.VIEW_TITLE_ROSTER,
                SarosConstant.CONTEXT_MENU_DELETE, SarosConstant.BUDDIES,
                contact);
            // SWTBotTree tree = delegate.viewByTitle("Roster").bot().tree();
            // if (tree != null) {
            // SWTBotTreeItem buddy = tree.getTreeItem("Buddies");
            // SWTBotTreeItem item = buddy.getNode(contact).select();
            // // remove by context menu
            // delegate.sleep(750);
            // item.contextMenu("Delete").click();
            // delegate.sleep(750);
            // confirm delete
            waitUntilShellActive(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
            confirmWindow(SarosConstant.SHELL_TITLE_CONFIRM_DELETE,
                SarosConstant.BUTTON_YES);
            // activateShellByText(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
            // delegate.sleep(sleepTime);
            // clickButton(SarosConstant.BUTTON_YES);
            // delegate.button("Yes").click();

            // send backspace
            // item.pressShortcut(0, '\b'); // 0 == don't add keystroke

            delegate.sleep(sleepTime);
            // }
        } catch (WidgetNotFoundException e) {
            log.info("Contact not found: " + contact, e);
        }
    }

    // /**
    // * Create a {@link ISarosSession} using context menu off the given project
    // * on package explorer view.
    // */
    // public void clickProjectContextMenu(String projectName,
    // String nameOfContextMenu) throws RemoteException {
    // SWTBotView view = delegate.viewByTitle("Package Explorer");
    // SWTBotTree tree = view.bot().tree().select(projectName);
    // SWTBotTreeItem item = tree.getTreeItem(projectName).select();
    // SWTBotMenu menu = item.contextMenu(nameOfContextMenu);
    // menu.click();
    // }

    /***************** share *******************/
    /**
     * This method captures two screenshots as side effect.
     */
    public void shareProjectParallel(String projectName, List<String> invitees)
        throws RemoteException {
        clickCMShareProjectInPEView(projectName);
        waitUntilShellActive(SarosConstant.SHELL_TITLE_INVITATION);
        captureScreenshot(TEMPDIR + "/shareProjectStepParallel1.png");
        tableObject.selectCheckBoxsInTable(invitees);
        captureScreenshot(TEMPDIR + "/shareProjectStepParallel2.png");
        waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        delegate.button(SarosConstant.BUTTON_FINISH).click();
    }

    /*********** not exported Helper Methods *****************/

    // protected SWTBotToolbarButton getXmppDisconnectButton() {
    // for (SWTBotToolbarButton toolbarButton : delegate.viewByTitle("Roster")
    // .getToolbarButtons()) {
    // if (toolbarButton.getToolTipText().matches("Disconnect.*")) {
    // return toolbarButton;
    // }
    //
    // }
    //
    // return null;
    // }

    /************** waitUntil ****************/

    public void waitUntilConnected() {
        wUntilObject.waitUntil(SarosConditions.isConnect(delegate));
    }

    public void waitUntilDisConnected() {
        wUntilObject.waitUntil(SarosConditions.isDisConnected(delegate));
    }

    public void waitUntilSessionCloses() throws RemoteException {
        log.info("wait begin " + System.currentTimeMillis());
        wUntilObject.waitUntil(SarosConditions.isSessionClosed(state));
        log.info("wait end " + System.currentTimeMillis());
    }

    public void waitUntilSessionCloses(ISarosState state)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isSessionClosed(state));
        delegate.sleep(sleepTime);
    }

    public void waitUntilSessionOpen() throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isInSession(state));
    }

    public void waitUntilSessionOpenBy(ISarosState state)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isInSession(state));
    }

    // /**
    // * "Shared Project Session" View must be open
    // */
    // public void leaveSession() throws RemoteException {
    // clickTBLeaveTheSessionInSPSView();
    // delegate.sleep(sleepTime);
    // }

    public void addContact(String plainJID) throws RemoteException {
        openRosterView();
        activateRosterView();
        clickTBAddANewContactInRosterView();
        waitUntilShellActive(SarosConstant.SHELL_TITLE_NEW_CONTACT);
        // activateShellWithText(SarosConstant.SHELL_TITLE_NEW_CONTACT);
        delegate.textWithLabel(SarosConstant.TEXT_LABEL_JABBER_ID).setText(
            plainJID);
        waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        delegate.button(SarosConstant.BUTTON_FINISH).click();
    }

    /*************** set ***********************/
    public void setTextInJavaEditor(String contentPath, String projectName,
        String packageName, String className) throws RemoteException {
        String contents = state.getContents(contentPath);
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        editorObject.setTextinEditor(contents, className + ".java");
    }

    /**************** close ***********************/
    public void closePackageExplorerView() throws RemoteException {
        viewObject.closeViewWithText(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
    }

    public void closeWelcomeView() throws RemoteException {
        viewObject.closeViewWithText(SarosConstant.VIEW_TITLE_WELCOME);
    }

    /************* select ***************/
    public SWTBotTreeItem selectBuddy(String contact) throws RemoteException {

        return treeObject.selectTreeWithLabelsInView(
            SarosConstant.VIEW_TITLE_ROSTER, "Buddies", contact);
    }

    public void giveDriverRole(String inviteeJID) throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, inviteeJID,
            SarosConstant.CONTEXT_MENU_GIVE_DRIVER_ROLE);
    }

    public void inviteUser(String inviteeJID, String projectName)
        throws RemoteException {
        clickTBOpenInvitationInterfaceInSPSView();
        waitUntilShellActive("Invitation");
        confirmWindowWithCheckBox("Invitation", SarosConstant.BUTTON_FINISH,
            inviteeJID);
    }

    public void xmppConnect(JID jid, String password) throws RemoteException {

        log.trace("connectedByXMPP");
        boolean connectedByXMPP = isConnectedByXMPP();
        if (!connectedByXMPP) {
            log.trace("clickTBConnectInRosterView");
            clickTBConnectInRosterView();
            sleep(100);// wait a bit to check if shell pops up
            log.trace("isShellActive");
            boolean shellActive = isShellActive(SarosConstant.SAROS_CONFI_SHELL_TITLE);
            if (shellActive) {
                log.trace("confirmSarosConfigurationWindow");
                confirmSarosConfigurationWindow(jid.getDomain(), jid.getName(),
                    password);
            }
            waitUntilConnected();
        }

    }

    public void xmppDisconnect() throws RemoteException {
        if (isConnectedByXMPP()) {
            clickTBDisconnectInRosterView();
            waitUntilDisConnected();
        }
    }

    public void creatNewAccount(JID jid, String password)
        throws RemoteException {
        menuObject.clickMenuWithTexts("Saros", "Create Account");
        confirmCreateNewUserAccountWindow(jid.getDomain(), jid.getName(),
            password);
    }

    public boolean hasContactWith(JID jid) throws RemoteException {
        return state.hasContactWith(jid) && hasContactWith(jid.getBase());
    }

    public void openSarosViews() throws RemoteException {
        openRosterView();
        openSessionView();
        openChatView();
        openRemoteScreenView();
    }

    // public void leaveSession(JID jid) throws RemoteException {
    // // Need to check for isDriver before leaving.
    // final boolean isDriver = state.isDriver(jid);
    // clickTBLeaveTheSessionInSPSView();
    // if (!isDriver) {
    // confirmWindow(SarosConstant.SHELL_TITLE_CONFIRM_LEAVING_SESSION,
    // SarosConstant.BUTTON_YES);
    // } else {
    // Util.runSafeAsync(log, new Runnable() {
    // public void run() {
    // try {
    // confirmWindow("Confirm Closing Session",
    // SarosConstant.BUTTON_YES);
    // } catch (RemoteException e) {
    // // no popup
    // }
    // }
    // });
    // if (isShellActive("Confirm Closing Session"))
    // confirmWindow("Confirm Closing Session",
    // SarosConstant.BUTTON_YES);
    // }
    // waitUntilSessionCloses();
    // }

    public void activateRosterView() throws RemoteException {
        viewObject.activateViewWithTitle(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public void activateRemoteScreenView() throws RemoteException {
        viewObject
            .activateViewWithTitle(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    public void activateSharedSessionView() throws RemoteException {
        viewObject
            .activateViewWithTitle(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
    }

    public void activateChatView() throws RemoteException {
        viewObject.activateViewWithTitle(SarosConstant.VIEW_TITLE_CHAT_VIEW);
    }

    public void leaveSession(JID jid) throws RemoteException {
        // Need to check for isDriver before leaving.
        final boolean isDriver = state.isDriver(jid);
        clickTBLeaveTheSessionInSPSView();
        if (!isDriver) {
            confirmWindow(SarosConstant.SHELL_TITLE_CONFIRM_LEAVING_SESSION,
                SarosConstant.BUTTON_YES);
        } else {
            Util.runSafeAsync(log, new Runnable() {
                public void run() {
                    try {
                        confirmWindow("Confirm Closing Session",
                            SarosConstant.BUTTON_YES);
                    } catch (RemoteException e) {
                        // no popup
                    }
                }
            });
            if (isShellActive("Confirm Closing Session"))
                confirmWindow("Confirm Closing Session",
                    SarosConstant.BUTTON_YES);
        }
        waitUntilSessionCloses();
    }

    public void followUser(ISarosState participantState, JID participatnJid)
        throws RemoteException {
        openSessionView();
        activateSharedSessionView();
        if (participantState.isDriver(participatnJid))
            viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW, participatnJid.getBase()
                    + " (Driver)", SarosConstant.CONTEXT_MENU_FOLLOW_THIS_USER);

        else
            viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW, participatnJid.getBase()
                    + "", SarosConstant.CONTEXT_MENU_FOLLOW_THIS_USER);
    }

    public void clickShareProjectWith(String projectName,
        String shareProjectWith) throws RemoteException {
        if (shareProjectWith.equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT)) {
            clickCMShareProjectInPEView(projectName);
        } else if (shareProjectWith
            .equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS))
            clickCMShareprojectWithVCSSupportInPEView(projectName);
        else if (shareProjectWith
            .equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY))
            clickCMShareProjectParticallyInPEView(projectName);
        else
            clickCMAddToSessionInPEView(projectName);
    }

    public void confirmSessionUsingNewOrExistProject(
        ISarosRmiSWTWorkbenchBot inviteeBot, JID inviterJID,
        String projectName, int typeOfSharingProject) throws RemoteException {
        inviteeBot
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        switch (typeOfSharingProject) {
        case SarosConstant.CREATE_NEW_PROJECT:
            inviteeBot.confirmSessionInvitationWizard(inviterJID.getBase(),
                projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT:
            inviteeBot.confirmSessionInvitationWizardUsingExistProject(
                inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE:
            inviteeBot
                .confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
                    inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_COPY:
            inviteeBot.confirmSessionInvitationWizardUsingExistProjectWithCopy(
                inviterJID.getBase(), projectName);
            break;
        default:
            break;
        }
    }

    public void shareScreenWithUser(ISarosState respondentState,
        JID respondentJID) throws RemoteException {
        openRemoteScreenView();
        if (respondentState.isDriver(respondentJID)) {
            viewObject.selectTableItemWithLabelInView(
                SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
                respondentJID.getBase() + " (Driver)");

        } else {
            viewObject.selectTableItemWithLabelInView(
                SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
                respondentJID.getBase());
        }
        clickShareYourScreenWithSelectedUserInSPSView();
    }
}
