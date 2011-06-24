package de.fu_berlin.inf.dpp.stf.server;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.shared.Constants;

public abstract class StfRemoteObject implements Constants {

    private static Saros saros;
    private static SarosSessionManager sessionManager;
    private static DataTransferManager dataTransferManager;
    private static EditorManager editorManager;
    private static XMPPAccountStore xmppAccountStore;
    private static FeedbackManager feedbackManager;

    static void setSaros(Saros saros) {
        StfRemoteObject.saros = saros;
    }

    static void setSessionManager(SarosSessionManager sessionManager) {
        StfRemoteObject.sessionManager = sessionManager;
    }

    static void setDataTransferManager(DataTransferManager dataTransferManager) {
        StfRemoteObject.dataTransferManager = dataTransferManager;
    }

    static void setEditorManager(EditorManager editorManager) {
        StfRemoteObject.editorManager = editorManager;
    }

    static void setXmppAccountStore(XMPPAccountStore xmppAccountStore) {
        StfRemoteObject.xmppAccountStore = xmppAccountStore;
    }

    static void setFeedbackManager(FeedbackManager feedbackManager) {
        StfRemoteObject.feedbackManager = feedbackManager;
    }

    protected Saros getSaros() {
        return saros;
    }

    protected SarosSessionManager getSessionManager() {
        return sessionManager;
    }

    protected DataTransferManager getDataTransferManager() {
        return dataTransferManager;
    }

    protected EditorManager getEditorManager() {
        return editorManager;
    }

    protected XMPPAccountStore getXmppAccountStore() {
        return xmppAccountStore;
    }

    protected FeedbackManager getFeedbackManager() {
        return feedbackManager;
    }
}