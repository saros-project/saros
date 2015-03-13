package de.fu_berlin.inf.dpp.intellij.context;

import java.util.Arrays;

import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.AbstractSarosContextFactory;
import de.fu_berlin.inf.dpp.ISarosContextBindings;
import de.fu_berlin.inf.dpp.communication.connection.IProxyResolver;
import de.fu_berlin.inf.dpp.connection.NullProxyResolver;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.concurrent.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.core.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.core.ui.eventhandler.NegotiationHandler;
import de.fu_berlin.inf.dpp.core.ui.eventhandler.UserStatusChangeHandler;
import de.fu_berlin.inf.dpp.core.ui.eventhandler.XMPPAuthorizationHandler;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.filesystem.ChecksumCacheImpl;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFileContentChangedNotifier;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.intellij.editor.EditorAPI;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorManipulator;
import de.fu_berlin.inf.dpp.intellij.editor.ProjectAPI;
import de.fu_berlin.inf.dpp.intellij.preferences.IntelliJPreferences;
import de.fu_berlin.inf.dpp.intellij.preferences.PropertiesComponentAdapter;
import de.fu_berlin.inf.dpp.intellij.project.fs.FileContentChangedNotifierBridge;
import de.fu_berlin.inf.dpp.intellij.project.fs.PathFactory;
import de.fu_berlin.inf.dpp.intellij.runtime.IntelliJSynchronizer;
import de.fu_berlin.inf.dpp.intellij.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.intellij.ui.swt_browser.IntelliJDialogManager;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.preferences.IPreferences;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;

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
        Component.create(EditorManager.class),
        Component.create(LocalEditorHandler.class),
        Component.create(LocalEditorManipulator.class),

        // UI handlers
        Component.create(NegotiationHandler.class),
        Component.create(UserStatusChangeHandler.class),
        Component.create(XMPPAuthorizationHandler.class),

        Component.create(IChecksumCache.class, ChecksumCacheImpl.class),

        Component.create(UISynchronizer.class, IntelliJSynchronizer.class),

        Component.create(IFileContentChangedNotifier.class,
            FileContentChangedNotifierBridge.class),

        Component.create(IPreferenceStore.class,
            PropertiesComponentAdapter.class),
        Component.create(IPreferences.class, IntelliJPreferences.class),

        Component.create(FollowModeAction.class),
        Component.create(LeaveSessionAction.class),

        Component.create(IDialogManager.class, IntelliJDialogManager.class),

        // Proxy Support for the XMPP server connection
        Component.create(IProxyResolver.class, NullProxyResolver.class) };

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
            ISarosContextBindings.SarosVersion.class), "14.1.31.DEVEL"); // todo

        container.addComponent(BindKey.bindKey(String.class,
            ISarosContextBindings.PlatformVersion.class), "4.3.2"); // todo

    }
}
