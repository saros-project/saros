package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RemoteScreenViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosMainMenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosPopUpWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.WorkbenchObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseBasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseEditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PackageExplorerViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewObject;

public abstract class EclipseObject {
    protected static final transient Logger log = Logger
        .getLogger(EclipseObject.class);

    protected int sleepTime = 750;

    protected final String PEViewName = SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER;
    protected final String PGViewName = SarosConstant.VIEW_TITLE_PROGRESS;

    // exported objects
    public static SarosStateObject stateObject;
    public static RosterViewObject rosterVObject;
    public static SessionViewObject sessonVObject;
    public static RemoteScreenViewObject remoteScreenVObject;
    public static ChatViewObject chatVObject;
    public static WorkbenchObject workbenchObject;
    public static SarosPopUpWindowObject exportedWindowObject;
    public static EclipseEditorObject exportedEditorObject;
    public static PackageExplorerViewObject packageExplorerVObject;
    public static SarosMainMenuObject exportedMenuObject;
    public static ProgressViewObject progressVObject;
    public static EclipseBasicObject exportedBasicObject;

    // No exported objects
    public static TableObject tableObject;
    public static MenuObject menuObject;
    public static TreeObject treeObject;
    public static WindowObject windowObject;
    public static BasicObject basicObject;
    public static ViewObject viewObject;
    public static HelperObject helperObject;
    public static PerspectiveObject persObject;
    public static EditorObject editorObject;
    public static ToolbarObject toolbarObject;
    public static SarosSWTBot bot;

    /**
     * Creates an instance of a NoExportedObject.<br/>
     * 
     * 
     * @param rmiBot
     *            delegates to {@link SWTWorkbenchBot} to implement an java.rmi
     *            interface for {@link SWTWorkbenchBot}. Using the
     *            {@link EclipseObject} can access other objects.
     */

    protected void waitUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }
}
