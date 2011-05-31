package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.stf.server.STFMessage;
import de.fu_berlin.inf.dpp.stf.server.bot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

public class Component extends STFMessage {

    public static SarosSessionManager sessionManager;
    public static DataTransferManager dataTransferManager;
    public static EditorManager editorManager;
    public static XMPPAccountStore xmppAccountStore;
    public static FeedbackManager feedbackManager;

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
