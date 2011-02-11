package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.contextMenu;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

public class TeamCImp extends EclipseComponentImp implements TeamC {

    private static transient TeamCImp teamImp;

    /**
     * {@link TeamCImp} is a singleton, but inheritance is possible.
     */
    public static TeamCImp getInstance() {
        if (teamImp != null)
            return teamImp;
        teamImp = new TeamCImp();
        return teamImp;
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
    public void shareProjectWithSVN(String viewTitle, String projectName,
        String repositoryURL) throws RemoteException {

        String[] contexts = { CM_TEAM, CM_SHARE_PROJECT_OF_TEAM };

        view(viewTitle).bot().tree()
            .clickContextMenusOfTreeItem(contexts, changeToRegex(projectName));

        shell(SHELL_SHARE_PROJECT).confirmShellWithTable(SHELL_SHARE_PROJECT,
            TABLE_ITEM_REPOSITORY_TYPE_SVN, NEXT);
        log.debug("SVN share project text: " + bot.text());
        if (bot.table().containsItem(repositoryURL)) {
            shell(SHELL_SHARE_PROJECT).confirmShellWithTable(
                SHELL_SHARE_PROJECT, repositoryURL, NEXT);
        } else {
            bot.radio(LABEL_CREATE_A_NEW_REPOSITORY_LOCATION).click();
            bot.button(NEXT).click();
            bot.comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
        }
        buttonW.waitUntilButtonEnabled(FINISH);
        bot.button(FINISH).click();
        shell(SHELL_SHARE_PROJECT).waitsUntilIsShellClosed(SHELL_SHARE_PROJECT);
    }

    public void shareProjectWithSVNWhichIsConfiguredWithSVNInfos(
        String viewTitle, String projectName, String repositoryURL)
        throws RemoteException {

        String[] contexts = { CM_TEAM, CM_SHARE_PROJECT_OF_TEAM };

        view(viewTitle).bot().tree()
            .clickContextMenusOfTreeItem(contexts, changeToRegex(projectName));

        shell(SHELL_SHARE_PROJECT).confirmShellWithTable(SHELL_SHARE_PROJECT,
            TABLE_ITEM_REPOSITORY_TYPE_SVN, NEXT);
        log.debug("SVN share project text: " + bot.text());
        buttonW.waitUntilButtonEnabled(FINISH);
        bot.button(FINISH).click();
        shell(SHELL_SHARE_PROJECT).waitsUntilIsShellClosed(SHELL_SHARE_PROJECT);
    }

    public void shareProjectWithSVNUsingSpecifiedFolderName(String viewTitle,
        String projectName, String repositoryURL, String specifiedFolderName)
        throws RemoteException {
        precondition(viewTitle);

        String[] contexts = { CM_TEAM, CM_SHARE_PROJECT_OF_TEAM };

        view(viewTitle).bot().tree()
            .clickContextMenusOfTreeItem(contexts, changeToRegex(projectName));

        shell(SHELL_SHARE_PROJECT).confirmShellWithTable(SHELL_SHARE_PROJECT,
            TABLE_ITEM_REPOSITORY_TYPE_SVN, NEXT);

        SWTBotTable table = null;
        final SWTBotShell shareProjectShell = bot.shell(SHELL_SHARE_PROJECT);
        try {
            table = shareProjectShell.bot().table();
        } catch (WidgetNotFoundException e) {
            //
        }

        if (table == null || !table.containsItem(repositoryURL)) {
            // close window
            shareProjectShell.close();
            // in svn repos view: enter url
            view(VIEW_SVN_REPOSITORIES).openById();
            view(VIEW_SVN_REPOSITORIES).setViewTitle("SVN Repositories");
            view(VIEW_SVN_REPOSITORIES).setFocus();
            final boolean viewWasOpen = view(VIEW_SVN_REPOSITORIES).isOpen();
            toolbarButtonW.clickToolbarButtonWithTooltipOnView(
                VIEW_SVN_REPOSITORIES, "Add SVN Repository");

            shell("Add SVN Repository").activateShellAndWait(
                "Add SVN Repository");
            bot.comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
            bot.button(FINISH).click();
            shell("Add SVN Repository").waitsUntilIsShellClosed(
                "Add SVN Repository");
            if (!viewWasOpen)
                view(VIEW_SVN_REPOSITORIES).close();
            // recur...
            shareProjectWithSVNUsingSpecifiedFolderName(viewTitle, projectName,
                repositoryURL, specifiedFolderName);
            return;
        }

        shell(SHELL_SHARE_PROJECT).confirmShellWithTable(SHELL_SHARE_PROJECT,
            repositoryURL, NEXT);

        bot.radio("Use specified folder name:").click();
        bot.text().setText(specifiedFolderName);
        bot.button(FINISH).click();
        shell("Remote Project Exists").waitUntilShellActive(
            "Remote Project Exists");
        shell("Remote Project Exists").confirmShell("Remote Project Exists",
            YES);
        try {
            shell("Confirm Open Perspective").waitUntilShellActive(
                "Confirm Open Perspective");
            shell("Confirm Open Perspective").confirmShell(
                "Confirm Open Perspective", NO);
        } catch (TimeoutException e) {
            // ignore
        }
        shell(SHELL_SHARE_PROJECT).waitsUntilIsShellClosed(SHELL_SHARE_PROJECT);
    }

    public void importProjectFromSVN(String repositoryURL)
        throws RemoteException {

        menuW.clickMenuWithTexts("File", "Import...");
        shell(SHELL_IMPORT).confirmShellWithTreeWithFilterText(SHELL_IMPORT,
            TABLE_ITEM_REPOSITORY_TYPE_SVN, "Checkout Projects from SVN", NEXT);
        if (bot.table().containsItem(repositoryURL)) {
            shell("Checkout from SVN").confirmShellWithTable(
                "Checkout from SVN", repositoryURL, NEXT);
        } else {
            bot.radio("Create a new repository location").click();
            bot.button(NEXT).click();
            bot.comboBoxWithLabel("Url:").setText(repositoryURL);
            bot.button(NEXT).click();
            shell("Checkout from SVN")
                .waitUntilShellActive("Checkout from SVN");
        }
        shell("Checkout from SVN").confirmShellWithTreeWithWaitingExpand(
            "Checkout from SVN", FINISH, repositoryURL, "trunk", "examples");
        shell("SVN Checkout").waitUntilShellActive("SVN Checkout");
        shell("SVN Checkout").waitsUntilIsShellClosed("SVN Checkout");
    }

    public void disConnect(String viewTitle, String projectName)
        throws RemoteException {

        String[] contexts = { CM_TEAM, CM_DISCONNECT };

        view(viewTitle).bot().tree()
            .clickContextMenusOfTreeItem(contexts, changeToRegex(projectName));

        shell(SHELL_CONFIRM_DISCONNECT_FROM_SVN).confirmShell(
            SHELL_CONFIRM_DISCONNECT_FROM_SVN, YES);
    }

    public void revertProject(String viewTitle, String projectName)
        throws RemoteException {
        precondition(viewTitle);

        String[] contexts = { CM_TEAM, CM_REVERT };

        view(viewTitle).bot().tree()
            .clickContextMenusOfTreeItem(contexts, changeToRegex(projectName));

        shell(SHELL_REVERT).confirmShell(SHELL_REVERT, OK);
        shell(SHELL_REVERT).waitsUntilIsShellClosed(SHELL_REVERT);
    }

    public void updateProject(String viewTitle, String projectName,
        String versionID) throws RemoteException {
        String[] nodes = { projectName + ".*" };
        switchToAnotherRevision(viewTitle, nodes, versionID);
    }

    public void updateClass(String viewTitle, String projectName, String pkg,
        String className, String revision) throws RemoteException {
        String[] nodes = getClassNodes(projectName, pkg, className);
        nodes = changeToRegex(nodes);
        switchToAnotherRevision(viewTitle, nodes, revision);
    }

    public void switchProjectWithGui(String viewTitle, String projectName,
        String url) throws RemoteException {
        precondition(viewTitle);

        String[] contexts = { CM_TEAM, CM_SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION };

        view(viewTitle).bot().tree()
            .clickContextMenusOfTreeItem(contexts, changeToRegex(projectName));

        shell(SHELL_SWITCH).waitUntilShellActive(SHELL_SWITCH);
        bot.comboBoxWithLabel(LABEL_TO_URL).setText(url);
        bot.button(OK).click();
        shell(SHELL_SVN_SWITCH).waitsUntilIsShellClosed(SHELL_SVN_SWITCH);
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

    private void switchToAnotherRevision(String viewTitle, String[] matchTexts,
        String versionID) throws RemoteException {
        precondition(viewTitle);
        String[] contexts = { CM_TEAM, CM_SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION };

        view(viewTitle).bot().tree()
            .clickContextMenusOfTreeItem(contexts, matchTexts);
        shell(SHELL_SWITCH).waitUntilShellActive(SHELL_SWITCH);
        if (bot.checkBox(LABEL_SWITCH_TOHEAD_REVISION).isChecked())
            bot.checkBox(LABEL_SWITCH_TOHEAD_REVISION).click();
        bot.textWithLabel(LABEL_REVISION).setText(versionID);
        bot.button(OK).click();
        if (shell(SHELL_SVN_SWITCH).isShellOpen(SHELL_SVN_SWITCH))
            shell(SHELL_SVN_SWITCH).waitsUntilIsShellClosed(SHELL_SVN_SWITCH);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isProjectManagedBySVN(String projectName)
        throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        final VCSAdapter vcs = VCSAdapter.getAdapter(project);
        if (vcs == null)
            return false;
        return true;
    }

    public String getRevision(String fullPath) throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            throw new RemoteException("Resource \"" + fullPath
                + "\" not found.");
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        VCSResourceInfo info = vcs.getCurrentResourceInfo(resource);
        String result = info != null ? info.revision : null;
        return result;
    }

    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            throw new RemoteException("Resource not found at \"" + fullPath
                + "\"");
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        final VCSResourceInfo info = vcs.getResourceInfo(resource);
        return info.url;
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException {
        shell(SHELL_SAROS_RUNNING_VCS_OPERATION).waitsUntilIsShellClosed(
            SHELL_SAROS_RUNNING_VCS_OPERATION);
    }

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException {
        waitUntil(SarosConditions.isInSVN(projectName));
    }

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException {
        waitUntil(SarosConditions.isNotInSVN(projectName));
    }

    public void waitUntilRevisionIsSame(String fullPath, String revision)
        throws RemoteException {
        waitUntil(SarosConditions.isRevisionSame(fullPath, revision));
    }

    public void waitUntilUrlIsSame(String fullPath, String url)
        throws RemoteException {
        waitUntil(SarosConditions.isUrlSame(fullPath, url));
    }

    protected void precondition(String viewTitle) throws RemoteException {
        view(viewTitle).openById();
        view(viewTitle).setViewTitle(viewTitle);
        view(viewTitle).setFocus();
    }
}
