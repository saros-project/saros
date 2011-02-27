package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.program.Program;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.PEView;

public class OpenCImp extends EclipseComponentImp implements OpenC {

    private static transient OpenCImp self;

    private PEView view;

    /**
     * {@link OpenCImp} is a singleton, but inheritance is possible.
     */
    public static OpenCImp getInstance() {
        if (self != null)
            return self;
        self = new OpenCImp();
        return self;
    }

    public void setView(PEView view) {
        this.view = view;
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

    // public void openClassWith(String whichEditor, String projectName,
    // String pkg, String className) throws RemoteException {
    // assert isValidClassPath(projectName, pkg, className) :
    // "The given classPath is not invalid!";
    // openFileWith(whichEditor, getClassNodes(projectName, pkg, className));
    // }

    // public void openFileWith(String whichEditor, String... fileNodes)
    // throws RemoteException {
    // view.selectFile(fileNodes).contextMenu(CM_OPEN_WITH, CM_OTHER).click();
    // bot().waitUntilShellIsOpen(SHELL_EDITOR_SELECTION);
    // STFBotShell shell_bob = bot().shell(SHELL_EDITOR_SELECTION);
    // shell_bob.activate();
    // shell_bob.bot().table().getTableItem(whichEditor).select();
    // shell_bob.bot().button(OK).waitUntilIsEnabled();
    // shell_bob.confirm(OK);
    // }

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

}
