package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.ISuperBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.SuperBot;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.SarosSWTBot;

public class Component extends STF {

    public static SarosSessionManager sessionManager;
    public static DataTransferManager dataTransferManager;
    public static EditorManager editorManager;
    public static XMPPAccountStore xmppAccountStore;
    public static FeedbackManager feedbackManager;

    // // SWTBot framework
    public static SarosSWTBot bot = SarosSWTBot.getInstance();
    public static int sleepTime = STFController.sleepTime;

    protected IRemoteWorkbenchBot bot() {
        RemoteWorkbenchBot stfBot = RemoteWorkbenchBot.getInstance();
        return stfBot;
    }

    protected ISuperBot sarosBot() {
        return SuperBot.getInstance();
    }

}
