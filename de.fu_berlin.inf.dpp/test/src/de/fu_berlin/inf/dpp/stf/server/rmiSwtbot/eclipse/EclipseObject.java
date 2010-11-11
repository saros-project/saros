package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.BasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.EditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.HelperObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.MenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.PerspectiveObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.TableObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.TreeObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.ViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.WindowObject;

public class EclipseObject {
    protected static final transient Logger log = Logger
        .getLogger(EclipseObject.class);

    protected final String PEViewName = SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER;
    protected final String PGViewName = SarosConstant.VIEW_TITLE_PROGRESS;

    protected RmiSWTWorkbenchBot rmiBot;

    // No exported objects
    protected TableObject tableObject;
    protected MenuObject menuObject;
    protected TreeObject treeObject;
    protected WindowObject windowObject;
    protected BasicObject basicObject;
    protected ViewObject viewObject;
    protected HelperObject mainObject;
    protected PerspectiveObject persObject;
    protected EditorObject editorObject;
    protected SarosSWTBot bot;

    /**
     * Creates an instance of a NoExportedObject.<br/>
     * 
     * 
     * @param rmiBot
     *            delegates to {@link SWTWorkbenchBot} to implement an java.rmi
     *            interface for {@link SWTWorkbenchBot}. Using the
     *            {@link EclipseObject} can access other objects.
     */
    public EclipseObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.bot = RmiSWTWorkbenchBot.delegate;
        this.tableObject = rmiBot.tableObject;
        this.menuObject = rmiBot.menuObject;
        this.treeObject = rmiBot.treeObject;
        this.windowObject = rmiBot.windowObject;
        this.basicObject = rmiBot.basicObject;
        this.viewObject = rmiBot.viewObject;
        this.mainObject = rmiBot.mainObject;
        this.persObject = rmiBot.persObject;
        this.editorObject = rmiBot.editorObject;
    }

    protected void waitUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }
}
