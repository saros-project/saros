package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents;

import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBotImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.noFinder.NoBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.noFinder.NoBotImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.SarosBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.SarosBotImp;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.SarosSWTBot;

public class Component extends STF {

    // Picocontainer initiated by STFController.

    // // SWTBot framework
    public static SarosSWTBot bot = SarosSWTBot.getInstance();
    public static int sleepTime = STFController.sleepTime;

    protected STFWorkbenchBot bot() {
        STFWorkbenchBotImp stfBot = STFWorkbenchBotImp.getInstance();
        return stfBot;
    }

    protected SarosBot sarosBot() {
        return SarosBotImp.getInstance();
    }

    protected NoBot noBot() {
        return NoBotImp.getInstance();
    }
}
