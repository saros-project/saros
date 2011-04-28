package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.sarosView;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.net.JID;

public class ContextMenusInSessionArea extends ContextMenusInSarosView
    implements IContextMenusInSessionArea {
    private static transient ContextMenusInSessionArea self;

    /**
     * {@link ContextMenusInSessionArea} is a singleton, but inheritance is
     * possible.
     */
    public static ContextMenusInSessionArea getInstance() {
        if (self != null)
            return self;
        self = new ContextMenusInSessionArea();

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
        remoteBot().sleep(300);
    }

    public void restrictToReadOnlyAccess() throws RemoteException {
        if (!hasWriteAccess()) {
            throw new RuntimeException("User \"" + treeItem.getText()
                + "\" already has read-only access!");
        }
        treeItem.contextMenus(CM_RESTRICT_TO_READ_ONLY_ACCESS).click();
        waitUntilHasReadOnlyAccess();
        remoteBot().sleep(300);
    }

    public void followParticipant() throws RemoteException {
        if (isFollowing()) {
            log.debug(participantJID.getBase() + " is already followed by you.");
            return;
        }
        if (localJID.equals(participantJID)) {
            throw new RuntimeException(
                "Hi guy, you can't follow youself, it makes no sense! Please pass a correct parameter to the method.");
        }
        treeItem.contextMenus(CM_FOLLOW_PARTICIPANT).click();
        // waitUntilIsFollowingThisBuddy();
    }

    public void stopFollowing() throws RemoteException {
        log.debug(" JID of the followed user: " + participantJID.getBase());
        treeItem.contextMenus(CM_STOP_FOLLOWING).click();
        waitUntilIsNotFollowing();
    }

    public void jumpToPositionOfSelectedBuddy() throws RemoteException {
        if (localJID.equals(participantJID)) {
            throw new RuntimeException(
                "Hi guy, you can't jump to the position of youself, it makes no sense! Please pass a correct parameter to the method.");
        }
        treeItem.contextMenus(CM_JUMP_TO_POSITION_SELECTED_BUDDY).click();
    }

    public void addProjects(String... projectNames) throws RemoteException {
        treeItem.contextMenus(ADD_PROJECTS).click();
        superBot().confirmShellAddProjectsToSession(projectNames);
    }

    public void addBuddies(String... jidOfInvitees) throws RemoteException {
        treeItem.contextMenus(ADD_BUDDIES).click();
        superBot().confirmShellAddBuddyToSession(jidOfInvitees);
    }

    public void shareProjects(String projectName, JID... jids)
        throws RemoteException {
        treeItem.contextMenus(SHARE_PROJECTS).click();
        superBot().confirmShellShareProject(projectName, jids);
    }

    /**********************************************
     * 
     * States
     * 
     **********************************************/

    public boolean hasWriteAccess() throws RemoteException {
        return !treeItem.contextMenus(CM_GRANT_WRITE_ACCESS).isEnabled()
            && !treeItem.getText().contains(PERMISSION_NAME);
    }

    public boolean hasReadOnlyAccess() throws RemoteException {
        return !treeItem.contextMenus(CM_RESTRICT_TO_READ_ONLY_ACCESS)
            .isEnabled() && treeItem.getText().contains(PERMISSION_NAME);
    }

    public boolean isFollowing() throws RemoteException {
        return treeItem.existsContextMenu(CM_STOP_FOLLOWING);
    }

    /**********************************************
     * 
     * Wait untils
     * 
     **********************************************/

    public void waitUntilHasWriteAccess() throws RemoteException {
        remoteBot().waitUntil(new DefaultCondition() {
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
        remoteBot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !hasWriteAccess();
            }

            public String getFailureMessage() {
                return "can't restrict " + localJID.getBase()
                    + " to read-only access";
            }
        });
    }

    public void waitUntilIsFollowing() throws RemoteException {
        remoteBot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isFollowing();
            }

            public String getFailureMessage() {
                return localJID.getBase() + " is not following the user ";
            }
        });
    }

    public void waitUntilIsNotFollowing() throws RemoteException {
        remoteBot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isFollowing();
            }

            public String getFailureMessage() {
                return "Can't not stop following this user.";
            }
        });
    }

}