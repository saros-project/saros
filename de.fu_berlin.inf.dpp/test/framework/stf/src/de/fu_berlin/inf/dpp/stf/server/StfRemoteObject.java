package de.fu_berlin.inf.dpp.stf.server;

import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.stf.shared.Constants;
import de.fu_berlin.inf.dpp.util.VersionManager;

public abstract class StfRemoteObject implements Constants {

    private static MutablePicoContainer container;

    static void setPicoContainer(MutablePicoContainer container) {
        StfRemoteObject.container = container;
    }

    protected Saros getSaros() {
        return container.getComponent(Saros.class);
    }

    protected SarosNet getSarosNet() {
        return container.getComponent(SarosNet.class);
    }

    protected ISarosSessionManager getSessionManager() {
        return container.getComponent(ISarosSessionManager.class);
    }

    protected DataTransferManager getDataTransferManager() {
        return container.getComponent(DataTransferManager.class);
    }

    protected EditorManager getEditorManager() {
        return container.getComponent(EditorManager.class);
    }

    protected XMPPAccountStore getXmppAccountStore() {
        return container.getComponent(XMPPAccountStore.class);
    }

    protected VersionManager getVersionManager() {
        return container.getComponent(VersionManager.class);
    }

    protected EditorAPI getEditorAPI() {
        return container.getComponent(EditorAPI.class);
    }
}