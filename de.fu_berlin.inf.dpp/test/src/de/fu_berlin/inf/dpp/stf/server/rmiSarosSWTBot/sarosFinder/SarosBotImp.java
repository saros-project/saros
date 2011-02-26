package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBotImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.TeamCImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.EditM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.EditMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.FileM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.FileMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.RefactorM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.RefactorMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.SarosM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.SarosMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.WindowM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.WindowMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ConsoleViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.PEView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.PEViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ProgressViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RSViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RosterView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RosterViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionViewImp;

public class SarosBotImp extends STF implements SarosBot {

    private static transient SarosBotImp self;

    private static STFWorkbenchBot bot;

    private static TeamCImp teamC;
    private static EditMImp editM;
    private static FileMImp fileM;
    private static RefactorMImp refactorM;
    private static SarosMImp sarosM;

    private static WindowMImp windowM;
    private static ChatViewImp chatV;
    private static RosterViewImp rosterV;
    private static RSViewImp rsV;
    private static SessionViewImp sessionV;
    private static ConsoleViewImp consoleV;
    private static PEViewImp pEV;
    private static ProgressViewImp progressvV;

    /**
     * {@link SarosBotImp} is a singleton, but inheritance is possible.
     */
    public static SarosBotImp getInstance() {
        if (self != null)
            return self;
        self = new SarosBotImp();
        bot = STFWorkbenchBotImp.getInstance();

        teamC = TeamCImp.getInstance();
        editM = EditMImp.getInstance();
        fileM = FileMImp.getInstance();
        refactorM = RefactorMImp.getInstance();
        sarosM = SarosMImp.getInstance();
        windowM = WindowMImp.getInstance();
        chatV = ChatViewImp.getInstance();
        rosterV = RosterViewImp.getInstance();
        rsV = RSViewImp.getInstance();
        sessionV = SessionViewImp.getInstance();
        consoleV = ConsoleViewImp.getInstance();
        pEV = PEViewImp.getInstance();
        progressvV = ProgressViewImp.getInstance();

        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    public FileM file() throws RemoteException {
        return fileM;
    }

    public EditM edit() throws RemoteException {
        return editM;
    }

    public RefactorM refactor() throws RemoteException {
        return refactorM;
    }

    public SarosM saros() throws RemoteException {
        return sarosM;
    }

    public WindowM window() throws RemoteException {
        return windowM;
    }

    public ChatView chatView() throws RemoteException {
        return chatV;
    }

    public RosterView buddiesView() throws RemoteException {
        return rosterV;
    }

    public RSView remoteScreenView() throws RemoteException {
        return rsV;
    }

    public SessionView sessionView() throws RemoteException {
        return sessionV;
    }

    public ConsoleView consoleView() throws RemoteException {
        return consoleV;
    }

    public PEView packageExplorerView() throws RemoteException {
        bot.openViewById(VIEW_PACKAGE_EXPLORER_ID);
        bot.view(VIEW_PACKAGE_EXPLORER).show();
        return pEV.setWidget(bot.view(VIEW_PACKAGE_EXPLORER).bot().tree());
    }

    public ProgressView progressView() throws RemoteException {
        return progressvV;
    }

    public void setJID(JID jid) throws RemoteException {
        localJID = jid;
    }

    /**********************************************
     * 
     * shells
     * 
     **********************************************/
    public void confirmShellEditorSelection(String editorType)
        throws RemoteException {
        bot.waitUntilShellIsOpen(SHELL_EDITOR_SELECTION);
        STFBotShell shell_bob = bot.shell(SHELL_EDITOR_SELECTION);
        shell_bob.activate();
        shell_bob.bot().table().getTableItem(editorType).select();
        shell_bob.bot().button(OK).waitUntilIsEnabled();
        shell_bob.confirm(OK);
    }

    public void confirmShellInvitation(String... baseJIDOfinvitees)
        throws RemoteException {
        bot.waitUntilShellIsOpen(SHELL_INVITATION);
        STFBotShell shell = bot.shell(SHELL_INVITATION);
        shell.activate();
        shell.confirmWithCheckBoxs(FINISH, baseJIDOfinvitees);
    }

    public void confirmShellAddProjectWithNewProject(String projectname)
        throws RemoteException {
        STFBotShell shell = bot.shell(SHELL_SHELL_ADD_PROJECT);
        if (!shell.activate())
            shell.waitUntilActive();
        shell.bot().radio(RADIO_CREATE_NEW_PROJECT).click();
        shell.bot().button(FINISH).click();

        try {
            bot.shell(SHELL_SHELL_ADD_PROJECT).waitLongUntilIsClosed();
        } catch (Exception e) {
            /*
             * sometimes session can not be completely builded because of
             * unclear reason, so after timeout STF try to close
             * "the sesion invitation" window, but it can't close the window
             * before stopping the invitation process. In this case a Special
             * treatment should be done, so that the following tests still will
             * be run.
             */
            bot.captureScreenshot(bot.getPathToScreenShot()
                + "/sessionInvitationFailedUsingNewProject.png");
            if (bot.activeShell().getText().equals(SHELL_SHELL_ADD_PROJECT)) {
                bot.activeShell().bot().toggleButton().click();
            }
            throw new RuntimeException("session invitation is failed!");
        }

    }

    public void confirmShellAddProjectUsingExistProject(String projectName)
        throws RemoteException {
        bot.waitUntilShellIsOpen(SHELL_SHELL_ADD_PROJECT);
        STFBotShell shell = bot.shell(SHELL_SHELL_ADD_PROJECT);
        shell.activate();
        shell.bot().radio(RADIO_USING_EXISTING_PROJECT).click();

        // bot.button(BUTTON_BROWSE).click();
        // windowPart.activateShellWithText(FOLDER_SELECTION);
        // windowPart.confirmWindowWithTree(FOLDER_SELECTION, OK,
        // helperPart.changeToRegex(projectName));
        // windowPart.waitUntilShellCloses(FOLDER_SELECTION);
        shell.bot().textWithLabel("Project name", 1).setText(projectName);
        shell.bot().button(FINISH).click();
        /*
         * if there are some files locally, which are not saved yet, you will
         * get a popup window with the title "Save Resource" after you comfirm
         * the window "Warning: Local changes will be deleted" with YES.
         */

        // if (windowPart.isShellActive(SHELL_SAVE_RESOURCE)) {
        // windowPart.confirmWindow(SHELL_SAVE_RESOURCE, YES);
        /*
         * If there are local unsaved files, it take more time for the session
         * invitation to complete. So waitUntilShellCloses is necessary here.
         */
        // windowPart.waitUntilShellCloses(bot.shell(SHELL_SAVE_RESOURCE));
        // }
        if (bot.isShellOpen(SHELL_WARNING_LOCAL_CHANGES_DELETED))
            bot().shell(SHELL_WARNING_LOCAL_CHANGES_DELETED).confirm(YES);

        if (bot().isShellOpen(SHELL_SAVE_RESOURCE)
            && bot().shell(SHELL_SAVE_RESOURCE).isActive()) {
            bot().shell(SHELL_SAVE_RESOURCE).confirm(YES);
        }

        // windowPart.confirmWindow(WARNING_LOCAL_CHANGES_DELETED, YES);

        /*
         * after the release 10.10.28 it take less time for the session
         * invitation to complete, so the popup window "Session Invitation" is
         * immediately disappeared after you confirm the window ""Warning: Local
         * changes will be deleted".
         * 
         * Before waitUntil it would be better to first check, whether the
         * window "Session Invitation" is still open at all.
         */
        if (bot().isShellOpen(SHELL_SHELL_ADD_PROJECT)) {
            try {
                bot().shell(SHELL_SHELL_ADD_PROJECT).waitLongUntilIsClosed();
            } catch (Exception e) {
                bot().captureScreenshot(
                    bot().getPathToScreenShot()
                        + "/sessionInvitationFailedUsingExistProject.png");
                if (bot().activeShell().getText()
                    .equals(SHELL_SHELL_ADD_PROJECT)) {
                    bot().activeShell().bot().toggleButton().click();
                }
            }
        }
    }

    public void confirmShellAddProjectUsingExistProjectWithCopyAfterCancelLocalChange(
        String projectName) throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_SHELL_ADD_PROJECT);
        STFBotShell shell = bot().shell(SHELL_SHELL_ADD_PROJECT);
        shell.activate();
        shell.bot().radio("Use existing project").click();
        shell.bot().textWithLabel("Project name", 1).setText(projectName);
        shell.bot().button(FINISH).click();
        bot().shell(SHELL_WARNING_LOCAL_CHANGES_DELETED).confirm(NO);

        confirmShellAddProjectUsingExistProjectWithCopy(projectName);
    }

    public void confirmShellAddProjectUsingExistProjectWithCopy(
        String projectName) throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_SHELL_ADD_PROJECT);
        if (!shell.activate())
            shell.waitUntilActive();

        shell.bot().radio("Use existing project").click();
        shell.bot()
            .checkBox("Create copy for working distributed. New project name:")
            .click();
        shell.bot().button(FINISH).click();
        bot().shell(SHELL_SHELL_ADD_PROJECT).waitLongUntilIsClosed();
    }

    public void confirmShellSessionnInvitation() throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_SESSION_INVITATION);
        if (!shell.activate())
            shell.waitUntilActive();
        shell.bot().button(FINISH).click();
    }

    public void confirmShellInvitationCancelled() throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_INVITATION_CANCELLED);
        shell.activate();
        shell.setFocus();
        shell.bot().button().click();
    }

    public void confirmShellAddProjectUsingWhichProject(String projectName,
        TypeOfCreateProject usingWhichProject) throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_SHELL_ADD_PROJECT);
        bot().shell(SHELL_SHELL_ADD_PROJECT).activate();
        switch (usingWhichProject) {
        case NEW_PROJECT:
            confirmShellAddProjectWithNewProject(projectName);
            break;
        case EXIST_PROJECT:
            confirmShellAddProjectUsingExistProject(projectName);
            break;
        case EXIST_PROJECT_WITH_COPY:
            confirmShellAddProjectUsingExistProjectWithCopy(projectName);
            break;
        case EXIST_PROJECT_WITH_COPY_AFTER_CANCEL_LOCAL_CHANGE:
            confirmShellAddProjectUsingExistProjectWithCopyAfterCancelLocalChange(projectName);
            break;
        default:
            break;
        }
    }

    private STFWorkbenchBot bot() {
        return bot;
    }
}
