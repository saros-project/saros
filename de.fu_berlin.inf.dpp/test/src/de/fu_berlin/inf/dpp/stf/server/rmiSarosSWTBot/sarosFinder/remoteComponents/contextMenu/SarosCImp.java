package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.SarosComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.PEView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.PEViewImp;

public class SarosCImp extends SarosComponentImp implements SarosC {

    private static transient SarosCImp self;

    private PEView view;

    /**
     * {@link SarosCImp} is a singleton, but inheritance is possible.
     */
    public static SarosCImp getInstance() {
        if (self != null)
            return self;
        self = new SarosCImp();
        return self;
    }

    public void setView(PEViewImp view) {
        this.view = view;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void shareProjectWith(String projectName, String howToshareProject,
        String[] baseJIDOfInvitees) throws RemoteException {
        view.selectProject(projectName)
            .contextMenu(CM_SAROS, howToshareProject).click();
        confirmShellInvitation(baseJIDOfInvitees);
    }

    public void shareProject(String projectName, String... baseJIDOfInvitees)
        throws RemoteException {
        view.selectProject(projectName).contextMenu(CM_SAROS, CM_SHARE_PROJECT)
            .click();
        confirmShellInvitation(baseJIDOfInvitees);
    }

    public void confirmShellAddProjectWithNewProject(String projectname)
        throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_ADD_PROJECT);
        if (!shell.activate())
            shell.waitUntilActive();
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
        bot().waitUntilShellIsOpen(SHELL_ADD_PROJECT);
        STFBotShell shell = bot().shell(SHELL_ADD_PROJECT);
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
        if (bot().isShellOpen(SHELL_WARNING_LOCAL_CHANGES_DELETED))
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
        bot().waitUntilShellIsOpen(SHELL_ADD_PROJECT);
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
        STFBotShell shell = bot().shell(SHELL_ADD_PROJECT);
        if (!shell.activate())
            shell.waitUntilActive();

        shell.bot().radio("Use existing project").click();
        shell.bot()
            .checkBox("Create copy for working distributed. New project name:")
            .click();
        shell.bot().button(FINISH).click();
        bot().shell(SHELL_ADD_PROJECT).waitLongUntilIsClosed();
    }

    public void confirmShellSessionnInvitation() throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_SESSION_INVITATION);
        if (!shell.activate())
            shell.waitUntilActive();
        shell.bot().button(FINISH).click();
    }

    public void confirmWindowInvitationCancelled() throws RemoteException {
        STFBotShell shell = bot().shell(SHELL_INVITATION_CANCELLED);
        shell.activate();
        shell.setFocus();
        shell.bot().button().click();
    }

    public void confirmShellAddProjectUsingWhichProject(String projectName,
        TypeOfCreateProject usingWhichProject) throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_ADD_PROJECT);
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

}
