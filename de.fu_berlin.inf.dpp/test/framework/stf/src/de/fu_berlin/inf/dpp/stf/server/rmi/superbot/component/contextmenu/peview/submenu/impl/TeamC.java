package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.ITeamC;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;

public final class TeamC extends StfRemoteObject implements ITeamC {

    private static final Logger log = Logger.getLogger(TeamC.class);

    private static final TeamC INSTANCE = new TeamC();

    private SWTBotTree tree;
    private SWTBotTreeItem treeItem;

    public static TeamC getInstance() {
        return INSTANCE;
    }

    public void setTree(SWTBotTree tree) {
        this.tree = tree;
    }

    public void setTreeItem(SWTBotTreeItem view) {
        this.treeItem = view;
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
    public void shareProject(String repositoryURL) throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM,
            CM_SHARE_PROJECT_OF_TEAM);

        IRemoteBotShell shell = RemoteWorkbenchBot.getInstance().shell(
            SHELL_SHARE_PROJECT);
        shell.confirmWithTable(TABLE_ITEM_REPOSITORY_TYPE_SVN, NEXT);

        if (shell.bot().table().containsItem(repositoryURL)) {
            shell.confirmWithTable(repositoryURL, NEXT);
        } else {
            shell.bot().radio(LABEL_CREATE_A_NEW_REPOSITORY_LOCATION).click();
            shell.bot().button(NEXT).click();
            shell.bot().comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
        }
        shell.bot().button(FINISH).waitUntilIsEnabled();
        shell.bot().button(FINISH).click();
        RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(
            SHELL_SHARE_PROJECT);
    }

    public void shareProjectConfiguredWithSVNInfos(String repositoryURL)
        throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM,
            CM_SHARE_PROJECT_OF_TEAM);

        IRemoteBotShell shell = RemoteWorkbenchBot.getInstance().shell(
            SHELL_SHARE_PROJECT);
        shell.confirmWithTable(TABLE_ITEM_REPOSITORY_TYPE_SVN, NEXT);
        log.debug("SVN share project text: " + shell.bot().text());
        shell.bot().button(FINISH).waitUntilIsEnabled();
        shell.bot().button(FINISH).click();
        RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(
            SHELL_SHARE_PROJECT);
    }

    public void shareProjectUsingSpecifiedFolderName(String repositoryURL,
        String specifiedFolderName) throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM,
            CM_SHARE_PROJECT_OF_TEAM);

        RemoteWorkbenchBot.getInstance().shell(SHELL_SHARE_PROJECT)
            .confirmWithTable(TABLE_ITEM_REPOSITORY_TYPE_SVN, NEXT);

        IRemoteBotShell shell = RemoteWorkbenchBot.getInstance().shell(
            SHELL_SHARE_PROJECT);
        IRemoteBotTable table = shell.bot().table();

        if (table == null || !table.containsItem(repositoryURL)) {
            // close window
            shell.close();
            // in svn repos view: enter url

            RemoteWorkbenchBot.getInstance().openViewById(
                VIEW_SVN_REPOSITORIES_ID);
            IRemoteBotView view = RemoteWorkbenchBot.getInstance().view(
                VIEW_SVN_REPOSITORIES);

            view.show();
            final boolean viewWasOpen = RemoteWorkbenchBot.getInstance()
                .isViewOpen(VIEW_SVN_REPOSITORIES);
            RemoteWorkbenchBot.getInstance().view(VIEW_SVN_REPOSITORIES)
                .toolbarButton("Add SVN Repository").click();

            RemoteWorkbenchBot.getInstance().waitUntilShellIsOpen(
                "Add SVN Repository");
            IRemoteBotShell shell2 = RemoteWorkbenchBot.getInstance().shell(
                "Add SVN Repository");
            shell2.activate();
            shell2.bot().comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
            shell2.bot().button(FINISH).click();
            RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(
                "Add SVN Repository");
            if (!viewWasOpen)
                RemoteWorkbenchBot.getInstance().view(VIEW_SVN_REPOSITORIES)
                    .close();
            // recur...
            shareProjectUsingSpecifiedFolderName(repositoryURL,
                specifiedFolderName);
            return;
        }

        RemoteWorkbenchBot.getInstance().shell(SHELL_SHARE_PROJECT)
            .confirmWithTable(repositoryURL, NEXT);
        IRemoteBotShell shell3 = RemoteWorkbenchBot.getInstance().shell(
            SHELL_SHARE_PROJECT);
        shell3.bot().radio("Use specified folder name:").click();
        shell3.bot().text().setText(specifiedFolderName);
        shell3.bot().button(FINISH).click();
        RemoteWorkbenchBot.getInstance().shell("Remote Project Exists")
            .waitUntilActive();
        RemoteWorkbenchBot.getInstance().shell("Remote Project Exists")
            .confirm(YES);
        RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(
            SHELL_SHARE_PROJECT);
        try {
            RemoteWorkbenchBot.getInstance().sleep(1000);
            if (RemoteWorkbenchBot.getInstance().isShellOpen(
                "Confirm Open Perspective"))
                RemoteWorkbenchBot.getInstance()
                    .shell("Confirm Open Perspective").confirm(NO);
        } catch (TimeoutException e) {
            // ignore
        }

    }

    public void importProjectFromSVN(String repositoryURL)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().menu(MENU_FILE).menu("Import...")
            .click();
        IRemoteBotShell shell = RemoteWorkbenchBot.getInstance().shell(
            SHELL_IMPORT);
        shell.confirmWithTreeWithFilterText(TABLE_ITEM_REPOSITORY_TYPE_SVN,
            "Checkout Projects from SVN", NEXT);
        if (shell.bot().table().containsItem(repositoryURL)) {
            RemoteWorkbenchBot.getInstance().shell("Checkout from SVN")
                .confirmWithTable(repositoryURL, NEXT);
        } else {
            shell.bot().radio("Create a new repository location").click();
            shell.bot().button(NEXT).click();
            shell.bot().comboBoxWithLabel("Url:").setText(repositoryURL);
            shell.bot().button(NEXT).click();
            RemoteWorkbenchBot.getInstance().shell("Checkout from SVN")
                .waitUntilActive();
        }
        RemoteWorkbenchBot
            .getInstance()
            .shell("Checkout from SVN")
            .confirmWithTreeWithWaitingExpand("Checkout from SVN", FINISH,
                repositoryURL, "trunk", "examples");
        RemoteWorkbenchBot.getInstance().shell("SVN Checkout")
            .waitUntilActive();
        RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed("SVN Checkout");
    }

    public void disConnect() throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM, CM_DISCONNECT);

        RemoteWorkbenchBot.getInstance()
            .shell(SHELL_CONFIRM_DISCONNECT_FROM_SVN).confirm(YES);
    }

    public void revert() throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM, CM_REVERT);

        RemoteWorkbenchBot.getInstance().shell(SHELL_REVERT).confirm(OK);
        RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(SHELL_REVERT);
    }

    public void update(String versionID) throws RemoteException {
        switchToAnotherRevision(versionID);
    }

    private void switchToAnotherRevision(String versionID)
        throws RemoteException {

        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM,
            CM_SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION);

        IRemoteBotShell shell = RemoteWorkbenchBot.getInstance().shell(
            SHELL_SWITCH);
        shell.waitUntilActive();
        if (shell.bot().checkBox(LABEL_SWITCH_TOHEAD_REVISION).isChecked())
            shell.bot().checkBox(LABEL_SWITCH_TOHEAD_REVISION).click();
        shell.bot().textWithLabel(LABEL_REVISION).setText(versionID);
        shell.bot().button(OK).click();
        if (RemoteWorkbenchBot.getInstance().isShellOpen(SHELL_SVN_SWITCH))
            RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(
                SHELL_SVN_SWITCH);
    }

    public void switchProject(String projectName, String url)
        throws RemoteException {
        switchResource(projectName, url, "HEAD");
    }

    public void switchResource(String fullPath, String url)
        throws RemoteException {
        switchResource(fullPath, url, "HEAD");
    }

    public void switchResource(String fullPath, String url, String revision)
        throws RemoteException {

        final IPath path = new Path(fullPath);
        final IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            throw new RemoteException("Resource \"" + path + "\" not found.");

        final IProject project = resource.getProject();
        VCSAdapter vcs = VCSAdapter.getAdapter(project);
        if (vcs == null) {
            throw new RemoteException("No VCSAdapter found for \""
                + project.getName() + "\".");
        }

        vcs.switch_(resource, url, revision, new NullProgressMonitor());
    }
    /**********************************************
     * 
     * States
     * 
     **********************************************/

}
