package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateImp;
import de.fu_berlin.inf.dpp.ui.RosterView;

public class RosterViewObjectImp extends EclipseObject implements
    RosterViewObject {

    public static RosterViewObjectImp classVariable;

    public RosterViewObjectImp(SarosRmiSWTWorkbenchBot rmiBot) {
        super(rmiBot);
    }

    public void openRosterView() throws RemoteException {
        if (!isRosterViewOpen())
            viewObject.openViewById(SarosConstant.ID_ROSTER_VIEW);
    }

    public boolean isRosterViewOpen() throws RemoteException {
        return viewObject.isViewOpen(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public void setFocusOnRosterView() throws RemoteException {
        viewObject.setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public void closeRosterView() throws RemoteException {
        viewObject.closeViewById(SarosConstant.ID_ROSTER_VIEW);
    }

    public void xmppDisconnect() throws RemoteException {
        if (isConnectedByXMPP()) {
            clickTBDisconnectInRosterView();
            waitUntilDisConnected();
            // sleep(200);
        }
    }

    public SWTBotTreeItem selectBuddy(String contact) throws RemoteException {
        return viewObject.selectTreeWithLabelsInView(
            SarosConstant.VIEW_TITLE_ROSTER, "Buddies", contact);
    }

    public boolean isBuddyExist(String contact) throws RemoteException {
        SWTBotTree tree = rmiBot.viewObject
            .getTreeInView(SarosConstant.VIEW_TITLE_ROSTER);
        return treeObject.isTreeItemWithMatchTextExist(tree,
            SarosConstant.BUDDIES, contact + ".*");
    }

    public boolean isConnectedByXmppGuiCheck() throws RemoteException {
        try {
            openRosterView();
            setFocusOnRosterView();
            SWTBotToolbarButton toolbarButton = viewObject
                .getToolbarButtonWithTooltipInView(
                    SarosConstant.VIEW_TITLE_ROSTER,
                    SarosConstant.TOOL_TIP_TEXT_DISCONNECT);
            return (toolbarButton != null && toolbarButton.isVisible());
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**
     * This method returns true if {@link SarosStateImp} and the GUI
     * {@link RosterView} having the connected state.
     */
    public boolean isConnectedByXMPP() throws RemoteException {
        return rmiBot.state.isConnectedByXMPP() && isConnectedByXmppGuiCheck();
    }

    public void clickTBAddANewContactInRosterView() throws RemoteException {
        openRosterView();
        setFocusOnRosterView();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_ADD_A_NEW_CONTACT);
    }

    /**
     * Roster must be open
     */
    public void clickTBConnectInRosterView() throws RemoteException {
        openRosterView();
        setFocusOnRosterView();
        viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_CONNECT);
    }

    /**
     * Roster must be open
     */
    public boolean clickTBDisconnectInRosterView() throws RemoteException {
        openRosterView();
        setFocusOnRosterView();
        return viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_DISCONNECT) != null;
    }

    public void waitUntilConnected() throws RemoteException {
        waitUntil(SarosConditions.isConnect(bot));
    }

    public void waitUntilDisConnected() throws RemoteException {
        waitUntil(SarosConditions.isDisConnected(bot));
    }

    public void addContact(JID jid) throws RemoteException {
        if (!hasContactWith(jid)) {
            openRosterView();
            setFocusOnRosterView();
            clickTBAddANewContactInRosterView();
            windowObject
                .waitUntilShellActive(SarosConstant.SHELL_TITLE_NEW_CONTACT);
            // activateShellWithText(SarosConstant.SHELL_TITLE_NEW_CONTACT);
            bot.textWithLabel(SarosConstant.TEXT_LABEL_JABBER_ID).setText(
                jid.getBase());
            basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
            bot.button(SarosConstant.BUTTON_FINISH).click();
        }
    }

    public boolean hasContactWith(JID jid) throws RemoteException {
        return rmiBot.state.hasContactWith(jid) && isBuddyExist(jid.getBase());
    }

    /**
     * Remove given contact from Roster, if contact was added before.
     */
    public void deleteContact(JID jid) throws RemoteException {
        if (!hasContactWith(jid))
            return;
        try {
            viewObject.clickContextMenuOfTreeInView(
                SarosConstant.VIEW_TITLE_ROSTER,
                SarosConstant.CONTEXT_MENU_DELETE, SarosConstant.BUDDIES,
                jid.getBase());
            windowObject
                .waitUntilShellActive(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
            rmiBot.exportedPopUpWindow.confirmWindow(
                SarosConstant.SHELL_TITLE_CONFIRM_DELETE,
                SarosConstant.BUTTON_YES);
        } catch (WidgetNotFoundException e) {
            log.info("Contact not found: " + jid.getBase(), e);
        }
    }

    public void renameContact(String contact, String newName)
        throws RemoteException {
        SWTBotTree tree = bot.viewByTitle(SarosConstant.VIEW_TITLE_ROSTER)
            .bot().tree();
        SWTBotTreeItem item = treeObject.getTreeItemWithMatchText(tree,
            SarosConstant.BUDDIES + ".*", contact + ".*");
        item.contextMenu("Rename...").click();
        windowObject.waitUntilShellActive("Set new nickname");
        bot.text(contact).setText(newName);
        bot.button(SarosConstant.BUTTON_OK).click();
    }

    public void xmppConnect(JID jid, String password) throws RemoteException {
        log.trace("connectedByXMPP");
        boolean connectedByXMPP = isConnectedByXMPP();
        if (!connectedByXMPP) {
            log.trace("clickTBConnectInRosterView");
            clickTBConnectInRosterView();
            rmiBot.eclipseBasicObject.sleep(100);// wait a bit to check if shell
                                                 // pops
            // up
            log.trace("isShellActive");
            boolean shellActive = rmiBot.exportedPopUpWindow
                .isShellActive(SarosConstant.SAROS_CONFI_SHELL_TITLE);
            if (shellActive) {
                log.trace("confirmSarosConfigurationWindow");
                rmiBot.exportedPopUpWindow.confirmSarosConfigurationWizard(
                    jid.getDomain(), jid.getName(), password);
            }
            waitUntilConnected();
        }
    }

}
