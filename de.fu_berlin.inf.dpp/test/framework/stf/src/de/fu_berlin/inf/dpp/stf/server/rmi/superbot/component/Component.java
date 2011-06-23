package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component;

import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

public class Component extends StfRemoteObject {

    // SWTBot framework

    public static int sleepTime = STFController.sleepTime;

    protected SarosSWTBot bot() {
        return SarosSWTBot.getInstance();
    }

    protected IRemoteWorkbenchBot remoteBot() {
        return RemoteWorkbenchBot.getInstance();
    }

    protected ISuperBot superBot() {
        return SuperBot.getInstance();
    }

}
