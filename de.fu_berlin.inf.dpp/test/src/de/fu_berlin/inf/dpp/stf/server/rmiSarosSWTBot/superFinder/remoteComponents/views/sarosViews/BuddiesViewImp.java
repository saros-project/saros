package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.hamcrest.Matcher;
import org.jivesoftware.smack.Roster;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToolbarDropDownButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.SarosContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.ViewsImp;

/**
 * This implementation of {@link BuddiesView}
 * 
 * @author lchen
 */
public class BuddiesViewImp extends ViewsImp implements BuddiesView {

    private static transient BuddiesViewImp self;

    private STFBotView view;
    private STFBotTree tree;

    // private STFBotTreeItem treeItem;

    /**
     * {@link BuddiesViewImp} is a singleton, but inheritance is possible.
     */
    public static BuddiesViewImp getInstance() {
        if (self != null)
            return self;
        self = new BuddiesViewImp();
        return self;
    }

    public BuddiesView setView(STFBotView view) throws RemoteException {
        setViewWithTree(view);
        return this;
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

    public void connectWith(JID jid, String password) throws RemoteException {
        log.trace("connectedByXMPP");
        if (!isConnected()) {
            log.trace("click the toolbar button \"Connect\" in the buddies view");
            if (!sarosBot().menuBar().saros().preferences().existsAccount(jid)) {
                sarosBot().menuBar().saros().preferences()
                    .addAccount(jid, password);
            } else {
                if (!sarosBot().menuBar().saros().preferences()
                    .isAccountActive(jid)) {
                    sarosBot().menuBar().saros().preferences()
                        .activateAccount(jid);
                }
            }
            if (!isConnected()) {
                clickToolbarButtonWithTooltip(TB_CONNECT);
                waitUntilIsConnected();
            }
        }
    }

    public void connectWithActiveAccount() throws RemoteException {
        if (!isConnected()) {
            if (!sarosBot().menuBar().saros().preferences().existsAccount()) {
                throw new RuntimeException(
                    "You need to at first add a account!");
            }
            clickToolbarButtonWithTooltip(TB_CONNECT);
            waitUntilIsConnected();
        }
    }

    public SarosContextMenuWrapper selectBuddy(JID buddyJID)
        throws RemoteException {
        if (getNickName(buddyJID) == null) {
            throw new RuntimeException("No buddy with the ID "
                + buddyJID.getBase() + " existed!");
        }
        initSarosContextMenuWrapper(tree.selectTreeItemWithRegex(NODE_BUDDIES
            + ".*", getNickName(buddyJID) + ".*"));
        sarosContextMenu.setBuddiesView(this);
        return sarosContextMenu;
    }

    public boolean hasBuddy(JID buddyJID) throws RemoteException {
        String nickName = getNickName(buddyJID);
        if (nickName == null)
            return false;
        return tree.selectTreeItemWithRegex(NODE_BUDDIES)
            .existsSubItemWithRegex(nickName + ".*");
    }

    public void addANewBuddy(JID jid) throws RemoteException {
        if (!hasBuddy(jid)) {
            clickToolbarButtonWithTooltip(TB_ADD_A_NEW_BUDDY);
            sarosBot().confirmShellAddBuddy(jid);
        }
    }

    public void disconnect() throws RemoteException {
        if (isConnected()) {
            clickToolbarButtonWithTooltip(TB_DISCONNECT);
            waitUntilDisConnected();
        }
    }

    /*
     * FIXME: there are some problems by clicking the toolbarDropDownButton.
     */
    @SuppressWarnings("unused")
    private void selectConnectAccount(String baseJID) throws RemoteException {
        STFBotToolbarDropDownButton b = view.toolbarDropDownButton(TB_CONNECT);
        @SuppressWarnings("static-access")
        Matcher<MenuItem> withRegex = WidgetMatcherFactory.withRegex(baseJID
            + ".*");
        b.menuItem(withRegex).click();
        try {
            b.pressShortcut(KeyStroke.getInstance("ESC"));
        } catch (ParseException e) {
            log.debug("", e);
        }
    }

    /*
     * FIXME: there are some problems by clicking the toolbarDropDownButton.
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    private boolean isConnectAccountExist(String baseJID)
        throws RemoteException {
        Matcher matcher = allOf(widgetOfType(MenuItem.class));
        STFBotToolbarDropDownButton b = view.toolbarDropDownButton(TB_CONNECT);
        List<? extends STFBotMenu> accounts = b.menuItems(matcher);
        b.pressShortcut(Keystrokes.ESC);
        for (STFBotMenu account : accounts) {
            log.debug("existed account: " + account.getText() + "hier");
            if (account.getText().trim().equals(baseJID)) {
                return true;
            }
        }
        return false;
    }

    /**********************************************
     * 
     * state
     * 
     **********************************************/

    public boolean isConnected() throws RemoteException {
        return isToolbarButtonEnabled(TB_DISCONNECT);
    }

    public boolean isDisConnected() throws RemoteException {
        return isToolbarButtonEnabled(TB_CONNECT);
    }

    public String getNickName(JID buddyJID) throws RemoteException {
        Roster roster = saros.getRoster();
        if (roster.getEntry(buddyJID.getBase()) == null)
            return null;
        if (roster.getEntry(buddyJID.getBase()).getName() == null)
            return buddyJID.getBase();
        else
            return roster.getEntry(buddyJID.getBase()).getName();
    }

    public boolean hasNickName(JID buddyJID) throws RemoteException {
        if (getNickName(buddyJID) == null)
            return false;
        if (!getNickName(buddyJID).equals(buddyJID.getBase()))
            return true;
        return false;
    }

    public List<String> getAllBuddies() throws RemoteException {
        return tree.selectTreeItemWithRegex(NODE_BUDDIES + ".*")
            .getTextOfItems();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/

    public void waitUntilIsConnected() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isConnected();
            }

            public String getFailureMessage() {
                return "Can't connect.";
            }
        });
    }

    public void waitUntilDisConnected() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isConnected();
            }

            public String getFailureMessage() {
                return "Can't disconnect.";
            }
        });
    }

    protected boolean isToolbarButtonEnabled(String tooltip)
        throws RemoteException {
        if (!view.existsToolbarButton(tooltip))
            return false;
        return view.toolbarButton(tooltip).isEnabled();
    }

    private void setViewWithTree(STFBotView view) throws RemoteException {
        this.view = view;
        tree = view.bot().tree();
        // treeItem = null;
    }

    private void initSarosContextMenuWrapper(STFBotTreeItem treeItem) {
        // this.treeItem = treeItem;
        sarosContextMenu.setTree(tree);
        sarosContextMenu.setTreeItem(treeItem);
    }

    private void clickToolbarButtonWithTooltip(String tooltipText)
        throws RemoteException {
        if (!view.existsToolbarButton(tooltipText))
            throw new RuntimeException("The toolbarbutton " + tooltipText
                + " doesn't exist!");
        view.toolbarButton(tooltipText).click();
    }

}
