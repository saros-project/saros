package saros.intellij.context;

import java.util.Arrays;
import saros.context.AbstractContextFactory;
import saros.context.IContextKeyBindings;
import saros.core.monitoring.remote.IntelliJRemoteProgressIndicatorFactoryImpl;
import saros.core.ui.eventhandler.NegotiationHandler;
import saros.core.ui.eventhandler.UserStatusChangeHandler;
import saros.core.ui.eventhandler.XMPPAuthorizationHandler;
import saros.core.util.IntellijCollaborationUtilsImpl;
import saros.editor.IEditorManager;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IPathFactory;
import saros.filesystem.IWorkspace;
import saros.filesystem.IWorkspaceRoot;
import saros.filesystem.NullChecksumCache;
import saros.intellij.editor.EditorManager;
import saros.intellij.negotiation.ModuleConfigurationProvider;
import saros.intellij.negotiation.hooks.ModuleTypeNegotiationHook;
import saros.intellij.preferences.IntelliJPreferences;
import saros.intellij.preferences.PropertiesComponentAdapter;
import saros.intellij.project.filesystem.IntelliJWorkspaceImpl;
import saros.intellij.project.filesystem.IntelliJWorkspaceRootImpl;
import saros.intellij.project.filesystem.PathFactory;
import saros.intellij.runtime.IntelliJSynchronizer;
import saros.intellij.ui.eventhandler.SessionStatusChangeHandler;
import saros.intellij.ui.util.UIProjectUtils;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;
import saros.repackaged.picocontainer.BindKey;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.ISarosSessionContextFactory;
import saros.synchronize.UISynchronizer;
import saros.ui.util.ICollaborationUtils;

/** IntelliJ related context */
public class SarosIntellijContextFactory extends AbstractContextFactory {

  /**
   * Must not be static in order to avoid heavy work during class initialization
   *
   * @see <a href= "https://github.com/saros-project/saros/commit/237daca">commit&nbsp;237daca</a>
   */
  private final Component[] getContextComponents() {
    return new Component[] {
      // Core Managers
      Component.create(IEditorManager.class, EditorManager.class),
      Component.create(ISarosSessionContextFactory.class, SarosIntellijSessionContextFactory.class),

      // additional project negotiation data providers
      Component.create(ModuleConfigurationProvider.class),

      // UI handlers
      Component.create(NegotiationHandler.class),
      Component.create(UserStatusChangeHandler.class),
      Component.create(XMPPAuthorizationHandler.class),
      Component.create(SessionStatusChangeHandler.class),
      Component.create(IChecksumCache.class, NullChecksumCache.class),
      Component.create(UISynchronizer.class, IntelliJSynchronizer.class),
      Component.create(IPreferenceStore.class, PropertiesComponentAdapter.class),
      Component.create(Preferences.class, IntelliJPreferences.class),
      Component.create(
          IRemoteProgressIndicatorFactory.class, IntelliJRemoteProgressIndicatorFactoryImpl.class),

      // UI Utility
      Component.create(UIProjectUtils.class),

      // IDE-specific classes for the HTML GUI
      Component.create(ICollaborationUtils.class, IntellijCollaborationUtilsImpl.class),
      Component.create(IWorkspaceRoot.class, IntelliJWorkspaceRootImpl.class),
    };
  }

  @Override
  public void createComponents(MutablePicoContainer container) {

    // Saros Core PathIntl Support
    container.addComponent(IPathFactory.class, new PathFactory());

    container.addComponent(IWorkspace.class, IntelliJWorkspaceImpl.class);

    for (Component component : Arrays.asList(getContextComponents())) {
      container.addComponent(component.getBindKey(), component.getImplementation());
    }

    container.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.SarosVersion.class),
        IntelliJVersionProvider.getPluginVersion());

    container.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.PlatformVersion.class),
        IntelliJVersionProvider.getBuildNumber());

    container.addComponent(ModuleTypeNegotiationHook.class);
  }
}
