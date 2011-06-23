package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.IWorkTogetherOnC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

public final class WorkTogetherOnC extends StfRemoteObject implements
    IWorkTogetherOnC {

    private static final WorkTogetherOnC INSTANCE = new WorkTogetherOnC();

    private IRemoteBotTreeItem treeItem;

    public static WorkTogetherOnC getInstance() {
        return INSTANCE;
    }

    public void setTreeItem(IRemoteBotTreeItem treeItem) {
        this.treeItem = treeItem;
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

    public void multipleProjects(String projectName, JID... baseJIDOfInvitees)
        throws RemoteException {
        treeItem.contextMenus(CM_WORK_TOGETHER_ON, CM_MULTIPLE_PROJECTS)
            .click();
        SuperBot.getInstance().confirmShellShareProjects(projectName,
            baseJIDOfInvitees);
    }

    public void project(String projectName) throws RemoteException {
        treeItem.contextMenus(CM_WORK_TOGETHER_ON, projectName).click();
    }

}
