package saros.stf.server;

import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.Preferences;
import saros.Saros;
import saros.account.XMPPAccountStore;
import saros.context.IContainerContext;
import saros.editor.EditorManager;
import saros.editor.FollowModeManager;
import saros.net.IConnectionManager;
import saros.net.internal.DataTransferManager;
import saros.net.xmpp.XMPPConnectionService;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.stf.shared.Constants;
import saros.versioning.VersionManager;

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
