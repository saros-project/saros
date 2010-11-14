package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.BasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.EditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.HelperObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.MenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.PerspectiveObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.TableObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ToolbarObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.TreeObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.WindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ExStateObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExChatViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExMainMenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExRemoteScreenViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExRosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExSessionViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExWorkbenchObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExBasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExEditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExPackageExplorerViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExProgressViewObject;

public abstract class EclipseObject {
    protected static final transient Logger log = Logger
        .getLogger(EclipseObject.class);

    // Title of Buttons
    protected final static String YES = SarosConstant.BUTTON_YES;
    protected final static String OK = SarosConstant.BUTTON_OK;
    protected final static String NO = SarosConstant.BUTTON_NO;
    protected final static String CANCEL = SarosConstant.BUTTON_CANCEL;
    protected final static String FINISH = SarosConstant.BUTTON_FINISH;
    protected final static String NEXT = SarosConstant.BUTTON_NEXT;

    // Title of Shells
    protected final static String PROGRESSINFORMATION = SarosConstant.SHELL_TITLE_PROGRESS_INFORMATION;

    // Role:Driver
    protected final static String ROLENAME = SarosConstant.ROLENAME;

    // exported objects
    public static ExBasicObject exBasicO;
    public static ExMainMenuObject exMainMenuO;
    public static ExEditorObject exEditorO;
    public static ExWindowObject exWindowO;
    public static ExWorkbenchObject exWorkbenchO;
    public static ExStateObject exStateO;
    public static ExRosterViewObject exRosterVO;
    public static ExSessionViewObject exSessonVO;
    public static ExRemoteScreenViewObject exRemoteScreenVO;
    public static ExChatViewObject exChatVO;
    public static ExPackageExplorerViewObject exPackageExplorerVO;
    public static ExProgressViewObject exProgressVO;

    // No exported objects
    public static TableObject tableO;
    public static MenuObject menuO;
    public static TreeObject treeO;
    public static WindowObject windowO;
    public static BasicObject basicO;
    public static ViewObject viewO;
    public static HelperObject helperO;
    public static PerspectiveObject perspectiveO;
    public static EditorObject editorO;
    public static ToolbarObject toolbarO;

    public static SarosSWTBot bot;
    public static int sleepTime = 750;

    protected void waitUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }
}
