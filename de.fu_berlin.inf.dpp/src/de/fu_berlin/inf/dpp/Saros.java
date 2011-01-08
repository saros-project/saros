/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.security.sasl.SaslException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.jingle.JingleManager;
import org.jivesoftware.smackx.socks5bytestream.Socks5Proxy;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.annotations.Inject;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.CompositeInjection;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.injectors.Reinjector;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.audio.AudioService;
import de.fu_berlin.inf.dpp.communication.audio.AudioServiceManager;
import de.fu_berlin.inf.dpp.communication.audio.MixerManager;
import de.fu_berlin.inf.dpp.communication.muc.MUCManager;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferencesNegotiatingManager;
import de.fu_berlin.inf.dpp.communication.muc.singleton.MUCManagerSingletonWrapperChatView;
import de.fu_berlin.inf.dpp.concurrent.undo.UndoManager;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogServer;
import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.concurrent.watchdog.SessionViewOpener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.feedback.DataTransferCollector;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.feedback.FollowModeCollector;
import de.fu_berlin.inf.dpp.feedback.JumpFeatureUsageCollector;
import de.fu_berlin.inf.dpp.feedback.ParticipantCollector;
import de.fu_berlin.inf.dpp.feedback.RoleChangeCollector;
import de.fu_berlin.inf.dpp.feedback.SelectionCollector;
import de.fu_berlin.inf.dpp.feedback.SessionDataCollector;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.feedback.TextEditCollector;
import de.fu_berlin.inf.dpp.feedback.VoIPCollector;
import de.fu_berlin.inf.dpp.invitation.ArchiveStreamService;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.XMPPUtil;
import de.fu_berlin.inf.dpp.net.business.ActivitiesHandler;
import de.fu_berlin.inf.dpp.net.business.CancelInviteHandler;
import de.fu_berlin.inf.dpp.net.business.ConsistencyWatchdogHandler;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.business.InvitationHandler;
import de.fu_berlin.inf.dpp.net.business.LeaveHandler;
import de.fu_berlin.inf.dpp.net.business.RequestForActivityHandler;
import de.fu_berlin.inf.dpp.net.business.UserListHandler;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.ConnectionTestManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.InvitationAcknowledgementExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.InvitationInfo;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.net.internal.SubscriptionListener;
import de.fu_berlin.inf.dpp.net.internal.UserListInfo;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.LeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestActivityExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.JingleFileTransferManagerObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.VideoSessionObservable;
import de.fu_berlin.inf.dpp.observables.VoIPSessionObservable;
import de.fu_berlin.inf.dpp.optional.jdt.JDTFacade;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.PingPongCentral;
import de.fu_berlin.inf.dpp.project.SarosRosterListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.project.SharedResourcesManager;
import de.fu_berlin.inf.dpp.project.internal.ChangeColorManager;
import de.fu_berlin.inf.dpp.project.internal.RoleManager;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.ui.LocalPresenceTracker;
import de.fu_berlin.inf.dpp.ui.RemoteProgressManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.actions.SendFileAction;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.pico.ChildContainer;
import de.fu_berlin.inf.dpp.util.pico.ChildContainerProvider;
import de.fu_berlin.inf.dpp.util.pico.DotGraphMonitor;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing;
import de.fu_berlin.inf.dpp.videosharing.VideoSharingService;

/**
 * The main plug-in of Saros.
 * 
 * @author rdjemili
 * @author coezbek
 */
@Component(module = "core")
public class Saros extends AbstractUIPlugin {
    private static final int REFRESH_SECONDS = 3;

    /**
     * The single instance of the Saros plugin.
     */
    protected static Saros plugin;

    /**
     * True if the Saros instance has been initialized so that calling
     * reinject() will be well defined.
     */
    protected static boolean isInitialized;

    /**
     * This is the Bundle-SymbolicName (a.k.a the pluginID)
     */
    public static final String SAROS = "de.fu_berlin.inf.dpp"; //$NON-NLS-1$

    /**
     * The name of the XMPP namespace used by Saros. At the moment it is only
     * used to advertise the Saros feature in the Service Discovery.
     * 
     * TODO Add version information, so that only compatible versions of Saros
     * can use each other.
     */
    public final static String NAMESPACE = SAROS;

    /**
     * The name of the resource identifier used by Saros when connecting to the
     * XMPP Server (for instance when logging in as john@doe.com, Saros will
     * connect using john@doe.com/Saros)
     */
    public final static String RESOURCE = "Saros";

    public String sarosVersion;

    public String sarosFeatureID;

    protected SarosSessionManager sessionManager;

    /**
     * A caching container which holds all the singletons in Saros. This
     * container has plug-in scope: The objects it manages are created when the
     * plug-in is started, and disposed when the plug-in is stopped.
     */
    protected MutablePicoContainer container;

    /**
     * The reinjector used to inject dependencies into those objects that are
     * created by Eclipse and not by our PicoContainer.
     */
    protected Reinjector reinjector;

    /**
     * To print an architecture diagram at the end of the plug-in life-cycle
     * initialize the dotMonitor with a new instance:
     * 
     * <code>dotMonitor= new DotGraphMonitor();</code>
     */
    protected DotGraphMonitor dotMonitor;

    protected XMPPConnection connection;

    /**
     * The RQ-JID of the local user or null if the user is
     * {@link ConnectionState#NOT_CONNECTED}.
     */
    protected JID myJID;

    protected ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

    protected Exception connectionError;

    protected final List<IConnectionListener> listeners = new CopyOnWriteArrayList<IConnectionListener>();

    // Smack (XMPP) connection listener
    protected ConnectionListener smackConnectionListener;

    /**
     * The global plug-in preferences, shared among all workspaces. Should only
     * be accessed over {@link #getConfigPrefs()} from outside this class.
     */
    protected Preferences configPrefs;

    public static final Random RANDOM = new Random();

    protected Logger log;

    static {
        Roster.setDefaultSubscriptionMode(SubscriptionMode.accept_all);
    }

    /**
     * Create the shared instance.
     */
    public Saros() {

        // Only start a DotGraphMonitor if asserts are enabled (aka debug mode)
        assert (dotMonitor = new DotGraphMonitor()) != null;

        setInitialized(false);
        setDefault(this);

        PicoBuilder picoBuilder = new PicoBuilder(new CompositeInjection(
            new ConstructorInjection(), new AnnotatedFieldInjection()))
            .withCaching().withLifecycle();

        /*
         * If given, the dotMonitor is used to capture an architecture diagram
         * of the application
         */
        if (dotMonitor != null) {
            picoBuilder = picoBuilder.withMonitor(dotMonitor);
        }

        // Initialize our dependency injection container
        this.container = picoBuilder.build();

        // Add Adapter which creates ChildContainers
        this.container.as(Characteristics.NO_CACHE).addAdapter(
            new ProviderAdapter(new ChildContainerProvider(this.container)));
        /*
         * All singletons which exist for the whole plug-in life-cycle are
         * managed by PicoContainer for us.
         * 
         * The addComponent() calls are sorted alphabetically according to the
         * first argument. This makes it easier to search for a class without
         * tool support.
         */
        this.container.addComponent(Saros.class, this);

        // Thread Context
        this.container.addComponent(DispatchThreadContext.class);

        // Core Managers
        this.container.addComponent(ChangeColorManager.class);
        this.container.addComponent(ConsistencyWatchdogClient.class);
        this.container.addComponent(ConsistencyWatchdogServer.class);
        this.container.addComponent(DataTransferManager.class);
        this.container.addComponent(DiscoveryManager.class);
        this.container.addComponent(EditorAPI.class);
        this.container.addComponent(EditorManager.class);
        this.container.addComponent(ErrorLogManager.class);
        this.container.addComponent(FeedbackManager.class);
        this.container.addComponent(JDTFacade.class);
        this.container.addComponent(LocalPresenceTracker.class);
        this.container.addComponent(MUCManager.class);
        this.container.addComponent(MUCManagerSingletonWrapperChatView.class);
        this.container.addComponent(PingPongCentral.class);
        this.container.addComponent(PreferenceManager.class);
        this.container.addComponent(PreferenceUtils.class);
        this.container.addComponent(RoleManager.class);
        this.container.addComponent(RosterTracker.class);
        this.container.addComponent(SarosRosterListener.class);
        this.container.addComponent(SarosUI.class);
        this.container.addComponent(SarosSessionManager.class);
        this.container.addComponent(SessionViewOpener.class);
        this.container.addComponent(SharedResourcesManager.class);
        this.container.addComponent(SkypeManager.class);
        this.container.addComponent(StatisticManager.class);
        this.container.addComponent(StopManager.class);
        this.container.addComponent(StreamServiceManager.class);
        this.container.addComponent(AudioServiceManager.class);
        this.container.addComponent(MixerManager.class);
        this.container.addComponent(SubscriptionListener.class);
        this.container.addComponent(UndoManager.class);
        this.container.addComponent(VideoSharing.class);
        this.container.addComponent(VersionManager.class);
        this.container
            .addComponent(MUCSessionPreferencesNegotiatingManager.class);
        this.container.addComponent(XMPPReceiver.class);
        this.container.addComponent(XMPPTransmitter.class);
        this.container.addComponent(RemoteProgressManager.class);
        this.container.addComponent(XMPPAccountStore.class);

        // Observables
        this.container.addComponent(FileReplacementInProgressObservable.class);
        this.container.addComponent(InvitationProcessObservable.class);
        this.container.addComponent(IsInconsistentObservable.class);
        this.container.addComponent(JingleFileTransferManagerObservable.class);
        this.container.addComponent(SessionIDObservable.class);
        this.container.addComponent(SarosSessionObservable.class);
        this.container.addComponent(VoIPSessionObservable.class);
        this.container.addComponent(VideoSessionObservable.class);

        // Handlers
        this.container.addComponent(CancelInviteHandler.class);
        this.container.addComponent(UserListHandler.class);
        this.container.addComponent(InvitationHandler.class);
        this.container.addComponent(LeaveHandler.class);
        this.container.addComponent(RequestForActivityHandler.class);
        this.container.addComponent(ConsistencyWatchdogHandler.class);
        this.container.addComponent(ActivitiesHandler.class);
        this.container.addComponent(ConnectionTestManager.class);

        // Extensions
        this.container.addComponent(CancelInviteExtension.class);
        this.container.addComponent(UserListExtension.class);
        this.container.addComponent(RequestActivityExtension.class);
        this.container.addComponent(LeaveExtension.class);

        // Extension Providers
        this.container.addComponent(ActivitiesExtensionProvider.class);
        this.container
            .addComponent(InvitationInfo.InvitationExtensionProvider.class);
        this.container
            .addComponent(IncomingTransferObjectExtensionProvider.class);
        this.container
            .addComponent(InvitationAcknowledgementExtensionProvider.class);
        this.container
            .addComponent(DefaultInvitationInfo.FileListRequestExtensionProvider.class);
        this.container.addComponent(UserListInfo.JoinExtensionProvider.class);
        this.container
            .addComponent(DefaultInvitationInfo.UserListConfirmationExtensionProvider.class);
        this.container
            .addComponent(DefaultInvitationInfo.InvitationCompleteExtensionProvider.class);

        // Statistic collectors
        this.container.addComponent(DataTransferCollector.class);
        this.container.addComponent(RoleChangeCollector.class);
        this.container.addComponent(ParticipantCollector.class);
        this.container.addComponent(SessionDataCollector.class);
        this.container.addComponent(TextEditCollector.class);
        this.container.addComponent(JumpFeatureUsageCollector.class);
        this.container.addComponent(FollowModeCollector.class);
        this.container.addComponent(SelectionCollector.class);
        this.container.addComponent(VoIPCollector.class);

        // streaming services
        this.container.addComponent(SendFileAction.SendFileStreamService.class);
        this.container.addComponent(AudioService.class);
        this.container.addComponent(VideoSharingService.class);
        this.container.addComponent(ArchiveStreamService.class);
        /*
         * The following classes are initialized by the re-injector because they
         * are created by Eclipse:
         * 
         * All User interface classes like all Views, but also
         * SharedDocumentProvider.
         * 
         * CAUTION: Classes from which duplicates can exists, should not be
         * managed by PicoContainer.
         */
        reinjector = new Reinjector(this.container);
    }

    protected static void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    /**
     * Adds the object to Saros' container, and injects dependencies into the
     * annotated fields of the given object. It should only be used for objects
     * that were created by Eclipse, which have the same life cycle as the Saros
     * plug-in, e.g. the popup menu actions.
     */
    public static synchronized void reinject(Object toInjectInto) {
        checkInitialized();

        try {
            // Remove the component if an instance of it was already registered
            Class<? extends Object> clazz = toInjectInto.getClass();
            ComponentAdapter<Object> removed = plugin.container
                .removeComponent(clazz);
            if (removed != null && clazz != Saros.class) {
                LogLog.warn(clazz.toString() + " added more than once!",
                    new StackTrace());
            }

            // Add the given instance to the container
            plugin.container.addComponent(clazz, toInjectInto);

            /*
             * Ask PicoContainer to inject into the component via fields
             * annotated with @Inject
             */
            plugin.reinjector.reinject(clazz, new AnnotatedFieldInjection());
        } catch (PicoCompositionException e) {
            LogLog.error("Internal error in reinjection:", e);
        }
    }

    /**
     * Injects dependencies into the annotated fields of the given object. This
     * method should be used for objects that were created by Eclipse, which
     * have a different life cycle than the Saros plug-in.
     */
    public static synchronized void injectDependenciesOnly(Object toInjectInto) {
        checkInitialized();

        ChildContainer dummyContainer = plugin.container
            .getComponent(ChildContainer.class);
        dummyContainer.reinject(toInjectInto);
        plugin.container.removeChildContainer(dummyContainer);
    }

    protected static void checkInitialized() {
        if (plugin == null || !isInitialized()) {
            LogLog.error("Saros not initialized", new StackTrace());
            throw new IllegalStateException();
        }
    }

    /**
     * Returns true if the Saros instance has been initialized so that calling
     * {@link #reinject(Object)} will be well defined.
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    protected void setBytestreamConnectionProperties() {
        boolean settingsChanged = false;
        int port = container.getComponent(PreferenceUtils.class)
            .getFileTransferPort();
        boolean proxyEnabled = !getPreferenceStore().getBoolean(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED);

        // Note: The proxy gets restarted on port change, too.
        if (port != SmackConfiguration.getLocalSocks5ProxyPort()) {
            settingsChanged = true;
            SmackConfiguration.setLocalSocks5ProxyPort(port);
        }

        /*
         * TODO Fix in Smack: Either always start proxy when enabled in the
         * smack configuration or never start it automatically. Currently it
         * only starts after initiation the singleton on first access.
         */
        Socks5Proxy proxy = Socks5Proxy.getSocks5Proxy();
        if (proxyEnabled != SmackConfiguration.isLocalSocks5ProxyEnabled()) {
            settingsChanged = true;
            SmackConfiguration.setLocalSocks5ProxyEnabled(proxyEnabled);
        }

        if (settingsChanged || proxy.isRunning() != proxyEnabled) {
            StringBuilder sb = new StringBuilder(
                "Socks5Proxy properties changed.");
            if (proxy.isRunning()) {
                proxy.stop();
                sb.append(" Stopping...");
            }
            if (proxyEnabled) {
                sb.append(" Starting.");
                proxy.start();
            }
            if (settingsChanged)
                log.debug(sb);
        }

        // TODO: just pasted from before
        // This disables Jingle if the user has selected to use XMPP
        // file transfer exclusively
        JingleManager.setServiceEnabled(connection, !getPreferenceStore()
            .getBoolean(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT));
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {

        super.start(context);

        sarosVersion = Util.getBundleVersion(getBundle(), "Unknown Version");

        sarosFeatureID = SAROS + "_" + sarosVersion;

        Connection.DEBUG_ENABLED = getPreferenceStore().getBoolean(
            PreferenceConstants.DEBUG);

        // Jingle has to be started once!
        JingleManager.setJingleServiceEnabled();

        /*
         * add Saros as XMPP feature once XMPPConnection is connected to the
         * XMPP server
         */
        Connection
            .addConnectionCreationListener(new ConnectionCreationListener() {
                public void connectionCreated(Connection connection) {
                    if (Saros.this.connection != connection) {
                        // Ignore the connections created in createAccount.
                        return;
                    }

                    ServiceDiscoveryManager sdm = ServiceDiscoveryManager
                        .getInstanceFor(connection);
                    sdm.addFeature(Saros.NAMESPACE);

                    setBytestreamConnectionProperties();
                }
            });

        setupLoggers();
        log.info("Starting Saros " + sarosVersion + " running:\n"
            + Util.getPlatformInfo());

        // Remove the Bundle if an instance of it was already registered
        container.removeComponent(Bundle.class);
        container.addComponent(Bundle.class, getBundle());

        // Make sure that all components in the container are
        // instantiated
        container.getComponents(Object.class);

        this.sessionManager = container.getComponent(SarosSessionManager.class);

        isInitialized = true;

        // determine if auto-connect can and should be performed
        boolean autoConnect = getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_CONNECT);

        if (!autoConnect)
            return;

        StatisticManager statisticManager = container
            .getComponent(StatisticManager.class);
        ErrorLogManager errorLogManager = container
            .getComponent(ErrorLogManager.class);

        // we need at least a user name, but also the agreement to the
        // statistic and error log submission
        boolean hasUserName = this.container
            .getComponent(PreferenceUtils.class).hasUserName();
        boolean hasAgreement = statisticManager.hasStatisticAgreement()
            && errorLogManager.hasErrorLogAgreement();

        if (hasUserName && hasAgreement) {
            asyncConnect();
        }
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {

        // TODO Devise a general way to stop and dispose our components
        saveConfigPrefs();

        if (dotMonitor != null) {
            File f = new File("Saros-" + sarosFeatureID + ".dot");
            log.info("Saving Saros architecture diagram dot file: "
                + f.getAbsolutePath());
            dotMonitor.save(f);
        }

        try {
            if (isConnected()) {
                /*
                 * Need to fork because disconnect should not be run in the SWT
                 * thread.
                 */

                /*
                 * FIXME Provide a unique thread context in which all
                 * connecting/disconnecting is done.
                 */
                Util.runSafeAsync(log, new Runnable() {
                    public void run() {
                        disconnect();
                    }
                });
            }

            /**
             * This will cause dispose() to be called on all components managed
             * by PicoContainer which implement {@link Disposable}.
             */
            container.dispose();
        } finally {
            super.stop(context);
        }

        isInitialized = false;
        setDefault(null);
    }

    public void removeChildContainer(PicoContainer child) {
        container.removeChildContainer(child);
    }

    public static void setDefault(Saros newPlugin) {
        Saros.plugin = newPlugin;

    }

    /**
     * The RQ-JID of the local user
     */
    public JID getMyJID() {
        return this.myJID;
    }

    public Roster getRoster() {
        if (!isConnected()) {
            return null;
        }

        return this.connection.getRoster();
    }

    /**
     * Returns the global {@link Preferences} with {@link ConfigurationScope}
     * for this plug-in or null if the node couldn't be determined. <br>
     * <br>
     * The returned Preferences can be accessed concurrently by multiple threads
     * of the same JVM without external synchronization. If they are used by
     * multiple JVMs no guarantees can be made concerning data consistency (see
     * {@link Preferences} for details).
     * 
     * @return the preferences node for this plug-in containing global
     *         preferences that are visible for all workspaces of this eclipse
     *         installation
     */
    public synchronized Preferences getConfigPrefs() {
        // TODO Singleton-Pattern code smell: ConfigPrefs should be a @component
        if (configPrefs == null) {
            configPrefs = new ConfigurationScope().getNode(SAROS);
        }
        return configPrefs;
    }

    /**
     * Saves the global preferences to disk. Should be called at least before
     * the bundle is stopped to prevent loss of data. Can be called whenever
     * found necessary.
     */
    public synchronized void saveConfigPrefs() {
        /*
         * Note: If multiple JVMs use the config preferences and the underlying
         * backing store, they might not always work with latest data, e.g. when
         * using multiple instances of the same eclipse installation.
         */
        if (configPrefs != null) {
            try {
                configPrefs.flush();
            } catch (BackingStoreException e) {
                log.error("Couldn't store global plug-in preferences", e);
            }
        }
    }

    /**
     * @nonBlocking
     */
    public void asyncConnect() {
        Util.runSafeAsync("Saros-AsyncConnect-", log, new Runnable() {
            public void run() {
                connect(false);
            }
        });
    }

    @Inject
    PreferenceUtils preferenceUtils;
    @Inject
    StatisticManager statisticManager;
    @Inject
    ErrorLogManager errorLogManager;

    /**
     * Connects using the credentials from the preferences. If no credentials
     * are present a wizard is opened before. It uses TLS if possible.
     * 
     * If there is already an established connection when calling this method,
     * it disconnects before connecting (including state transitions!).
     * 
     * @blocking
     */
    public void connect(boolean failSilently) {
        // check if we need to do a reinject
        if (preferenceUtils == null || statisticManager == null
            || errorLogManager == null)
            Saros.reinject(this);

        /*
         * see if we have a user name and an agreement to submitting user
         * statistics and the error log, if not, show wizard before connecting
         */
        boolean hasUsername = preferenceUtils.hasUserName();
        boolean hasAgreement = statisticManager.hasStatisticAgreement()
            && errorLogManager.hasErrorLogAgreement();

        if (!hasUsername || !hasAgreement) {
            boolean ok = Util.showConfigurationWizard(!hasUsername,
                !hasAgreement);
            if (!ok)
                return;
        }

        IPreferenceStore prefStore = getPreferenceStore();

        final String server = prefStore.getString(PreferenceConstants.SERVER);
        final String username = prefStore
            .getString(PreferenceConstants.USERNAME);

        try {
            if (isConnected()) {
                disconnect();
            }

            /**
             * Infinite connecting state: when providing an empty server address
             * and connecting via Roster view.
             */

            while (prefStore.getString(PreferenceConstants.SERVER).equals("")) {
                boolean ok = Util.showConfigurationWizard(true, false);
                if (!ok)
                    return;
            }

            setConnectionState(ConnectionState.CONNECTING, null);
            this.connection = new XMPPConnection(getConnectionConfiguration());
            this.connection.connect();

            // add connection listener so we get notified if it will be closed
            if (this.smackConnectionListener == null) {
                this.smackConnectionListener = new SafeConnectionListener(log,
                    new XMPPConnectionListener());
            }
            connection.addConnectionListener(this.smackConnectionListener);

            Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

            String password = prefStore.getString(PreferenceConstants.PASSWORD);
            /*
             * TODO SS Possible race condition, as our packet listeners are
             * registered only after the login (in CONNECTED Connection State),
             * so we might for instance receive subscription requests even
             * though we do not have a packet listener running yet!
             */
            this.connection.login(username, password, Saros.RESOURCE);
            /* other people can now send invitations */

            this.myJID = new JID(this.connection.getUser());
            setConnectionState(ConnectionState.CONNECTED, null);

        } catch (URISyntaxException e) {
            log.info("URI not parseable: " + e.getInput());
            Util.popUpFailureMessage("URI not parseable", e.getInput()
                + " is not a valid URI.", failSilently);

        } catch (IllegalArgumentException e) {
            log.info("Illegal argument: " + e.getMessage());
            setConnectionState(ConnectionState.ERROR, null);
            Util.popUpFailureMessage("Illegal argument", e.getMessage(),
                failSilently);

        } catch (XMPPException e) {
            Throwable t = e.getWrappedThrowable();
            Exception cause = (t != null) ? (Exception) t : e;

            setConnectionState(ConnectionState.ERROR, cause);

            if (cause instanceof SaslException) {
                Util.popUpFailureMessage("Error Connecting via SASL",
                    cause.getMessage(), failSilently);
            } else {
                String question;
                if (cause instanceof UnknownHostException) {
                    log.info("Unknown host: " + cause);

                    question = "Error Connecting to XMPP server: '" + server
                        + "'.\n\nDo you want to use other parameters?";
                } else {
                    log.info("xmpp: " + cause.getMessage(), cause);

                    question = "Could not connect to server '" + server
                        + "' as user '" + username
                        + "'. Do You want to use other parameters?";
                }
                boolean showConfigurationWizard = Util.popUpYesNoQuestion(
                    "Error Connecting", question, failSilently);
                if (showConfigurationWizard) {
                    if (Util.showConfigurationWizard(true, false))
                        connect(failSilently);
                }
            }
        } catch (Exception e) {
            log.warn("Unhandled exception:", e);
            setConnectionState(ConnectionState.ERROR, e);
            Util.popUpFailureMessage("Error Connecting",
                "Could not connect to server '" + server + "' as user '"
                    + username + "'.\nErrorMessage was:\n" + e.getMessage(),
                failSilently);
        }
    }

    /**
     * Returns a @link{ConnectionConfiguration} representing the settings stored
     * in the Eclipse preferences.
     * 
     * This methods will fall back to use DNS SRV to get the XMPP port of the
     * server if the SERVER configured in the preferences does not have an
     * explicit port set.
     * 
     * Also this method configures the SecurityMode and the reconnection
     * attribute.
     * 
     * @throws URISyntaxException
     *             If the server string in the preferences cannot be transformed
     *             into an URI
     */
    protected ConnectionConfiguration getConnectionConfiguration()
        throws URISyntaxException {

        IPreferenceStore prefStore = getPreferenceStore();

        String serverString = prefStore.getString(PreferenceConstants.SERVER);

        URI uri;
        uri = (serverString.matches("://")) ? new URI(serverString) : new URI(
            "jabber://" + serverString);

        String server = uri.getHost();
        if (server == null) {
            throw new URISyntaxException(
                prefStore.getString(PreferenceConstants.SERVER),
                "The XMPP server address is invalid: " + serverString);
        }

        ProxyInfo proxyInfo = getProxyInfo(uri.getHost());
        ConnectionConfiguration conConfig = null;

        if (uri.getPort() < 0) {
            conConfig = proxyInfo == null ? new ConnectionConfiguration(
                uri.getHost()) : new ConnectionConfiguration(uri.getHost(),
                proxyInfo);
        } else {
            conConfig = proxyInfo == null ? new ConnectionConfiguration(
                uri.getHost(), uri.getPort()) : new ConnectionConfiguration(
                uri.getHost(), uri.getPort(), proxyInfo);
        }

        /*
         * TODO It has to ask the user, if s/he wants to use non-TLS connections
         * with PLAIN SASL if TLS is not supported by the server.
         * 
         * TODO use MessageDialog and Util.runSWTSync() to provide a password
         * call-back if the user has no password set in the preferences.
         */
        conConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
        /*
         * We handle reconnecting ourselves
         */
        conConfig.setReconnectionAllowed(false);

        return conConfig;
    }

    /**
     * Returns @link{IProxyService} if there is a registered service otherwise
     * null.
     */
    protected IProxyService getProxyService() {
        BundleContext bundleContext = getBundle().getBundleContext();
        ServiceReference serviceReference = bundleContext
            .getServiceReference(IProxyService.class.getName());
        return (IProxyService) bundleContext.getService(serviceReference);
    }

    /**
     * Returns a @link{ProxyInfo}, if a configuration of a proxy for the given
     * host is available. If @link{IProxyData} is of type
     * 
     * @link{IProxyData.HTTP_PROXY_TYPE it tries to use Smacks
     * @link{ProxyInfo.forHttpProxy and if it is of type
     * @link{IProxyData.SOCKS_PROXY_TYPE then it tries to use Smacks
     * @link{ProxyInfo.forSocks5Proxy otherwise it returns null.
     * 
     * @param host
     *            The host to which you want to connect to.
     * 
     * @return Returns a @link{ProxyInfo} if available otherwise null.
     * 
     * @SuppressWarnings("deprecation") -> getProxyDataForHost replacement is
     *                                  only available in Eclipse 3.5
     */
    @SuppressWarnings("deprecation")
    protected ProxyInfo getProxyInfo(String host) {
        IProxyService ips = getProxyService();
        if (ips == null)
            return null;

        for (IProxyData pd : ips.getProxyDataForHost(host)) {
            if (IProxyData.HTTP_PROXY_TYPE.equals(pd.getType())) {
                return ProxyInfo.forHttpProxy(pd.getHost(), pd.getPort(),
                    pd.getUserId(), pd.getPassword());
            } else if (IProxyData.SOCKS_PROXY_TYPE.equals(pd.getType())) {
                return ProxyInfo.forSocks5Proxy(pd.getHost(), pd.getPort(),
                    pd.getUserId(), pd.getPassword());
            }
        }

        return null;
    }

    /**
     * Disconnects (if currently connected)
     * 
     * @blocking
     * 
     * @post this.myjid == null && this.connection == null &&
     *       this.connectionState == ConnectionState.NOT_CONNECTED
     */
    public void disconnect() {
        if (isConnected()) {
            setConnectionState(ConnectionState.DISCONNECTING, null);

            disconnectInternal();

            setConnectionState(ConnectionState.NOT_CONNECTED, null);
        }
        this.myJID = null;

        // Make a sanity check on the connection and connection state
        if (this.connectionState != ConnectionState.NOT_CONNECTED) {
            log.warn("Connection state is out of sync");
            this.connectionState = ConnectionState.NOT_CONNECTED;
        }
        if (this.connection != null) {
            log.warn("Connection has not been closed");
            this.connection = null;
        }
    }

    protected void disconnectInternal() {
        if (connection != null) {
            try {
                connection.removeConnectionListener(smackConnectionListener);
                connection.disconnect();
            } catch (RuntimeException e) {
                log.warn("Could not disconnect old XMPPConnection: ", e);
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Creates the given account on the given Jabber server.
     * 
     * @blocking
     * 
     * @param server
     *            the server on which to create the account.
     * @param username
     *            the username for the new account.
     * @param password
     *            the password for the new account.
     * @param monitor
     *            the progressmonitor for the operation.
     * @throws XMPPException
     *             exception that occurs while registering.
     */
    public void createAccount(String server, String username, String password,
        IProgressMonitor monitor) throws XMPPException {

        monitor.beginTask("Registering account", 3);

        try {
            XMPPConnection connection = new XMPPConnection(server);
            monitor.worked(1);

            connection.connect();
            monitor.worked(1);

            Registration registration = null;
            try {
                registration = XMPPUtil.getRegistrationInfo(username,
                    connection);
            } catch (XMPPException e) {
                log.error("Server " + server + " does not support XEP-0077"
                    + " (In-Band Registration) properly:", e);
            }
            if (registration != null) {
                if (registration.getAttributes().containsKey("registered")) {
                    throw new XMPPException("Account " + username
                        + " already exists on server");
                }
                if (!registration.getAttributes().containsKey("username")) {
                    String instructions = registration.getInstructions();
                    if (instructions != null) {
                        throw new XMPPException(
                            "Registration via Saros not possible. Please follow these instructions:\n"
                                + instructions);
                    } else {
                        throw new XMPPException(
                            "Registration via Saros not supported by Server. Please see the server web site for informations for how to create an account");
                    }
                }
            }
            monitor.worked(1);

            AccountManager manager = connection.getAccountManager();
            manager.createAccount(username, password);
            monitor.worked(1);

            connection.disconnect();
        } finally {
            monitor.done();
        }
    }

    /**
     * Adds given contact to the roster.
     * 
     * @blocking
     * 
     * @param jid
     *            the Jabber ID of the contact.
     * @param nickname
     *            the nickname under which the new contact should appear in the
     *            roster.
     * @param groups
     *            the groups to which the new contact should belong to. This
     *            information will be saved on the server.
     * @param monitor
     *            a SubMonitor to report progress to
     * @throws XMPPException
     *             is thrown if no connection is established or an error
     *             occurred when adding the user to the roster (which does not
     *             mean that the user really exists on the server)
     */
    public void addContact(JID jid, String nickname, String[] groups,
        SubMonitor monitor) throws XMPPException {

        monitor.beginTask("Adding contact " + jid + " to Roster..", 2);

        try {
            assertConnection();

            monitor.worked(1);

            // if roster already contains user with this jid, throw an exception
            if (connection.getRoster().contains(jid.toString())) {
                monitor.worked(1);

                throw new XMPPException("RosterEntry for user " + jid
                    + " already exists");
            }
            /*
             * if user is trying to add himself, throw exception since there is
             * a strange behaviour if he does (he appears as not using Saros)
             */
            if (jid.equals(getMyJID())) {
                monitor.worked(1);
                throw new XMPPException(
                    "You can't add yourself to your own roster.");
            }
            monitor.worked(1);

            connection.getRoster()
                .createEntry(jid.toString(), nickname, groups);
        } finally {
            monitor.done();
        }
    }

    /**
     * Given an XMPP Exception this method will return whether the exception
     * thrown by isJIDonServer indicates that the server does not support
     * ServiceDisco.<br>
     * <br>
     * In other words: If isJIDonServer throws an Exception and this method
     * returns true on the exception, then we should call addContact anyway.
     * 
     * @return true, if the exception occurred because the server does not
     *         support ServiceDiscovery
     */
    public static boolean isDiscoFailedException(XMPPException e) {

        /* feature-not-implemented */
        if (e.getMessage().contains("501"))
            return true;

        /* service-unavailable */
        if (e.getMessage().contains("503"))
            return true;

        return false;
    }

    /**
     * Returns whether the given JID can be found on the server.
     * 
     * @blocking
     * @cancelable
     * 
     * @param monitor
     *            a SubMonitor to report progress to
     * @throws XMPPException
     *             if the service disco failed. Use isDiscoFailedException to
     *             figure out, whether this might mean that the server does not
     *             support disco at all.
     */
    public boolean isJIDonServer(JID jid, SubMonitor monitor)
        throws XMPPException {
        monitor.beginTask("Performing Service Discovery on JID " + jid, 2);

        ServiceDiscoveryManager sdm = ServiceDiscoveryManager
            .getInstanceFor(connection);
        monitor.worked(1);

        if (monitor.isCanceled())
            throw new CancellationException();

        try {
            boolean discovered = sdm.discoverInfo(jid.toString())
                .getIdentities().hasNext();
            /*
             * discovery does not change any state, if the user wanted to cancel
             * it, we can do that even after the execution finished
             */
            if (monitor.isCanceled())
                throw new CancellationException();
            return discovered;
        } finally {
            monitor.done();
        }
    }

    /**
     * Removes given contact from the roster.
     * 
     * @blocking
     * 
     * @param rosterEntry
     *            the contact that is to be removed
     * @throws XMPPException
     *             is thrown if no connection is established.
     */
    public void removeContact(RosterEntry rosterEntry) throws XMPPException {
        assertConnection();
        this.connection.getRoster().removeEntry(rosterEntry);
    }

    public boolean isConnected() {
        return this.connectionState == ConnectionState.CONNECTED;
    }

    /**
     * @return the current state of the connection.
     */
    public ConnectionState getConnectionState() {
        return this.connectionState;
    }

    /**
     * @return an error string that contains the error message for the current
     *         connection error if the state is {@link ConnectionState#ERROR} or
     *         <code>null</code> if there is another state set.
     */
    public Exception getConnectionError() {
        return this.connectionError;
    }

    /**
     * @return the currently established connection or <code>null</code> if
     *         there is none.
     */
    public XMPPConnection getConnection() {
        return this.connection;
    }

    public void addListener(IConnectionListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeListener(IConnectionListener listener) {
        this.listeners.remove(listener);
    }

    protected void assertConnection() throws XMPPException {
        if (!isConnected()) {
            throw new XMPPException("No connection");
        }
    }

    /**
     * Sets a new connection state and notifies all connection listeners.
     */
    protected void setConnectionState(ConnectionState state, Exception error) {

        this.connectionState = state;
        this.connectionError = error;

        // Prefix the name of the user for which the state changed
        String prefix = "";
        if (connection != null) {
            String user = connection.getUser();
            if (user != null)
                prefix = Util.prefix(new JID(user));
        }

        if (error == null) {
            log.debug(prefix + "New Connection State == " + state);
        } else {
            log.error(prefix + "New Connection State == " + state, error);
        }

        for (IConnectionListener listener : this.listeners) {
            try {
                listener.connectionStateChanged(this.connection, state);
            } catch (RuntimeException e) {
                log.error("Internal error in setConnectionState:", e);
            }
        }
    }

    protected void setupLoggers() {
        try {
            log = Logger.getLogger("de.fu_berlin.inf.dpp");

            PropertyConfigurator.configureAndWatch("log4j.properties",
                REFRESH_SECONDS * 1000);

        } catch (SecurityException e) {
            System.err.println("Could not start logging:");
            e.printStackTrace();
        }
    }

    protected class XMPPConnectionListener implements ConnectionListener {

        public void connectionClosed() {
            // self inflicted, controlled disconnect
            setConnectionState(ConnectionState.NOT_CONNECTED, null);
        }

        public void connectionClosedOnError(Exception e) {

            log.error("XMPP Connection Error: ", e);

            if (e.toString().equals("stream:error (conflict)")) {

                disconnect();

                Util.runSafeSWTSync(log, new Runnable() {
                    public void run() {
                        MessageDialog.openError(
                            EditorAPI.getShell(),
                            "Connection error",
                            "You have been disconnected from Jabber, because of a resource conflict.\n"
                                + "This indicates that you might have logged on again using the same Jabber account"
                                + " and XMPP resource, for instance using Saros or an other instant messaging client.");
                    }
                });
                return;
            }

            // Only try to reconnect if we did achieve a connection...
            if (getConnectionState() != ConnectionState.CONNECTED)
                return;

            setConnectionState(ConnectionState.ERROR, e);

            disconnectInternal();

            Util.runSafeAsync(log, new Runnable() {
                public void run() {

                    Map<JID, Integer> expectedSequenceNumbers = Collections
                        .emptyMap();
                    if (sessionManager.getSarosSession() != null) {
                        expectedSequenceNumbers = sessionManager
                            .getSarosSession().getSequencer()
                            .getExpectedSequenceNumbers();
                    }

                    // HACK Improve this hack to stop an infinite reconnect
                    int i = 0;
                    final int CONNECTION_RETRIES = 15;

                    while (!isConnected() && i++ < CONNECTION_RETRIES) {

                        try {
                            log.info("Reconnecting...("
                                + InetAddress.getLocalHost().toString() + ")");

                            connect(true);
                            if (!isConnected())
                                Thread.sleep(5000);

                        } catch (InterruptedException e) {
                            log.error("Code not designed to be interruptable",
                                e);
                            Thread.currentThread().interrupt();
                            return;
                        } catch (UnknownHostException e) {
                            log.info("Could not get localhost, maybe the network interface is down.");
                        }
                    }

                    if (isConnected()) {
                        sessionManager.onReconnect(expectedSequenceNumbers);
                        log.debug("XMPP reconnected");
                    }
                }
            });
        }

        public void reconnectingIn(int seconds) {
            // TODO maybe using Smack reconnect is better
            assert false : "Reconnecting is disabled";
            // setConnectionState(ConnectionState.CONNECTING, null);
        }

        public void reconnectionFailed(Exception e) {
            // TODO maybe using Smack reconnect is better
            assert false : "Reconnecting is disabled";
            // setConnectionState(ConnectionState.ERROR, e.getMessage());
        }

        public void reconnectionSuccessful() {
            // TODO maybe using Smack reconnect is better
            assert false : "Reconnecting is disabled";
            // setConnectionState(ConnectionState.CONNECTED, null);
        }
    }

    /**
     * Returns a string representing the Saros Version number for instance
     * "9.5.7.r1266"
     * 
     * This method only returns a valid version string after the plugin has been
     * started.
     * 
     * This is equivalent to the bundle version.
     */
    public String getVersion() {
        return sarosVersion;
    }

    /**
     * Returns the configuration setting for multi driver support
     * 
     * @return the state of the feature as <code> boolean</code>
     */
    // TODO move to PreferenceUtils
    public boolean getMutliDriverEnabled() {
        return getPreferenceStore()
            .getBoolean(PreferenceConstants.MULTI_DRIVER);
    }

    /**
     * Returns the configuration setting for enabled auto follow mode
     * 
     * @return the state of the feature as <code> boolean</code>
     */
    // TODO move to PreferenceUtils
    public boolean getAutoFollowEnabled() {
        return getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_FOLLOW_MODE);
    }

    /**
     * Returns the configuration setting for enabled follow exclusive driver
     * 
     * @return the state of the feature as <code> boolean</code>
     */
    // TODO move to PreferenceUtils
    public boolean getFollowExclusiveDriverEnabled() {
        return getPreferenceStore().getBoolean(
            PreferenceConstants.FOLLOW_EXCLUSIVE_DRIVER);
    }

}
