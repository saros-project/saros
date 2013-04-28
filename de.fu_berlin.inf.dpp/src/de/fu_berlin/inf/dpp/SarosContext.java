package de.fu_berlin.inf.dpp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Version;
import org.osgi.service.prefs.Preferences;
import org.picocontainer.BindKey;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.annotations.Bind;
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
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatService;
import de.fu_berlin.inf.dpp.communication.chat.muc.negotiation.MUCSessionPreferencesNegotiatingManager;
import de.fu_berlin.inf.dpp.communication.chat.single.SingleUserChatService;
import de.fu_berlin.inf.dpp.concurrent.undo.UndoManager;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogServer;
import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.concurrent.watchdog.SessionViewOpener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.colorstorage.ColorIDSetStorage;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.business.CancelInviteHandler;
import de.fu_berlin.inf.dpp.net.business.CancelProjectSharingHandler;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.business.InvitationHandler;
import de.fu_berlin.inf.dpp.net.business.LeaveAndKickHandler;
import de.fu_berlin.inf.dpp.net.discoverymanager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.ConnectionTestManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.IBBTransport;
import de.fu_berlin.inf.dpp.net.internal.ITransport;
import de.fu_berlin.inf.dpp.net.internal.Socks5Transport;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.extensions.ActivitiesExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelProjectNegotiationExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.FileListExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationCompletedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationOfferingExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationParameterExchangeExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.KickUserExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.SarosLeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListReceivedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListRequestExtension;
import de.fu_berlin.inf.dpp.net.stun.internal.StunServiceImpl;
import de.fu_berlin.inf.dpp.net.subscriptionmanager.SubscriptionManager;
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
import de.fu_berlin.inf.dpp.project.SarosRosterListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.ChecksumCacheImpl;
import de.fu_berlin.inf.dpp.project.internal.FileContentNotifierBridge;
import de.fu_berlin.inf.dpp.project.internal.FollowingActivitiesManager;
import de.fu_berlin.inf.dpp.synchronize.internal.SWTSynchronizer;
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

    public static class Bindings {
        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.FIELD, ElementType.PARAMETER })
        @Bind
        public @interface IBBTransport {
            // marker interface
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.FIELD, ElementType.PARAMETER })
        @Bind
        public @interface Socks5Transport {
            // marker interface
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.FIELD, ElementType.PARAMETER })
        @Bind
        public @interface SarosVersion {
            // marker interface
        }

    }

    private static class Component {
        private Class<?> clazz;
        private Object instance;
        private Object bindKey;

        private Component(Object bindKey, Class<?> clazz, Object instance) {
            this.bindKey = bindKey;
            this.clazz = clazz;
            this.instance = instance;
        }

        public static Component create(Object bindKey, Class<?> clazz) {
            return new Component(bindKey, clazz, null);
        }

        public static Component create(Class<?> clazz) {
            return new Component(clazz, clazz, null);
        }

        public static <T> Component create(Class<T> clazz, T instance) {
            return new Component(clazz, clazz, instance);
        }

        public static <T> Component create(Object bindKey, Class<T> clazz,
            T instance) {
            return new Component(bindKey, clazz, instance);
        }

        public Object getBindKey() {
            return bindKey;
        }

        public Object getImplementation() {
            return instance != null ? instance : clazz;
        }

        @Override
        public boolean equals(Object obj) {
            return ((obj instanceof Component) && ((Component) obj).bindKey == this.bindKey);
        }

        @Override
        public int hashCode() {
            return bindKey.hashCode();
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
        Component.create(MultiUserChatService.class),
        Component.create(SingleUserChatService.class),
        Component.create(PreferenceUtils.class),
        Component.create(SarosUI.class),
        Component.create(SarosSessionManager.class),
        Component.create(SessionViewOpener.class),
        Component.create(AudioServiceManager.class),
        Component.create(MixerManager.class),
        Component.create(UndoManager.class),
        Component.create(VideoSharing.class),
        Component.create(MUCSessionPreferencesNegotiatingManager.class),
        Component.create(RemoteProgressManager.class),
        Component.create(XMPPAccountStore.class),
        Component.create(ColorIDSetStorage.class),

        // Network
        Component.create(ConnectionTestManager.class),
        Component.create(DataTransferManager.class),
        Component.create(DiscoveryManager.class),

        Component.create(
            BindKey.bindKey(ITransport.class, Bindings.IBBTransport.class),
            IBBTransport.class),

        Component.create(
            BindKey.bindKey(ITransport.class, Bindings.Socks5Transport.class),
            Socks5Transport.class),

        Component.create(RosterTracker.class),
        Component.create(SarosNet.class),
        Component.create(SarosRosterListener.class),
        Component.create(SkypeManager.class),
        Component.create(StreamServiceManager.class),
        Component.create(StunServiceImpl.class),
        Component.create(SubscriptionManager.class),
        Component.create(UPnPServiceImpl.class),
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
        Component.create(InvitationHandler.class),
        Component.create(LeaveAndKickHandler.class),

        // FIXME: remove all extensions providers here !

        // Extension Providers
        Component
            .create(IncomingTransferObject.IncomingTransferObjectExtensionProvider.class),

        // UI handlers
        Component.create(HostLeftAloneInSessionHandler.class),

        // streaming services
        Component.create(FileStreamService.class),
        Component.create(AudioService.class),
        Component.create(VideoSharingService.class),

        // Cache support
        Component.create(ChecksumCacheImpl.class, new ChecksumCacheImpl(
            new FileContentNotifierBridge())),

        // Version support
        Component.create(VersionManager.class),

        // SWT EDT support
        Component.create(SWTSynchronizer.class)

    };

    /*
     * Use the SarosContextBuilder to build a SarosContext. {@link
     * SarosContextBuilder}
     */

    private SarosContext(Saros saros, DotGraphMonitor dotGraphMonitor) {

        this.saros = saros;
        this.dotMonitor = dotGraphMonitor;
        init();
    }

    private void installPacketExtensionProviders() {
        /*
         * The packet extensions must be loaded here so they are added to the
         * Smack ExtensionProvider at context startup.
         */

        try {
            Class.forName(ActivitiesExtension.class.getName());
            Class.forName(CancelInviteExtension.class.getName());
            Class.forName(InvitationOfferingExtension.class.getName());
            Class.forName(InvitationParameterExchangeExtension.class.getName());
            Class.forName(InvitationAcknowledgedExtension.class.getName());
            Class.forName(InvitationAcceptedExtension.class.getName());
            Class.forName(InvitationCompletedExtension.class.getName());
            Class.forName(CancelProjectNegotiationExtension.class.getName());
            Class.forName(FileListExtension.class.getName());
            Class.forName(KickUserExtension.class.getName());
            Class.forName(UserListExtension.class.getName());
            Class.forName(SarosLeaveExtension.class.getName());
            Class.forName(UserListRequestExtension.class.getName());
            Class.forName(UserListReceivedExtension.class.getName());

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {

        installPacketExtensionProviders();

        PicoBuilder picoBuilder = new PicoBuilder(new CompositeInjection(
            new ConstructorInjection(), new AnnotatedFieldInjection()))
            .withCaching().withLifecycle();

        /*
         * If given, the dotMonitor is used to capture an architecture diagram
         * of the application
         */
        if (dotMonitor != null)
            picoBuilder = picoBuilder.withMonitor(dotMonitor);

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

        container.addComponent(BindKey.bindKey(Version.class,
            Bindings.SarosVersion.class), saros.getBundle().getVersion());

        container.addComponent(IPreferenceStore.class,
            saros.getPreferenceStore());

        container
            .addComponent(ISecurePreferences.class, saros.getSecurePrefs());

        container.addComponent(Preferences.class, saros.getConfigPrefs());

        Set<Component> contextComponents = new HashSet<Component>(
            Arrays.asList(COMPONENTS));

        for (Component component : contextComponents)
            container.addComponent(component.getBindKey(),
                component.getImplementation());

        container.addComponent(ISarosContext.class, this);

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
        reinjector = new Reinjector(container);
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
    @Override
    public synchronized void initComponent(Object toInjectInto) {
        ChildContainer dummyContainer = container
            .getComponent(ChildContainer.class);
        dummyContainer.reinject(toInjectInto);
        container.removeChildContainer(dummyContainer);
    }

    @Override
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

    @Override
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

    @Override
    public MutablePicoContainer createSimpleChildContainer() {
        return container.makeChildContainer();
    }
}
