package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.BasicPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.EditorPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.HelperPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.MenuPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.PerspectivePart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.TablePart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ToolbarPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.TreePart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ViewPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.WindowPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosMainMenuComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RSViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosWorkbenchComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.BasicComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PEViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewComponent;

public abstract class EclipseComponent {
    protected static final transient Logger log = Logger
        .getLogger(EclipseComponent.class);

    // Title of Buttons
    protected final static String YES = SarosConstant.BUTTON_YES;
    protected final static String OK = SarosConstant.BUTTON_OK;
    protected final static String NO = SarosConstant.BUTTON_NO;
    protected final static String CANCEL = SarosConstant.BUTTON_CANCEL;
    protected final static String FINISH = SarosConstant.BUTTON_FINISH;
    protected final static String NEXT = SarosConstant.BUTTON_NEXT;

    protected String VIEWNAME = SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER;

    // Title of Shells
    protected final static String PROGRESSINFORMATION = SarosConstant.SHELL_TITLE_PROGRESS_INFORMATION;

    // Role:Driver
    protected final static String ROLENAME = SarosConstant.ROLENAME;

    // exported objects
    public static BasicComponent exBasicO;
    public static SarosMainMenuComponent exMainMenuO;
    public static EditorComponent exEditorO;
    // public static ExWindowObject exWindowO;
    public static SarosWorkbenchComponent exWorkbenchO;
    public static SarosState exStateO;
    public static RosterViewComponent exRosterVO;
    public static SessionViewComponent exSessonVO;
    public static RSViewComponent exRemoteScreenVO;
    public static ChatViewComponent exChatVO;
    public static PEViewComponent exPackageExplorerVO;
    public static ProgressViewComponent exProgressVO;

    // No exported objects
    public static TablePart tableO;
    public static MenuPart menuO;
    public static TreePart treeO;
    public static WindowPart windowO;
    public static BasicPart basicO;
    public static ViewPart viewO;
    public static HelperPart helperO;
    public static PerspectivePart perspectiveO;
    public static EditorPart editorO;
    public static ToolbarPart toolbarO;

    public static SarosSWTBot bot;
    public static int sleepTime = 750;

    protected void waitUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }

    abstract protected void precondition() throws RemoteException;
}
