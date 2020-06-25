package saros.context;

import java.util.Arrays;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.Preferences;
import saros.Saros;
import saros.awareness.AwarenessInformationCollector;
import saros.communication.SkypeManager;
import saros.communication.chat.muc.negotiation.MUCNegotiationManager;
import saros.editor.EditorManager;
import saros.editor.IEditorManager;
import saros.filesystem.EclipsePathFactory;
import saros.filesystem.EclipseWorkspaceImpl;
import saros.filesystem.IPathFactory;
import saros.filesystem.IWorkspace;
import saros.filesystem.checksum.EclipseAbsolutePathResolver;
import saros.filesystem.checksum.FileContentNotifierBridge;
import saros.filesystem.checksum.FileSystemChecksumCache;
import saros.filesystem.checksum.IChecksumCache;
import saros.monitoring.remote.EclipseRemoteProgressIndicatorFactoryImpl;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.preferences.EclipsePreferenceStoreAdapter;
import saros.preferences.EclipsePreferences;
import saros.preferences.IPreferenceStore;
import saros.repackaged.picocontainer.BindKey;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.ISarosSessionContextFactory;
import saros.synchronize.UISynchronizer;
import saros.synchronize.internal.EclipseSWTSynchronizer;
import saros.ui.eventhandler.ConnectingFailureHandler;
import saros.ui.eventhandler.HostLeftAloneInSessionHandler;
import saros.ui.eventhandler.IncomingFileTransferHandler;
import saros.ui.eventhandler.NegotiationHandler;
import saros.ui.eventhandler.SessionViewOpener;
import saros.ui.eventhandler.UserStatusChangeHandler;
import saros.ui.eventhandler.XMPPAuthorizationHandler;
import saros.ui.util.XMPPConnectionSupport;

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
      Component.create(SessionViewOpener.class),
      // disabled, https://github.com/saros-project/saros/issues/60
      // do not forget to enable the option in the GeneralPreferencePage once it is fixed
      // Component.create(UndoManager.class),
      Component.create(ISarosSessionContextFactory.class, SarosEclipseSessionContextFactory.class),

      // UI handlers
      Component.create(HostLeftAloneInSessionHandler.class),
      Component.create(NegotiationHandler.class),
      Component.create(UserStatusChangeHandler.class),
      Component.create(XMPPAuthorizationHandler.class),
      Component.create(ConnectingFailureHandler.class),
      Component.create(IncomingFileTransferHandler.class),

      // Cache support
      /*
       * TODO avoid direct creation as this will become tricky especially
       * if we are the delegate and depends on components that are only
       * available after we added all our context stuff or vice versa
       */
      Component.create(
          IChecksumCache.class,
          new FileSystemChecksumCache(
              new FileContentNotifierBridge(), new EclipseAbsolutePathResolver())),
      Component.create(IWorkspace.class, new EclipseWorkspaceImpl(ResourcesPlugin.getWorkspace())),

      // Saros Core Path Support
      Component.create(IPathFactory.class, EclipsePathFactory.class),

      // SWT EDT support
      Component.create(UISynchronizer.class, EclipseSWTSynchronizer.class),

      // Remote progress indication
      Component.create(
          IRemoteProgressIndicatorFactory.class, EclipseRemoteProgressIndicatorFactoryImpl.class),
      Component.create(MUCNegotiationManager.class),
      Component.create(SkypeManager.class),
      Component.create(AwarenessInformationCollector.class),
      // Central connect/disconnect access point for the UI
      Component.create(XMPPConnectionSupport.class)
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

    final org.eclipse.jface.preference.IPreferenceStore instanceScopePreferenceStore =
        saros.getPreferenceStore();

    container.addComponent(
        IPreferenceStore.class, new EclipsePreferenceStoreAdapter(instanceScopePreferenceStore));

    /* FIXME the class states that it should be the global preferences, however it does not have method to write preferences and so it is fine for now to use the instance scope */
    //    container.addComponent(
    //        saros.preferences.Preferences.class,
    //        new ScopedPreferenceStore(ConfigurationScope.INSTANCE, Saros.PLUGIN_ID));

    container.addComponent(
        saros.preferences.Preferences.class, new EclipsePreferences(instanceScopePreferenceStore));

    // TODO remove
    // for plain Eclipse components like preference pages etc.
    container.addComponent(
        org.eclipse.jface.preference.IPreferenceStore.class, instanceScopePreferenceStore);

    /* FIXME either use Preferences or IPreferencestore (e.g ScopedPreferenceStore which access Preferences) but NOT both at the same time.
     * This is currently a madness, we have a Preferences core class that actually does not do anything but is just a convenience class,
     * then we have a PreferenceStore and OSGi Preferences here which are only used by the Feedback Component which is disabled anyways !!!!
     * The STF also has the ability to return this instance however it is not used anywhere in the framework.
     */
    container.addComponent(Preferences.class, saros.getGlobalPreferences());
  }
}
