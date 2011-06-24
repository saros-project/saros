package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.condition.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.IContextMenusInPEView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.impl.ContextMenusInPEView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IPEView;
import de.fu_berlin.inf.dpp.stf.server.util.Util;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

public final class PEView extends StfRemoteObject implements IPEView {

    private static final Logger log = Logger.getLogger(PEView.class);

    private static final PEView INSTANCE = new PEView();

    private IRemoteBotView view;
    private IRemoteBotTree tree;

    public static PEView getInstance() {
        return INSTANCE;
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
        ContextMenusInPEView.getInstance().setTree(tree);
        ContextMenusInPEView.getInstance().setTreeItem(null);
        ContextMenusInPEView.getInstance().setTreeItemType(null);
        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectSrc(String projectName)
        throws RemoteException {

        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(Util.changeToRegex(projectName), SRC),
            TreeItemType.JAVA_PROJECT);
        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectJavaProject(String projectName)
        throws RemoteException {

        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(Util.changeToRegex(projectName)),
            TreeItemType.JAVA_PROJECT);
        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectProject(String projectName)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(Util.changeToRegex(projectName)),
            TreeItemType.PROJECT);
        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectPkg(String projectName, String pkg)
        throws RemoteException {
        String[] nodes = { projectName, SRC, pkg };
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(Util.changeToRegex(nodes)),
            TreeItemType.PKG);

        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectClass(String projectName, String pkg,
        String className) throws RemoteException {
        String[] nodes = Util.getClassNodes(projectName, pkg, className);

        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(Util.changeToRegex(nodes)),
            TreeItemType.CLASS);

        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectFolder(String... folderNodes)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(Util.changeToRegex(folderNodes)),
            TreeItemType.FOLDER);

        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectFile(String... fileNodes)
        throws RemoteException {
        initContextMenuWrapper(
            tree.selectTreeItemWithRegex(Util.changeToRegex(fileNodes)),
            TreeItemType.FILE);

        return ContextMenusInPEView.getInstance();
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
            throw new RemoteException("resource '" + fullPath + "' not found.");
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
            throw new RemoteException("resource not found at '" + fullPath
                + "'");
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        final VCSResourceInfo info = vcs.getResourceInfo(resource);
        return info.url;
    }

    public String getFileContent(String... nodes) throws RemoteException,
        IOException, CoreException {
        IPath path = new Path(Util.getPath(nodes));
        log.debug("checking existence of file '" + path + "'");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        log.debug("Checking full path: '" + file.getFullPath().toOSString()
            + "'");
        return Util.convertStreamToString(file.getContents());
    }

    /**********************************************
     * 
     * wait until
     * 
     **********************************************/
    public void waitUntilFolderExists(String... folderNodes)
        throws RemoteException {
        String fullPath = Util.getPath(folderNodes);
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilPkgExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            RemoteWorkbenchBot.getInstance().waitUntil(
                SarosConditions.isResourceExist(Util.getPkgPath(projectName,
                    pkg)));
        } else {
            throw new RuntimeException(
                "the passed parameter '"
                    + pkg
                    + "' is not valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilPkgNotExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            RemoteWorkbenchBot.getInstance().waitUntil(
                SarosConditions.isResourceNotExist(Util.getPkgPath(projectName,
                    pkg)));
        } else {
            throw new RuntimeException(
                "the passed parameter '"
                    + pkg
                    + "' is not valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilFileExists(String... fileNodes) throws RemoteException {
        String fullPath = Util.getPath(fileNodes);
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilClassExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = Util.getClassPath(projectName, pkg, className);
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isResourceExist(path));
    }

    public void waitUntilClassNotExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = Util.getClassPath(projectName, pkg, className);
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isResourceNotExist(path));
    }

    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(
            SHELL_SAROS_RUNNING_VCS_OPERATION);
    }

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isInSVN(projectName));
    }

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isNotInSVN(projectName));
    }

    public void waitUntilRevisionIsSame(String fullPath, String revision)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isRevisionSame(fullPath, revision));
    }

    public void waitUntilUrlIsSame(String fullPath, String url)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isUrlSame(fullPath, url));
    }

    public void waitUntilFileContentSame(final String otherClassContent,
        final String... fileNodes) throws RemoteException {

        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return getFileContent(fileNodes).equals(otherClassContent);
            }

            public String getFailureMessage() {
                return "the content of the file " + Arrays.toString(fileNodes)
                    + " does not match: " + otherClassContent;
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
        ContextMenusInPEView.getInstance().setTree(tree);
        ContextMenusInPEView.getInstance().setTreeItem(treeItem);
        ContextMenusInPEView.getInstance().setTreeItemType(type);
    }

    public boolean isResourceShared(String path) throws RemoteException {
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(new Path(path));
        return getSessionManager().getSarosSession().isShared(resource);
    }

    public void waitUntilResourceIsShared(final String path)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitLongUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isResourceShared(path);
            }

            public String getFailureMessage() {
                return "the resource " + path
                    + " is not shared in the current session";
            }
        });
    }
}
