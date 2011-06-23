package de.fu_berlin.inf.dpp.stf.server;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.shared.Constants;

public abstract class StfRemoteObject implements Constants {

    protected static Saros saros;
    protected static SarosSessionManager sessionManager;
    protected static DataTransferManager dataTransferManager;
    protected static EditorManager editorManager;
    protected static XMPPAccountStore xmppAccountStore;
    protected static FeedbackManager feedbackManager;
}