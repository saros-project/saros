package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.IMenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.MenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.IViews;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.Views;
import de.fu_berlin.inf.dpp.stf.stfMessages.STFMessages;

public class SuperBot extends STFMessages implements ISuperBot {

    private static transient SuperBot self;

    private static IRemoteWorkbenchBot bot;

    private static Views views;

    private static MenuBar menuBar;

    /**
     * {@link SuperBot} is a singleton, but inheritance is possible.
     */
    public static SuperBot getInstance() {
        if (self != null)
            return self;
        self = new SuperBot();
        bot = RemoteWorkbenchBot.getInstance();

        views = Views.getInstance();
        menuBar = MenuBar.getInstance();

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

    private IRemoteWorkbenchBot bot() {
        return bot;
    }

    public IViews views() throws RemoteException {
        return views;
    }

    public IMenuBar menuBar() throws RemoteException {
        bot().activateWorkbench();
        return menuBar;
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
        IRemoteBotShell shell = bot().shell(SHELL_ADD_PROJECT);
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
        IRemoteBotShell shell = bot().shell(SHELL_ADD_PROJECT);
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
        IRemoteBotShell shell = bot().shell(SHELL_ADD_PROJECT);
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
        IRemoteBotShell shell = bot().shell(SHELL_ADD_PROJECT);
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

    public void confirmShellEditXMPPJabberAccount(String xmppJabberID,
        String newPassword) throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_EDIT_XMPP_JABBER_ACCOUNT);
        IRemoteBotShell shell = bot().shell(SHELL_EDIT_XMPP_JABBER_ACCOUNT);
        shell.activate();
        shell.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_ID)
            .setText(xmppJabberID);
        shell.bot().textWithLabel(LABEL_PASSWORD).setText(newPassword);
        shell.bot().button(FINISH).click();
    }

    public void confirmShellCreateNewXMPPJabberAccount(JID jid, String password)
        throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        IRemoteBotShell shell = bot().shell(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
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

    public void confirmShellAddXMPPJabberAccount(JID jid, String password)
        throws RemoteException {
        if (bot().isShellOpen(SHELL_ADD_XMPP_JABBER_ACCOUNT))
            bot().waitUntilShellIsOpen(SHELL_ADD_XMPP_JABBER_ACCOUNT);
        IRemoteBotShell shell = bot().shell(SHELL_ADD_XMPP_JABBER_ACCOUNT);
        shell.activate();
        /*
         * FIXME with comboBoxInGroup(GROUP_EXISTING_ACCOUNT) you wil get
         * WidgetNoFoundException.
         */
        shell.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_ID)
            .setText(jid.getBase());

        shell.bot().textWithLabel(LABEL_PASSWORD).setText(password);
        shell.bot().button(FINISH).click();
    }

    public void confirmShellClosingTheSession() throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_CLOSING_THE_SESSION);
        bot().shell(SHELL_CLOSING_THE_SESSION).activate();
        bot().shell(SHELL_CLOSING_THE_SESSION).confirm(OK);
        bot().waitUntilShellIsClosed(SHELL_CLOSING_THE_SESSION);
    }

    public void confirmShellRemovelOfSubscription() throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_REMOVAL_OF_SUBSCRIPTION);
        bot().shell(SHELL_REMOVAL_OF_SUBSCRIPTION).activate();
        bot().shell(SHELL_REMOVAL_OF_SUBSCRIPTION).confirm(OK);
    }

    public void confirmShellAddBuddyToSession(String... baseJIDOfinvitees)
        throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_ADD_BUDDY_TO_SESSION);
        IRemoteBotShell shell = bot().shell(SHELL_ADD_BUDDY_TO_SESSION);
        shell.activate();
        for (String baseJID : baseJIDOfinvitees) {
            shell.bot().tree().selectTreeItemWithRegex(baseJID + ".*" + "")
                .check();
        }
        shell.bot().button(FINISH).click();
    }

    public void confirmShellAddBuddy(JID jid) throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_ADD_BUDDY);
        bot().shell(SHELL_ADD_BUDDY).activate();
        bot().shell(SHELL_ADD_BUDDY).bot()
            .comboBoxWithLabel(LABEL_XMPP_JABBER_ID).setText(jid.getBase());
        bot().shell(SHELL_ADD_BUDDY).bot().button(FINISH).waitUntilIsEnabled();
        bot().shell(SHELL_ADD_BUDDY).bot().button(FINISH).click();

        bot().sleep(500);
        if (bot().isShellOpen("Buddy Unknown")) {
            bot().shell("Buddy Unknown").confirm(YES);
        }
    }

    public void confirmShellShareProject(String projectName, JID... jids)
        throws RemoteException {
        if (!bot().isShellOpen(SHELL_ADD_PROJECT)) {
            bot().waitUntilShellIsOpen(SHELL_SHARE_PROJECT);
        }
        IRemoteBotShell shell = bot().shell(SHELL_SHARE_PROJECT);
        shell.activate();

        shell.bot().table().getTableItemWithRegex(projectName + ".*").check();
        shell.bot().button(NEXT).click();

        for (IRemoteBotTreeItem item : shell.bot().tree().getAllItems()) {
            if (item.isChecked())
                item.check();
        }

        for (JID jid : jids) {
            shell.bot().tree().selectTreeItemWithRegex(jid.getBase() + ".*")
                .check();
        }
        shell.bot().button(FINISH).click();
    }

    public void confirmShellAddProjectsToSession(String... projectNames)
        throws RemoteException {
        if (!bot().isShellOpen(SHELL_ADD_PROJECTS_TO_SESSION)) {
            bot().waitUntilShellIsOpen(SHELL_ADD_PROJECTS_TO_SESSION);
        }
        IRemoteBotShell shell = bot().shell(SHELL_ADD_PROJECTS_TO_SESSION);
        shell.activate();

        for (String projectName : projectNames) {
            shell.bot().table().getTableItemWithRegex(projectName + ".*")
                .check();
        }
        shell.bot().button(FINISH).click();
    }

    public void confirmShellSessionInvitationAndShellAddProject(
        String projectName, TypeOfCreateProject usingWhichProject)
        throws RemoteException {
        if (!bot().isShellOpen(SHELL_SESSION_INVITATION)) {
            bot().waitLongUntilShellIsOpen(SHELL_SESSION_INVITATION);
        }
        bot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);
        confirmShellAddProjectUsingWhichProject(projectName, usingWhichProject);
        views().sarosView().waitUntilIsInSession();
    }

    public void confirmShellAddProjects(String projectName,
        TypeOfCreateProject usingWhichProject) throws RemoteException {
        if (!bot().isShellOpen(SHELL_ADD_PROJECTS)) {
            bot().waitLongUntilShellIsOpen(SHELL_ADD_PROJECTS);
        }
        IRemoteBotShell shell = bot().shell(SHELL_ADD_PROJECTS);

        switch (usingWhichProject) {
        case NEW_PROJECT:
            shell.bot().radio(RADIO_CREATE_NEW_PROJECT).click();
            break;
        case EXIST_PROJECT:
            shell.bot().radio(RADIO_USING_EXISTING_PROJECT).click();
            shell.bot().textWithLabel("Project name", 1).setText(projectName);
            break;
        case EXIST_PROJECT_WITH_COPY:
            shell.bot().radio("Use existing project").click();
            shell
                .bot()
                .checkBox(
                    "Create copy for working distributed. New project name:")
                .click();
            break;
        default:
            break;
        }
        shell.bot().button(FINISH).click();
        shell.waitLongUntilIsClosed();
    }

    public void confirmShellRequestOfSubscriptionReceived()
        throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED);
        bot().shell(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED).activate();
        bot().shell(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED).bot().button(OK)
            .click();
        bot().sleep(500);
    }

    public void confirmShellLeavingClosingSession() throws RemoteException {
        if (!views.sarosView().isHost()) {
            bot().waitUntilShellIsOpen(SHELL_CONFIRM_LEAVING_SESSION);
            bot().shell(SHELL_CONFIRM_LEAVING_SESSION).activate();
            bot().shell(SHELL_CONFIRM_LEAVING_SESSION).confirm(YES);
        } else {
            bot().waitUntilShellIsOpen(SHELL_CONFIRM_CLOSING_SESSION);
            bot().shell(SHELL_CONFIRM_CLOSING_SESSION).activate();
            bot().shell(SHELL_CONFIRM_CLOSING_SESSION).confirm(YES);
        }
        views.sarosView().waitUntilIsNotInSession();
    }

}
