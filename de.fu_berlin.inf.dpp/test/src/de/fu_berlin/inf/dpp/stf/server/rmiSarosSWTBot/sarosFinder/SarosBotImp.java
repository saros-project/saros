package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBotImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.EditM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.EditMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.FileM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.FileMImp;
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

    private static EditMImp editM;
    private static FileMImp fileM;

    private static StateImp state;
    private static WaitImp wait;
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

        wait = WaitImp.getInstance();
        state = StateImp.getInstance();
        editM = EditMImp.getInstance();
        fileM = FileMImp.getInstance();
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

    private STFWorkbenchBot bot() {
        return bot;
    }

    public State state() throws RemoteException {
        return state;
    }

    public Wait condition() throws RemoteException {
        return wait;
    }

    public FileM file() throws RemoteException {
        bot.activateWorkbench();
        return fileM;
    }

    public EditM edit() throws RemoteException {
        bot.activateWorkbench();
        return editM;
    }

    public SarosM saros() throws RemoteException {
        bot.activateWorkbench();
        return sarosM;
    }

    public WindowM window() throws RemoteException {
        bot.activateWorkbench();
        return windowM;
    }

    public ChatView chatView() throws RemoteException {
        bot.openViewById(VIEW_SAROS_CHAT_ID);
        bot.view(VIEW_SAROS_CHAT).show();
        return chatV.setView(bot.view(VIEW_SAROS_CHAT));
    }

    public RosterView buddiesView() throws RemoteException {
        bot.openViewById(VIEW_SAROS_BUDDIES_ID);
        bot.view(VIEW_SAROS_BUDDIES).show();
        return rosterV.setView(bot.view(VIEW_SAROS_BUDDIES));
    }

    public RSView remoteScreenView() throws RemoteException {
        bot.openViewById(VIEW_REMOTE_SCREEN_ID);
        bot.view(VIEW_REMOTE_SCREEN).show();
        return rsV.setView(bot.view(VIEW_REMOTE_SCREEN));
    }

    public SessionView sessionView() throws RemoteException {
        bot.openViewById(VIEW_SAROS_SESSION_ID);
        bot.view(VIEW_SAROS_SESSION).show();
        return sessionV.setView(bot.view(VIEW_SAROS_SESSION));
    }

    public ConsoleView consoleView() throws RemoteException {
        return consoleV;
    }

    public PEView packageExplorerView() throws RemoteException {
        bot.openViewById(VIEW_PACKAGE_EXPLORER_ID);
        bot.view(VIEW_PACKAGE_EXPLORER).show();
        return pEV.setView(bot.view(VIEW_PACKAGE_EXPLORER));
    }

    public ProgressView progressView() throws RemoteException {
        bot.openViewById(VIEW_PROGRESS_ID);
        bot.view(VIEW_PROGRESS).show();
        return progressvV.setView(bot.view(VIEW_PROGRESS));
    }

    public void setJID(JID jid) throws RemoteException {
        localJID = jid;
    }

    /**********************************************
     * 
     * shells
     * 
     **********************************************/
    public void confirmShellAddProjectWithNewProject(String projectname)
        throws RemoteException {

        bot().waitLongUntilShellIsOpen(SHELL_ADD_PROJECT);
        STFBotShell shell = bot().shell(SHELL_ADD_PROJECT);
        shell.activate();
        shell.bot().radio(RADIO_CREATE_NEW_PROJECT).click();
        shell.bot().button(FINISH).click();

        try {
            bot().shell(SHELL_ADD_PROJECT).waitLongUntilIsClosed();
        } catch (Exception e) {
            /*
             * sometimes session can not be completely builded because of
             * unclear reason, so after timeout STF try to close
             * "the sesion invitation" window, but it can't close the window
             * before stopping the invitation process. In this case a Special
             * treatment should be done, so that the following tests still will
             * be run.
             */
            bot().captureScreenshot(
                bot().getPathToScreenShot()
                    + "/sessionInvitationFailedUsingNewProject.png");
            if (bot().activeShell().getText().equals(SHELL_ADD_PROJECT)) {
                bot().activeShell().bot().toggleButton().click();
            }
            throw new RuntimeException("session invitation is failed!");
        }

    }

    public void confirmShellAddProjectUsingExistProject(String projectName)
        throws RemoteException {
        bot().waitLongUntilShellIsOpen(SHELL_ADD_PROJECT);
        STFBotShell shell = bot().shell(SHELL_ADD_PROJECT);
        shell.activate();
        shell.bot().radio(RADIO_USING_EXISTING_PROJECT).click();
        shell.bot().textWithLabel("Project name", 1).setText(projectName);
        shell.bot().button(FINISH).click();

        if (bot().isShellOpen(SHELL_WARNING_LOCAL_CHANGES_DELETED))
            bot().shell(SHELL_WARNING_LOCAL_CHANGES_DELETED).confirm(YES);

        if (bot().isShellOpen(SHELL_SAVE_RESOURCE)
            && bot().shell(SHELL_SAVE_RESOURCE).isActive()) {
            bot().shell(SHELL_SAVE_RESOURCE).confirm(YES);
        }

        /*
         * after the release 10.10.28 it take less time for the session
         * invitation to complete, so the popup window "Session Invitation" is
         * immediately disappeared after you confirm the window ""Warning: Local
         * changes will be deleted".
         * 
         * Before waitUntil it would be better to first check, whether the
         * window "Session Invitation" is still open at all.
         */
        if (bot().isShellOpen(SHELL_ADD_PROJECT)) {
            try {
                bot().shell(SHELL_ADD_PROJECT).waitLongUntilIsClosed();
            } catch (Exception e) {
                bot().captureScreenshot(
                    bot().getPathToScreenShot()
                        + "/sessionInvitationFailedUsingExistProject.png");
                if (bot().activeShell().getText().equals(SHELL_ADD_PROJECT)) {
                    bot().activeShell().bot().toggleButton().click();
                }
            }
        }
    }

    public void confirmShellAddProjectUsingExistProjectWithCopyAfterCancelLocalChange(
        String projectName) throws RemoteException {
        bot().waitLongUntilShellIsOpen(SHELL_ADD_PROJECT);
        STFBotShell shell = bot().shell(SHELL_ADD_PROJECT);
        shell.activate();
        shell.bot().radio("Use existing project").click();
        shell.bot().textWithLabel("Project name", 1).setText(projectName);
        shell.bot().button(FINISH).click();
        bot().shell(SHELL_WARNING_LOCAL_CHANGES_DELETED).confirm(NO);

        confirmShellAddProjectUsingExistProjectWithCopy(projectName);
    }

    public void confirmShellAddProjectUsingExistProjectWithCopy(
        String projectName) throws RemoteException {
        bot().waitLongUntilShellIsOpen(SHELL_ADD_PROJECT);
        STFBotShell shell = bot().shell(SHELL_ADD_PROJECT);
        shell.activate();
        shell.bot().radio("Use existing project").click();
        shell.bot()
            .checkBox("Create copy for working distributed. New project name:")
            .click();
        shell.bot().button(FINISH).click();
        bot().shell(SHELL_ADD_PROJECT).waitLongUntilIsClosed();
    }

    public void confirmShellAddProjectUsingWhichProject(String projectName,
        TypeOfCreateProject usingWhichProject) throws RemoteException {
        bot().waitLongUntilShellIsOpen(SHELL_ADD_PROJECT);
        bot().shell(SHELL_ADD_PROJECT).activate();
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

    public void confirmShellChangeXMPPAccount(String newServer,
        String newUserName, String newPassword) throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_CHANGE_ACCOUNT);
        STFBotShell shell = bot().shell(SHELL_CHANGE_ACCOUNT);
        shell.activate();
        shell.bot().textWithLabel("Server").setText(newServer);
        shell.bot().textWithLabel("Username:").setText(newUserName);
        shell.bot().textWithLabel("Password:").setText(newPassword);
        shell.bot().textWithLabel("Confirm:").setText(newPassword);

        shell.bot().button(FINISH).click();
    }

    public void confirmShellCreateNewXMPPAccount(JID jid, String password)
        throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        STFBotShell shell = bot().shell(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        shell.activate();
        shell.bot().textWithLabel(LABEL_XMPP_JABBER_SERVER)
            .setText(jid.getDomain());
        shell.bot().textWithLabel(LABEL_USER_NAME).setText(jid.getName());
        shell.bot().textWithLabel(LABEL_PASSWORD).setText(password);
        shell.bot().textWithLabel(LABEL_REPEAT_PASSWORD).setText(password);

        shell.bot().button(FINISH).click();
        try {
            shell.waitShortUntilIsClosed();
        } catch (TimeoutException e) {
            String errorMessage = shell.getErrorMessage();
            if (errorMessage.matches(ERROR_MESSAGE_TOO_FAST_REGISTER_ACCOUNTS
                + ".*"))
                throw new RuntimeException(
                    "You are not allowed to register accounts so fast!");
            else if (errorMessage.matches(ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS
                + ".*\n*.*"))
                throw new RuntimeException("The Account " + jid.getBase()
                    + " is already existed!");
        }
    }

    public void confirmWizardSarosConfiguration(JID jid, String password)
        throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_SAROS_CONFIGURATION);
        STFBotShell shell = bot().shell(SHELL_SAROS_CONFIGURATION);
        shell.activate();
        shell.bot().textWithLabel(LABEL_XMPP_JABBER_SERVER)
            .setText(jid.getDomain());
        shell.bot().textWithLabel(LABEL_USER_NAME).setText(jid.getName());
        shell.bot().textWithLabel(LABEL_PASSWORD).setText(password);

        shell.bot().button(NEXT).click();
        shell.bot().button(FINISH).click();
    }

    public void confirmShellInvitation(String... baseJIDOfinvitees)
        throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_INVITATION);
        STFBotShell shell = bot().shell(SHELL_INVITATION);
        shell.activate();
        shell.confirmWithCheckBoxs(FINISH, baseJIDOfinvitees);
    }

    public void confirmShellClosingTheSession() throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_CLOSING_THE_SESSION);
        bot().shell(SHELL_CLOSING_THE_SESSION).activate();
        bot().shell(SHELL_CLOSING_THE_SESSION).confirm(OK);
        bot().waitsUntilShellIsClosed(SHELL_CLOSING_THE_SESSION);
    }
}
