package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest.TypeOfShareProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.BasicComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PEViewComponentImp;

public class SarosPEViewComponentImp extends PEViewComponentImp implements
    SarosPEViewComponent {

    private static transient SarosPEViewComponentImp self;

    public final static int CREATE_NEW_PROJECT = 1;
    public final static int USE_EXISTING_PROJECT = 2;
    public final static int USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE = 3;
    public final static int USE_EXISTING_PROJECT_WITH_COPY = 4;
    /*
     * title of shells which are pop up by performing the actions on the package
     * explorer view.
     */
    private final static String INVITATION = "Invitation";
    private final static String INVITATIONCANCELLED = "Invitation Cancelled";
    private final static String SESSION_INVITATION = "Session Invitation";
    private final static String PROBLEMOCCURRED = "Problem Occurred";
    private final static String WARNING_LOCAL_CHANGES_DELETED = "Warning: Local changes will be deleted";
    private final static String FOLDER_SELECTION = "Folder Selection";
    private final static String SHELL_SAVE_RESOURCE = "Save all files now?";

    /* Context menu of a selected tree item on the package explorer view */
    private final static String SAROS = "Saros";

    /* All the sub menus of the context menu "Saros" */
    private final static String SHARE_PROJECT = "Share project...";
    private final static String SHARE_PROJECT_PARTIALLY = "Share project partially (experimental)...";
    private final static String ADD_TO_SESSION = "Add to session (experimental)...";
    private final static String BUTTON_BROWSE = "Browse";

    /*
     * second page of the wizard "Session invitation"
     */
    private final static String RADIO_USING_EXISTING_PROJECT = "Use existing project";
    private final static String RADIO_CREATE_NEW_PROJECT = "Create new project";

    /**
     * {@link BasicComponentImp} is a singleton, but inheritance is possible.
     */
    public static SarosPEViewComponentImp getInstance() {
        if (self != null)
            return self;
        self = new SarosPEViewComponentImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    public void shareProjectWith(String projectName,
        TypeOfShareProject howToshareProject, String[] baseJIDOfInvitees)
        throws RemoteException {
        switch (howToshareProject) {
        case SHARE_PROJECT:
            clickContextMenushareProject(projectName);
            break;
        case SHARE_PROJECT_PARTICALLY:
            clickContextMemnuShareProjectPartically(projectName);
            break;
        case ADD_SESSION:
            clickContextMenuAddToSession(projectName);
            break;
        default:
            break;
        }
        confirmWindowInvitation(baseJIDOfInvitees);
    }

    public void shareProject(String projectName, String... baseJIDOfInvitees)
        throws RemoteException {
        clickContextMenushareProject(projectName);
        confirmWindowInvitation(baseJIDOfInvitees);
    }

    public void confirmWindowInvitation(String... baseJIDOfinvitees)
        throws RemoteException {
        shellC.activateShellWithText(INVITATION);
        shellC.confirmWindowWithCheckBox(INVITATION, FINISH, baseJIDOfinvitees);
    }

    public void confirmWirzardSessionInvitationWithNewProject(String projectname)
        throws RemoteException {
        if (!shellC.activateShellWithText(SESSION_INVITATION))
            shellC.waitUntilShellActive(SESSION_INVITATION);
        confirmFirstPageOfWizardSessionInvitation();
        confirmSecondPageOfWizardSessionInvitationUsingNewproject();

    }

    public void confirmWizardSessionInvitationUsingExistProject(
        String projectName) throws RemoteException {
        confirmFirstPageOfWizardSessionInvitation();
        confirmSecondPageOfWizardSessionInvitationUsingExistProject(projectName);
    }

    public void confirmWizardSessionInvitationUsingExistProjectWithCopyAfterCancelLocalChange(
        String projectName) throws RemoteException {
        confirmFirstPageOfWizardSessionInvitation();
        confirmSecondPageOfWizardSessionInvitationUsingExistProjectWithCancelLocalChange(projectName);
    }

    public void confirmWizardSessionInvitationUsingExistProjectWithCopy(
        String projectName) throws RemoteException {
        confirmFirstPageOfWizardSessionInvitation();
        confirmSecondPageOfWizardSessionInvitationUsingExistProjectWithCopy(projectName);
    }

    public void confirmFirstPageOfWizardSessionInvitation()
        throws RemoteException {
        bot.button(NEXT).click();
        basicC.waitUntilButtonEnabled(FINISH);
    }

    public void confirmSecondPageOfWizardSessionInvitationUsingNewproject()
        throws RemoteException {
        bot.radio(RADIO_CREATE_NEW_PROJECT).click();
        bot.button(FINISH).click();
        shellC.waitUntilShellClosed(SESSION_INVITATION);
        // try {
        // shellC.waitLongUntilShellClosed(bot.shell(SESSION_INVITATION));
        // } catch (TimeoutException e) {
        /*
         * sometimes session can not be completely builded because of unclear
         * reason, so after timeout STF try to close "the sesion invitation"
         * window, but it can't close the window before stopping the invitation
         * process. In this case a Special treatment should be done, so that the
         * following tests still will be run.
         */
        // basicC.captureScreenshot(basicC.getPathToScreenShot()
        // + "/sessionInvitationFailedUsingNewProject.png");
        // if (bot.activeShell().getText().equals(SESSION_INVITATION)) {
        // bot.activeShell().bot().toggleButton().click();
        // }
        // throw new RuntimeException("session invitation is failed!");
        // }
    }

    public void confirmSecondPageOfWizardSessionInvitationUsingExistProject(
        String projectName) throws RemoteException {
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
        if (shellC.isShellOpen(WARNING_LOCAL_CHANGES_DELETED))
            shellC.confirmShell(WARNING_LOCAL_CHANGES_DELETED, YES);

        if (shellC.isShellActive("Save Resource")) {
            shellC.confirmShell("Save Resource", YES);
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
        if (shellC.isShellActive(SESSION_INVITATION)) {
            shellC.waitUntilShellClosed(SESSION_INVITATION);
            // try {
            // shellC.waitLongUntilShellClosed(bot.shell(SESSION_INVITATION));
            // } catch (TimeoutException e) {
            // basicC.captureScreenshot(basicC.getPathToScreenShot()
            // + "/sessionInvitationFailedUsingExistProject.png");
            // if (bot.activeShell().getText().equals(SESSION_INVITATION)) {
            // bot.activeShell().bot().toggleButton().click();
            // }
            // }
        }
    }

    public void confirmSecondPageOfWizardSessionInvitationUsingExistProjectWithCancelLocalChange(
        String projectName) throws RemoteException {
        bot.radio("Use existing project").click();
        bot.textWithLabel("Project name", 1).setText(projectName);
        bot.button(FINISH).click();
        shellC.confirmShell(WARNING_LOCAL_CHANGES_DELETED, NO);
    }

    public void confirmSecondPageOfWizardSessionInvitationUsingExistProjectWithCopy(
        String projectName) throws RemoteException {
        bot.radio("Use existing project").click();
        bot.checkBox("Create copy for working distributed. New project name:")
            .click();
        bot.button(FINISH).click();
        shellC.waitUntilShellClosed(SESSION_INVITATION);
    }

    public void confirmWindowInvitationCancelled() throws RemoteException {
        SWTBotShell shell = bot.shell(INVITATIONCANCELLED);
        shell.activate().setFocus();
        SWTBotButton button = shell.bot().button();
        button.click();
    }

    public void confirmWizardSessionInvitationUsingWhichProject(
        String projectName, TypeOfCreateProject usingWhichProject)
        throws RemoteException {
        shellC.waitUntilShellActive(SESSION_INVITATION);
        switch (usingWhichProject) {
        case NEW_PROJECT:
            confirmWirzardSessionInvitationWithNewProject(projectName);
            break;
        case EXIST_PROJECT:
            confirmWizardSessionInvitationUsingExistProject(projectName);
            break;
        case EXIST_PROJECT_WITH_COPY:
            confirmWizardSessionInvitationUsingExistProjectWithCopy(projectName);
            break;
        case EXIST_PROJECT_WITH_COPY_AFTER_CANCEL_LOCAL_CHANGE:
            confirmWizardSessionInvitationUsingExistProjectWithCopyAfterCancelLocalChange(projectName);
            break;
        default:
            break;
        }
    }

    public boolean isWindowInvitationCancelledActive() throws RemoteException {
        return shellC.isShellActive(INVITATIONCANCELLED);
    }

    public void closeWindowInvitationCancelled() throws RemoteException {
        shellC.closeShell(INVITATIONCANCELLED);
    }

    public void waitUntilWindowInvitationCnacelledActive()
        throws RemoteException {
        shellC.waitUntilShellActive(INVITATIONCANCELLED);
    }

    public boolean isWIndowSessionInvitationActive() throws RemoteException {
        return shellC.isShellActive(SESSION_INVITATION);
    }

    public void closeWIndowSessionInvitation() throws RemoteException {
        shellC.closeShell(SESSION_INVITATION);
    }

    public void waitUntilWindowSessionInvitationActive() throws RemoteException {
        shellC.waitUntilShellActive(SESSION_INVITATION);
    }

    public void waitUntilWindowProblemOccurredActive() throws RemoteException {
        shellC.isShellActive(PROBLEMOCCURRED);
    }

    public String getSecondLabelOfWindowProblemOccurred()
        throws RemoteException {
        return bot.shell(PROBLEMOCCURRED).bot().label(2).getText();
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    /**
     * Clicks the sub menu "Share project" of the context menu "Saros" of the
     * given project in the package explorer view.
     * 
     * @param projectName
     *            the name of the project, which you want to share with other
     *            peoples.
     * @throws RemoteException
     */
    private void clickContextMenushareProject(String projectName)
        throws RemoteException {
        precondition();
        String[] matchTexts = helperPart.changeToRegex(projectName);
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            matchTexts, SAROS, SHARE_PROJECT);
    }

    /**
     * Clicks the sub menu "Share project partically" of the context menu
     * "Saros" of the given project in the package explorer view.
     * 
     * @param projectName
     *            the name of the project, which you want to share with other
     *            peoples.
     * @throws RemoteException
     */
    private void clickContextMemnuShareProjectPartically(String projectName)
        throws RemoteException {
        precondition();
        clickContextMenuOfSaros(projectName, SHARE_PROJECT_PARTIALLY);
    }

    /**
     * Clicks the sub menu "Add to session" of the context menu "Saros" of the
     * given project in the package explorer view.
     * 
     * @param projectName
     *            the name of the project, which you want to share with other
     *            peoples.
     */
    private void clickContextMenuAddToSession(String projectName)
        throws RemoteException {
        precondition();
        clickContextMenuOfSaros(projectName, ADD_TO_SESSION);
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
    private void clickContextMenuOfSaros(String projectName, String contextName)
        throws RemoteException {
        String[] matchTexts = helperPart.changeToRegex(projectName);
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            matchTexts, SAROS, contextName);
    }

}
