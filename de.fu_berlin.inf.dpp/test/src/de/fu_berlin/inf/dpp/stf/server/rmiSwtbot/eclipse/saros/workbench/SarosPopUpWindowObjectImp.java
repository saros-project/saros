package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipsePopUpWindowObjectImp;

public class SarosPopUpWindowObjectImp extends EclipsePopUpWindowObjectImp
    implements SarosPopUpWindowObject {

    public static SarosPopUpWindowObjectImp classVariable;

    public SarosPopUpWindowObjectImp(SarosRmiSWTWorkbenchBot rmiBot) {
        super(rmiBot);
    }

    public void IncomingScreensharingSession(String YesOrNot)
        throws RemoteException {
        rmiBot.exportedPopUpWindow.confirmWindow(
            SarosConstant.SHELL_TITLE_INCOMING_SCREENSHARING_SESSION, YesOrNot);
    }

    public void confirmProblemOccurredWindow(String plainJID)
        throws RemoteException {
        windowObject.waitUntilShellActive("Problem Occurred");
        RmiSWTWorkbenchBot.delegate.text().getText()
            .matches("*." + plainJID + ".*");
        basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_OK);
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_OK).click();
    }

    public void confirmNewContactWindow(String plainJID) throws RemoteException {
        windowObject
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_NEW_CONTACT);
        RmiSWTWorkbenchBot.delegate.textWithLabel(
            SarosConstant.TEXT_LABEL_JABBER_ID).setText(plainJID);
        basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_FINISH).click();
    }

    public void comfirmInvitationWindow(String inviteeJID)
        throws RemoteException {
        windowObject.waitUntilShellActive("Invitation");
        rmiBot.exportedPopUpWindow.confirmWindowWithCheckBox("Invitation",
            SarosConstant.BUTTON_FINISH, inviteeJID);
    }

    public void confirmRequestOfSubscriptionReceivedWindow()
        throws RemoteException {
        windowObject
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED);
        rmiBot.exportedPopUpWindow.confirmWindow(
            SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED,
            SarosConstant.BUTTON_OK);
    }

    public void confirmInvitationWindow(String... invitees)
        throws RemoteException {
        windowObject
            .activateShellWithText(SarosConstant.SHELL_TITLE_INVITATION);
        rmiBot.exportedPopUpWindow.confirmWindowWithCheckBox(
            SarosConstant.SHELL_TITLE_INVITATION, SarosConstant.BUTTON_FINISH,
            invitees);
    }

    public void confirmSessionInvitationWizard(String inviter,
        String projectname) throws RemoteException {
        windowObject
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingNewproject(projectname);
    }

    public void confirmSessionInvitationWizardUsingExistProject(String inviter,
        String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingExistProject(projectName);
    }

    public void confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
        String inviter, String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(projectName);
    }

    public void confirmSessionInvitationWizardUsingExistProjectWithCopy(
        String inviter, String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(projectName);
    }

    public void confirmCreateNewUserAccountWindow(String server,
        String username, String password) throws RemoteException {
        try {
            windowObject.activateShellWithText("Create New User Account");
            RmiSWTWorkbenchBot.delegate.textWithLabel("Jabber Server").setText(
                server);
            RmiSWTWorkbenchBot.delegate.textWithLabel("Username").setText(
                username);
            RmiSWTWorkbenchBot.delegate.textWithLabel("Password").setText(
                password);
            RmiSWTWorkbenchBot.delegate.textWithLabel("Repeat Password")
                .setText(password);
            RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_FINISH)
                .click();
        } catch (WidgetNotFoundException e) {
            log.error("widget not found while accountBySarosMenu", e);
        }
    }

    /**
     * First step: invitee acknowledge session to given inviter
     * 
     * This method captures two screenshots as side effect.
     */
    public void confirmSessionInvitationWindowStep1() throws RemoteException {
        // if (!isTextWithLabelEqualWithText(SarosConstant.TEXT_LABEL_INVITER,
        // inviter))
        // log.warn("inviter does not match: " + inviter);
        // rmiBot.captureScreenshot(rmiBot.TEMPDIR +
        // "/acknowledge_project1.png");
        basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_NEXT);
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_NEXT).click();
        // rmiBot.captureScreenshot(rmiBot.TEMPDIR +
        // "/acknowledge_project2.png");
        basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
    }

    /**
     * Second step: invitee acknowledge a new project
     * 
     * This method captures two screenshots as side effect.
     */
    public void confirmSessionInvitationWindowStep2UsingNewproject(
        String projectName) throws RemoteException {
        RmiSWTWorkbenchBot.delegate.radio(
            SarosConstant.RADIO_LABEL_CREATE_NEW_PROJECT).click();
        // rmiBot.captureScreenshot(rmiBot.TEMPDIR +
        // "/acknowledge_project3.png");
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_FINISH).click();
        // rmiBot.captureScreenshot(rmiBot.TEMPDIR +
        // "/acknowledge_project4.png");
        windowObject.waitUntilShellCloses(RmiSWTWorkbenchBot.delegate
            .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
    }

    public void confirmSessionInvitationWindowStep2UsingExistProject(
        String projectName) throws RemoteException {
        RmiSWTWorkbenchBot.delegate.radio("Use existing project").click();
        RmiSWTWorkbenchBot.delegate.button("Browse").click();
        windowObject.confirmWindowWithTree("Folder Selection",
            SarosConstant.BUTTON_OK, projectName);
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_FINISH).click();

        rmiBot.exportedPopUpWindow.confirmWindow(
            "Warning: Local changes will be deleted", SarosConstant.BUTTON_YES);

        /*
         * if there are some files locally, which are not saved yet, you will
         * get a popup window with the title "Save Resource" after you comfirm
         * the window "Warning: Local changes will be deleted" with YES.
         */
        if (rmiBot.exportedPopUpWindow.isShellActive("Save Resource")) {
            rmiBot.exportedPopUpWindow.confirmWindow("Save Resource",
                SarosConstant.BUTTON_YES);
            /*
             * it take some more time for the session invitation if you don't
             * save your files locally. So rmiBot need to wait until the
             * invitation is finished.
             */
            windowObject.waitUntilShellCloses(RmiSWTWorkbenchBot.delegate
                .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
        }

        /*
         * after the release 10.10.28 the time of sharing project has become so
         * fast, that the pop up window "Session Invitation" is immediately
         * disappeared after you confirm the window ""Warning: Local changes
         * will be deleted".
         * 
         * So i have to check first,whether the window "Session Invitation" is
         * still open at all before i run the waitUntilShellCloses(it guarantees
         * that rmiBot wait until the invitation is finished). Otherwise you may
         * get the WidgetNotfoundException.
         */
        if (rmiBot.exportedPopUpWindow
            .isShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION)) {
            windowObject.waitUntilShellCloses(RmiSWTWorkbenchBot.delegate
                .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
        }

    }

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(
        String projectName) throws RemoteException {
        RmiSWTWorkbenchBot.delegate.radio("Use existing project").click();
        RmiSWTWorkbenchBot.delegate.button("Browse").click();
        windowObject.confirmWindowWithTree("Folder Selection",
            SarosConstant.BUTTON_OK, projectName);
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_FINISH).click();
        rmiBot.exportedPopUpWindow.confirmWindow(
            "Warning: Local changes will be deleted", SarosConstant.BUTTON_NO);
    }

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(
        String projectName) throws RemoteException {
        RmiSWTWorkbenchBot.delegate.radio("Use existing project").click();
        RmiSWTWorkbenchBot.delegate.button("Browse").click();
        windowObject.confirmWindowWithTree("Folder Selection",
            SarosConstant.BUTTON_OK, projectName);
        RmiSWTWorkbenchBot.delegate.checkBox(
            "Create copy for working distributed. New project name:").click();
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_FINISH).click();
        windowObject.waitUntilShellCloses(RmiSWTWorkbenchBot.delegate
            .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
    }

    /**
     * Fill up the configuration wizard with title "Saros Configuration".
     */

    public void confirmSarosConfigurationWizard(String xmppServer, String jid,
        String password) throws RemoteException {
        rmiBot.windowObject
            .activateShellWithText(SarosConstant.SAROS_CONFI_SHELL_TITLE);
        RmiSWTWorkbenchBot.delegate.textWithLabel(
            SarosConstant.TEXT_LABEL_JABBER_SERVER).setText(xmppServer);
        RmiSWTWorkbenchBot.delegate.sleep(rmiBot.sleepTime);
        RmiSWTWorkbenchBot.delegate.textWithLabel(
            SarosConstant.TEXT_LABEL_USER_NAME).setText(jid);
        RmiSWTWorkbenchBot.delegate.sleep(rmiBot.sleepTime);
        RmiSWTWorkbenchBot.delegate.textWithLabel(
            SarosConstant.TEXT_LABEL_PASSWORD).setText(password);
        RmiSWTWorkbenchBot.delegate.textWithLabel("Confirm:").setText(password);
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_FINISH).click();

        // while (delegate.button("Next >").isEnabled()) {
        // delegate.button("Next >").click();
        // log.debug("click Next > Button.");
        // delegate.sleep(sleepTime);
        // }
        //
        // if (delegate.button(SarosConstant.BUTTON_FINISH).isEnabled()) {
        // delegate.button(SarosConstant.BUTTON_FINISH).click();
        // return;
        // } else {
        // System.out.println("can't click finish button");
        // }
        // throw new NotImplementedException(
        // "only set text fields and click Finish is implemented.");
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

    public void confirmInvitationCancelledWindow() throws RemoteException {
        SWTBotShell shell = bot.shell("Invitation Cancelled");
        shell.activate().setFocus();
        SWTBotButton button = shell.bot().button();
        button.click();
    }

    public void cancelInivtationInSessionInvitationWindow()
        throws RemoteException {
        SWTBotShell shell = bot.activeShell();
        shell.bot().toolbarButton().click();
    }

    public void confirmSessionUsingNewOrExistProject(JID inviterJID,
        String projectName, int typeOfSharingProject) throws RemoteException {
        waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        switch (typeOfSharingProject) {
        case SarosConstant.CREATE_NEW_PROJECT:
            confirmSessionInvitationWizard(inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT:
            confirmSessionInvitationWizardUsingExistProject(
                inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE:
            confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
                inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_COPY:
            confirmSessionInvitationWizardUsingExistProjectWithCopy(
                inviterJID.getBase(), projectName);
            break;
        default:
            break;
        }
    }

}
