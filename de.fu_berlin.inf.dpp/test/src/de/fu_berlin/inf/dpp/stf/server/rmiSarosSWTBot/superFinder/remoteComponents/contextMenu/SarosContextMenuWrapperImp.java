package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTableItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.BuddiesView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.SessionView;

public class SarosContextMenuWrapperImp extends Component implements
    SarosContextMenuWrapper {
    private static transient SarosContextMenuWrapperImp self;

    /**
     * {@link SarosContextMenuWrapperImp} is a singleton, but inheritance is
     * possible.
     */
    public static SarosContextMenuWrapperImp getInstance() {
        if (self != null)
            return self;
        self = new SarosContextMenuWrapperImp();

        return self;
    }

    protected JID participantJID;
    protected STFBotTreeItem treeItem;
    protected STFBotTree tree;
    protected STFBotTableItem tableItem;
    protected SessionView sessionView;
    protected BuddiesView buddiesView;

    public void setTreeItem(STFBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    public void setTableItem(STFBotTableItem tableItem) {
        this.tableItem = tableItem;
    }

    public void setTree(STFBotTree tree) {
        this.tree = tree;
    }

    public void setParticipantJID(JID jid) {
        this.participantJID = jid;
    }

    public void setSessionView(SessionView sessionView) {
        this.sessionView = sessionView;
    }

    public void setBuddiesView(BuddiesView buddiesView) {
        this.buddiesView = buddiesView;
    }

    // Session View
    public void grantWriteAccess() throws RemoteException {
        if (hasWriteAccess()) {
            throw new RuntimeException("User \"" + tableItem.getText()
                + "\" already has write access!.");
        }
        tableItem.contextMenu(CM_GRANT_WRITE_ACCESS).click();
        waitUntilHasWriteAccess();
        bot().sleep(300);
    }

    public void restrictToReadOnlyAccess() throws RemoteException {
        if (!hasWriteAccess()) {
            throw new RuntimeException("User \"" + tableItem.getText()
                + "\" already has read-only access!");
        }
        tableItem.contextMenu(CM_RESTRICT_TO_READ_ONLY_ACCESS).click();
        waitUntilHasReadOnlyAccess();
        bot().sleep(300);
    }

    public void followThisBuddy() throws RemoteException {
        if (isFollowingThisBuddy()) {
            log.debug(participantJID.getBase() + " is already followed by you.");
            return;
        }
        if (localJID.equals(participantJID)) {
            throw new RuntimeException(
                "Hi guy, you can't follow youself, it makes no sense! Please pass a correct parameter to the method.");
        }
        tableItem.contextMenu(CM_FOLLOW_THIS_BUDDY).click();
        waitUntilIsFollowingThisBuddy();
    }

    public void stopFollowingThisBuddy() throws RemoteException {
        log.debug(" JID of the followed user: " + participantJID.getBase());
        tableItem.contextMenu(CM_STOP_FOLLOWING_THIS_BUDDY).click();
        waitUntilIsNotFollowingThisBuddy();
    }

    public void jumpToPositionOfSelectedBuddy() throws RemoteException {
        if (localJID.equals(participantJID)) {
            throw new RuntimeException(
                "Hi guy, you can't jump to the position of youself, it makes no sense! Please pass a correct parameter to the method.");
        }
        tableItem.contextMenu(CM_JUMP_TO_POSITION_SELECTED_BUDDY).click();
    }

    // Buddies View

    public void delete() throws RemoteException {
        treeItem.contextMenu(CM_DELETE).click();
        bot().waitUntilShellIsOpen(CONFIRM_DELETE);
        bot().shell(CONFIRM_DELETE).activate();
        bot().shell(CONFIRM_DELETE).bot().button(YES).click();
    }

    public void rename(String newBuddyName) throws RemoteException {
        treeItem.contextMenu(CM_RENAME).click();
        STFBotShell shell = bot().shell(SHELL_SET_NEW_NICKNAME);
        if (!shell.activate()) {
            shell.waitUntilActive();
        }
        shell.bot().text().setText(newBuddyName);
        shell.bot().button(OK).click();
    }

    public void inviteBuddy() throws RemoteException {
        if (!treeItem.isEnabled()) {
            throw new RuntimeException(
                "You can't invite this user, he isn't conntected yet");
        }
        if (!treeItem.contextMenu(CM_RENAME).isEnabled()) {
            throw new RuntimeException(
                "You can't invite this user. Are you sure that you haven't invited him?");
        }
        treeItem.contextMenu(CM_INVITE_BUDDY).click();
    }

    public boolean hasWriteAccess() throws RemoteException {
        return !tableItem.contextMenu(CM_GRANT_WRITE_ACCESS).isEnabled()
            && !tableItem.getText().contains(PERMISSION_NAME);
    }

    public void waitUntilHasWriteAccess() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return hasWriteAccess();
            }

            public String getFailureMessage() {
                return "can't grant " + localJID.getBase()
                    + " the write access.";
            }
        });
    }

    public void waitUntilHasReadOnlyAccess() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !hasWriteAccess();
            }

            public String getFailureMessage() {
                return "can't restrict " + localJID.getBase()
                    + " to read-only access";
            }
        });
    }

    public boolean hasReadOnlyAccess() throws RemoteException {
        return !tableItem.contextMenu(CM_RESTRICT_TO_READ_ONLY_ACCESS)
            .isEnabled() && tableItem.getText().contains(PERMISSION_NAME);
    }

    public boolean isFollowingThisBuddy() throws RemoteException {
        return tableItem.existsContextMenu(CM_STOP_FOLLOWING_THIS_BUDDY);
    }

    public void waitUntilIsFollowingThisBuddy() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isFollowingThisBuddy();
            }

            public String getFailureMessage() {
                return localJID.getBase() + " is not folloing the user ";
            }
        });
    }

    public void waitUntilIsNotFollowingThisBuddy() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isFollowingThisBuddy();
            }

            public String getFailureMessage() {
                return "Can't not stop following this user.";
            }
        });
    }
}
