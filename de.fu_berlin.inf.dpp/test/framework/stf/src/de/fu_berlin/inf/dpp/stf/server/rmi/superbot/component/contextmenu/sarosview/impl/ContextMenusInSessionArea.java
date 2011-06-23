package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInSessionArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

public final class ContextMenusInSessionArea extends ContextMenusInSarosView
    implements IContextMenusInSessionArea {

    private static final Logger log = Logger
        .getLogger(ContextMenusInSessionArea.class);

    protected JID participantJID;

    private static final ContextMenusInSessionArea INSTANCE = new ContextMenusInSessionArea();

    public static ContextMenusInSessionArea getInstance() {
        return INSTANCE;
    }

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
            throw new RuntimeException("user \"" + treeItem.getText()
                + "\" already has write access!.");
        }
        treeItem.contextMenus(CM_GRANT_WRITE_ACCESS).click();
        waitUntilHasWriteAccess();
        RemoteWorkbenchBot.getInstance().sleep(300);
    }

    public void restrictToReadOnlyAccess() throws RemoteException {
        if (!hasWriteAccess()) {
            throw new RuntimeException("user \"" + treeItem.getText()
                + "\" already has read-only access!");
        }
        treeItem.contextMenus(CM_RESTRICT_TO_READ_ONLY_ACCESS).click();
        waitUntilHasReadOnlyAccess();
        RemoteWorkbenchBot.getInstance().sleep(300);
    }

    public void followParticipant() throws RemoteException {
        if (isFollowing()) {
            log.debug(participantJID.getBase() + " is already followed by you.");
            return;
        }
        if (SuperBot.getInstance().getJID().equals(participantJID)) {
            throw new RuntimeException("you can't follow yourself");
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
        if (SuperBot.getInstance().getJID().equals(participantJID)) {
            throw new RuntimeException(
                "you can't jump to the position of yourself");
        }
        treeItem.contextMenus(CM_JUMP_TO_POSITION_SELECTED_BUDDY).click();
    }

    public void addProjects(String... projectNames) throws RemoteException {
        treeItem.contextMenus(ADD_PROJECTS).click();
        SuperBot.getInstance().confirmShellAddProjectsToSession(projectNames);
    }

    public void addBuddies(String... jidOfInvitees) throws RemoteException {
        treeItem.contextMenus(ADD_BUDDIES).click();
        SuperBot.getInstance().confirmShellAddBuddyToSession(jidOfInvitees);
    }

    public void shareProjects(String projectName, JID... jids)
        throws RemoteException {
        treeItem.contextMenus(SHARE_PROJECTS).click();
        SuperBot.getInstance().confirmShellShareProjects(projectName, jids);
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
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return hasWriteAccess();
            }

            public String getFailureMessage() {
                return "unable to grant write access to "
                    + participantJID.getBase();
            }
        });
    }

    public void waitUntilHasReadOnlyAccess() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return hasReadOnlyAccess();
            }

            public String getFailureMessage() {
                return "unable to restrict " + participantJID.getBase()
                    + " to read-only access";
            }
        });
    }

    public void waitUntilIsFollowing() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isFollowing();
            }

            public String getFailureMessage() {
                return "unable to follow " + participantJID.getBase();
            }
        });
    }

    public void waitUntilIsNotFollowing() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isFollowing();
            }

            public String getFailureMessage() {
                return "unable to stop following mode on user"
                    + participantJID.getBase();
            }
        });
    }

}