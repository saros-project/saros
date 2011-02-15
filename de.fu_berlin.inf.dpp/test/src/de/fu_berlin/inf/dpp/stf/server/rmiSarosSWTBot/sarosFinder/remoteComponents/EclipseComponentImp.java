package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBotImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCComboImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotComboImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotEditorImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotLabelImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotListImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotMenuImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotPerspectiveImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotStyledTextImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTableImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTextImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToolbarButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToolbarDropDownButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToolbarPushButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToolbarRadioButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToolbarToggleButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotViewMenuImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.OpenCImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosCImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.TeamCImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.EditMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.FileMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.RefactorMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.SarosMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.WindowMImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.PEViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ProgressViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RSViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RosterViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionViewImp;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.SarosSWTBot;

public class EclipseComponentImp extends STF implements EclipseComponent {
    protected static final transient Logger log = Logger
        .getLogger(EclipseComponentImp.class);

    // simple widgets

    public static STFBotViewMenuImp stfViewMenu = STFBotViewMenuImp
        .getInstance();
    public static STFBotToolbarDropDownButtonImp stfToolbarDropDownButton = STFBotToolbarDropDownButtonImp
        .getInstance();
    public static STFBotToolbarPushButtonImp stfToolbarPushButton = STFBotToolbarPushButtonImp
        .getInstance();
    public static STFBotToolbarRadioButtonImp stfToolbarRadioButton = STFBotToolbarRadioButtonImp
        .getInstance();
    public static STFBotToolbarToggleButtonImp stfToolbarToggleButton = STFBotToolbarToggleButtonImp
        .getInstance();

    public static STFBotTableImp stfTable = STFBotTableImp.getInstance();
    public static STFBotTreeImp stfTree = STFBotTreeImp.getInstance();
    // public static STFBotButtonImp stfButton = STFBotButtonImp.getInstance();
    public static STFBotToolbarButtonImp stfToolbarButton = STFBotToolbarButtonImp
        .getInstance();
    // public static STFBotViewImp stfView = STFBotViewImp.getInstance();
    public static STFBotPerspectiveImp stfPerspective = STFBotPerspectiveImp
        .getInstance();
    public static STFBotMenuImp stfMenu = STFBotMenuImp.getInstance();
    public static STFBotLabelImp stfLabel = STFBotLabelImp.getInstance();
    public static STFBotStyledTextImp stfStyledText = STFBotStyledTextImp
        .getInstance();
    public static STFBotTextImp stfText = STFBotTextImp.getInstance();
    // public static STFBotShell stfShell = STFBotShellImp.getInstance();
    public static STFBotEditorImp stfEditor = STFBotEditorImp.getInstance();
    public static STFBotListImp stfList = STFBotListImp.getInstance();

    public static STFBotCComboImp stfCCombo = STFBotCComboImp.getInstance();
    public static STFBotComboImp stfCombo = STFBotComboImp.getInstance();
    // workbench
    public static WorkbenchImp workbench = WorkbenchImp.getInstance();

    // Views
    public static RosterViewImp rosterV = RosterViewImp.getInstance();
    public static SessionViewImp sessionV = SessionViewImp.getInstance();
    public static RSViewImp remoteScreenV = RSViewImp.getInstance();
    public static ChatViewImp chatV = ChatViewImp.getInstance();
    public static PEViewImp pEV = PEViewImp.getInstance();
    public static ProgressViewImp progressV = ProgressViewImp.getInstance();

    // menus in menu bar
    public static FileMImp fileM = FileMImp.getInstance();
    public static EditMImp editM = EditMImp.getInstance();
    public static RefactorMImp refactorM = RefactorMImp.getInstance();
    public static SarosMImp sarosM = SarosMImp.getInstance();
    public static WindowMImp windowM = WindowMImp.getInstance();

    // Context menu
    public static TeamCImp team = TeamCImp.getInstance();
    public static SarosCImp sarosC = SarosCImp.getInstance();
    public static OpenCImp openC = OpenCImp.getInstance();

    // Picocontainer initiated by STFController.
    public static Saros saros;
    public static SarosSessionManager sessionManager;
    public static DataTransferManager dataTransferManager;
    public static EditorManager editorManager;
    public static XMPPAccountStore xmppAccountStore;
    public static FeedbackManager feedbackManager;

    // local JID
    public static JID localJID;

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
        waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilPkgExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            waitUntil(SarosConditions.isResourceExist(getPkgPath(projectName,
                pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilPkgNotExists(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches(PKG_REGEX)) {
            waitUntil(SarosConditions.isResourceNotExist(getPkgPath(
                projectName, pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilFileExists(String... fileNodes) throws RemoteException {
        String fullPath = getPath(fileNodes);
        waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void waitUntilClassExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        waitUntil(SarosConditions.isResourceExist(path));
    }

    public void waitUntilClassNotExists(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        waitUntil(SarosConditions.isResourceNotExist(path));
    }

    /**********************************************
     * 
     * state
     * 
     **********************************************/
    public boolean existsFile(String viewTitle, String... fileNodes)
        throws RemoteException {

        return bot().view(viewTitle).bot_().tree()
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
    protected void waitUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }

    protected void waitLongUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
    }

    protected void waitShortUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_SHORT_TIMEOUT);
    }

    protected String getFileContentNoGUI(String filePath) {
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
}
