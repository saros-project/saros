package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.eclipseViews;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.IContextMenusInPEView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.Views;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

public class PEView extends Views implements IPEView {
    private static transient PEView pEViewImp;

    private IRemoteBotView view;
    private IRemoteBotTree tree;

    /**
     * {@link PEView} is a singleton, but inheritance is possible.
     */
    public static PEView getInstance() {
        if (pEViewImp != null)
            return pEViewImp;
        pEViewImp = new PEView();
        return pEViewImp;
    }

    public IPEView setView(IRemoteBotView view) throws RemoteException {
        this.view = view;
        tree = this.view.bot().tree();
        return this;
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

    public IContextMenusInPEView tree() throws RemoteException {
        contextMenu.setTree(tree);
        contextMenu.setTreeItem(null);
        contextMenu.setTreeItemType(null);
        return contextMenu;
    }

    public IContextMenusInPEView selectSrc(String projectName)
        throws RemoteException {

        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(projectName), SRC),
            TreeItemType.JAVA_PROJECT);
        return contextMenu;
    }

    public IContextMenusInPEView selectJavaProject(String projectName)
        throws RemoteException {

        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(projectName)),
            TreeItemType.JAVA_PROJECT);
        return contextMenu;
    }

    public IContextMenusInPEView selectProject(String projectName)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(projectName)),
            TreeItemType.PROJECT);
        return contextMenu;
    }

    public IContextMenusInPEView selectPkg(String projectName, String pkg)
        throws RemoteException {
        String[] nodes = { projectName, SRC, pkg };
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(nodes)),
            TreeItemType.PKG);

        return contextMenu;
    }

    public IContextMenusInPEView selectClass(String projectName, String pkg,
        String className) throws RemoteException {
        String[] nodes = getClassNodes(projectName, pkg, className);

        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(nodes)),
            TreeItemType.CLASS);

        return contextMenu;
    }

    public IContextMenusInPEView selectFolder(String... folderNodes)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(folderNodes)),
            TreeItemType.FOLDER);

        return contextMenu;
    }

    public IContextMenusInPEView selectFile(String... fileNodes)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(changeToRegex(fileNodes)),
            TreeItemType.FILE);

        return contextMenu;
    }

    /**********************************************
     * 
     * States
     * 
     **********************************************/

    public String getTitle() throws RemoteException {
        return VIEW_PACKAGE_EXPLORER;
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

    public String getFileContent(String... nodes) throws RemoteException,
        IOException, CoreException {
        IPath path = new Path(getPath(nodes));
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        log.info("Checking full path: \"" + file.getFullPath().toOSString()
            + "\"");
        return ConvertStreamToString(file.getContents());
    }

    /**********************************************
     * 
     * wait until
     * 
     **********************************************/
    public void waitUntilFolderExists(String... folderNodes)
        throws RemoteException {
        String fullPath = getPath(folderNodes);
        remoteBot().waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilPkgExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            remoteBot().waitUntil(
                SarosConditions.isResourceExist(getPkgPath(projectName, pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilPkgNotExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            remoteBot().waitUntil(
                SarosConditions
                    .isResourceNotExist(getPkgPath(projectName, pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilFileExists(String... fileNodes) throws RemoteException {
        String fullPath = getPath(fileNodes);
        remoteBot().waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilClassExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        remoteBot().waitUntil(SarosConditions.isResourceExist(path));
    }

    public void waitUntilClassNotExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        remoteBot().waitUntil(SarosConditions.isResourceNotExist(path));
    }

    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException {
        remoteBot().waitUntilShellIsClosed(SHELL_SAROS_RUNNING_VCS_OPERATION);
    }

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException {
        remoteBot().waitUntil(SarosConditions.isInSVN(projectName));
    }

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException {
        remoteBot().waitUntil(SarosConditions.isNotInSVN(projectName));
    }

    public void waitUntilRevisionIsSame(String fullPath, String revision)
        throws RemoteException {
        remoteBot().waitUntil(SarosConditions.isRevisionSame(fullPath, revision));
    }

    public void waitUntilUrlIsSame(String fullPath, String url)
        throws RemoteException {
        remoteBot().waitUntil(SarosConditions.isUrlSame(fullPath, url));
    }

    public void waitUntilFileContentSame(final String otherClassContent,
        final String... fileNodes) throws RemoteException {

        remoteBot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return getFileContent(fileNodes).equals(otherClassContent);
            }

            public String getFailureMessage() {
                return "The both contents are not" + " same.";
            }
        });
    }

    /**********************************************
     * 
     * innner function
     * 
     **********************************************/

    private void initContextMenuWrapper(IRemoteBotTreeItem treeItem,
        TreeItemType type) {
        contextMenu.setTree(tree);
        contextMenu.setTreeItem(treeItem);
        contextMenu.setTreeItemType(type);
    }

}
