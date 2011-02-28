package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;

public class SarosContextMenuWrapperImp extends ContextMenuWrapperImp implements
    SarosContextMenuWrapper {
    private static transient SarosContextMenuWrapperImp self;
    private static SarosCImp sarosC;
    protected JID participantJID;

    /**
     * {@link SarosContextMenuWrapperImp} is a singleton, but inheritance is
     * possible.
     */
    public static SarosContextMenuWrapperImp getInstance() {
        if (self != null)
            return self;
        self = new SarosContextMenuWrapperImp();
        sarosC = SarosCImp.getInstance();
        teamC = TeamCImp.getInstance();
        reafactorC = RefactorCImp.getInstance();
        return self;
    }

    public void setParticipantJID(JID jid) {
        this.participantJID = jid;
    }

    public SarosC saros() throws RemoteException {
        sarosC.setTreeItem(treeItem);
        return sarosC;
    }

    public void grantWriteAccess() throws RemoteException {
        if (sarosBot().state().hasWriteAccessBy(tableItem.getText())) {
            throw new RuntimeException("User \"" + tableItem.getText()
                + "\" already has write access!.");
        }
        tableItem.contextMenu(CM_GRANT_WRITE_ACCESS).click();
        sarosBot().condition().waitUntilHasWriteAccessBy(participantJID);
        bot().sleep(300);
    }

    public void restrictToReadOnlyAccess() throws RemoteException {
        if (!sarosBot().state().hasWriteAccessBy(tableItem.getText())) {
            throw new RuntimeException("User \"" + tableItem.getText()
                + "\" already has read-only access!");
        }
        tableItem.contextMenu(CM_RESTRICT_TO_READ_ONLY_ACCESS).click();
        sarosBot().condition().waitUntilHasReadOnlyAccessBy(participantJID);
        bot().sleep(300);
    }

    public void followThisBuddy() throws RemoteException {
        if (sarosBot().state().isFollowingBuddy(participantJID)) {
            log.debug(participantJID.getBase() + " is already followed by you.");
            return;
        }
        if (localJID.equals(participantJID)) {
            throw new RuntimeException(
                "Hi guy, you can't follow youself, it makes no sense! Please pass a correct parameter to the method.");
        }
        tableItem.contextMenu(CM_FOLLOW_THIS_BUDDY).click();
        sarosBot().condition().waitUntilIsFollowingBuddy(participantJID);
    }

    public void stopFollowingThisBuddy() throws RemoteException {
        log.debug(" JID of the followed user: " + participantJID.getBase());
        tableItem.contextMenu(CM_STOP_FOLLOWING_THIS_BUDDY).click();
        sarosBot().condition().waitUntilIsNotFollowingBuddy(participantJID);
    }

    public void jumpToPositionOfSelectedBuddy() throws RemoteException {
        if (localJID.equals(participantJID)) {
            throw new RuntimeException(
                "Hi guy, you can't jump to the position of youself, it makes no sense! Please pass a correct parameter to the method.");
        }
        tableItem.contextMenu(CM_JUMP_TO_POSITION_SELECTED_BUDDY).click();
    }

}
