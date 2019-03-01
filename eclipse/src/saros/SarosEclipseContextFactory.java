package saros;

import java.util.Arrays;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.Preferences;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;
import saros.awareness.AwarenessInformationCollector;
import saros.communication.SkypeManager;
import saros.communication.chat.muc.negotiation.MUCNegotiationManager;
import saros.communication.connection.IProxyResolver;
import saros.communication.connection.Socks5ProxyResolver;
import saros.concurrent.undo.UndoManager;
import saros.context.AbstractContextFactory;
import saros.context.IContextKeyBindings;
import saros.editor.EditorManager;
import saros.editor.IEditorManager;
import saros.filesystem.EclipsePathFactory;
import saros.filesystem.EclipseWorkspaceImpl;
import saros.filesystem.EclipseWorkspaceRootImpl;
import saros.filesystem.FileContentNotifierBridge;
import saros.filesystem.FileSystemChecksumCache;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IPathFactory;
import saros.filesystem.IWorkspace;
import saros.filesystem.IWorkspaceRoot;
import saros.monitoring.remote.EclipseRemoteProgressIndicatorFactoryImpl;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.preferences.EclipsePreferenceStoreAdapter;
import saros.preferences.EclipsePreferences;
import saros.preferences.IPreferenceStore;
import saros.project.internal.SarosEclipseSessionContextFactory;
import saros.session.ISarosSessionContextFactory;
import saros.synchronize.UISynchronizer;
import saros.synchronize.internal.SWTSynchronizer;
import saros.ui.eventhandler.ConnectingFailureHandler;
import saros.ui.eventhandler.HostLeftAloneInSessionHandler;
import saros.ui.eventhandler.JoinSessionRejectedHandler;
import saros.ui.eventhandler.JoinSessionRequestHandler;
import saros.ui.eventhandler.NegotiationHandler;
import saros.ui.eventhandler.ServerPreferenceHandler;
import saros.ui.eventhandler.SessionStatusRequestHandler;
import saros.ui.eventhandler.SessionViewOpener;
import saros.ui.eventhandler.UserStatusChangeHandler;
import saros.ui.eventhandler.XMPPAuthorizationHandler;

/**
 * Factory used for creating the Saros context when running as Eclipse plugin.
 *
 * @author srossbach
 */
public class SarosEclipseContextFactory extends AbstractContextFactory {

  private final Saros saros;

  /**
   * Must not be static in order to avoid heavy work during class initialization
   *
   * @see <a href="https://github.com/saros-project/saros/commit/237daca">commit&nbsp;237daca</a>
   */
  private final Component[] getContextComponents() {
    return new Component[] {
      // Core Managers
      Component.create(IEditorManager.class, EditorManager.class),
      Component.create(saros.preferences.Preferences.class, EclipsePreferences.class),
      Component.create(SessionViewOpener.class),
      Component.create(UndoManager.class),
      Component.create(ISarosSessionContextFactory.class, SarosEclipseSessionContextFactory.class),

      // UI handlers
      Component.create(HostLeftAloneInSessionHandler.class),
      Component.create(NegotiationHandler.class),
      Component.create(UserStatusChangeHandler.class),
      Component.create(JoinSessionRequestHandler.class),
      Component.create(JoinSessionRejectedHandler.class),
      Component.create(ServerPreferenceHandler.class),
      Component.create(SessionStatusRequestHandler.class),
      Component.create(XMPPAuthorizationHandler.class),
      Component.create(ConnectingFailureHandler.class),
      // Cache support
      /*
       * TODO avoid direct creation as this will become tricky especially
       * if we are the delegate and depends on components that are only
       * available after we added all our context stuff or vice versa
       */
      Component.create(
          IChecksumCache.class, new FileSystemChecksumCache(new FileContentNotifierBridge())),
      Component.create(IWorkspace.class, new EclipseWorkspaceImpl(ResourcesPlugin.getWorkspace())),
      Component.create(
          IWorkspaceRoot.class,
          new EclipseWorkspaceRootImpl(ResourcesPlugin.getWorkspace().getRoot())),

      // Saros Core Path Support
      Component.create(IPathFactory.class, EclipsePathFactory.class),

      // SWT EDT support
      Component.create(UISynchronizer.class, SWTSynchronizer.class),

      // Proxy Support for the XMPP server connection
      Component.create(IProxyResolver.class, Socks5ProxyResolver.class),

      // Remote progress indication
      Component.create(
          IRemoteProgressIndicatorFactory.class, EclipseRemoteProgressIndicatorFactoryImpl.class),
      Component.create(MUCNegotiationManager.class),
      Component.create(SkypeManager.class),
      Component.create(AwarenessInformationCollector.class)
    };
  }

  public SarosEclipseContextFactory(Saros saros) {
    this.saros = saros;
  }

  @Override
  public void createComponents(MutablePicoContainer container) {
    for (Component component : Arrays.asList(getContextComponents()))
      container.addComponent(component.getBindKey(), component.getImplementation());

    container.addComponent(saros);

    container.addComponent(Bundle.class, saros.getBundle());

    container.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.SarosVersion.class),
        saros.getBundle().getVersion().toString());

    container.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.PlatformVersion.class),
        Platform.getBundle("org.eclipse.core.runtime").getVersion().toString());

    // for core logic and extended Eclipse session components
    container.addComponent(
        IPreferenceStore.class, new EclipsePreferenceStoreAdapter(saros.getPreferenceStore()));

    // TODO remove
    // for plain Eclipse components like preference pages etc.
    container.addComponent(
        org.eclipse.jface.preference.IPreferenceStore.class, saros.getPreferenceStore());

    container.addComponent(Preferences.class, saros.getGlobalPreferences());
  }
}
