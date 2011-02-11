package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.contextMenu;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.program.Program;
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
     * actions
     * 
     **********************************************/

    public void openFile(String viewTitle, String... fileNodes)
        throws RemoteException {
        precondition(viewTitle);
        view(viewTitle).bot().tree()
            .selectTreeItemWithRegexs(changeToRegex(fileNodes))
            .contextMenu(CM_OPEN).click();

    }

    public void openClass(String viewTitle, String projectName, String pkg,
        String className) throws RemoteException {
        assert isValidClassPath(projectName, pkg, className) : "The given classPath is not invalid!";
        String[] classNodes = getClassNodes(projectName, pkg, className);
        openFile(viewTitle, classNodes);
    }

    public void openClassWith(String viewTitle, String whichEditor,
        String projectName, String pkg, String className)
        throws RemoteException {
        assert isValidClassPath(projectName, pkg, className) : "The given classPath is not invalid!";
        openFileWith(viewTitle, whichEditor,
            getClassNodes(projectName, pkg, className));
    }

    public void openFileWith(String viewTitle, String whichEditor,
        String... fileNodes) throws RemoteException {
        precondition(viewTitle);
        SWTBotTree tree = treeW.getTreeInView(viewTitle);
        view(viewTitle).bot().tree().selectTreeItem(fileNodes);
        ContextMenuHelper.clickContextMenu(tree, CM_OPEN_WITH, CM_OTHER);
        shell(SHELL_EDITOR_SELECTION).waitUntilShellActive(
            SHELL_EDITOR_SELECTION);
        tableW.selectTableItem(whichEditor);
        buttonW.waitUntilButtonEnabled(OK);
        shell(SHELL_EDITOR_SELECTION).confirmShell(SHELL_EDITOR_SELECTION, OK);
    }

    /**************************************************************
     * 
     * No GUI
     * 
     **************************************************************/
    public void openClassWithSystemEditorNoGUI(String projectName, String pkg,
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

        view(viewTitle).openById();
        view(viewTitle).setFocus();
    }
}
