package de.fu_berlin.inf.dpp.stf.swtbot.helpers;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.stf.RMISwtbot.eclipse.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.saros.finder.SarosSWTBot;

public class ToolbarObject {
    private static final transient Logger log = Logger
        .getLogger(ToolbarObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private static SarosSWTBot bot = new SarosSWTBot();

    public ToolbarObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
    }

}
