package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBotImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.MenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.MenuBarImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.Views;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.ViewsImp;

public class SuperBotImp extends STF implements SuperBot {

    private static transient SuperBotImp self;

    private static STFWorkbenchBot bot;

    private static StateImp state;
    private static WaitImp wait;

    private static ViewsImp views;

    private static MenuBarImp menuBar;

    /**
     * {@link SuperBotImp} is a singleton, but inheritance is possible.
     */
    public static SuperBotImp getInstance() {
        if (self != null)
            return self;
        self = new SuperBotImp();
        bot = STFWorkbenchBotImp.getInstance();

        wait = WaitImp.getInstance();
        state = StateImp.getInstance();

        views = ViewsImp.getInstance();
        menuBar = MenuBarImp.getInstance();

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

    public Views views() throws RemoteException {
        return views;
    }

    public MenuBar menuBar() throws RemoteException {
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

    public void confirmShellChangeXMPPAccount(String xmppJabberID,
        String newPassword) throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_EDIT_XMPP_JABBER_ACCOUNT);
        STFBotShell shell = bot().shell(SHELL_EDIT_XMPP_JABBER_ACCOUNT);
        shell.activate();
        shell.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_ID)
            .setText(xmppJabberID);
        shell.bot().textWithLabel(LABEL_PASSWORD).setText(newPassword);
        shell.bot().button(FINISH).click();
    }

    public void confirmShellCreateNewXMPPAccount(JID jid, String password)
        throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        STFBotShell shell = bot().shell(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
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

    public void confirmWizardAddXMPPJabberAccount(JID jid, String password)
        throws RemoteException {
        if (bot().isShellOpen(SHELL_ADD_XMPP_JABBER_ACCOUNT))
            bot().waitUntilShellIsOpen(SHELL_ADD_XMPP_JABBER_ACCOUNT);
        STFBotShell shell = bot().shell(SHELL_ADD_XMPP_JABBER_ACCOUNT);
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

    public void confirmShellAddBuddyToSession(String... baseJIDOfinvitees)
        throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_ADD_BUDDY_TO_SESSION);
        STFBotShell shell = bot().shell(SHELL_ADD_BUDDY_TO_SESSION);
        shell.activate();
        for (String baseJID : baseJIDOfinvitees) {
            shell.bot().tree().selectTreeItem(baseJID).check();
        }
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

    public void confirmShellAddBuddy(JID jid) throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_ADD_BUDDY);
        bot().shell(SHELL_ADD_BUDDY).activate();
        bot().shell(SHELL_ADD_BUDDY).bot()
            .comboBoxWithLabel(LABEL_XMPP_JABBER_ID).setText(jid.getBase());
        bot().shell(SHELL_ADD_BUDDY).bot().button(FINISH).waitUntilIsEnabled();
        bot().shell(SHELL_ADD_BUDDY).bot().button(FINISH).click();

        bot().sleep(500);
        if (bot().isShellOpen("Unknown Buddy Status")) {
            bot().shell("Unknown Buddy Status").confirm(YES);
        }
    }

    public void confirmWizardShareProject(String projectName, JID... jids)
        throws RemoteException {
        if (!bot().isShellOpen(SHELL_SHARE_PROJECT)) {
            bot().waitUntilShellIsOpen(SHELL_SHARE_PROJECT);
        }
        STFBotShell shell = bot().shell(SHELL_SHARE_PROJECT);
        shell.activate();

        shell.bot().table().getTableItem(projectName).check();
        shell.bot().button(NEXT).click();
        for (JID jid : jids) {
            shell.bot().tree().selectTreeItem(jid.getBase()).check();
        }
        shell.bot().button(FINISH).click();
    }

    public void confirmShellSessionInvitationAndAddProject(String projectName,
        TypeOfCreateProject usingWhichProject) throws RemoteException {
        if (!bot().isShellOpen(SHELL_SESSION_INVITATION)) {
            bot().waitLongUntilShellIsOpen(SHELL_SESSION_INVITATION);
        }
        bot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);
        confirmShellAddProjectUsingWhichProject(projectName, usingWhichProject);
        views().sessionView().waitUntilIsInSession();
    }

}
