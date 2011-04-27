package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.submenus;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class WorkTogetherOnC extends Component implements IWorkTogetherOnC {

    private static transient WorkTogetherOnC self;

    private IRemoteBotTreeItem treeItem;

    /**
     * {@link NewC} is a singleton, but inheritance is possible.
     */
    public static WorkTogetherOnC getInstance() {
        if (self != null)
            return self;
        self = new WorkTogetherOnC();
        return self;
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
        sarosBot().confirmShellShareProject(projectName, baseJIDOfInvitees);
    }

    public void project(String projectName) throws RemoteException {
        treeItem.contextMenus(CM_WORK_TOGETHER_ON, projectName).click();
    }

}
