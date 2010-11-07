package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;

public class NoExportedObject {
    protected static final transient Logger log = Logger
        .getLogger(NoExportedObject.class);

    protected RmiSWTWorkbenchBot rmiBot;
    protected TableObject tableObject;
    protected MenuObject menuObject;
    protected TreeObject treeObject;
    protected SarosSWTBot bot;

    /**
     * Creates an instance of a NoExportedObject.<br/>
     * 
     * 
     * @param rmiBot
     *            delegates to {@link SWTWorkbenchBot} to implement an java.rmi
     *            interface for {@link SWTWorkbenchBot}. Using the
     *            {@link NoExportedObject} can access other objects.
     */
    public NoExportedObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.bot = RmiSWTWorkbenchBot.delegate;
        this.tableObject = rmiBot.tableObject;
        this.menuObject = rmiBot.menuObject;
        this.treeObject = rmiBot.treeObject;
    }

    protected void waitUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }
}
