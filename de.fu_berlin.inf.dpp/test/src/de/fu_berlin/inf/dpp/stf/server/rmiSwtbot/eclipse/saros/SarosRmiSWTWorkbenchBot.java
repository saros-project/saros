package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.widgets.SarosSWTBotChatInput;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ISarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.IRosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.ISarosWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.ISessionViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.PopUpWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.RosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.SessionViewObject;
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

    public static final transient String TEMPDIR = System
        .getProperty("java.io.tmpdir");

    private static transient SarosRmiSWTWorkbenchBot self;

    /** RMI exported Saros object */
    public ISarosState stateObject;

    private IRosterViewObject rosterViewObject;

    private ISarosWindowObject popupWindowObject;

    private ISessionViewObject sessonViewObject;

    public IRosterViewObject getRosterViewObject() throws RemoteException {
        return rosterViewObject;
    }

    public ISarosWindowObject getPopupWindowObject() throws RemoteException {
        return popupWindowObject;
    }

    public ISessionViewObject getSessionViewObject() throws RemoteException {
        return sessonViewObject;
    }

    /**
     * SarosRmiSWTWorkbenchBot is a singleton
     */
    public static SarosRmiSWTWorkbenchBot getInstance() {
        if (delegate != null && self != null)
            return self;

        SarosSWTBot swtwbb = new SarosSWTBot();
        self = new SarosRmiSWTWorkbenchBot(swtwbb);
        return self;
    }

    /**
     * RmiSWTWorkbenchBot is a singleton, but inheritance is possible
     */
    protected SarosRmiSWTWorkbenchBot(SarosSWTBot bot) {
        super(bot);

    }

    /**
     * Export given state object by given name on our local RMI Registry.
     */
    public void exportState(SarosState state, String exportName) {
        try {
            this.stateObject = (ISarosState) UnicastRemoteObject.exportObject(
                state, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.stateObject);
        } catch (RemoteException e) {
            log.error("Could not export stat object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind stat object, because it is bound already.", e);
        }
    }

    /**
     * Export given roster view object by given name on our local RMI Registry.
     */
    public void exportRosterView(RosterViewObject rosterView, String exportName) {
        try {
            this.rosterViewObject = (IRosterViewObject) UnicastRemoteObject
                .exportObject(rosterView, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.rosterViewObject);
        } catch (RemoteException e) {
            log.error("Could not export rosterview object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind rosterview object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given pop up window object by given name on our local RMI
     * Registry.
     */
    public void exportPopUpWindow(PopUpWindowObject popUpWindowObject,
        String exportName) {
        try {
            this.popupWindowObject = (ISarosWindowObject) UnicastRemoteObject
                .exportObject(popUpWindowObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.popupWindowObject);
        } catch (RemoteException e) {
            log.error("Could not export popup window object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind popup window object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given shared session view object by given name on our local RMI
     * Registry.
     */
    public void exportSessionView(SessionViewObject sharedSessonViewObject,
        String exportName) {
        try {
            this.sessonViewObject = (ISessionViewObject) UnicastRemoteObject
                .exportObject(sharedSessonViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.sessonViewObject);
        } catch (RemoteException e) {
            log.error("Could not export shared session view object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind shared session view object, because it is bound already.",
                e);
        }
    }

    /*******************************************************************************
     * 
     * Share project session view page
     * 
     *******************************************************************************/

    /*******************************************************************************
     * 
     * Saros Package explorer page
     * 
     *******************************************************************************/

    /**
     * This method captures two screenshots as side effect.
     */
    public void clickCMShareProjectInPEView(String projectName)
        throws RemoteException {
        packageExplorerViewObject.showViewPackageExplorer();
        packageExplorerViewObject.activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = mainObject.changeToRegex(nodes);

        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT);
        // viewObject.clickContextMenuOfTreeInView(
        // SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, projectName);

    }

    public void clickCMShareprojectWithVCSSupportInPEView(String projectName)
        throws RemoteException {
        packageExplorerViewObject.showViewPackageExplorer();
        packageExplorerViewObject.activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = mainObject.changeToRegex(nodes);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS);
    }

    public void clickCMShareProjectParticallyInPEView(String projectName)
        throws RemoteException {
        packageExplorerViewObject.showViewPackageExplorer();
        packageExplorerViewObject.activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = mainObject.changeToRegex(nodes);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY);
        // viewObject.clickContextMenuOfTableInView(
        // SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY, projectName);
    }

    public void clickCMAddToSessionInPEView(String projectName)
        throws RemoteException {
        packageExplorerViewObject.showViewPackageExplorer();
        packageExplorerViewObject.activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = mainObject.changeToRegex(nodes);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_ADD_TO_SESSION);

        // viewObject.clickContextMenuOfTableInView(
        // SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
        // SarosConstant.CONTEXT_MENU_ADD_TO_SESSION, projectName);
    }

    /*******************************************************************************
     * 
     * Remote screen view page
     * 
     *******************************************************************************/
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

    public void clickTBChangeModeOfImageSourceInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_CHANGE_MODE_IMAGE_SOURCE);
    }

    public void clickTBStopRunningSessionInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_STOP_RUNNING_SESSION);
    }

    public void clickTBResumeInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_RESUME);
    }

    public void clickTBPauseInRSView() throws RemoteException {
        openRemoteScreenView();
        activateRemoteScreenView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_REMOTE_SCREEN,
            SarosConstant.TOOL_TIP_TEXT_PAUSE);
    }

    /*******************************************************************************
     * 
     * Chat view page
     * 
     *******************************************************************************/
    public void activateChatView() throws RemoteException {
        viewObject.setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_CHAT_VIEW);
    }

    public void openChatView() throws RemoteException {
        if (!isChatViewOpen())
            viewObject.openViewById(SarosConstant.ID_CHAT_VIEW);
    }

    public void closeChatView() throws RemoteException {
        viewObject.closeViewById(SarosConstant.ID_CHAT_VIEW);
    }

    public boolean isChatViewOpen() throws RemoteException {
        return viewObject.isViewOpen(SarosConstant.VIEW_TITLE_CHAT_VIEW);
    }

    public void sendChatMessage(String message) throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        SarosSWTBotChatInput chatInput = delegate.chatInput();
        chatInput.setText(message);
        delegate.text();
        log.debug("inerted message in chat view: " + chatInput.getText());
        // chatInput.pressShortcut(Keystrokes.LF);
        chatInput.pressEnterKey();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator()
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("user name of the first chat line partner change separator: "
            + delegate.chatLinePartnerChangeSeparator().getPlainID());
        return delegate.chatLinePartnerChangeSeparator().getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(int index)
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("user name of the chat line partner change separator with the index"
            + index
            + ": "
            + delegate.chatLinePartnerChangeSeparator(index).getPlainID());
        return delegate.chatLinePartnerChangeSeparator(index).getPlainID();
    }

    public String getUserNameOnChatLinePartnerChangeSeparator(String plainID)
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("user name of the chat line partner change separator with the plainID "
            + plainID
            + ": "
            + delegate.chatLinePartnerChangeSeparator(plainID).getPlainID());
        return delegate.chatLinePartnerChangeSeparator(plainID).getPlainID();
    }

    public String getTextOfChatLine() throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the first chat line: "
            + delegate.chatLine().getText());
        return delegate.chatLine().getText();
    }

    public String getTextOfChatLine(int index) throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the chat line with the index " + index + ": "
            + delegate.chatLine(index).getText());
        return delegate.chatLine(index).getText();
    }

    public String getTextOfLastChatLine() throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the last chat line: "
            + delegate.lastChatLine().getText());
        return delegate.lastChatLine().getText();
    }

    public String getTextOfChatLine(String regex) throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("text of the chat line with the specifed regex: "
            + delegate.chatLine(regex).getText());
        return delegate.chatLine(regex).getText();
    }

    public boolean compareChatMessage(String jid, String message)
        throws RemoteException {
        if (!isChatViewOpen())
            openChatView();
        activateChatView();
        log.debug("chatLine: " + delegate.lastChatLine());
        // log.debug("text of the lastChatLine: "
        // + delegate.lastChatLine().widget.getText());
        log.debug("text of the lastChatLine: "
            + delegate.lastChatLine().getText());
        String text = delegate.lastChatLine().getText();
        return text.equals(message);

        // return Comperator.compareStrings(jid, message, text);
    }

    // public boolean isContactOnline(String contact) {
    // throw new NotImplementedException(
    // "Can not be implemented, because no information is visible by swtbot. Enhance information with a tooltip or toher stuff.");
    // }

    // /**
    // * Returns true if the given jid was found in Shared Project Session View.
    // */
    // public boolean isInSharedProject(String jid) {
    // SWTBotView sessionView = delegate.viewByTitle("Shared Project Session");
    // SWTBot bot = sessionView.bot();
    //
    // try {
    // SWTBotTable table = bot.table();
    // SWTBotTableItem item = table.getTableItem(jid);
    // return item != null;
    // } catch (WidgetNotFoundException e) {
    // return false;
    // }
    // }

    /*******************************************************************************
     * 
     * frequently used components
     * 
     *******************************************************************************/

    public void leaveSessionByPeer() throws RemoteException {
        // Need to check for isDriver before leaving.
        sessonViewObject.leaveTheSession();
        eclipseWindowObject.confirmWindow(
            SarosConstant.SHELL_TITLE_CONFIRM_LEAVING_SESSION,
            SarosConstant.BUTTON_YES);
        sessonViewObject.waitUntilSessionCloses();
    }

    public void leaveSessionByHost() throws RemoteException {
        sessonViewObject.leaveTheSession();
        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                try {
                    eclipseWindowObject.confirmWindow(
                        "Confirm Closing Session", SarosConstant.BUTTON_YES);
                } catch (RemoteException e) {
                    // no popup
                }
            }
        });
        if (eclipseWindowObject.isShellActive("Confirm Closing Session"))
            eclipseWindowObject.confirmWindow("Confirm Closing Session",
                SarosConstant.BUTTON_YES);
        sessonViewObject.waitUntilSessionCloses();
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
        inviteeBot.getEclipseWindowObject().waitUntilShellActive(
            SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        switch (typeOfSharingProject) {
        case SarosConstant.CREATE_NEW_PROJECT:
            inviteeBot.getPopupWindowObject().confirmSessionInvitationWizard(
                inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT:
            inviteeBot.getPopupWindowObject()
                .confirmSessionInvitationWizardUsingExistProject(
                    inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE:
            inviteeBot
                .getPopupWindowObject()
                .confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
                    inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_COPY:
            inviteeBot.getPopupWindowObject()
                .confirmSessionInvitationWizardUsingExistProjectWithCopy(
                    inviterJID.getBase(), projectName);
            break;
        default:
            break;
        }
    }

    public void xmppConnect(JID jid, String password) throws RemoteException {
        log.trace("connectedByXMPP");
        boolean connectedByXMPP = rosterViewObject.isConnectedByXMPP();
        if (!connectedByXMPP) {
            log.trace("clickTBConnectInRosterView");
            rosterViewObject.clickTBConnectInRosterView();
            sleep(100);// wait a bit to check if shell pops up
            log.trace("isShellActive");
            boolean shellActive = eclipseWindowObject
                .isShellActive(SarosConstant.SAROS_CONFI_SHELL_TITLE);
            if (shellActive) {
                log.trace("confirmSarosConfigurationWindow");
                popupWindowObject.confirmSarosConfigurationWizard(
                    jid.getDomain(), jid.getName(), password);
            }
            rosterViewObject.waitUntilConnected();
        }
    }

    public void creatNewAccount(JID jid, String password)
        throws RemoteException {
        getEclipseShell().activate().setFocus();
        menuObject.clickMenuWithTexts("Saros", "Create Account");
        popupWindowObject.confirmCreateNewUserAccountWindow(jid.getDomain(),
            jid.getName(), password);
    }

    public void openSarosViews() throws RemoteException {
        rosterViewObject.openRosterView();
        sessonViewObject.openSessionView();
        openChatView();
        openRemoteScreenView();
    }

    public boolean hasContactWith(JID jid) throws RemoteException {
        return stateObject.hasContactWith(jid)
            && rosterViewObject.isBuddyExist(jid.getBase());
    }

    public void renameContact(String contact, String newName)
        throws RemoteException {
        SWTBotTree tree = delegate.viewByTitle(SarosConstant.VIEW_TITLE_ROSTER)
            .bot().tree();
        SWTBotTreeItem item = treeObject.getTreeItemWithMatchText(tree,
            SarosConstant.BUDDIES + ".*", contact + ".*");
        item.contextMenu("Rename...").click();
        windowObject.waitUntilShellActive("Set new nickname");
        delegate.text(contact).setText(newName);
        delegate.button(SarosConstant.BUTTON_OK).click();
    }

    /**
     * Remove given contact from Roster, if contact was added before.
     */
    public void deleteContact(JID jid, ISarosRmiSWTWorkbenchBot participant)
        throws RemoteException {
        if (!hasContactWith(jid))
            return;
        try {
            viewObject.clickContextMenuOfTreeInView(
                SarosConstant.VIEW_TITLE_ROSTER,
                SarosConstant.CONTEXT_MENU_DELETE, SarosConstant.BUDDIES,
                jid.getBase());
            windowObject
                .waitUntilShellActive(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
            eclipseWindowObject.confirmWindow(
                SarosConstant.SHELL_TITLE_CONFIRM_DELETE,
                SarosConstant.BUTTON_YES);
            participant.getEclipseWindowObject().waitUntilShellActive(
                SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION);
            participant.getEclipseWindowObject().confirmWindow(
                SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION,
                SarosConstant.BUTTON_OK);

        } catch (WidgetNotFoundException e) {
            log.info("Contact not found: " + jid.getBase(), e);
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

    public void shareProject(String projectName, List<String> inviteeJIDS)
        throws RemoteException {
        clickCMShareProjectInPEView(projectName);
        windowObject.waitUntilShellActive(SarosConstant.SHELL_TITLE_INVITATION);
        captureScreenshot(TEMPDIR + "/shareProjectStepParallel1.png");
        tableObject.selectCheckBoxsInTable(inviteeJIDS);
        captureScreenshot(TEMPDIR + "/shareProjectStepParallel2.png");
        basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        delegate.button(SarosConstant.BUTTON_FINISH).click();
    }

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

    public void addContact(JID jid, ISarosRmiSWTWorkbenchBot participant)
        throws RemoteException {
        if (!hasContactWith(jid)) {
            rosterViewObject.openRosterView();
            rosterViewObject.setFocusOnRosterView();
            rosterViewObject.clickTBAddANewContactInRosterView();
            windowObject
                .waitUntilShellActive(SarosConstant.SHELL_TITLE_NEW_CONTACT);
            // activateShellWithText(SarosConstant.SHELL_TITLE_NEW_CONTACT);
            delegate.textWithLabel(SarosConstant.TEXT_LABEL_JABBER_ID).setText(
                jid.getBase());
            basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
            delegate.button(SarosConstant.BUTTON_FINISH).click();
            participant.getPopupWindowObject()
                .confirmRequestOfSubscriptionReceivedWindow();
            popupWindowObject.confirmRequestOfSubscriptionReceivedWindow();
        }

    }

    /*******************************************************************************
     * 
     * waitUntil
     * 
     *******************************************************************************/

    /**
     * For some tests a host need to invite many peers concurrently and some
     * operations should not be performed if the invitation processes aren't
     * finished yet. In this case, you can use this method to guarantee, that
     * host wait so long until all the invitation Processes are finished.
     */
    public void waitUntilNoInvitationProgress() throws RemoteException {
        openProgressView();
        activateProgressView();
        delegate.waitUntil(SarosConditions.existNoInvitationProgress(delegate),
            100000);
    }

    /******************************/
    public void setTextInJavaEditorWithSave(String contentPath,
        String projectName, String packageName, String className)
        throws RemoteException {
        String contents = stateObject.getContents(contentPath);
        // activateEclipseShell();

        packageExplorerViewObject
            .openClass(projectName, packageName, className);
        eclipseEditorObject.activateJavaEditor(className);
        SWTBotEditor editor;
        editor = delegate.editorByTitle(className + ".java");
        SWTBotEclipseEditor e = editor.toTextEditor();

        // Display.getDefault().syncExec(new Runnable() {
        // public void run() {
        // final IWorkbench wb = PlatformUI.getWorkbench();
        // final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        // log.debug("shell name: " + win.getShell().getText());
        // win.getShell().forceActive();
        // win.getShell().forceFocus();
        // }
        // });
        // e.setFocus();
        e.setText(contents);
        // e.typeText("hallo wie geht es dir !%%%");
        // e.pressShortcut(Keystrokes.LF);
        // e.typeText("mir geht es gut!");
        // delegate.sleep(2000);
        //
        // delegate.sleep(2000);

        e.save();
        // editorObject.setTextinEditorWithSave(contents, className + ".java");
    }

    public void setTextInEditorWithSave(String contentPath, String... filePath)
        throws RemoteException {
        String contents = stateObject.getContents(contentPath);
        String fileName = filePath[filePath.length - 1];
        packageExplorerViewObject.openFile(filePath);
        eclipseEditorObject.activateEditor(fileName);
        editorObject.setTextinEditorWithSave(contents, fileName);
    }

    public void setTextInJavaEditorWithoutSave(String contentPath,
        String projectName, String packageName, String className)
        throws RemoteException {
        String contents = stateObject.getContents(contentPath);
        packageExplorerViewObject
            .openClass(projectName, packageName, className);
        eclipseEditorObject.activateJavaEditor(className);
        editorObject.setTextinEditorWithoutSave(contents, className + ".java");
    }

    public void typeTextInJavaEditor(String contentPath, String projectName,
        String packageName, String className) throws RemoteException {
        String contents = stateObject.getContents(contentPath);
        activateEclipseShell();
        packageExplorerViewObject
            .openClass(projectName, packageName, className);
        eclipseEditorObject.activateJavaEditor(className);
        editorObject.typeTextInEditor(contents, className + ".java");
    }

    /*******************************************************************************
     * 
     * saros main page
     * 
     *******************************************************************************/

    public void resetSaros() throws RemoteException {
        rosterViewObject.xmppDisconnect();
        eclipseState.deleteAllProjects();
    }

    /**
     * remove the progress. ie. Click the gray clubs delete icon.
     */
    public void removeProgress() throws RemoteException {
        openProgressView();
        activateProgressView();
        SWTBotView view = delegate.viewByTitle("Progress");
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton();
        b.click();
    }

    public void invitateUser(String inviteeJID) throws RemoteException {
        sessonViewObject.openInvitationInterface();
        popupWindowObject.comfirmInvitationWindow(inviteeJID);
    }

    /**
     * end the invitation process. ie. Click the red stop icon in Progress view.
     */
    public void cancelInvitation() throws RemoteException {
        openProgressView();
        activateProgressView();
        SWTBotView view = delegate.viewByTitle("Progress");
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton();
        b.click();
    }

    public void cancelInvitation(int index) throws RemoteException {
        openProgressView();
        activateProgressView();
        SWTBotView view = delegate.viewByTitle("Progress");
        view.toolbarButton("Remove All Finished Operations").click();
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton(index);
        b.click();
    }

    public void cancelInivtationInSessionInvitationWindow()
        throws RemoteException {
        SWTBotShell shell = delegate.activeShell();
        shell.bot().toolbarButton().click();
    }

    public boolean isProgressViewOpen() throws RemoteException {
        return viewObject.isViewOpen("Progress");
    }

    public void confirmInvitationCancelledWindow() throws RemoteException {
        SWTBotShell shell = delegate.shell("Invitation Cancelled");
        shell.activate().setFocus();
        SWTBotButton button = shell.bot().button();
        button.click();
    }

    public boolean isToolbarNoInconsistenciesEnabled() throws RemoteException {
        sessonViewObject.openSessionView();
        sessonViewObject.setFocusOnSessionView();
        return viewObject.isToolbarInViewEnabled(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_NO_INCONSISTENCIES);
    }

}
