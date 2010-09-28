package de.fu_berlin.inf.dpp.stf.swtbot.helpers;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

public class ToolbarObject {
    private static final transient Logger log = Logger
        .getLogger(ToolbarObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private static SarosSWTWorkbenchBot bot = new SarosSWTWorkbenchBot();

    public ToolbarObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
    }

}
