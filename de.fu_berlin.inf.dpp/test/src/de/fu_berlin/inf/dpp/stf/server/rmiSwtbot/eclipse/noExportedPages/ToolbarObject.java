package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.saros.finder.SarosSWTBot;

public class ToolbarObject {
    private static final transient Logger log = Logger
        .getLogger(ToolbarObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private SarosSWTBot bot;

    public ToolbarObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.bot = RmiSWTWorkbenchBot.delegate;
    }
}
