package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.pages;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.SarosRmiSWTWorkbenchBot;

public class PopUpWindowObject implements IPopUpWindowObject {
    private transient static final Logger log = Logger
        .getLogger(PopUpWindowObject.class);

    private static transient SarosRmiSWTWorkbenchBot rmiBot;
    public static PopUpWindowObject classVariable;

    public PopUpWindowObject() {
        // Default constructor needed for RMI
    }

    public PopUpWindowObject(SarosRmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
    }

    public void confirmProblemOccurredWindow(String plainJID)
        throws RemoteException {
        rmiBot.waitUntilShellActive("Problem Occurred");
        RmiSWTWorkbenchBot.delegate.text().getText()
            .matches("*." + plainJID + ".*");
        rmiBot.waitUntilButtonEnabled(SarosConstant.BUTTON_OK);
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_OK).click();
    }

    public void confirmNewContactWindow(String plainJID) throws RemoteException {
        rmiBot.waitUntilShellActive(SarosConstant.SHELL_TITLE_NEW_CONTACT);
        RmiSWTWorkbenchBot.delegate.textWithLabel(
            SarosConstant.TEXT_LABEL_JABBER_ID).setText(plainJID);
        rmiBot.waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_FINISH).click();
    }

    public void comfirmInvitationWindow(String inviteeJID)
        throws RemoteException {
        rmiBot.waitUntilShellActive("Invitation");
        rmiBot.confirmWindowWithCheckBox("Invitation",
            SarosConstant.BUTTON_FINISH, inviteeJID);
    }

    public void confirmRequestOfSubscriptionReceivedWindow()
        throws RemoteException {
        rmiBot
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED);
        rmiBot.confirmWindow(
            SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED,
            SarosConstant.BUTTON_OK);
    }

    public void confirmInvitationWindow(String... invitees)
        throws RemoteException {
        rmiBot.windowObject
            .activateShellWithText(SarosConstant.SHELL_TITLE_INVITATION);
        rmiBot.confirmWindowWithCheckBox(SarosConstant.SHELL_TITLE_INVITATION,
            SarosConstant.BUTTON_FINISH, invitees);
    }

    public void confirmSessionInvitationWizard(String inviter,
        String projectname) throws RemoteException {
        rmiBot
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
            rmiBot.windowObject
                .activateShellWithText("Create New User Account");
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
        rmiBot.waitUntilButtonEnabled(SarosConstant.BUTTON_NEXT);
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_NEXT).click();
        // rmiBot.captureScreenshot(rmiBot.TEMPDIR +
        // "/acknowledge_project2.png");
        rmiBot.waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
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
        rmiBot.waitUntilShellCloses(RmiSWTWorkbenchBot.delegate
            .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
    }

    public void confirmSessionInvitationWindowStep2UsingExistProject(
        String projectName) throws RemoteException {
        RmiSWTWorkbenchBot.delegate.radio("Use existing project").click();
        RmiSWTWorkbenchBot.delegate.button("Browse").click();
        rmiBot.confirmWindowWithTree("Folder Selection",
            SarosConstant.BUTTON_OK, projectName);
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_FINISH).click();

        rmiBot.confirmWindow("Warning: Local changes will be deleted",
            SarosConstant.BUTTON_YES);
        if (rmiBot.isShellActive("Save Resource"))
            rmiBot.confirmWindow("Save Resource", SarosConstant.BUTTON_YES);
        rmiBot.waitUntilShellCloses(RmiSWTWorkbenchBot.delegate
            .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
    }

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(
        String projectName) throws RemoteException {
        RmiSWTWorkbenchBot.delegate.radio("Use existing project").click();
        RmiSWTWorkbenchBot.delegate.button("Browse").click();
        rmiBot.confirmWindowWithTree("Folder Selection",
            SarosConstant.BUTTON_OK, projectName);
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_FINISH).click();
        rmiBot.confirmWindow("Warning: Local changes will be deleted",
            SarosConstant.BUTTON_NO);
    }

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(
        String projectName) throws RemoteException {
        RmiSWTWorkbenchBot.delegate.radio("Use existing project").click();
        RmiSWTWorkbenchBot.delegate.button("Browse").click();
        rmiBot.confirmWindowWithTree("Folder Selection",
            SarosConstant.BUTTON_OK, projectName);
        RmiSWTWorkbenchBot.delegate.checkBox(
            "Create copy for working distributed. New project name:").click();
        RmiSWTWorkbenchBot.delegate.button(SarosConstant.BUTTON_FINISH).click();
        rmiBot.waitUntilShellCloses(RmiSWTWorkbenchBot.delegate
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
}
