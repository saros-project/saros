package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.contextMenu;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.program.Program;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.ContextMenuHelper;

public class OpenCImp extends EclipseComponentImp implements OpenC {

    private static transient OpenCImp self;

    /**
     * {@link OpenCImp} is a singleton, but inheritance is possible.
     */
    public static OpenCImp getInstance() {
        if (self != null)
            return self;
        self = new OpenCImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Open"
     * 
     **********************************************/

    public void openFile(String viewTitle, String... fileNodes)
        throws RemoteException {
        precondition(viewTitle);
        treeW.clickContextMenuOfTreeItemInView(viewTitle, CM_OPEN, fileNodes);
    }

    public void openClass(String viewTitle, String projectName, String pkg,
        String className) throws RemoteException {
        String[] classNodes = getClassNodes(projectName, pkg, className);
        openFile(viewTitle, changeToRegex(classNodes));
    }

    public void openClassWith(String viewTitle, String whichEditor,
        String projectName, String pkg, String className)
        throws RemoteException {
        openFileWith(viewTitle, whichEditor,
            getClassNodes(projectName, pkg, className));
    }

    public void openFileWith(String viewTitle, String whichEditor,
        String... fileNodes) throws RemoteException {
        precondition(viewTitle);
        SWTBotTree tree = treeW.getTreeInView(VIEW_PACKAGE_EXPLORER);
        tree.expandNode(fileNodes).select();
        ContextMenuHelper.clickContextMenu(tree, CM_OPEN_WITH, CM_OTHER);
        shellW.waitUntilShellActive(SHELL_EDITOR_SELECTION);
        SWTBotTable table = bot.table();
        table.select(whichEditor);
        buttonW.waitUntilButtonEnabled(OK);
        shellW.confirmShell(SHELL_EDITOR_SELECTION, OK);
    }

    public void openClassWithSystemEditor(String projectName, String pkg,
        String className) throws RemoteException {
        IPath path = new Path(getClassPath(projectName, pkg, className));
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        Program.launch(resource.getLocation().toString());
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    protected void precondition(String viewTitle) throws RemoteException {

        viewW.openViewById(viewTitlesAndIDs.get(viewTitle));

        viewW.setFocusOnViewByTitle(viewTitle);
    }
}
