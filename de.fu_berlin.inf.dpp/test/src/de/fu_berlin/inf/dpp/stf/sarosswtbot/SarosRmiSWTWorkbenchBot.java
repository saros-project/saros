package de.fu_berlin.inf.dpp.stf.sarosswtbot;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

/**
 * SarosRmiSWTWorkbenchBot controls Eclipse Saros from the GUI perspective. It
 * exports {@link ISarosState} via RMI. You should not use this within tests.
 * Have a look at {@link Musician} if you want to write tests.
 */
public class SarosRmiSWTWorkbenchBot extends RmiSWTWorkbenchBot implements
    ISarosRmiSWTWorkbenchBot {
    private static final transient Logger log = Logger
        .getLogger(SarosRmiSWTWorkbenchBot.class);

    public final static transient String TEMPDIR = System
        .getProperty("java.io.tmpdir");

    private static transient SarosRmiSWTWorkbenchBot self;

    /** RMI exported Saros state object */
    private ISarosState state;

    /** SarosRmiSWTWorkbenchBot is a singleton */
    public static SarosRmiSWTWorkbenchBot getInstance() {
        if (delegate != null && self != null)
            return self;

        SarosSWTWorkbenchBot swtwbb = new SarosSWTWorkbenchBot();
        self = new SarosRmiSWTWorkbenchBot(swtwbb);
        return self;
    }

    /** RmiSWTWorkbenchBot is a singleton, but inheritance is possible */
    protected SarosRmiSWTWorkbenchBot(SarosSWTWorkbenchBot bot) {
        super(bot);
    }

    /*************** RMI Methods ******************/

    /**
     * Export given state object by given name on our local RMI Registry.
     */
    public void exportState(SarosState state, String exportName) {
        try {
            this.state = (ISarosState) UnicastRemoteObject.exportObject(state,
                0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.state);
        } catch (RemoteException e) {
            log.error("Could not export stat object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind stat object, because it is bound already.", e);
        }
    }

    /*************** Saros-specific-highlevel RMI exported Methods ******************/

    public void accountBySarosMenu(String server, String username,
        String password) {
        try {
            delegate.menu("Saros").menu("Create Account").click();
            delegate.shell("Create New User Account").activate();
            delegate.textWithLabel("Jabber Server").setText(server);
            delegate.textWithLabel("Username").setText(username);
            delegate.textWithLabel("Password").setText(password);
            delegate.textWithLabel("Repeat Password").setText(password);
            delegate.buttonWithLabel("Finish").click();
        } catch (WidgetNotFoundException e) {
            log.error("widget not found while accountBySarosMenu", e);
        }
    }

    /**
     * First step: invitee acknowledge session to given inviter
     * 
     * This method captures two screenshots as side effect.
     */
    public void ackProjectStep1(String inviter) throws RemoteException {
        activateShellWithText(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        if (!equalFieldTextWithText(SarosConstant.TEXT_LABEL_INVITER, inviter))
            log.warn("inviter does not match: " + inviter);
        captureScreenshot(TEMPDIR + "/acknowledge_project1.png");
        waitUntilButtonEnabled(SarosConstant.BUTTON_NEXT);
        clickButton(SarosConstant.BUTTON_NEXT);
        captureScreenshot(TEMPDIR + "/acknowledge_project2.png");
        waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
    }

    /**
     * Second step: invitee acknowledge a new project
     * 
     * This method captures two screenshots as side effect.
     */
    public void ackProjectStep2UsingNewProject(String projectName)
        throws RemoteException {
        clickRadio(SarosConstant.RADIO_LABEL_CREATE_NEW_PROJECT);
        captureScreenshot(TEMPDIR + "/acknowledge_project3.png");
        clickButton(SarosConstant.BUTTON_FINISH);
        captureScreenshot(TEMPDIR + "/acknowledge_project4.png");
        waitUntilShellCloses(getShellWithText(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
    }

    public void ackProjectStep2UsingExistProject(String projectName)
        throws RemoteException {
        delegate.radio("Use existing project").click();
        delegate.sleep(750);
        delegate.button("Browse").click();
        activateShellWithText("Folder Selection");
        delegate.tree().getTreeItem(projectName).click();
        delegate.sleep(750);
        delegate.button("OK").click();
        delegate.sleep(750);
        delegate.button("Finish").click();
        delegate.sleep(750);
        confirmWindow("Warning: Local changes will be deleted", "Yes");
        waitUntilShellCloses(getShellWithText("Session Invitation"));
    }

    public void ackProjectStep2UsingExistProjectWithCopy(String projectName)
        throws RemoteException {
        delegate.radio("Use existing project").click();
        delegate.sleep(750);
        delegate
            .checkBox("Create copy for working distributed. New project name:");
        delegate.sleep(750);
        delegate.button("Finish").click();
        waitUntilShellCloses(getShellWithText("Session Invitation"));
    }

    public void addNewContact(String name) throws RemoteException {
        if (!isRosterViewOpen())
            addSarosSessionView();

        // delegate.viewByTitle("Roster").toolbarButton("Add a new contact")
        // .click();
        clickToolbarButtonWithTooltipInViewWithTitle(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_ADD_A_NEW_CONTACT);

        // new contact window
        // SWTBotShell shell = delegate.shell("New Contact");
        // while (!shell.isEnabled()) {
        // delegate.sleep(100);
        // log.debug("shell was not enabled");
        // shell = delegate.shell("New Contact");
        // }
        // shell.activate();
        activateShellWithText(SarosConstant.SHELL_TITLE_NEW_CONTACT);

        // try to add contact
        setTextWithLabel(SarosConstant.TEXT_LABEL_JABBER_ID, name);
        waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        clickButton(SarosConstant.BUTTON_FINISH);
        delegate.sleep(sleepTime);

        // // server respond with failure code 503, service unavailable, add
        // // contact anyway
        // try {
        // delegate.shell("Contact look-up failed").activate();
        // delegate.button("Yes").click();
        // } catch (WidgetNotFoundException e) {
        // // ignore, server responds
        // }
    }

    // public void ackContactAdded(String name) {
    // try {
    // delegate.shell("Request of subscription received").activate();
    // delegate.sleep(750);
    // delegate.button("OK").click();
    // delegate.sleep(750);
    // } catch (WidgetNotFoundException e) {
    // // ignore
    // }
    // }

    public void addSarosRosterView() throws RemoteException {
        openViewByName("Saros", "Roster");
    }

    public void addSarosSessionView() throws RemoteException {
        openViewByName("Saros", "Saros Session");
    }

    public void addToSharedProject(String invitee) throws RemoteException {
        setFocusOnViewByTitle("Shared Project Session");
        delegate.viewByTitle("Shared Project Session")
            .toolbarButton("Open invitation interface").click();
        selectCheckBoxInvitation(invitee);
    }

    /**
     * Fill up the configuration wizard with title "Saros Configuration".
     */

    public void doSarosConfiguration(String xmppServer, String jid,
        String password) throws RemoteException {

        delegate.shell(SarosConstant.SAROS_CONFI_SHELL_TITLE);
        setTextWithLabel(SarosConstant.TEXT_LABEL_JABBER_SERVER, xmppServer);
        delegate.sleep(sleepTime);
        setTextWithLabel(SarosConstant.TEXT_LABEL_USER_NAME, jid);
        delegate.sleep(sleepTime);
        setTextWithLabel(SarosConstant.TEXT_LABEL_PASSWORD, password);
        delegate.sleep(sleepTime);

        while (delegate.button("Next >").isEnabled()) {
            delegate.button("Next >").click();
            log.debug("click Next > Button.");
            delegate.sleep(sleepTime);
        }

        if (isButtonEnabled(SarosConstant.BUTTON_FINISH)) {
            clickButton(SarosConstant.BUTTON_FINISH);
            delegate.sleep(sleepTime);
            return;
        } else {
            System.out.println("can't click finish button");
        }

        throw new NotImplementedException(
            "only set text fields and click Finish is implemented.");
    }

    // public boolean isConfigShellPoppedUp() throws RemoteException {
    // try {
    // delegate.shell("Saros Configuration");
    // return true;
    // } catch (WidgetNotFoundException e) {
    // // ignore
    // }
    // return false;
    // }

    public boolean isConnectedByXmppGuiCheck() throws RemoteException {
        if (!isRosterViewOpen())
            return false;
        setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_ROSTER);
        SWTBotToolbarButton toolbarButton = getToolbarButtonWithTooltipInViewWithTitle(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_DISCONNECT);
        return (toolbarButton != null && toolbarButton.isVisible());
    }

    public boolean isContactInRosterView(String contact) throws RemoteException {
        if (!isRosterViewOpen())
            addSarosSessionView();
        if (!isConnectedByXmppGuiCheck())
            xmppConnect();
        setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_ROSTER);
        SWTBotTreeItem contact_added = selectTreeWithLabelsInViewWithTitle(
            SarosConstant.VIEW_TITLE_ROSTER, "Buddies", contact);
        return contact_added != null && contact_added.getText().equals(contact);
        // try {
        // SWTBotTree tree = delegate.viewByTitle("Roster").bot().tree();
        // if (tree != null) {
        // SWTBotTreeItem buddy = tree.getTreeItem("Buddies");
        // SWTBotTreeItem contact_added = buddy.getNode(contact).select();
        // delegate.sleep(1000);
        // return contact_added != null
        // && contact_added.getText().equals(contact);
        // }
        // } catch (WidgetNotFoundException e) {
        // log.warn("Contact not found: " + contact, e);
        // }
        // return false;
    }

    public boolean isContactOnline(String contact) {
        throw new NotImplementedException(
            "Can not be implemented, because no information is visible by swtbot. Enhance information with a tooltip or toher stuff.");
    }

    public void inviteToProject(String jid) throws RemoteException {
        SWTBotView view = delegate.viewByTitle("Shared Project Session");
        view.setFocus();
        delegate.sleep(750);
        view.toolbarButton("Open invitation interface").click();
        selectCheckBoxInvitation(jid);
        delegate.sleep(750);
        delegate.button("Finish").click();
    }

    /**
     * "Shared Project Session" View must be open
     */
    public boolean isInSession() {
        try {
            setFocusOnViewByTitle("Shared Project Session");
            return delegate.viewByTitle("Shared Project Session")
                .toolbarButton("Leave the session").isEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Returns true if the given jid was found in Shared Project Session View.
     */
    public boolean isInSharedProject(String jid) {
        SWTBotView sessionView = delegate.viewByTitle("Shared Project Session");
        SWTBot bot = sessionView.bot();

        try {
            SWTBotTable table = bot.table();
            SWTBotTableItem item = table.getTableItem(jid);
            return item != null;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean isRosterViewOpen() throws RemoteException {
        return isViewOpen(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public boolean isSharedSessionViewOpen() throws RemoteException {
        return isViewOpen(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
    }

    /**
     * "Shared Project Session" View must be open
     */
    public void leaveSession() throws RemoteException {
        setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
        clickToolbarButtonWithTooltipInViewWithTitle(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.CONTEXT_MENU_LEAVE_THE_SESSION);
        delegate.sleep(sleepTime);
        // delegate.viewByTitle("Shared Project Session").toolbarButton(
        // "Leave the session").click();
    }

    /**
     * Remove given contact from Roster, if contact was added before.
     */
    public void removeContact(String contact) throws RemoteException {
        if (!isContactInRosterView(contact))
            return;
        try {

            clickContextMenuOfTreeInViewWithTitle(
                SarosConstant.VIEW_TITLE_ROSTER,
                SarosConstant.CONTEXT_MENU_DELETE, SarosConstant.BUDDIES,
                contact);
            // SWTBotTree tree = delegate.viewByTitle("Roster").bot().tree();
            // if (tree != null) {
            // SWTBotTreeItem buddy = tree.getTreeItem("Buddies");
            // SWTBotTreeItem item = buddy.getNode(contact).select();
            // // remove by context menu
            // delegate.sleep(750);
            // item.contextMenu("Delete").click();
            // delegate.sleep(750);
            // confirm delete
            confirmWindow(SarosConstant.SHELL_TITLE_CONFIRM_DELETE,
                SarosConstant.BUTTON_YES);
            // activateShellByText(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
            // delegate.sleep(sleepTime);
            // clickButton(SarosConstant.BUTTON_YES);
            // delegate.button("Yes").click();

            // send backspace
            // item.pressShortcut(0, '\b'); // 0 == don't add keystroke

            delegate.sleep(sleepTime);
            // }
        } catch (WidgetNotFoundException e) {
            log.info("Contact not found: " + contact, e);
        }
    }

    /**
     * Select the given invitee
     */
    public void selectCheckBoxInvitation(String invitee) throws RemoteException {
        for (int i = 0; i < delegate.table().rowCount(); i++) {
            if (delegate.table().getTableItem(i).getText(0).equals(invitee)) {
                delegate.table().getTableItem(i).check();
                log.debug("found invitee: " + invitee);
                delegate.sleep(sleepTime);
                return;
            }
        }
    }

    public void selectCheckBoxInvitation(List<String> invitees) {
        for (int i = 0; i < delegate.table().rowCount(); i++) {
            String next = delegate.table().getTableItem(i).getText(0);
            if (invitees.contains(next)) {
                delegate.table().getTableItem(i).check();
            }
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

    /**
     * This method captures two screenshots as side effect.
     */
    public void shareProject(String projectName, String nameOfContextMenu,
        String invitee) throws RemoteException {

        // clickProjectContextMenu(projectName, nameOfContextMenu);
        clickContextMenuOfTreeInViewWithTitle(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, nameOfContextMenu,
            projectName);
        captureScreenshot(TEMPDIR + "/shareProjectStep1.png");
        selectCheckBoxInvitation(invitee);
        captureScreenshot(TEMPDIR + "/shareProjectStep2.png");
        delegate.button("Finish").click();
    }

    public void shareProjectSequential(String projectName,
        String nameOfContextMenu, List<String> invitees) throws RemoteException {
        shareProject(projectName, nameOfContextMenu, invitees.remove(0));
        for (String toInvite : invitees)
            inviteToProject(toInvite);
    }

    /**
     * This method captures two screenshots as side effect.
     */
    public void shareProjectParallel(String projectName, List<String> invitees)
        throws RemoteException {
        clickContextMenuOfTreeInViewWithTitle(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
            SarosConstant.SHARE_PROJECT, projectName);
        // clickProjectContextMenu(projectName, "Share project...");
        captureScreenshot(TEMPDIR + "/shareProjectStepParallel1.png");
        selectCheckBoxInvitation(invitees);
        captureScreenshot(TEMPDIR + "/shareProjectStepParallel2.png");
        // delegate.button("Finish").click();
        clickButton(SarosConstant.BUTTON_FINISH);
    }

    /**
     * Roster must be open
     */
    public void xmppConnect() throws RemoteException {
        setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_ROSTER);
        clickToolbarButtonWithTooltipInViewWithTitle(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_CONNECT);
        // while (delegate.viewByTitle("Roster").toolbarButton("Connect") ==
        // null
        // || !delegate.viewByTitle("Roster").toolbarButton("Connect")
        // .isEnabled())
        // delegate.sleep(100);
        // delegate.viewByTitle("Roster").toolbarButton("Connect").click();

    }

    /**
     * Roster must be open
     */
    public boolean xmppDisconnect() throws RemoteException {
        setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_ROSTER);
        return clickToolbarButtonWithTooltipInViewWithTitle(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_DISCONNECT) != null;

    }

    /*********** not exported Helper Methods *****************/

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

    public void follow(String participantJID, String sufix)
        throws RemoteException {
        if (!isSharedSessionViewOpen())
            addSarosSessionView();
        clickContextMenuOfTableInViewWithTitle(
            BotConfiguration.NAME_SESSION_VIEW, participantJID + sufix,
            SarosConstant.CONTEXT_MENU_FOLLOW_THIS_USER);
    }

    public void giveDriverRole(String inviteeJID) throws RemoteException {
        if (!isSharedSessionViewOpen())
            addSarosSessionView();
        clickContextMenuOfTableInViewWithTitle(
            BotConfiguration.NAME_SESSION_VIEW, inviteeJID,
            SarosConstant.CONTEXT_MENU_GIVE_DRIVER_ROLE);
    }

    public boolean isInFollowMode(String participantJID, String sufix)
        throws RemoteException {
        if (!isSharedSessionViewOpen())
            addSarosSessionView();
        setFocusOnViewByTitle(BotConfiguration.NAME_SESSION_VIEW);

        return existContextMenuOfTableItemOnView(
            BotConfiguration.NAME_SESSION_VIEW, participantJID + sufix,
            SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);

    }

}
