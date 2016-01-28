package de.fu_berlin.inf.dpp;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.annotations.Inject;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.CompositeInjection;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.Reinjector;

import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.extensions.ActivitiesExtension;
import de.fu_berlin.inf.dpp.communication.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.communication.extensions.CancelProjectNegotiationExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationCompletedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationOfferingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationParameterExchangeExtension;
import de.fu_berlin.inf.dpp.communication.extensions.JoinSessionRejectedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.JoinSessionRequestExtension;
import de.fu_berlin.inf.dpp.communication.extensions.KickUserExtension;
import de.fu_berlin.inf.dpp.communication.extensions.LeaveSessionExtension;
import de.fu_berlin.inf.dpp.communication.extensions.PingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.PongExtension;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationMissingFilesExtension;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationOfferingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.SessionStatusRequestExtension;
import de.fu_berlin.inf.dpp.communication.extensions.SessionStatusResponseExtension;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingRequest;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.communication.extensions.UserFinishedProjectNegotiationExtension;
import de.fu_berlin.inf.dpp.communication.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.communication.extensions.UserListReceivedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.VersionExchangeExtension;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * Encapsulates a {@link org.picocontainer.PicoContainer} and its Saros-specific
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
 * @author pcordes
 * @author srossbach
 */
public class SarosContext implements ISarosContext {

    private static final Logger log = Logger.getLogger(SarosContext.class);

    private static final String SAROS_DATA_DIRECTORY = ".saros";

    private static final String SAROS_XMPP_ACCOUNT_FILE = "config.dat";

    private final ComponentMonitor componentMonitor;

    private final List<ISarosContextFactory> factories;
    /**
     * A caching container which holds all the singletons in Saros.
     */
    private MutablePicoContainer container;

    /**
     * The reinjector used to inject dependencies into those objects that are
     * created by Eclipse and not by our PicoContainer.
     */
    private Reinjector reinjector;

    public SarosContext(final List<ISarosContextFactory> factories,
        final ComponentMonitor componentMonitor) {
        this.factories = factories;
        this.componentMonitor = componentMonitor;
        init();
    }

    private void installPacketExtensionProviders() {

        /* *
         * 
         * @JTourBusStop 6, Creating custom network messages, Installing the
         * provider:
         * 
         * This should be straight forward. Follow the code pattern below.
         */

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
            Class.forName(ProjectNegotiationOfferingExtension.class.getName());
            Class.forName(ProjectNegotiationMissingFilesExtension.class
                .getName());
            Class.forName(KickUserExtension.class.getName());
            Class.forName(UserListExtension.class.getName());
            Class.forName(LeaveSessionExtension.class.getName());
            Class.forName(UserListReceivedExtension.class.getName());
            Class.forName(PingExtension.class.getName());
            Class.forName(PongExtension.class.getName());

            Class.forName(UserFinishedProjectNegotiationExtension.class
                .getName());

            Class.forName(StartActivityQueuingRequest.class.getName());
            Class.forName(StartActivityQueuingResponse.class.getName());

            Class.forName(VersionExchangeExtension.class.getName());

            Class.forName(JoinSessionRequestExtension.class.getName());
            Class.forName(JoinSessionRejectedExtension.class.getName());
            Class.forName(SessionStatusRequestExtension.class.getName());
            Class.forName(SessionStatusResponseExtension.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {

        log.info("creating Saros runtime context...");
        /*
         * All singletons which exist for the whole plug-in life-cycle are
         * managed by PicoContainer for us.
         */

        PicoBuilder picoBuilder = new PicoBuilder(new CompositeInjection(
            new ConstructorInjection(), new AnnotatedFieldInjection()))
            .withCaching().withLifecycle();

        /*
         * If given, the dotMonitor is used to capture an architecture diagram
         * of the application
         */
        if (componentMonitor != null)
            picoBuilder = picoBuilder.withMonitor(componentMonitor);

        // Initialize our dependency injection container
        container = picoBuilder.build();

        for (ISarosContextFactory factory : factories) {
            factory.createComponents(container);
        }

        container.addComponent(ISarosContext.class, this);

        /*
         * Create a reinjector to allow platform specific stuff to reinject
         * itself into the context.
         */
        reinjector = new Reinjector(container);

        initAccountStore(container.getComponent(XMPPAccountStore.class));

        installPacketExtensionProviders();

        XMPPUtils.setDefaultConnectionService(container
            .getComponent(XMPPConnectionService.class));

        log.info("successfully created Saros runtime context");
    }

    private void initAccountStore(XMPPAccountStore store) {
        // see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4787931

        String os = System.getProperty("os.name");

        boolean isWindows = os != null && os.toLowerCase().contains("windows");

        String homeDirectory = null;

        if (isWindows) {
            String userHome = System.getenv("USERPROFILE");

            if (userHome != null) {
                File directory = new File(userHome);

                if (directory.exists() && directory.isDirectory())
                    homeDirectory = userHome;
            }
        }

        if (homeDirectory == null)
            homeDirectory = System.getProperty("user.home");

        if (homeDirectory == null) {
            log.warn("home directory not set, cannot save and load account data");
            return;
        }

        File sarosDataDir = new File(homeDirectory, SAROS_DATA_DIRECTORY);
        File accountFile = new File(sarosDataDir, SAROS_XMPP_ACCOUNT_FILE);

        store.setAccountFile(accountFile, System.getProperty("user.name"));
    }

    /**
     * Adds the object to Saros container, and injects dependencies into the
     * annotated fields of the given object. It should only be used for objects
     * that were created by a third party, which have the same life cycle as the
     * Saros plug-in.
     */
    public synchronized void reinject(final Object component) {
        try {

            Class<?> clazz = component.getClass();
            ComponentAdapter<?> removed = container.removeComponent(clazz);
            if (removed != null) {
                log.warn(clazz.getName() + " added more than once!",
                    new StackTrace());
            }

            container.addComponent(clazz, component);

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
     * method should only be used for objects that cannot be put directly into
     * the context scope, i.e the objects are created by a third party.
     * 
     * @see Inject
     */
    @Override
    public void initComponent(final Object component) {

        final MutablePicoContainer reinjectionContainer = container
            .makeChildContainer();

        try {
            reinjectionContainer.addComponent(component.getClass(), component);
            new Reinjector(reinjectionContainer).reinject(component.getClass(),
                new AnnotatedFieldInjection());
        } finally {
            container.removeChildContainer(reinjectionContainer);
        }
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

    @Override
    public MutablePicoContainer createSimpleChildContainer() {
        return container.makeChildContainer();
    }
}
