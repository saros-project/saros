package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;
import de.fu_berlin.inf.dpp.ui.RosterView;

public class RosterViewObject implements IRosterViewObject {

    private transient static final Logger log = Logger
        .getLogger(RosterViewObject.class);

    private static transient SarosRmiSWTWorkbenchBot rmiBot;
    public static RosterViewObject classVariable;

    public RosterViewObject() {
        // Default constructor needed for RMI
    }

    public RosterViewObject(SarosRmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
    }

    public void openRosterView() throws RemoteException {
        if (!isRosterViewOpen())
            rmiBot.viewObject.openViewById(SarosConstant.ID_ROSTER_VIEW);
    }

    public boolean isRosterViewOpen() throws RemoteException {
        return rmiBot.viewObject.isViewOpen(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public void setFocusOnRosterView() throws RemoteException {
        rmiBot.viewObject
            .setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public void closeRosterView() throws RemoteException {
        rmiBot.viewObject.closeViewById(SarosConstant.ID_ROSTER_VIEW);
    }

    public void xmppDisconnect() throws RemoteException {
        if (isConnectedByXMPP()) {
            clickTBDisconnectInRosterView();
            rmiBot.waitUntilDisConnected();
            // sleep(200);
        }
    }

    public SWTBotTreeItem selectBuddy(String contact) throws RemoteException {
        return rmiBot.viewObject.selectTreeWithLabelsInView(
            SarosConstant.VIEW_TITLE_ROSTER, "Buddies", contact);
    }

    public boolean isBuddyExist(String contact) throws RemoteException {
        SWTBotTree tree = rmiBot.viewObject
            .getTreeInView(SarosConstant.VIEW_TITLE_ROSTER);
        return rmiBot.treeObject.isTreeItemWithMatchTextExist(tree,
            SarosConstant.BUDDIES, contact + ".*");
    }

    public boolean isConnectedByXmppGuiCheck() throws RemoteException {
        try {
            openRosterView();
            setFocusOnRosterView();
            SWTBotToolbarButton toolbarButton = rmiBot.viewObject
                .getToolbarButtonWithTooltipInView(
                    SarosConstant.VIEW_TITLE_ROSTER,
                    SarosConstant.TOOL_TIP_TEXT_DISCONNECT);
            return (toolbarButton != null && toolbarButton.isVisible());
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**
     * This method returns true if {@link SarosState} and the GUI
     * {@link RosterView} having the connected state.
     */
    public boolean isConnectedByXMPP() throws RemoteException {
        return rmiBot.stateObject.isConnectedByXMPP()
            && isConnectedByXmppGuiCheck();
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
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_CONNECT);
    }

    /**
     * Roster must be open
     */
    public boolean clickTBDisconnectInRosterView() throws RemoteException {
        openRosterView();
        setFocusOnRosterView();
        return rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_ROSTER,
            SarosConstant.TOOL_TIP_TEXT_DISCONNECT) != null;
    }
}
