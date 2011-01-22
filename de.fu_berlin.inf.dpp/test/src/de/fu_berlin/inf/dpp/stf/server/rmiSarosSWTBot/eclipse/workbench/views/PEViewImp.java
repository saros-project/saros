package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.program.Program;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.TableImp;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.ContextMenuHelper;

public class PEViewImp extends EclipsePart implements PEView {

    private static transient PEViewImp pEViewImp;

    /**
     * {@link TableImp} is a singleton, but inheritance is possible.
     */
    public static PEViewImp getInstance() {
        if (pEViewImp != null)
            return pEViewImp;
        pEViewImp = new PEViewImp();
        return pEViewImp;
    }

    /* View infos */
    protected final static String VIEWNAME = "Package Explorer";
    protected final static String VIEWID = "org.eclipse.jdt.ui.PackageExplorer";

    /*
     * title of shells which are pop up by performing the actions on the package
     * explorer view.
     */

    private final static String SHELL_EDITOR_SELECTION = "Editor Selection";

    /* Context menu of a selected file on the package explorer view */
    private final static String OPEN = "Open";
    private final static String OPEN_WITH = "Open With";
    private final static String OTHER = "Other...";

    /* All the sub menus of the context menu "Open with" */
    // private final static String TEXT_EDITOR = "Text Editor";
    // private final static String SYSTEM_EDITOR = "System Editor";
    // private final static String DEFAULT_EDITOR = "Default Editor";

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
            viewW.openViewById(VIEWID);
    }

    public boolean isPEViewOpen() throws RemoteException {
        return viewW.isViewOpen(VIEWNAME);
    }

    public void closePEView() throws RemoteException {
        viewW.closeViewByTitle(VIEWNAME);
    }

    public void setFocusOnPEView() throws RemoteException {
        viewW.setFocusOnViewByTitle(VIEWNAME);
    }

    public boolean isPEViewActive() throws RemoteException {
        return viewW.isViewActive(VIEWNAME);
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Open"
     * 
     **********************************************/

    public void openFile(String... fileNodes) throws RemoteException {
        precondition();
        treeW.clickContextMenuOfTreeItemInView(VIEWNAME, OPEN, fileNodes);
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
        SWTBotTree tree = treeW.getTreeInView(VIEWNAME);
        tree.expandNode(fileNodes).select();
        ContextMenuHelper.clickContextMenu(tree, OPEN_WITH, OTHER);
        shellC.waitUntilShellActive(SHELL_EDITOR_SELECTION);
        SWTBotTable table = bot.table();
        table.select(whichEditor);
        buttonW.waitUntilButtonEnabled(OK);
        shellC.confirmShell(SHELL_EDITOR_SELECTION, OK);
    }

    public void openClassWithSystemEditor(String projectName, String pkg,
        String className) throws RemoteException {
        IPath path = new Path(getClassPath(projectName, pkg, className));
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        Program.launch(resource.getLocation().toString());
    }

    public void selectProject(String projectName) throws RemoteException {
        treeW.selectTreeItemInView(VIEWNAME, projectName);
    }

    public void selectPkg(String projectName, String pkg)
        throws RemoteException {
        String[] nodes = { projectName, SRC, pkg };
        treeW.selectTreeItemInView(VIEWNAME, nodes);
    }

    public void selectClass(String projectName, String pkg, String className)
        throws RemoteException {
        precondition();
        String[] nodes = getClassNodes(projectName, pkg, className);
        treeW.selectTreeItemInView(VIEWNAME, nodes);
    }

    public void selectFolder(String... pathToFolder) throws RemoteException {
        treeW.selectTreeItemInView(VIEWNAME, pathToFolder);
    }

    public void selectFile(String... pathToFile) throws RemoteException {
        treeW.selectTreeItemInView(VIEWNAME, pathToFile);
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

}
