package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.condition.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.IContextMenusInPEView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.impl.ContextMenusInPEView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IPackageExplorerView;
import de.fu_berlin.inf.dpp.stf.server.util.Util;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

public final class PackageExplorerView extends StfRemoteObject implements
    IPackageExplorerView {

    private static final Logger log = Logger
        .getLogger(PackageExplorerView.class);

    private static final PackageExplorerView INSTANCE = new PackageExplorerView();

    private SWTBotView view;
    private SWTBotTree tree;

    public static PackageExplorerView getInstance() {
        return INSTANCE;
    }

    public IPackageExplorerView setView(SWTBotView view) {
        this.view = view;
        tree = this.view.bot().tree();
        return this;
    }

    public IContextMenusInPEView tree() throws RemoteException {
        ContextMenusInPEView.getInstance().setTree(tree);
        ContextMenusInPEView.getInstance().setTreeItem(null);
        ContextMenusInPEView.getInstance().setTreeItemType(null);
        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectSrc(String projectName)
        throws RemoteException {

        initContextMenuWrapper(
            Util.getTreeItemWithRegex(tree, Pattern.quote(projectName),
                Pattern.quote(SRC)), TreeItemType.JAVA_PROJECT);
        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectJavaProject(String projectName)
        throws RemoteException {

        initContextMenuWrapper(
            Util.getTreeItemWithRegex(tree, Pattern.quote(projectName)),
            TreeItemType.JAVA_PROJECT);
        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectJavaProjectWithRegex(String projectName)
        throws RemoteException {

        initContextMenuWrapper(Util.getTreeItemWithRegex(tree, projectName),
            TreeItemType.JAVA_PROJECT);
        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectProject(String projectName)
        throws RemoteException {
        initContextMenuWrapper(
            Util.getTreeItemWithRegex(tree, Pattern.quote(projectName)),
            TreeItemType.PROJECT);
        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectProjectWithRegex(String projectName)
        throws RemoteException {
        initContextMenuWrapper(Util.getTreeItemWithRegex(tree, projectName),
            TreeItemType.PROJECT);
        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectPkg(String projectName, String pkg)
        throws RemoteException {
        initContextMenuWrapper(
            Util.getTreeItemWithRegex(tree, Pattern.quote(projectName),
                Pattern.quote(SRC), Pattern.quote(pkg)), TreeItemType.PKG);

        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectPkgWithRegex(String projectName,
        String pkg) throws RemoteException {
        initContextMenuWrapper(Util.getTreeItemWithRegex(tree, projectName,
            Pattern.quote(SRC), pkg), TreeItemType.PKG);

        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectClass(String projectName, String pkg,
        String className) throws RemoteException {

        initContextMenuWrapper(
            Util.getTreeItemWithRegex(tree, Pattern.quote(projectName),
                Pattern.quote(SRC), Pattern.quote(pkg),
                Pattern.quote(className + SUFFIX_JAVA)), TreeItemType.CLASS);

        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectClassWithRegex(String projectName,
        String pkg, String className) throws RemoteException {

        initContextMenuWrapper(Util.getTreeItemWithRegex(tree, projectName,
            Pattern.quote(SRC), pkg, className + Pattern.quote(SUFFIX_JAVA)),
            TreeItemType.CLASS);

        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectFolder(String... folderNodes)
        throws RemoteException {

        for (int i = 0; i < folderNodes.length; i++)
            folderNodes[i] = Pattern.quote(folderNodes[i]);

        initContextMenuWrapper(Util.getTreeItemWithRegex(tree, folderNodes),
            TreeItemType.FOLDER);

        return ContextMenusInPEView.getInstance();
    }

    public IContextMenusInPEView selectFile(String... fileNodes)
        throws RemoteException {

        for (int i = 0; i < fileNodes.length; i++)
            fileNodes[i] = Pattern.quote(fileNodes[i]);

        initContextMenuWrapper(Util.getTreeItemWithRegex(tree, fileNodes),
            TreeItemType.FILE);

        return ContextMenusInPEView.getInstance();
    }

    public String getTitle() throws RemoteException {
        return VIEW_PACKAGE_EXPLORER;
    }

    public boolean isProjectManagedBySVN(String projectName)
        throws RemoteException {
        try {
            IProject project = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(projectName);
            final VCSAdapter vcs = VCSAdapter.getAdapter(project);
            if (vcs == null)
                return false;
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
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
        IPath path = new Path(getPath(nodes));
        log.debug("checking existence of file '" + path + "'");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        log.debug("Checking full path: '" + file.getFullPath().toOSString()
            + "'");
        return convertStreamToString(file.getContents());
    }

    public void waitUntilFolderExists(String... folderNodes)
        throws RemoteException {
        String fullPath = getPath(folderNodes);
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilFolderNotExists(String... folderNodes)
        throws RemoteException {
        String fullPath = getPath(folderNodes);
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isResourceNotExist(fullPath));
    }

    public void waitUntilPkgExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            RemoteWorkbenchBot.getInstance().waitUntil(
                SarosConditions.isResourceExist(getPkgPath(projectName, pkg)));
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
                SarosConditions
                    .isResourceNotExist(getPkgPath(projectName, pkg)));
        } else {
            throw new RuntimeException(
                "the passed parameter '"
                    + pkg
                    + "' is not valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilFileExists(String... fileNodes) throws RemoteException {
        String fullPath = getPath(fileNodes);
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilFileNotExists(String... fileNodes)
        throws RemoteException {
        String fullPath = getPath(fileNodes);
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isResourceNotExist(fullPath));
    }

    public void waitUntilClassExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isResourceExist(path));
    }

    public void waitUntilClassNotExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.isResourceNotExist(path));
    }

    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(
            SHELL_SAROS_RUNNING_VCS_OPERATION);
    }

    public boolean isResourceShared(String path) throws RemoteException {
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(new Path(path));
        return getSessionManager().getSarosSession().isShared(resource);
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

    private void initContextMenuWrapper(SWTBotTreeItem treeItem,
        TreeItemType type) {
        ContextMenusInPEView.getInstance().setTree(tree);
        ContextMenusInPEView.getInstance().setTreeItem(treeItem);
        ContextMenusInPEView.getInstance().setTreeItemType(type);
    }

    private String getClassPath(String projectName, String pkg, String className) {
        return projectName + "/src/" + pkg.replace('.', '/') + "/" + className
            + ".java";
    }

    private String getPkgPath(String projectName, String pkg) {
        return projectName + "/src/" + pkg.replace('.', '/');
    }

    private String getPath(String... nodes) {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        for (; i < nodes.length - 1; i++)
            builder.append(nodes[i]).append('/');

        builder.append(nodes[i]);

        return builder.toString();
    }

    private String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[8192];
            try {
                Reader reader = new InputStreamReader(is, "UTF-8");
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
                writer.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

}
