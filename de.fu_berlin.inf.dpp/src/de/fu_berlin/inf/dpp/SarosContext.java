package de.fu_berlin.inf.dpp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.Preferences;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.CompositeInjection;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.injectors.Reinjector;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.communication.SkypeManager;
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
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.business.ActivitiesHandler;
import de.fu_berlin.inf.dpp.net.business.CancelInviteHandler;
import de.fu_berlin.inf.dpp.net.business.CancelProjectSharingHandler;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.business.InvitationHandler;
import de.fu_berlin.inf.dpp.net.business.LeaveHandler;
import de.fu_berlin.inf.dpp.net.business.UserListHandler;
import de.fu_berlin.inf.dpp.net.discoverymanager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.ConnectionTestManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.UserListRequestExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.IBBTransport;
import de.fu_berlin.inf.dpp.net.internal.InvitationInfo;
import de.fu_berlin.inf.dpp.net.internal.Socks5Transport;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.net.internal.UserListInfo;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelProjectSharingExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.LeaveExtension;
import de.fu_berlin.inf.dpp.net.stun.IStunService;
import de.fu_berlin.inf.dpp.net.stun.internal.StunServiceImpl;
import de.fu_berlin.inf.dpp.net.subscriptionmanager.SubscriptionManager;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.net.upnp.internal.UPnPServiceImpl;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.VideoSessionObservable;
import de.fu_berlin.inf.dpp.observables.VoIPSessionObservable;
import de.fu_berlin.inf.dpp.optional.jdt.JDTFacade;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.IChecksumCache;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.SarosRosterListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.ChecksumCacheImpl;
import de.fu_berlin.inf.dpp.project.internal.FileContentNotifierBridge;
import de.fu_berlin.inf.dpp.project.internal.FollowingActivitiesManager;
import de.fu_berlin.inf.dpp.ui.LocalPresenceTracker;
import de.fu_berlin.inf.dpp.ui.RemoteProgressManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.eventhandler.HostLeftAloneInSessionHandler;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.pico.ChildContainer;
import de.fu_berlin.inf.dpp.util.pico.ChildContainerProvider;
import de.fu_berlin.inf.dpp.util.pico.DotGraphMonitor;
import de.fu_berlin.inf.dpp.util.sendfile.FileStreamService;
import de.fu_berlin.inf.dpp.videosharing.VideoSharing;
import de.fu_berlin.inf.dpp.videosharing.VideoSharingService;

/**
 * Encapsulates a {@link org.picocontainer.PicoContainer} and its saros-specific
 * initialization. Basically it's used to get or reinject components in the
 * context:
 * 
 * {@link de.fu_berlin.inf.dpp.SarosContext#getComponent(Class)},
 * {@link de.fu_berlin.inf.dpp.SarosContext#reinject(Object)}
 * 
 * These methods change the context respectively the PicoContainer!
 * 
 * If you want to initialize a component with the components of the context
 * without changing the context you can use the method
 * {@link de.fu_berlin.inf.dpp.SarosContext#initComponent(Object)}.
 * 
 * @author philipp.cordes
 * @author Stefan Rossbach
 */
public class SarosContext implements ISarosContext {

    private static class Component {
        private Class<?> intf;
        private Class<?> clazz;
        private Object instance;

        private Component(Class<?> intf, Class<?> clazz, Object instance) {
            this.intf = intf;
            this.clazz = clazz;
            this.instance = instance;
        }

        public static <T> Component create(Class<T> intf,
            Class<? extends T> clazz) {
            return new Component(intf, clazz, null);
        }

        public static Component create(Class<?> clazz) {
            return new Component(clazz, clazz, null);
        }

        public static <T> Component create(Class<T> intf,
            Class<? extends T> clazz, Object instance) {
            return new Component(intf, clazz, instance);
        }

        public Class<?> getInterface() {
            return intf;
        }

        public Object getImplementation() {
            return instance != null ? instance : clazz;
        }

        @Override
        public boolean equals(Object obj) {
            return ((obj instanceof Component) && ((Component) obj).intf == this.intf);
        }

        @Override
        public int hashCode() {
            return intf.hashCode();
        }
    }

    private static final Logger log = Logger.getLogger(SarosContext.class);

    private DotGraphMonitor dotMonitor;

    /**
     * A caching container which holds all the singletons in Saros.
     */
    private MutablePicoContainer container;

    /**
     * The reinjector used to inject dependencies into those objects that are
     * created by Eclipse and not by our PicoContainer.
     */
    private Reinjector reinjector;

    /**
     * Because many components which are included in the pico-container need
     * saros
     */
    private Saros saros;

    private static final Component[] COMPONENTS = new Component[] {
        // Thread Context
        Component.create(DispatchThreadContext.class),

        // Core Managers
        Component.create(ConsistencyWatchdogClient.class),
        Component.create(ConsistencyWatchdogServer.class),
        Component.create(EditorAPI.class),
        Component.create(EditorManager.class),
        Component.create(JDTFacade.class),
        Component.create(LocalPresenceTracker.class),
        Component.create(MUCManager.class),
        Component.create(MUCManagerSingletonWrapperChatView.class),
        Component.create(PreferenceUtils.class),
        Component.create(SarosUI.class),
        Component.create(ISarosSessionManager.class, SarosSessionManager.class),
        Component.create(SessionViewOpener.class),
        Component.create(AudioServiceManager.class),
        Component.create(MixerManager.class),
        Component.create(UndoManager.class),
        Component.create(VideoSharing.class),
        Component.create(VersionManager.class),
        Component.create(MUCSessionPreferencesNegotiatingManager.class),
        Component.create(RemoteProgressManager.class),
        Component.create(XMPPAccountStore.class),

        // Network
        Component.create(ConnectionTestManager.class),
        Component.create(DataTransferManager.class),
        Component.create(DiscoveryManager.class),
        Component.create(IBBTransport.class),
        Component.create(RosterTracker.class),
        Component.create(SarosNet.class),
        Component.create(SarosRosterListener.class),
        Component.create(SkypeManager.class),
        Component.create(Socks5Transport.class),
        Component.create(StreamServiceManager.class),
        Component.create(IStunService.class, StunServiceImpl.class),
        Component.create(SubscriptionManager.class),
        Component.create(IUPnPService.class, UPnPServiceImpl.class),
        Component.create(XMPPReceiver.class),
        Component.create(XMPPTransmitter.class),

        // Observables
        Component.create(FileReplacementInProgressObservable.class),
        Component.create(InvitationProcessObservable.class),
        Component.create(ProjectNegotiationObservable.class),
        Component.create(IsInconsistentObservable.class),
        Component.create(SessionIDObservable.class),
        Component.create(SarosSessionObservable.class),
        Component.create(VoIPSessionObservable.class),
        Component.create(VideoSessionObservable.class),
        Component.create(AwarenessInformationCollector.class),
        Component.create(FollowingActivitiesManager.class),

        // Handlers
        Component.create(CancelInviteHandler.class),
        Component.create(CancelProjectSharingHandler.class),
        Component.create(UserListHandler.class),
        Component.create(InvitationHandler.class),
        Component.create(LeaveHandler.class),
        Component.create(ActivitiesHandler.class),

        // Extensions
        Component.create(CancelInviteExtension.class),
        Component.create(CancelProjectSharingExtension.class),
        Component.create(LeaveExtension.class),

        // Extension Providers
        Component.create(ActivitiesExtensionProvider.class),
        Component.create(InvitationInfo.InvitationExtensionProvider.class),
        Component
            .create(IncomingTransferObject.IncomingTransferObjectExtensionProvider.class),
        Component
            .create(DefaultInvitationInfo.InvitationAcknowledgementExtensionProvider.class),
        Component.create(UserListInfo.JoinExtensionProvider.class),
        Component
            .create(DefaultInvitationInfo.UserListConfirmationExtensionProvider.class),
        Component
            .create(DefaultInvitationInfo.InvitationCompleteExtensionProvider.class),
        Component.create(UserListRequestExtensionProvider.class),

        Component.create(HostLeftAloneInSessionHandler.class),

        // streaming services
        Component.create(FileStreamService.class),
        Component.create(AudioService.class),
        Component.create(VideoSharingService.class),

        // cache support

        Component.create(IChecksumCache.class, ChecksumCacheImpl.class,
            new ChecksumCacheImpl(new FileContentNotifierBridge())), };

    /*
     * Use the SarosContextBuilder to build a SarosContext. {@link
     * SarosContextBuilder}
     */

    private SarosContext(Saros saros, DotGraphMonitor dotGraphMonitor) {

        this.saros = saros;
        this.dotMonitor = dotGraphMonitor;
        init();
    }

    private void init() {
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
        container = picoBuilder.build();

        // Add Adapter which creates ChildContainers
        container.as(Characteristics.NO_CACHE).addAdapter(
            new ProviderAdapter(new ChildContainerProvider(this.container)));
        /*
         * All singletons which exist for the whole plug-in life-cycle are
         * managed by PicoContainer for us.
         * 
         * The addComponent() calls are sorted alphabetically according to the
         * first argument. This makes it easier to search for a class without
         * tool support.
         */

        container.addComponent(Saros.class, saros);

        container.addComponent(IPreferenceStore.class,
            saros.getPreferenceStore());

        container
            .addComponent(ISecurePreferences.class, saros.getSecurePrefs());

        container.addComponent(Preferences.class, saros.getConfigPrefs());

        Set<Component> contextComponents = new HashSet<Component>(
            Arrays.asList(COMPONENTS));

        for (Component component : contextComponents)
            container.addComponent(component.getInterface(),
                component.getImplementation());

        // add this context itself because some components need it ...
        container.addComponent(SarosContext.class, this);

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

    /**
     * Adds the object to Saros' container, and injects dependencies into the
     * annotated fields of the given object. It should only be used for objects
     * that were created by Eclipse, which have the same life cycle as the Saros
     * plug-in, e.g. the popup menu actions.
     */
    public synchronized void reinject(Object toInjectInto) {
        try {
            // Remove the component if an instance of it was already registered
            Class<?> clazz = toInjectInto.getClass();
            ComponentAdapter<?> removed = container.removeComponent(clazz);
            if (removed != null && clazz != Saros.class) {
                log.warn(clazz.getName() + " added more than once!",
                    new StackTrace());
            }

            // Add the given instance to the container
            container.addComponent(clazz, toInjectInto);

            /*
             * Ask PicoContainer to inject into the component via fields
             * annotated with @Inject
             */
            reinjector.reinject(clazz, new AnnotatedFieldInjection());
        } catch (PicoCompositionException e) {
            log.error("Internal error in reinjection:", e);
        }
    }

    /**
     * Injects dependencies into the annotated fields of the given object. This
     * method should be used for objects that were created by Eclipse, which
     * have a different life cycle than the Saros plug-in.
     */
    public synchronized void initComponent(Object toInjectInto) {
        ChildContainer dummyContainer = container
            .getComponent(ChildContainer.class);
        dummyContainer.reinject(toInjectInto);
        container.removeChildContainer(dummyContainer);
    }

    public <T> T getComponent(Class<T> tClass) {
        return container.getComponent(tClass);
    }

    public <T> List<T> getComponents(Class<T> tClass) {
        return container.getComponents(tClass);
    }

    public List<Object> getComponents() {
        return container.getComponents();
    }

    public void addComponent(Object o, Object o1, Parameter... parameters) {
        container.addComponent(o, o1, parameters);
    }

    public void removeComponent(Object o) {
        container.removeComponent(o);
    }

    public boolean removeChildContainer(PicoContainer picoContainer) {
        return container.removeChildContainer(picoContainer);
    }

    public void dispose() {
        container.dispose();
    }

    /**
     * Starting point for getting a correct initialized SarosContext.
     */
    public static SarosContextBuilder getContextForSaros(Saros saros) {
        return new SarosContextBuilder(saros);
    }

    /**
     * Builder to create a correct initialized SarosContext.
     */
    public static class SarosContextBuilder {
        private Saros saros;
        private DotGraphMonitor dotMonitor;

        public SarosContextBuilder(Saros saros) {
            this.saros = saros;
        }

        public SarosContextBuilder withDotMonitor(DotGraphMonitor dotMonitor) {
            this.dotMonitor = dotMonitor;
            return this;
        }

        public SarosContext build() {
            return new SarosContext(saros, dotMonitor);
        }
    }

    public MutablePicoContainer createSimpleChildContainer() {
        return container.makeChildContainer();
    }
}
