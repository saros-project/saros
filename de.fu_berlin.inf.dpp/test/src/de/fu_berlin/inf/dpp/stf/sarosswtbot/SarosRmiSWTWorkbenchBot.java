package de.fu_berlin.inf.dpp.stf.sarosswtbot;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;

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

        SWTWorkbenchBot swtwbb = new SWTWorkbenchBot();
        self = new SarosRmiSWTWorkbenchBot(swtwbb);
        return self;
    }

    /** RmiSWTWorkbenchBot is a singleton, but inheritance is possible */
    protected SarosRmiSWTWorkbenchBot(SWTWorkbenchBot bot) {
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
    public void ackProject1(String inviter) throws RemoteException {
        activateShellByText("Session Invitation");

        if (!delegate.textWithLabel("Inviter").getText().equals(inviter))
            log.warn("inviter does not match: " + inviter);

        captureScreenshot(TEMPDIR + "/acknowledge_project1.png");
        try {
            while (!delegate.button("Next >").isEnabled()) {
                delegate.sleep(100);
            }
        } catch (Exception e) {
            // next window opened
        }
        delegate.button("Next >").click();
        captureScreenshot(TEMPDIR + "/acknowledge_project2.png");
        try {
            while (!delegate.button("Finish").isEnabled()) {
                delegate.sleep(100);
            }
        } catch (Exception e) {
            // next window opened
        }
    }

    /**
     * Second step: invitee acknowledge a new project
     * 
     * This method captures two screenshots as side effect.
     */
    public void ackNewProject2(String projectName) throws RemoteException {
        delegate.radio("Create new project").click();
        captureScreenshot(TEMPDIR + "/acknowledge_project3.png");
        delegate.button("Finish").click();
        captureScreenshot(TEMPDIR + "/acknowledge_project4.png");
        try {
            while (true) {
                delegate.shell("Session Invitation");
                delegate.sleep(100);
            }
        } catch (Exception e) {
            // window closed
        }
    }

    public void addNewContact(String name) throws RemoteException {
        if (!isRosterViewOpen())
            addSarosSessionView();

        delegate.viewByTitle("Roster").toolbarButton("Add a new contact")
            .click();

        // new contact window
        SWTBotShell shell = delegate.shell("New Contact");
        while (!shell.isEnabled()) {
            delegate.sleep(100);
            log.debug("shell was not enabled");
            shell = delegate.shell("New Contact");
        }
        shell.activate();

        // try to add contact
        SWTBotText text = delegate.textWithLabel("Jabber ID");
        text.setText(name);
        while (!delegate.button("Finish").isEnabled()) {
            delegate.sleep(100);
            log.debug("Finish button is not enabled");
        }
        SWTBotButton button = delegate.button("Finish");
        button.click();

        // // server respond with failure code 503, service unavailable, add
        // // contact anyway
        // try {
        // delegate.shell("Contact look-up failed").activate();
        // delegate.button("Yes").click();
        // } catch (WidgetNotFoundException e) {
        // // ignore, server responds
        // }
    }

    public void ackContactAdded(String name) {
        try {
            delegate.shell("Request of subscription received").activate();
            delegate.sleep(750);
            delegate.button("OK").click();
            delegate.sleep(750);
        } catch (WidgetNotFoundException e) {
            // ignore
        }
    }

    public void addSarosRosterView() {
        openViewByName("Saros", "Roster");
    }

    public void addSarosSessionView() {
        openViewByName("Saros", "Saros Session");
    }

    public void addToSharedProject(String invitee) throws RemoteException {
        setFocusOnViewByTitle("Shared Project Session");
        delegate.viewByTitle("Shared Project Session").toolbarButton(
            "Open invitation interface").click();
        selectCheckBoxInvitation(invitee);
    }

    /**
     * Fill up the configuration wizard with title "Saros Configuration".
     */

    public void doSarosConfiguration(String xmppServer, String jid,
        String password) {

        delegate.shell("Saros Configuration");
        delegate.textWithLabel("Jabber Server").setText(xmppServer);
        delegate.sleep(750);
        delegate.textWithLabel("Username").setText(jid);
        delegate.sleep(750);
        delegate.textWithLabel("Password").setText(password);
        delegate.sleep(750);
        while (delegate.button("Next >").isEnabled()) {
            delegate.button("Next >").click();
            log.debug("click Next > Button.");
            delegate.sleep(750);
        }

        if (delegate.button("Finish").isEnabled()) {
            delegate.button("Finish").click();
            delegate.sleep(750);
            return;
        } else {
            System.out.println("can't click finish button");
        }

        throw new NotImplementedException(
            "only set text fields and click Finish is implemented.");
    }

    public boolean isConfigShellPoppedUp() {
        try {
            delegate.shell("Saros Configuration");
            return true;
        } catch (WidgetNotFoundException e) {
            // ignore
        }
        return false;
    }

    public boolean isConnectedByXmppGuiCheck() throws RemoteException {
        if (!isRosterViewOpen())
            return false;
        setFocusOnViewByTitle("Roster");
        SWTBotToolbarButton toolbarButton = getXmppDisconnectButton();
        return (toolbarButton != null && toolbarButton.isVisible());
    }

    public boolean isContactInRosterView(String contact) throws RemoteException {
        if (!isRosterViewOpen())
            addSarosSessionView();
        if (!isConnectedByXmppGuiCheck())
            xmppConnect();
        setFocusOnViewByTitle("Roster");
        try {
            SWTBotTree tree = delegate.viewByTitle("Roster").bot().tree();
            if (tree != null) {
                SWTBotTreeItem buddy = tree.getTreeItem("Buddies");
                SWTBotTreeItem contact_added = buddy.getNode(contact).select();
                delegate.sleep(1000);
                return contact_added != null
                    && contact_added.getText().equals(contact);
            }
        } catch (WidgetNotFoundException e) {
            log.warn("Contact not found: " + contact, e);
        }
        return false;
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

    public boolean isRosterViewOpen() {
        return isViewOpen("Roster");
    }

    public boolean isSharedSessionViewOpen() {
        return isViewOpen("Shared Project Session");
    }

    /**
     * "Shared Project Session" View must be open
     */
    public void leaveSession() throws RemoteException {
        setFocusOnViewByTitle("Shared Project Session");
        delegate.viewByTitle("Shared Project Session").toolbarButton(
            "Leave the session").click();
    }

    /**
     * Remove given contact from Roster, if contact was added before.
     */
    public void removeContact(String contact) throws RemoteException {
        if (!isContactInRosterView(contact))
            return;
        try {

            SWTBotTree tree = delegate.viewByTitle("Roster").bot().tree();
            if (tree != null) {
                SWTBotTreeItem buddy = tree.getTreeItem("Buddies");
                SWTBotTreeItem item = buddy.getNode(contact).select();
                // remove by context menu
                delegate.sleep(750);
                item.contextMenu("Delete").click();
                delegate.sleep(750);
                // confirm delete
                delegate.shell("Confirm Delete").activate();
                delegate.sleep(750);
                delegate.button("Yes").click();

                // send backspace
                // item.pressShortcut(0, '\b'); // 0 == don't add keystroke

                delegate.sleep(1000);
            }
        } catch (WidgetNotFoundException e) {
            log.info("Contact not found: " + contact, e);
        }
    }

    /**
     * Select the given invitee
     */
    public void selectCheckBoxInvitation(String invitee) {
        for (int i = 0; i < delegate.table().rowCount(); i++) {
            if (delegate.table().getTableItem(i).getText(0).equals(invitee)) {
                delegate.table().getTableItem(i).check();
                log.debug("found invitee: " + invitee);
                delegate.sleep(750);
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

    /**
     * Create a {@link ISarosSession} using context menu off the given project
     * on package explorer view.
     */
    public void shareProject(String projectName) throws RemoteException {
        SWTBotView view = delegate.viewByTitle("Package Explorer");
        SWTBotTree tree = view.bot().tree().select(projectName);
        SWTBotTreeItem item = tree.getTreeItem(projectName).select();
        SWTBotMenu menu = item.contextMenu("Share project...");
        menu.click();
    }

    /**
     * This method captures two screenshots as side effect.
     */
    public void shareProject(String projectName, String invitee)
        throws RemoteException {

        shareProject(projectName);
        captureScreenshot(TEMPDIR + "/shareProjectStep1.png");
        selectCheckBoxInvitation(invitee);
        captureScreenshot(TEMPDIR + "/shareProjectStep2.png");
        delegate.button("Finish").click();
    }

    public void shareProjectSequential(String projectName, List<String> invitees)
        throws RemoteException {
        shareProject(projectName, invitees.remove(0));
        for (String toInvite : invitees)
            inviteToProject(toInvite);
    }

    /**
     * This method captures two screenshots as side effect.
     */
    public void shareProjectParallel(String projectName, List<String> invitees)
        throws RemoteException {
        shareProject(projectName);
        captureScreenshot(TEMPDIR + "/shareProjectStepParallel1.png");
        selectCheckBoxInvitation(invitees);
        captureScreenshot(TEMPDIR + "/shareProjectStepParallel2.png");
        delegate.button("Finish").click();
    }

    /**
     * Roster must be open
     */
    public void xmppConnect() throws RemoteException {
        setFocusOnViewByTitle("Roster");
        while (delegate.viewByTitle("Roster").toolbarButton("Connect") == null
            || !delegate.viewByTitle("Roster").toolbarButton("Connect")
                .isEnabled())
            delegate.sleep(100);
        delegate.viewByTitle("Roster").toolbarButton("Connect").click();

    }

    /**
     * Roster must be open
     */
    public boolean xmppDisconnect() throws RemoteException {
        setFocusOnViewByTitle("Roster");
        // disconnect from xmpp
        if (delegate.viewByTitle("Roster") != null) {
            for (SWTBotToolbarButton toolbarButton : delegate.viewByTitle(
                "Roster").getToolbarButtons()) {
                String s = toolbarButton.getToolTipText();
                log.debug("tip:" + s);
                if (s.matches("Disconnect.*")) {
                    delegate.sleep(750);
                    toolbarButton.click();
                    return true;
                }
            }
        }
        return false;
    }

    /*********** not exported Helper Methods *****************/

    protected SWTBotToolbarButton getXmppDisconnectButton() {
        for (SWTBotToolbarButton toolbarButton : delegate.viewByTitle("Roster")
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches("Disconnect.*")) {
                return toolbarButton;
            }
        }

        return null;
    }

    public void follow(String participantJID, String sufix)
        throws RemoteException {
        if (!isSharedSessionViewOpen())
            addSarosSessionView();
        setFocusOnViewByTitle(BotConfiguration.NAME_SESSION_VIEW);
        try {
            SWTBotTable table = delegate.viewByTitle(
                BotConfiguration.NAME_SESSION_VIEW).bot().table();
            if (table != null) {
                SWTBotTableItem item = table.getTableItem(participantJID
                    + sufix);
                delegate.sleep(750);
                SWTBotMenu menu = item.contextMenu("Follow this user");
                delegate.sleep(750);
                menu.click();
                delegate.sleep(750);
            }
        } catch (WidgetNotFoundException e) {
            log.warn("Driver not found: " + participantJID, e);
        }
    }

    public boolean isInFollowMode(String participantJID, String sufix)
        throws RemoteException {
        if (!isSharedSessionViewOpen())
            addSarosSessionView();
        setFocusOnViewByTitle(BotConfiguration.NAME_SESSION_VIEW);
        try {
            SWTBotTable table = delegate.viewByTitle(
                BotConfiguration.NAME_SESSION_VIEW).bot().table();
            if (table != null) {
                SWTBotTableItem item;

                item = table.getTableItem(participantJID + sufix);
                log.error("item: " + item.getText());
                SWTBotMenu menu = item.contextMenu("Stop following this user");
                if (menu != null)
                    return true;
            }
            return false;
        } catch (WidgetNotFoundException e) {
            log.warn("Driver not found: " + participantJID, e);
            return false;
        }
    }
}
