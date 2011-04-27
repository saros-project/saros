package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.net.JID;

public class SessionContextMenuWrapper extends SarosContextMenuWrapper
    implements ISessionContextMenuWrapper {
    private static transient SessionContextMenuWrapper self;

    /**
     * {@link SessionContextMenuWrapper} is a singleton, but inheritance is
     * possible.
     */
    public static SessionContextMenuWrapper getInstance() {
        if (self != null)
            return self;
        self = new SessionContextMenuWrapper();

        return self;
    }

    protected JID participantJID;

    public void setParticipantJID(JID jid) {
        this.participantJID = jid;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * contextMenus showed in session-area
     * 
     **********************************************/
    public void grantWriteAccess() throws RemoteException {
        if (hasWriteAccess()) {
            throw new RuntimeException("User \"" + treeItem.getText()
                + "\" already has write access!.");
        }
        treeItem.contextMenus(CM_GRANT_WRITE_ACCESS).click();
        waitUntilHasWriteAccess();
        bot().sleep(300);
    }

    public void restrictToReadOnlyAccess() throws RemoteException {
        if (!hasWriteAccess()) {
            throw new RuntimeException("User \"" + treeItem.getText()
                + "\" already has read-only access!");
        }
        treeItem.contextMenus(CM_RESTRICT_TO_READ_ONLY_ACCESS).click();
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
        treeItem.contextMenus(CM_FOLLOW_THIS_BUDDY).click();
        // waitUntilIsFollowingThisBuddy();
    }

    public void stopFollowingThisBuddy() throws RemoteException {
        log.debug(" JID of the followed user: " + participantJID.getBase());
        treeItem.contextMenus(CM_STOP_FOLLOWING_THIS_BUDDY).click();
        waitUntilIsNotFollowingThisBuddy();
    }

    public void jumpToPositionOfSelectedBuddy() throws RemoteException {
        if (localJID.equals(participantJID)) {
            throw new RuntimeException(
                "Hi guy, you can't jump to the position of youself, it makes no sense! Please pass a correct parameter to the method.");
        }
        treeItem.contextMenus(CM_JUMP_TO_POSITION_SELECTED_BUDDY).click();
    }

    public boolean hasWriteAccess() throws RemoteException {
        return !treeItem.contextMenus(CM_GRANT_WRITE_ACCESS).isEnabled()
            && !treeItem.getText().contains(PERMISSION_NAME);
    }

    public boolean hasReadOnlyAccess() throws RemoteException {
        return !treeItem.contextMenus(CM_RESTRICT_TO_READ_ONLY_ACCESS)
            .isEnabled() && treeItem.getText().contains(PERMISSION_NAME);
    }

    public boolean isFollowingThisBuddy() throws RemoteException {
        return treeItem.existsContextMenu(CM_STOP_FOLLOWING_THIS_BUDDY);
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

    public void waitUntilIsFollowingThisBuddy() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isFollowingThisBuddy();
            }

            public String getFailureMessage() {
                return localJID.getBase() + " is not following the user ";
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