package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.bot.condition.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
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

    @Override
    public void shareProject(String repositoryURL) throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM,
            CM_SHARE_PROJECT_OF_TEAM);

        SWTBotShell shell = getSvnShell();
        shell.bot().sleep(500);

        if (shell.bot().table().containsItem(repositoryURL)) {
            shell.bot().table().select(repositoryURL);
            shell.bot().button(NEXT).click();
        } else {
            shell.bot().radio(LABEL_CREATE_A_NEW_REPOSITORY_LOCATION).click();
            shell.bot().button(NEXT).click();
            shell.bot().comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
        }

        shell.bot().button(FINISH).click();
        shell.bot().waitUntil(Conditions.shellCloses(shell));
    }

    @Override
    public void shareProjectConfiguredWithSVNInfos(String repositoryURL)
        throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM,
            CM_SHARE_PROJECT_OF_TEAM);

        SWTBotShell shell = getSvnShell();
        shell.bot().button(FINISH).click();
        shell.bot().waitUntil(Conditions.shellCloses(shell));
    }

    private SWTBotShell getSvnShell() {
        SWTBotShell shell = new SWTBot().shell(SHELL_SHARE_PROJECT);
        shell.activate();
        shell.bot().sleep(500);

        int tableItemCount = shell.bot().table().rowCount();
        SWTBotTable table = null;
        for (int i = 0; i < tableItemCount; i++) {
            shell.bot().sleep(500);
            SWTBotTableItem item = shell.bot().table().getTableItem(i);
            if (!item.getText().equals(TABLE_ITEM_REPOSITORY_TYPE_SVN))
                continue;

            item.select();
            shell.bot().button(NEXT).click();
            try {
                shell.bot().sleep(500);
                table = shell.bot().table();
                if (table.columnCount() != 1) {
                    table = null;
                    shell.bot().button(BACK).click();
                    continue;
                }
                break;
            } catch (WidgetNotFoundException e) {
                log.warn("expected table in SVN shell, wrong SVN client ?", e);
                shell.bot().button(BACK).click();
            }
        }
        if (table == null) {
            shell.close();
            throw new RuntimeException("no or wrong SVN client found");
        }
        return shell;

    }

    @Override
    public void shareProjectUsingSpecifiedFolderName(String repositoryURL,
        String specifiedFolderName) throws RemoteException {

        addRepositoryUrl(repositoryURL);

        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM,
            CM_SHARE_PROJECT_OF_TEAM);

        SWTBotShell shell = getSvnShell();
        shell.bot().table().select(repositoryURL);
        shell.bot().button(NEXT).click();
        shell.bot().radio("Use specified folder name:").click();
        shell.bot().text().setText(specifiedFolderName);
        shell.bot().button(FINISH).click();

        SWTBotShell shellPopup = new SWTBot().shell("Remote Project Exists");
        shellPopup.activate();
        shellPopup.bot().button(YES).click();

        shell.bot().waitUntil(Conditions.shellCloses(shell),
            SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);

        new SWTBot().sleep(1000);
        if (RemoteWorkbenchBot.getInstance().isShellOpen(
            "Confirm Open Perspective")) {
            shell = new SWTBot().shell("Confirm Open Perspective");
            shell.activate();
            shell.bot().button(NO).click();
            shell.bot().waitUntil(Conditions.shellCloses(shell));
        }
    }

    private void addRepositoryUrl(String repositoryURL) throws RemoteException {
        RemoteWorkbenchBot.getInstance().activateWorkbench();

        new SWTBot().menu(MENU_WINDOW).menu(MENU_SHOW_VIEW).menu(MENU_OTHER)
            .click();

        SWTBotShell viewShell = new SWTBot().shell(SHELL_SHOW_VIEW);
        viewShell.activate();
        new SWTBot().sleep(500);
        SWTBotTreeItem[] items = viewShell.bot().tree().getAllItems();

        boolean svnClientFound = false;
        outer: for (SWTBotTreeItem item : items) {
            if (item.getText().equals("SVN")) {
                item.expand();
                SWTBotTreeItem[] subItems = item.getItems();
                for (SWTBotTreeItem subItem : subItems) {
                    if (subItem.getText().equals("SVN Annotate")) {
                        svnClientFound = true;
                        item.select(VIEW_SVN_REPOSITORIES);
                        break outer;
                    }
                }
            }
        }

        if (!svnClientFound)
            throw new RuntimeException("SVN Client is not installed");

        viewShell.bot().button(OK).click();
        viewShell.bot().waitUntil(Conditions.shellCloses(viewShell));

        SWTBotView view = new SWTWorkbenchBot()
            .viewByTitle(VIEW_SVN_REPOSITORIES);

        view.show();
        view.bot().sleep(500);

        for (SWTBotTreeItem item : view.bot().tree().getAllItems())
            if (item.getText().equals(repositoryURL)) {
                view.close();
                return;
            }

        view.toolbarButton("Add SVN Repository").click();
        SWTBotShell shell = new SWTBot().shell("Add SVN Repository");
        shell.bot().comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
        shell.bot().button(FINISH).click();
        shell.bot().waitUntil(Conditions.shellCloses(shell),
            SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
        view.close();

    }

    @Override
    public void importProjectFromSVN(String repositoryURL)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().menu(MENU_FILE).menu("Import...")
            .click();

        SWTBotShell shell = new SWTBot().shell(SHELL_IMPORT);
        shell.activate();

        shell.bot().text("type filter text")
            .setText("Checkout Projects from SVN");
        shell
            .bot()
            .tree()
            .expandNode(TABLE_ITEM_REPOSITORY_TYPE_SVN,
                "Checkout Projects from SVN").select();
        shell.bot().button(NEXT).click();

        if (shell.bot().table().containsItem(repositoryURL)) {
            shell.bot().table().select(repositoryURL);
            shell.bot().button(NEXT).click();

        } else {
            shell.bot().radio("Create a new repository location").click();
            shell.bot().button(NEXT).click();
            shell.bot().comboBoxWithLabel("Url:").setText(repositoryURL);
            shell.bot().button(NEXT).click();
            shell.bot()
                .waitUntil(Conditions.shellIsActive("Checkout from SVN"));
        }
        shell.bot().tree()
            .expandNode(repositoryURL, "stf_tests", "stf_test_project");

        shell.bot().button(FINISH).click();
        shell.bot().waitUntil(Conditions.shellCloses(shell),
            SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
    }

    @Override
    public void disconnect() throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM, CM_DISCONNECT);
        SWTBotShell shell = new SWTBot()
            .shell(SHELL_CONFIRM_DISCONNECT_FROM_SVN);
        shell.activate();
        shell.bot().button(YES).click();
        shell.bot().waitUntil(Conditions.shellCloses(shell));
    }

    @Override
    public void revert() throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM, CM_REVERT);
        SWTBotShell shell = new SWTBot().shell(SHELL_REVERT);
        shell.activate();
        shell.bot().button(OK).click();
        shell.bot().waitUntil(Conditions.shellCloses(shell));
    }

    @Override
    public void update(String versionID) throws RemoteException {
        switchToAnotherRevision(versionID);
    }

    private void switchToAnotherRevision(String versionID) {

        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_TEAM,
            CM_SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION);

        SWTBotShell shell = new SWTBot().shell(SHELL_SWITCH);
        shell.activate();

        if (shell.bot().checkBox(LABEL_SWITCH_TOHEAD_REVISION).isChecked())
            shell.bot().checkBox(LABEL_SWITCH_TOHEAD_REVISION).click();
        shell.bot().textWithLabel(LABEL_REVISION).setText(versionID);
        shell.bot().button(OK).click();
        shell.bot().sleep(1000);
        shell.bot().waitWhile(
            SarosConditions
                .isShellOpen(new SWTWorkbenchBot(), SHELL_SVN_SWITCH));

    }

    @Override
    public void switchProject(String projectName, String url)
        throws RemoteException {
        switchResource(projectName, url, "HEAD");
    }

    @Override
    public void switchResource(String fullPath, String url)
        throws RemoteException {
        switchResource(fullPath, url, "HEAD");
    }

    @Override
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
}
