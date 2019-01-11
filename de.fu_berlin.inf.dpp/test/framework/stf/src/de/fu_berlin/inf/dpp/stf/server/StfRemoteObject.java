package de.fu_berlin.inf.dpp.stf.server;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.context.IContainerContext;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.FollowModeManager;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.stf.shared.Constants;
import de.fu_berlin.inf.dpp.versioning.VersionManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.Preferences;

public abstract class StfRemoteObject implements Constants {

  private static IContainerContext context;

  static void setContext(IContainerContext context) {
    StfRemoteObject.context = context;
  }

  protected Saros getSaros() {
    return context.getComponent(Saros.class);
  }

  protected XMPPConnectionService getConnectionService() {
    return context.getComponent(XMPPConnectionService.class);
  }

  protected ISarosSessionManager getSessionManager() {
    return context.getComponent(ISarosSessionManager.class);
  }

  protected DataTransferManager getDataTransferManager() {
    return (DataTransferManager) context.getComponent(IConnectionManager.class);
  }

  /** @return is <code>null</code> if there is no running session */
  protected FollowModeManager getFollowModeManager() {
    ISarosSession sarosSession = getSessionManager().getSession();

    if (sarosSession == null) return null;

    return sarosSession.getComponent(FollowModeManager.class);
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

  protected Preferences getGlobalPreferences() {
    return context.getComponent(Preferences.class);
  }

  protected IPreferenceStore getLocalPreferences() {
    return context.getComponent(IPreferenceStore.class);
  }
}
