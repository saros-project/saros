package de.fu_berlin.inf.dpp;

import java.util.Arrays;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.osgi.service.prefs.Preferences;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.communication.connection.IProxyResolver;
import de.fu_berlin.inf.dpp.communication.connection.Socks5ProxyResolver;
import de.fu_berlin.inf.dpp.concurrent.undo.UndoManager;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.filesystem.ChecksumCacheImpl;
import de.fu_berlin.inf.dpp.filesystem.EclipsePathFactory;
import de.fu_berlin.inf.dpp.filesystem.EclipseWorkspaceImpl;
import de.fu_berlin.inf.dpp.filesystem.EclipseWorkspaceRootImpl;
import de.fu_berlin.inf.dpp.filesystem.FileContentNotifierBridge;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRoot;
import de.fu_berlin.inf.dpp.monitoring.remote.EclipseRemoteProgressIndicatorFactoryImpl;
import de.fu_berlin.inf.dpp.monitoring.remote.IRemoteProgressIndicatorFactory;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceStoreAdapter;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferences;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.project.internal.SarosEclipseSessionContextFactory;
import de.fu_berlin.inf.dpp.session.ISarosSessionContextFactory;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.synchronize.internal.SWTSynchronizer;
import de.fu_berlin.inf.dpp.ui.eventhandler.ConnectingFailureHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.HostLeftAloneInSessionHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.JoinSessionRejectedHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.JoinSessionRequestHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.NegotiationHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.ServerPreferenceHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.SessionStatusRequestHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.SessionViewOpener;
import de.fu_berlin.inf.dpp.ui.eventhandler.UserStatusChangeHandler;
import de.fu_berlin.inf.dpp.ui.eventhandler.XMPPAuthorizationHandler;
import de.fu_berlin.inf.dpp.vcs.EclipseVCSProviderFactoryImpl;
import de.fu_berlin.inf.dpp.vcs.VCSProviderFactory;

/**
 * Factory used for creating the Saros context when running as Eclipse plugin.
 * 
 * @author srossbach
 */
public class SarosEclipseContextFactory extends AbstractSarosContextFactory {

    private final Saros saros;

    private final Component[] components = new Component[] {
        // Core Managers
        Component.create(EditorAPI.class),
        Component.create(IEditorManager.class, EditorManager.class),
        // disabled because of privacy violations
        // see
        // http://opus.haw-hamburg.de/volltexte/2011/1391/pdf/ba_krassmann_online.pdf
        // page 47
        // Component.create(LocalPresenceTracker.class),

        Component.create(de.fu_berlin.inf.dpp.preferences.Preferences.class,
            EclipsePreferences.class),
        Component.create(SessionViewOpener.class),
        Component.create(UndoManager.class),

        Component.create(ISarosSessionContextFactory.class,
            SarosEclipseSessionContextFactory.class),

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
         * TODO avoid direct creation as this will become tricky especially if
         * we are the delegate and depends on components that are only available
         * after we added all our context stuff or vice versa
         */
        Component.create(IChecksumCache.class, new ChecksumCacheImpl(
            new FileContentNotifierBridge())),

        Component.create(IWorkspace.class, new EclipseWorkspaceImpl(
            ResourcesPlugin.getWorkspace())),

        Component.create(IWorkspaceRoot.class, new EclipseWorkspaceRootImpl(
            ResourcesPlugin.getWorkspace().getRoot())),

        // Saros Core Path Support
        Component.create(IPathFactory.class, EclipsePathFactory.class),

        // SWT EDT support
        Component.create(UISynchronizer.class, SWTSynchronizer.class),

        // VCS (SVN only)
        Component.create(VCSProviderFactory.class,
            EclipseVCSProviderFactoryImpl.class),

        // Proxy Support for the XMPP server connection
        Component.create(IProxyResolver.class, Socks5ProxyResolver.class),

        // Remote progress indication
        Component.create(IRemoteProgressIndicatorFactory.class,
            EclipseRemoteProgressIndicatorFactoryImpl.class) };

    public SarosEclipseContextFactory(Saros saros) {
        this.saros = saros;
    }

    @Override
    public void createComponents(MutablePicoContainer container) {
        for (Component component : Arrays.asList(components))
            container.addComponent(component.getBindKey(),
                component.getImplementation());

        container.addComponent(saros);

        container.addComponent(BindKey.bindKey(String.class,
            ISarosContextBindings.SarosVersion.class), saros.getBundle()
            .getVersion().toString());

        container.addComponent(BindKey.bindKey(String.class,
            ISarosContextBindings.PlatformVersion.class),
            Platform.getBundle("org.eclipse.core.runtime").getVersion()
                .toString());

        // for core logic and extended Eclipse session components
        container.addComponent(IPreferenceStore.class,
            new EclipsePreferenceStoreAdapter(saros.getPreferenceStore()));

        // TODO remove
        // for plain Eclipse components like preference pages etc.
        container.addComponent(
            org.eclipse.jface.preference.IPreferenceStore.class,
            saros.getPreferenceStore());

        container.addComponent(Preferences.class, saros.getGlobalPreferences());
    }
}
