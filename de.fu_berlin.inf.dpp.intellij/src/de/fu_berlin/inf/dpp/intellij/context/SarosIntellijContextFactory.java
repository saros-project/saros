package de.fu_berlin.inf.dpp.intellij.context;

import de.fu_berlin.inf.dpp.AbstractSarosContextFactory;
import de.fu_berlin.inf.dpp.ISarosContextBindings;
import de.fu_berlin.inf.dpp.communication.connection.IProxyResolver;
import de.fu_berlin.inf.dpp.connection.NullProxyResolver;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.core.monitoring.remote.IntelliJRemoteProgressIndicatorFactoryImpl;
import de.fu_berlin.inf.dpp.core.project.internal.SarosIntellijSessionContextFactory;
import de.fu_berlin.inf.dpp.core.ui.eventhandler.NegotiationHandler;
import de.fu_berlin.inf.dpp.core.ui.eventhandler.UserStatusChangeHandler;
import de.fu_berlin.inf.dpp.core.ui.eventhandler.XMPPAuthorizationHandler;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.core.util.IntelliJCollaborationUtilsImpl;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.NullChecksumCacheImpl;
import de.fu_berlin.inf.dpp.intellij.editor.EditorAPI;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorManipulator;
import de.fu_berlin.inf.dpp.intellij.editor.ProjectAPI;
import de.fu_berlin.inf.dpp.intellij.preferences.IntelliJPreferences;
import de.fu_berlin.inf.dpp.intellij.preferences.PropertiesComponentAdapter;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.PathFactory;
import de.fu_berlin.inf.dpp.intellij.runtime.IntelliJSynchronizer;
import de.fu_berlin.inf.dpp.intellij.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.intellij.ui.swt_browser.IntelliJDialogManager;
import de.fu_berlin.inf.dpp.intellij.ui.swt_browser.IntelliJWebResourceLocator;
import de.fu_berlin.inf.dpp.monitoring.remote.IRemoteProgressIndicatorFactory;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.preferences.Preferences;
import de.fu_berlin.inf.dpp.session.ISarosSessionContextFactory;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.SarosSessionManager;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.ide_embedding.IWebResourceLocator;
import de.fu_berlin.inf.dpp.ui.util.ICollaborationUtils;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

import java.util.Arrays;

/**
 * IntelliJ related context
 */
public class SarosIntellijContextFactory extends AbstractSarosContextFactory {

    private Saros saros;

    private final Component[] components = new Component[] {

        Component.create(ISarosSessionManager.class, SarosSessionManager.class),
        // Core Managers
        Component.create(ConsistencyWatchdogClient.class),

        Component.create(EditorAPI.class),

        Component.create(ProjectAPI.class),

        Component.create(IEditorManager.class, EditorManager.class),
        Component.create(LocalEditorHandler.class),
        Component.create(LocalEditorManipulator.class),

        Component.create(ISarosSessionContextFactory.class,
            SarosIntellijSessionContextFactory.class),

        // UI handlers
        Component.create(NegotiationHandler.class),
        Component.create(UserStatusChangeHandler.class),
        Component.create(XMPPAuthorizationHandler.class),

        Component.create(IChecksumCache.class, NullChecksumCacheImpl.class),

        Component.create(UISynchronizer.class, IntelliJSynchronizer.class),

        Component
            .create(IPreferenceStore.class, PropertiesComponentAdapter.class),

        Component.create(Preferences.class, IntelliJPreferences.class),

        Component.create(FollowModeAction.class),
        Component.create(LeaveSessionAction.class),

        Component.create(IRemoteProgressIndicatorFactory.class,
            IntelliJRemoteProgressIndicatorFactoryImpl.class),

        // IDE-specific classes for the HTML GUI
        Component.create(DialogManager.class, IntelliJDialogManager.class),
        Component.create(IWebResourceLocator.class,
            IntelliJWebResourceLocator.class),

        Component.create(ICollaborationUtils.class,
            IntelliJCollaborationUtilsImpl.class),

        // Proxy Support for the XMPP server connection
        Component.create(IProxyResolver.class, NullProxyResolver.class),

        Component.create(AwarenessInformationCollector.class),
        Component.create(IsInconsistentObservable.class)

    };

    public SarosIntellijContextFactory(Saros saros) {
        this.saros = saros;
    }

    @Override
    public void createComponents(MutablePicoContainer container) {

        IWorkspace workspace = saros.getWorkspace();
        FileUtils.workspace = workspace;

        // Saros Core PathIntl Support
        container.addComponent(IPathFactory.class, new PathFactory());

        container.addComponent(IWorkspace.class, workspace);

        for (Component component : Arrays.asList(components)) {
            container.addComponent(component.getBindKey(),
                component.getImplementation());
        }

        container.addComponent(saros);

        container.addComponent(BindKey.bindKey(String.class,
                ISarosContextBindings.SarosVersion.class),
            "14.1.31.DEVEL"); // todo

        container.addComponent(BindKey.bindKey(String.class,
                ISarosContextBindings.PlatformVersion.class), "4.3.2"); // todo

    }
}
