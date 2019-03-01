package saros.stf.server.rmi.superbot.component.view.eclipse.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import saros.filesystem.ResourceAdapterFactory;
import saros.session.ISarosSession;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.bot.condition.SarosConditions;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.IContextMenusInPEView;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.impl.ContextMenusInPEView;
import saros.stf.server.rmi.superbot.component.view.eclipse.IPackageExplorerView;
import saros.stf.server.util.WidgetUtil;

public final class PackageExplorerView extends StfRemoteObject implements IPackageExplorerView {

  private static final Logger log = Logger.getLogger(PackageExplorerView.class);

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

  /*
   * begin of interface IPackageExplorerView implementation
   */

  @Override
  public IContextMenusInPEView tree() throws RemoteException {
    ContextMenusInPEView.getInstance().setTree(tree);
    ContextMenusInPEView.getInstance().setTreeItem(null);
    ContextMenusInPEView.getInstance().setTreeItemType(null);
    return ContextMenusInPEView.getInstance();
  }

  @Override
  public IContextMenusInPEView selectSrc(String projectName) throws RemoteException {

    initContextMenuWrapper(
        WidgetUtil.getTreeItemWithRegex(tree, createProjectRegex(projectName), Pattern.quote(SRC)),
        TreeItemType.JAVA_PROJECT);
    return ContextMenusInPEView.getInstance();
  }

  @Override
  public IContextMenusInPEView selectJavaProject(String projectName) throws RemoteException {

    initContextMenuWrapper(
        WidgetUtil.getTreeItemWithRegex(tree, createProjectRegex(projectName)),
        TreeItemType.JAVA_PROJECT);
    return ContextMenusInPEView.getInstance();
  }

  @Override
  public IContextMenusInPEView selectJavaProjectWithRegex(String projectName)
      throws RemoteException {

    initContextMenuWrapper(
        WidgetUtil.getTreeItemWithRegex(tree, projectName), TreeItemType.JAVA_PROJECT);
    return ContextMenusInPEView.getInstance();
  }

  @Override
  public IContextMenusInPEView selectProject(String projectName) throws RemoteException {
    initContextMenuWrapper(
        WidgetUtil.getTreeItemWithRegex(tree, createProjectRegex(projectName)),
        TreeItemType.PROJECT);
    return ContextMenusInPEView.getInstance();
  }

  @Override
  public IContextMenusInPEView selectProjectWithRegex(String projectName) throws RemoteException {
    initContextMenuWrapper(
        WidgetUtil.getTreeItemWithRegex(tree, projectName), TreeItemType.PROJECT);
    return ContextMenusInPEView.getInstance();
  }

  @Override
  public IContextMenusInPEView selectPkg(String projectName, String pkg) throws RemoteException {
    initContextMenuWrapper(
        WidgetUtil.getTreeItemWithRegex(
            tree, createProjectRegex(projectName), Pattern.quote(SRC), Pattern.quote(pkg)),
        TreeItemType.PKG);

    return ContextMenusInPEView.getInstance();
  }

  @Override
  public IContextMenusInPEView selectPkgWithRegex(String projectName, String pkg)
      throws RemoteException {
    initContextMenuWrapper(
        WidgetUtil.getTreeItemWithRegex(tree, projectName, Pattern.quote(SRC), pkg),
        TreeItemType.PKG);

    return ContextMenusInPEView.getInstance();
  }

  @Override
  public IContextMenusInPEView selectClass(String projectName, String pkg, String className)
      throws RemoteException {

    initContextMenuWrapper(
        WidgetUtil.getTreeItemWithRegex(
            tree,
            createProjectRegex(projectName),
            Pattern.quote(SRC),
            Pattern.quote(pkg),
            Pattern.quote(className + SUFFIX_JAVA)),
        TreeItemType.CLASS);

    return ContextMenusInPEView.getInstance();
  }

  @Override
  public IContextMenusInPEView selectClassWithRegex(
      String projectName, String pkg, String className) throws RemoteException {

    initContextMenuWrapper(
        WidgetUtil.getTreeItemWithRegex(
            tree, projectName, Pattern.quote(SRC), pkg, className + Pattern.quote(SUFFIX_JAVA)),
        TreeItemType.CLASS);

    return ContextMenusInPEView.getInstance();
  }

  @Override
  public IContextMenusInPEView selectFolder(String projectName, String... folderNodes)
      throws RemoteException {

    initContextMenuWrapper(
        WidgetUtil.getTreeItemWithRegex(tree, createNodeRegex(projectName, folderNodes)),
        TreeItemType.FOLDER);

    return ContextMenusInPEView.getInstance();
  }

  @Override
  public IContextMenusInPEView selectFile(String projectName, String... fileNodes)
      throws RemoteException {

    initContextMenuWrapper(
        WidgetUtil.getTreeItemWithRegex(tree, createNodeRegex(projectName, fileNodes)),
        TreeItemType.FILE);

    return ContextMenusInPEView.getInstance();
  }

  @Override
  public String getTitle() throws RemoteException {
    return VIEW_PACKAGE_EXPLORER;
  }

  @Override
  public String getFileContent(String... nodes) throws RemoteException, IOException, CoreException {
    IPath path = new Path(getPath(nodes));
    log.debug("checking existence of file '" + path + "'");
    final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

    log.debug("Checking full path: '" + file.getFullPath().toOSString() + "'");
    return convertStreamToString(file.getContents());
  }

  @Override
  public void waitUntilFolderExists(String... folderNodes) throws RemoteException {
    String fullPath = getPath(folderNodes);
    RemoteWorkbenchBot.getInstance().waitUntil(SarosConditions.isResourceExist(fullPath));
  }

  @Override
  public void waitUntilFolderNotExists(String... folderNodes) throws RemoteException {
    String fullPath = getPath(folderNodes);
    RemoteWorkbenchBot.getInstance().waitUntil(SarosConditions.isResourceNotExist(fullPath));
  }

  @Override
  public void waitUntilPkgExists(String projectName, String pkg) throws RemoteException {
    if (pkg.matches(PKG_REGEX)) {
      RemoteWorkbenchBot.getInstance()
          .waitUntil(SarosConditions.isResourceExist(getPkgPath(projectName, pkg)));
    } else {
      throw new RuntimeException(
          "the passed parameter '"
              + pkg
              + "' is not valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
    }
  }

  @Override
  public void waitUntilPkgNotExists(String projectName, String pkg) throws RemoteException {
    if (pkg.matches(PKG_REGEX)) {
      RemoteWorkbenchBot.getInstance()
          .waitUntil(SarosConditions.isResourceNotExist(getPkgPath(projectName, pkg)));
    } else {
      throw new RuntimeException(
          "the passed parameter '"
              + pkg
              + "' is not valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
    }
  }

  @Override
  public void waitUntilFileExists(String... fileNodes) throws RemoteException {
    String fullPath = getPath(fileNodes);
    RemoteWorkbenchBot.getInstance().waitUntil(SarosConditions.isResourceExist(fullPath));
  }

  @Override
  public void waitUntilFileNotExists(String... fileNodes) throws RemoteException {
    String fullPath = getPath(fileNodes);
    RemoteWorkbenchBot.getInstance().waitUntil(SarosConditions.isResourceNotExist(fullPath));
  }

  @Override
  public void waitUntilClassExists(String projectName, String pkg, String className)
      throws RemoteException {
    String path = getClassPath(projectName, pkg, className);
    RemoteWorkbenchBot.getInstance().waitUntil(SarosConditions.isResourceExist(path));
  }

  @Override
  public void waitUntilClassNotExists(String projectName, String pkg, String className)
      throws RemoteException {
    String path = getClassPath(projectName, pkg, className);
    RemoteWorkbenchBot.getInstance().waitUntil(SarosConditions.isResourceNotExist(path));
  }

  @Override
  public boolean isResourceShared(String path) throws RemoteException {
    IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));

    ISarosSession session = getSessionManager().getSession();

    if (session == null)
      throw new IllegalStateException(
          "cannot query shared resource status without a running session");

    return session.isShared(ResourceAdapterFactory.create(resource));
  }

  @Override
  public void waitUntilFileContentSame(final String otherClassContent, final String... fileNodes)
      throws RemoteException {

    RemoteWorkbenchBot.getInstance()
        .waitUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return getFileContent(fileNodes).equals(otherClassContent);
              }

              @Override
              public String getFailureMessage() {
                return "the content of the file "
                    + Arrays.toString(fileNodes)
                    + " does not match: "
                    + otherClassContent;
              }
            });
  }

  @Override
  public void waitUntilResourceIsShared(final String path) throws RemoteException {
    RemoteWorkbenchBot.getInstance()
        .waitLongUntil(
            new DefaultCondition() {
              @Override
              public boolean test() throws Exception {
                return isResourceShared(path);
              }

              @Override
              public String getFailureMessage() {
                return "the resource " + path + " is not shared in the current session";
              }
            });
  }

  /*
   * end of interface IPackageExplorerView implementation
   */

  private void initContextMenuWrapper(SWTBotTreeItem treeItem, TreeItemType type) {
    ContextMenusInPEView.getInstance().setTree(tree);
    ContextMenusInPEView.getInstance().setTreeItem(treeItem);
    ContextMenusInPEView.getInstance().setTreeItemType(type);
  }

  private String getClassPath(String projectName, String pkg, String className) {
    return projectName + "/src/" + pkg.replace('.', '/') + "/" + className + ".java";
  }

  private String getPkgPath(String projectName, String pkg) {
    return projectName + "/src/" + pkg.replace('.', '/');
  }

  private String getPath(String... nodes) {
    StringBuilder builder = new StringBuilder();
    int i = 0;

    for (; i < nodes.length - 1; i++) builder.append(nodes[i]).append('/');

    builder.append(nodes[i]);

    return builder.toString();
  }

  private String createProjectRegex(String projectName) {
    return Pattern.quote(projectName)
        + "("
        + Pattern.quote(PROJECT_SHARED_DECORATOR)
        + "|"
        + Pattern.quote(PROJECT_PARTIAL_SHARED_DECORATOR)
        + ")?+";
  }

  private String[] createNodeRegex(String projectName, String... nodes) {
    List<String> regex = new ArrayList<String>(nodes.length + 1);

    regex.add(createProjectRegex(projectName));

    for (String node : nodes) regex.add(Pattern.quote(node));

    return regex.toArray(new String[0]);
  }

  private String convertStreamToString(InputStream is) throws IOException {
    Writer writer = new StringWriter();
    char[] buffer = new char[8192];

    try {
      Reader reader = new InputStreamReader(is, "UTF-8");
      int n;

      while ((n = reader.read(buffer)) != -1) writer.write(buffer, 0, n);

    } finally {
      is.close();
      writer.close();
    }

    return writer.toString();
  }
}
