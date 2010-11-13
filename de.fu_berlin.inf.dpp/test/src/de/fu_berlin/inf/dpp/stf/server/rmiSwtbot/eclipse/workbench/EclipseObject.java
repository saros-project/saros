package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseControler;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.BasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.EditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.HelperObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.MenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.PerspectiveObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.TableObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.TreeObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.WindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosControler;

public class EclipseObject {
    protected static final transient Logger log = Logger
        .getLogger(EclipseObject.class);

    protected int sleepTime = 750;

    protected final String PEViewName = SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER;
    protected final String PGViewName = SarosConstant.VIEW_TITLE_PROGRESS;

    protected SarosControler rmiBot;

    // No exported objects
    protected TableObject tableObject;
    protected MenuObject menuObject;
    protected TreeObject treeObject;
    protected WindowObject windowObject;
    protected BasicObject basicObject;
    protected ViewObject viewObject;
    protected HelperObject helperObject;
    protected PerspectiveObject persObject;
    protected EditorObject editorObject;
    protected SarosSWTBot bot;

    public EclipseObject() {

    }

    /**
     * Creates an instance of a NoExportedObject.<br/>
     * 
     * 
     * @param rmiBot
     *            delegates to {@link SWTWorkbenchBot} to implement an java.rmi
     *            interface for {@link SWTWorkbenchBot}. Using the
     *            {@link EclipseObject} can access other objects.
     */
    public EclipseObject(SarosControler rmiBot) {
        this.rmiBot = rmiBot;
        this.sleepTime = rmiBot.sleepTime;
        this.bot = EclipseControler.sarosSWTBot;
        this.tableObject = rmiBot.table;
        this.menuObject = rmiBot.menu;
        this.treeObject = rmiBot.tree;
        this.windowObject = rmiBot.window;
        this.basicObject = rmiBot.basic;
        this.viewObject = rmiBot.view;
        this.helperObject = rmiBot.helper;
        this.persObject = rmiBot.perspective;
        this.editorObject = rmiBot.editor;
    }

    protected void waitUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }
}
