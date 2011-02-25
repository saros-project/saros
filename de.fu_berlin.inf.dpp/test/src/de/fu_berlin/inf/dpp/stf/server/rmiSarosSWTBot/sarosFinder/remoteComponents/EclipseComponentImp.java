package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBotImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.SarosBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.SarosBotImp;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.SarosSWTBot;

public class EclipseComponentImp extends STF implements EclipseComponent {

    // Picocontainer initiated by STFController.

    public static SarosSessionManager sessionManager;
    public static DataTransferManager dataTransferManager;
    public static EditorManager editorManager;
    public static XMPPAccountStore xmppAccountStore;
    public static FeedbackManager feedbackManager;

    // SWTBot framework
    public static SarosSWTBot bot = SarosSWTBot.getInstance();
    public static int sleepTime = STFController.sleepTime;

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilFolderExists(String... folderNodes)
        throws RemoteException {
        String fullPath = getPath(folderNodes);
        bot().waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilPkgExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            bot().waitUntil(
                SarosConditions.isResourceExist(getPkgPath(projectName, pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilPkgNotExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            bot().waitUntil(
                SarosConditions
                    .isResourceNotExist(getPkgPath(projectName, pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilFileExists(String... fileNodes) throws RemoteException {
        String fullPath = getPath(fileNodes);
        bot().waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilClassExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        bot().waitUntil(SarosConditions.isResourceExist(path));
    }

    public void waitUntilClassNotExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        bot().waitUntil(SarosConditions.isResourceNotExist(path));
    }

    /**********************************************
     * 
     * state
     * 
     **********************************************/
    public boolean existsFile(String viewTitle, String... fileNodes)
        throws RemoteException {

        return bot().view(viewTitle).bot().tree()
            .selectTreeItem(getParentNodes(fileNodes))
            .existsSubItemWithRegex(getLastNode(fileNodes + ".*"));
    }

    /**********************************************
     * 
     * No GUI
     * 
     **********************************************/
    public boolean existsProjectNoGUI(String projectName)
        throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        return project.exists();
    }

    public boolean existsFolderNoGUI(String... folderNodes)
        throws RemoteException {
        IPath path = new Path(getPath(folderNodes));
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            return false;
        return true;
    }

    public boolean existsPkgNoGUI(String projectName, String pkg)
        throws RemoteException {
        IPath path = new Path(getPkgPath(projectName, pkg));
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource != null)
            return true;
        return false;
    }

    public boolean existsFileNoGUI(String filePath) throws RemoteException {
        IPath path = new Path(filePath);
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);
        return file.exists();
    }

    public boolean existsFileNoGUI(String... nodes) throws RemoteException {
        return existsFileNoGUI(getPath(nodes));
    }

    public boolean existsClassNoGUI(String projectName, String pkg,
        String className) throws RemoteException {
        return existsFileNoGUI(getClassPath(projectName, pkg, className));
    }

    /**********************************************
     * 
     * Inner functions
     * 
     **********************************************/

    public String getFileContentNoGUI(String filePath) {
        Bundle bundle = saros.getBundle();
        String content;
        try {
            content = FileUtils.read(bundle.getEntry(filePath));
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not open " + filePath);
        }
        return content;
    }

    protected STFWorkbenchBot bot() {
        STFWorkbenchBotImp stfBot = STFWorkbenchBotImp.getInstance();
        return stfBot;
    }

    protected SarosBot sarosBot() {
        return SarosBotImp.getInstance();
    }
}
