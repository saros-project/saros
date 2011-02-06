package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.contextMenu;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfShareProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class SarosCImp extends EclipseComponentImp implements SarosC {

    private static transient SarosCImp self;

    /**
     * {@link SarosCImp} is a singleton, but inheritance is possible.
     */
    public static SarosCImp getInstance() {
        if (self != null)
            return self;
        self = new SarosCImp();
        return self;
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

    public void shareProjectWith(String viewTitle, String projectName,
        TypeOfShareProject howToshareProject, String[] baseJIDOfInvitees)
        throws RemoteException {
        switch (howToshareProject) {
        case SHARE_PROJECT:
            clickContextMenushareProject(viewTitle, projectName);
            break;
        case SHARE_PROJECT_PARTICALLY:
            clickContextMemnuShareProjectPartically(viewTitle, projectName);
            break;
        case ADD_SESSION:
            clickContextMenuAddToSession(viewTitle, projectName);
            break;
        default:
            break;
        }
        confirmShellInvitation(baseJIDOfInvitees);
    }

    public void shareProject(String viewTitle, String projectName,
        String... baseJIDOfInvitees) throws RemoteException {
        clickContextMenushareProject(viewTitle, projectName);
        confirmShellInvitation(baseJIDOfInvitees);
    }

    public void confirmShellAddProjectWithNewProject(String projectname)
        throws RemoteException {
        if (!shellW.activateShell(SHELL_SHELL_ADD_PROJECT))
            shellW.waitUntilShellActive(SHELL_SHELL_ADD_PROJECT);
        bot.radio(RADIO_CREATE_NEW_PROJECT).click();
        buttonW.clickButton(FINISH);

        try {
            shellW.waitLongUntilShellClosed(SHELL_SHELL_ADD_PROJECT);
        } catch (Exception e) {
            /*
             * sometimes session can not be completely builded because of
             * unclear reason, so after timeout STF try to close
             * "the sesion invitation" window, but it can't close the window
             * before stopping the invitation process. In this case a Special
             * treatment should be done, so that the following tests still will
             * be run.
             */
            workbench.captureScreenshot(workbench.getPathToScreenShot()
                + "/sessionInvitationFailedUsingNewProject.png");
            if (bot.activeShell().getText().equals(SHELL_SHELL_ADD_PROJECT)) {
                bot.activeShell().bot().toggleButton().click();
            }
            throw new RuntimeException("session invitation is failed!");
        }

    }

    public void confirmShellAddProjectUsingExistProject(String projectName)
        throws RemoteException {
        if (!shellW.activateShell(SHELL_SHELL_ADD_PROJECT))
            shellW.waitUntilShellActive(SHELL_SHELL_ADD_PROJECT);
        bot.radio(RADIO_USING_EXISTING_PROJECT).click();

        // bot.button(BUTTON_BROWSE).click();
        // windowPart.activateShellWithText(FOLDER_SELECTION);
        // windowPart.confirmWindowWithTree(FOLDER_SELECTION, OK,
        // helperPart.changeToRegex(projectName));
        // windowPart.waitUntilShellCloses(FOLDER_SELECTION);
        bot.textWithLabel("Project name", 1).setText(projectName);
        bot.button(FINISH).click();
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
        if (shellW.isShellOpen(SHELL_WARNING_LOCAL_CHANGES_DELETED))
            shellW.confirmShell(SHELL_WARNING_LOCAL_CHANGES_DELETED, YES);

        if (shellW.isShellActive("Save Resource")) {
            shellW.confirmShell("Save Resource", YES);
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
        if (shellW.isShellActive(SHELL_SHELL_ADD_PROJECT)) {
            try {
                shellW.waitLongUntilShellClosed(SHELL_SHELL_ADD_PROJECT);
            } catch (Exception e) {
                workbench.captureScreenshot(workbench.getPathToScreenShot()
                    + "/sessionInvitationFailedUsingExistProject.png");
                if (bot.activeShell().getText().equals(SHELL_SHELL_ADD_PROJECT)) {
                    bot.activeShell().bot().toggleButton().click();
                }
            }
        }
    }

    public void confirmShellAddProjectUsingExistProjectWithCopyAfterCancelLocalChange(
        String projectName) throws RemoteException {
        if (!shellW.activateShell(SHELL_SHELL_ADD_PROJECT))
            shellW.waitUntilShellActive(SHELL_SHELL_ADD_PROJECT);
        bot.radio("Use existing project").click();
        bot.textWithLabel("Project name", 1).setText(projectName);
        bot.button(FINISH).click();
        shellW.confirmShell(SHELL_WARNING_LOCAL_CHANGES_DELETED, NO);

        confirmShellAddProjectUsingExistProjectWithCopy(projectName);
    }

    public void confirmShellAddProjectUsingExistProjectWithCopy(
        String projectName) throws RemoteException {
        if (!shellW.activateShell(SHELL_SHELL_ADD_PROJECT))
            shellW.waitUntilShellActive(SHELL_SHELL_ADD_PROJECT);
        bot.radio("Use existing project").click();
        bot.checkBox("Create copy for working distributed. New project name:")
            .click();
        bot.button(FINISH).click();
        shellW.waitLongUntilShellClosed(SHELL_SHELL_ADD_PROJECT);
    }

    public void confirmShellSessionnInvitation() throws RemoteException {
        if (!shellW.activateShell(SHELL_SESSION_INVITATION))
            shellW.waitUntilShellActive(SHELL_SESSION_INVITATION);
        bot.button(FINISH).click();
    }

    public void confirmWindowInvitationCancelled() throws RemoteException {
        SWTBotShell shell = bot.shell(SHELL_INVITATION_CANCELLED);
        shell.activate().setFocus();
        SWTBotButton button = shell.bot().button();
        button.click();
    }

    public void confirmShellAddProjectUsingWhichProject(String projectName,
        TypeOfCreateProject usingWhichProject) throws RemoteException {
        shellW.waitUntilShellOpen(SHELL_SHELL_ADD_PROJECT);
        shellW.activateShell(SHELL_SHELL_ADD_PROJECT);
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

    public void closeShellInvitationCancelled() throws RemoteException {
        shellW.closeShell(SHELL_INVITATION_CANCELLED);
    }

    public void closeShellSessionInvitation() throws RemoteException {
        shellW.closeShell(SHELL_SESSION_INVITATION);
    }

    public void clickContextMenushareProject(String viewTitle,
        String projectName) throws RemoteException {
        precondition(viewTitle);
        String[] matchTexts = changeToRegex(projectName);
        String[] contexts = { CM_SAROS, CM_SHARE_PROJECT };
        treeW
            .clickContextMenusOfTreeItemInView(viewTitle, contexts, matchTexts);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isShellInvitationCancelledActive() throws RemoteException {
        return shellW.isShellActive(SHELL_INVITATION_CANCELLED);
    }

    public boolean isShellSessionInvitationActive() throws RemoteException {
        return shellW.isShellActive(SHELL_SESSION_INVITATION);
    }

    public String getSecondLabelOfShellProblemOccurred() throws RemoteException {
        return bot.shell(SHELL_PROBLEM_OCCURRED).bot().label(2).getText();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/

    public void waitUntilIsShellInvitationCnacelledActive()
        throws RemoteException {
        shellW.waitUntilShellActive(SHELL_INVITATION_CANCELLED);
    }

    public void waitUntilIsShellSessionInvitationActive()
        throws RemoteException {
        shellW.waitUntilShellActive(SHELL_SESSION_INVITATION);
    }

    public void waitUntilIsShellProblemOccurredActive() throws RemoteException {
        shellW.isShellActive(SHELL_PROBLEM_OCCURRED);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    /**
     * Clicks the sub menu "Share project partically" of the context menu
     * "Saros" of the given project in the package explorer view.
     * 
     * @param projectName
     *            the name of the project, which you want to share with other
     *            peoples.
     * @throws RemoteException
     */
    private void clickContextMemnuShareProjectPartically(String viewTitle,
        String projectName) throws RemoteException {
        precondition(viewTitle);
        clickContextMenuOfSaros(viewTitle, projectName,
            CM_SHARE_PROJECT_PARTIALLY);
    }

    /**
     * Clicks the sub menu "Add to session" of the context menu "Saros" of the
     * given project in the package explorer view.
     * 
     * @param projectName
     *            the name of the project, which you want to share with other
     *            peoples.
     */
    private void clickContextMenuAddToSession(String viewTitle,
        String projectName) throws RemoteException {
        precondition(viewTitle);
        clickContextMenuOfSaros(viewTitle, projectName, CM_ADD_TO_SESSION);
    }

    /**
     * Clicks the given sub menu of the context menu "Saros" of the given
     * project in the package explorer view.
     * 
     * @param projectName
     *            the name of the project, which you want to share with other
     *            peoples.
     * @throws RemoteException
     */
    private void clickContextMenuOfSaros(String viewTitle, String projectName,
        String contextName) throws RemoteException {
        String[] matchTexts = changeToRegex(projectName);
        String[] contexts = { CM_SAROS, contextName };
        treeW
            .clickContextMenusOfTreeItemInView(viewTitle, contexts, matchTexts);
    }

    protected void precondition(String viewTitle) throws RemoteException {
        viewW.openViewById(viewTitlesAndIDs.get(viewTitle));
        viewW.setFocusOnViewByTitle(viewTitle);
    }

}
