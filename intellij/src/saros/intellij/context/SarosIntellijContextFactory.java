package saros.intellij.context;

import saros.context.AbstractContextFactory;
import saros.context.IContextKeyBindings;
import saros.core.monitoring.remote.IntelliJRemoteProgressIndicatorFactoryImpl;
import saros.core.ui.eventhandler.NegotiationHandler;
import saros.core.ui.eventhandler.UserStatusChangeHandler;
import saros.core.ui.eventhandler.XMPPAuthorizationHandler;
import saros.editor.IEditorManager;
import saros.filesystem.IPathFactory;
import saros.intellij.editor.EditorManager;
import saros.intellij.filesystem.PathFactory;
import saros.intellij.preferences.IntelliJPreferences;
import saros.intellij.preferences.PropertiesComponentAdapter;
import saros.intellij.runtime.IntellijUISynchronizer;
import saros.intellij.ui.eventhandler.ConnectingFailureHandler;
import saros.intellij.ui.eventhandler.SessionStatusChangeHandler;
import saros.intellij.ui.util.UIProjectUtils;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;
import saros.repackaged.picocontainer.BindKey;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.ISarosSessionContextFactory;
import saros.synchronize.UISynchronizer;

/** Intellij related context */
public class SarosIntellijContextFactory extends AbstractContextFactory {

  /**
   * Must not be static in order to avoid heavy work during class initialization
   *
   * @see <a href= "https://github.com/saros-project/saros/commit/237daca">commit&nbsp;237daca</a>
   */
  private Component[] getContextComponents() {
    return new Component[] {
      // Core Managers
      Component.create(IEditorManager.class, EditorManager.class),
      Component.create(ISarosSessionContextFactory.class, SarosIntellijSessionContextFactory.class),

      // UI handlers
      Component.create(NegotiationHandler.class),
      Component.create(XMPPAuthorizationHandler.class),
      Component.create(UISynchronizer.class, IntellijUISynchronizer.class),
      Component.create(IPreferenceStore.class, PropertiesComponentAdapter.class),
      Component.create(Preferences.class, IntelliJPreferences.class),
      Component.create(
          IRemoteProgressIndicatorFactory.class, IntelliJRemoteProgressIndicatorFactoryImpl.class),

      // UI event handlers relaying information to the user
      Component.create(ConnectingFailureHandler.class),
      Component.create(UserStatusChangeHandler.class),
      Component.create(SessionStatusChangeHandler.class),

      // UI Utility
      Component.create(UIProjectUtils.class),
    };
  }

  @Override
  public void createComponents(MutablePicoContainer container) {

    // Saros Core Path Support
    container.addComponent(IPathFactory.class, new PathFactory());

    for (Component component : getContextComponents()) {
      container.addComponent(component.getBindKey(), component.getImplementation());
    }

    container.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.SarosVersion.class),
        IntellijVersionProvider.getPluginVersion());

    container.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.PlatformVersion.class),
        IntellijVersionProvider.getBuildNumber());
  }
}
