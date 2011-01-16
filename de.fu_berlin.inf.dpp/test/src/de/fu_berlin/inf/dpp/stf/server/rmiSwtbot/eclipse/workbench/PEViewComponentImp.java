package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.program.Program;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.ContextMenuHelper;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

public class PEViewComponentImp extends EclipseComponent implements
    PEViewComponent {

    /* View infos */
    protected final static String VIEWNAME = "Package Explorer";
    protected final static String VIEWID = "org.eclipse.jdt.ui.PackageExplorer";

    /*
     * title of shells which are pop up by performing the actions on the package
     * explorer view.
     */

    private final static String SHELL_EDITOR_SELECTION = "Editor Selection";

    private final static String SHELL_REVERT = "Revert";
    private final static String SHELL_SHARE_PROJECT = "Share Project";
    private final static String SHELL_SAROS_RUNNING_VCS_OPERATION = "Saros running VCS operation";

    private static final String SHELL_SWITCH = "Switch";
    private static final String SHELL_SVN_SWITCH = "SVN Switch";
    private final static String SHELL_CONFIRM_DISCONNECT_FROM_SVN = "Confirm Disconnect from SVN";
    private static final String SHELL_IMPORT = "Import";

    /* Label of pop up windows */
    private final static String LABEL_CREATE_A_NEW_REPOSITORY_LOCATION = "Create a new repository location";
    private final static String LABEL_URL = "Url:";

    private final static String LABEL_TO_URL = "To URL:";
    private static final String LABEL_SWITCH_TOHEAD_REVISION = "Switch to HEAD revision";
    private static final String LABEL_REVISION = "Revision:";

    /* Context menu of a selected file on the package explorer view */
    private final static String OPEN = "Open";
    private final static String OPEN_WITH = "Open With";
    private final static String TEAM = "Team";

    /* All the sub menus of the context menu "Open with" */
    // private final static String TEXT_EDITOR = "Text Editor";
    // private final static String SYSTEM_EDITOR = "System Editor";
    // private final static String DEFAULT_EDITOR = "Default Editor";
    private final static String OTHER = "Other...";

    /* All the sub menus of the context menu "Team" */
    private final static String REVERT = "Revert...";
    private final static String DISCONNECT = "Disconnect...";
    private final static String SHARE_PROJECT = "Share Project...";
    private final static String SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION = "Switch to another Branch/Tag/Revision...";

    /* table iems of the shell "Share project" of the conext menu "Team" */
    private final static String REPOSITORY_TYPE_SVN = "SVN";

    /***********************************************************************
     * 
     * exported functions
     * 
     ***********************************************************************/

    /**********************************************
     * 
     * open/close/activate the package explorer view
     * 
     **********************************************/
    public void openPEView() throws RemoteException {
        if (!isPEViewOpen())
            basic.openViewById(VIEWID);
    }

    public boolean isPEViewOpen() throws RemoteException {
        return basic.isViewOpen(VIEWNAME);
    }

    public void closePEView() throws RemoteException {
        basic.closeViewByTitle(VIEWNAME);
    }

    public void setFocusOnPEView() throws RemoteException {
        basic.setFocusOnViewByTitle(VIEWNAME);
    }

    public boolean isPEViewActive() throws RemoteException {
        return basic.isViewActive(VIEWNAME);
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Open"
     * 
     **********************************************/

    public void openFile(String... fileNodes) throws RemoteException {
        precondition();
        basic.clickContextsOfTreeItemInView(VIEWNAME, OPEN, fileNodes);
    }

    public void openClass(String projectName, String pkg, String className)
        throws RemoteException {
        String[] classNodes = getClassNodes(projectName, pkg, className);
        openFile(changeToRegex(classNodes));
    }

    public void openClassWith(String whichEditor, String projectName,
        String pkg, String className) throws RemoteException {
        openFileWith(whichEditor, getClassNodes(projectName, pkg, className));
    }

    public void openFileWith(String whichEditor, String... fileNodes)
        throws RemoteException {
        precondition();
        SWTBotTree tree = basic.getTreeInView(VIEWNAME);
        tree.expandNode(fileNodes).select();
        ContextMenuHelper.clickContextMenu(tree, OPEN_WITH, OTHER);
        shellC.waitUntilShellActive(SHELL_EDITOR_SELECTION);
        SWTBotTable table = bot.table();
        table.select(whichEditor);
        basic.waitUntilButtonEnabled(OK);
        shellC.confirmShell(SHELL_EDITOR_SELECTION, OK);
    }

    public void openClassWithSystemEditor(String projectName, String pkg,
        String className) throws RemoteException {
        IPath path = new Path(getClassPath(projectName, pkg, className));
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        Program.launch(resource.getLocation().toString());
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Team"
     * 
     **********************************************/
    public void shareProjectWithSVN(String projectName, String repositoryURL)
        throws RemoteException {

        String[] contexts = { TEAM, SHARE_PROJECT };
        basic.clickSubMenuOfContextsOfTreeItemInView(VIEWNAME, contexts,
            changeToRegex(projectName));

        shellC.confirmShellWithTable(SHELL_SHARE_PROJECT, REPOSITORY_TYPE_SVN,
            NEXT);
        log.debug("SVN share project text: " + bot.text());
        if (bot.table().containsItem(repositoryURL)) {
            shellC.confirmShellWithTable(SHELL_SHARE_PROJECT, repositoryURL,
                NEXT);
        } else {
            bot.radio(LABEL_CREATE_A_NEW_REPOSITORY_LOCATION).click();
            bot.button(NEXT).click();
            bot.comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
        }
        basic.waitUntilButtonEnabled(FINISH);
        bot.button(FINISH).click();
        shellC.waitUntilShellClosed(SHELL_SHARE_PROJECT);
    }

    public void shareProjectWithSVNWhichIsConfiguredWithSVNInfos(
        String projectName, String repositoryURL) throws RemoteException {

        String[] contexts = { TEAM, SHARE_PROJECT };
        basic.clickSubMenuOfContextsOfTreeItemInView(VIEWNAME, contexts,
            changeToRegex(projectName));

        shellC.confirmShellWithTable(SHELL_SHARE_PROJECT, REPOSITORY_TYPE_SVN,
            NEXT);
        log.debug("SVN share project text: " + bot.text());
        basic.waitUntilButtonEnabled(FINISH);
        bot.button(FINISH).click();
        shellC.waitUntilShellClosed(SHELL_SHARE_PROJECT);
    }

    public void shareProjectWithSVNUsingSpecifiedFolderName(String projectName,
        String repositoryURL, String specifiedFolderName)
        throws RemoteException {
        precondition();

        String[] contexts = { TEAM, SHARE_PROJECT };
        basic.clickSubMenuOfContextsOfTreeItemInView(VIEWNAME, contexts,
            changeToRegex(projectName));

        shellC.confirmShellWithTable(SHELL_SHARE_PROJECT, REPOSITORY_TYPE_SVN,
            NEXT);

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
            basic
                .openViewById("org.tigris.subversion.subclipse.ui.repository.RepositoriesView");
            basic.setFocusOnViewByTitle("SVN Repositories");
            final boolean viewWasOpen = basic.isViewOpen("SVN Repositories");
            final SWTBotView repoView = basic.getView("SVN Repositories");
            repoView.toolbarButton("Add SVN Repository").click();
            if (!shellC.activateShellWithText("Add SVN Repository")) {
                shellC.waitUntilShellActive("Add SVN Repository");
            }
            bot.comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
            bot.button(FINISH).click();
            shellC.waitUntilShellClosed("Add SVN Repository");
            if (!viewWasOpen)
                repoView.close();
            // recur...
            shareProjectWithSVNUsingSpecifiedFolderName(projectName,
                repositoryURL, specifiedFolderName);
            return;
        }

        shellC.confirmShellWithTable(SHELL_SHARE_PROJECT, repositoryURL, NEXT);

        bot.radio("Use specified folder name:").click();
        bot.text().setText(specifiedFolderName);
        bot.button(FINISH).click();
        shellC.waitUntilShellActive("Remote Project Exists");
        shellC.confirmShell("Remote Project Exists", YES);
        bot.sleep(500);
        if (shellC.isShellOpen("Confirm Open Perspective"))
            shellC.confirmShell("Confirm Open Perspective", NO);
        else
            shellC.waitUntilShellClosed(SHELL_SHARE_PROJECT);
    }

    public void importProjectFromSVN(String repositoryURL)
        throws RemoteException {
        precondition();
        basic.clickMenuWithTexts("File", "Import...");
        shellC.confirmShellWithTreeWithFilterText(SHELL_IMPORT,
            REPOSITORY_TYPE_SVN, "Checkout Projects from SVN", NEXT);
        if (bot.table().containsItem(repositoryURL)) {
            shellC.confirmShellWithTable("Checkout from SVN", repositoryURL,
                NEXT);
        } else {
            bot.radio("Create a new repository location").click();
            bot.button(NEXT).click();
            bot.comboBoxWithLabel("Url:").setText(repositoryURL);
            bot.button(NEXT).click();
            shellC.waitUntilShellActive("Checkout from SVN");
        }
        shellC.confirmShellWithTreeWithWaitingExpand("Checkout from SVN",
            FINISH, repositoryURL, "trunk", "examples");
        shellC.waitUntilShellActive("SVN Checkout");
        shellC.waitUntilShellClosed("SVN Checkout");
    }

    public void disConnect(String projectName) throws RemoteException {

        String[] contexts = { TEAM, DISCONNECT };
        basic.clickSubMenuOfContextsOfTreeItemInView(VIEWNAME, contexts,
            changeToRegex(projectName));

        shellC.confirmShell(SHELL_CONFIRM_DISCONNECT_FROM_SVN, YES);
    }

    public void revertProject(String projectName) throws RemoteException {
        precondition();

        String[] contexts = { TEAM, REVERT };
        basic.clickSubMenuOfContextsOfTreeItemInView(VIEWNAME, contexts,
            changeToRegex(projectName));

        shellC.confirmShell(SHELL_REVERT, OK);
        shellC.waitUntilShellClosed(SHELL_REVERT);
    }

    public void updateProject(String projectName, String versionID)
        throws RemoteException {
        String[] nodes = { projectName + ".*" };
        switchToAnotherRevision(nodes, versionID);
    }

    public void updateClass(String projectName, String pkg, String className,
        String revision) throws RemoteException {
        String[] nodes = getClassNodes(projectName, pkg, className);
        nodes = changeToRegex(nodes);
        switchToAnotherRevision(nodes, revision);
    }

    public void switchProjectWithGui(String projectName, String url)
        throws RemoteException {
        precondition();

        String[] contexts = { TEAM, SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION };
        basic.clickSubMenuOfContextsOfTreeItemInView(VIEWNAME, contexts,
            changeToRegex(projectName));

        shellC.waitUntilShellActive(SHELL_SWITCH);
        bot.comboBoxWithLabel(LABEL_TO_URL).setText(url);
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_SVN_SWITCH);
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
        precondition();
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

    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException {
        shellC.waitUntilShellClosed(SHELL_SAROS_RUNNING_VCS_OPERATION);
    }

    public boolean isProjectManagedBySVN(String projectName)
        throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        final VCSAdapter vcs = VCSAdapter.getAdapter(project);
        if (vcs == null)
            return false;
        return true;
    }

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException {
        waitUntil(SarosConditions.isInSVN(projectName));
    }

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException {
        waitUntil(SarosConditions.isNotInSVN(projectName));
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

    public void waitUntilRevisionIsSame(String fullPath, String revision)
        throws RemoteException {
        waitUntil(SarosConditions.isRevisionSame(fullPath, revision));
    }

    public void waitUntilUrlIsSame(String fullPath, String url)
        throws RemoteException {
        waitUntil(SarosConditions.isUrlSame(fullPath, url));
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

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    protected void precondition() throws RemoteException {
        openPEView();
        setFocusOnPEView();
    }

    private void switchToAnotherRevision(String[] matchTexts, String versionID)
        throws RemoteException {
        precondition();
        String[] contexts = { TEAM, SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION };
        basic.clickSubMenuOfContextsOfTreeItemInView(VIEWNAME, contexts,
            matchTexts);
        shellC.waitUntilShellActive(SHELL_SWITCH);
        if (bot.checkBox(LABEL_SWITCH_TOHEAD_REVISION).isChecked())
            bot.checkBox(LABEL_SWITCH_TOHEAD_REVISION).click();
        bot.textWithLabel(LABEL_REVISION).setText(versionID);
        bot.button(OK).click();
        if (shellC.isShellOpen(SHELL_SVN_SWITCH))
            shellC.waitUntilShellClosed(SHELL_SVN_SWITCH);
    }

    public void copyProject(String target, String source)
        throws RemoteException {

        if (fileM.existsProject(target)) {
            throw new RemoteException("Can't copy project from " + source
                + " to " + target + " , the target already exists.");
        }
        precondition();

        basic.clickContextsOfTreeItemInView(VIEWNAME, "Copy",
            changeToRegex(source));
        basic.clickContextsOfTreeItemInView(VIEWNAME, "Paste",
            changeToRegex(source));

        shellC.activateShellWithText("Copy Project");
        bot.textWithLabel("Project name:").setText(target);
        bot.button(OK).click();
        shellC.waitUntilShellClosed("Copy Project");
        bot.sleep(1000);
    }

}
