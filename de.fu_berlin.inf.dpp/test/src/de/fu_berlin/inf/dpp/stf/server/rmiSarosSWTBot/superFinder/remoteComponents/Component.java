package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents;

import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBotImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.noFinder.NoBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.noFinder.NoBotImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.SuperBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.SuperBotImp;
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

    protected SuperBot sarosBot() {
        return SuperBotImp.getInstance();
    }

    protected NoBot noBot() {
        return NoBotImp.getInstance();
    }
}
