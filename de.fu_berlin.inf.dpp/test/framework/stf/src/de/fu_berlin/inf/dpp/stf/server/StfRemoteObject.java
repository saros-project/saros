package de.fu_berlin.inf.dpp.stf.server;

import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.Preferences;

import de.fu_berlin.inf.dpp.ISarosContext;
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

    private static ISarosContext context;

    static void setContext(ISarosContext context) {
        StfRemoteObject.context = context;
    }

    protected Saros getSaros() {
        return context.getComponent(Saros.class);
    }

    protected SarosNet getSarosNet() {
        return context.getComponent(SarosNet.class);
    }

    protected ISarosSessionManager getSessionManager() {
        return context.getComponent(ISarosSessionManager.class);
    }

    protected DataTransferManager getDataTransferManager() {
        return context.getComponent(DataTransferManager.class);
    }

    protected EditorManager getEditorManager() {
        return context.getComponent(EditorManager.class);
    }

    protected XMPPAccountStore getXmppAccountStore() {
        return context.getComponent(XMPPAccountStore.class);
    }

    protected VersionManager getVersionManager() {
        return context.getComponent(VersionManager.class);
    }

    protected EditorAPI getEditorAPI() {
        return context.getComponent(EditorAPI.class);
    }

    protected Preferences getGlobalPreferences() {
        return context.getComponent(Preferences.class);
    }

    protected IPreferenceStore getLocalPreferences() {
        return context.getComponent(IPreferenceStore.class);
    }
}