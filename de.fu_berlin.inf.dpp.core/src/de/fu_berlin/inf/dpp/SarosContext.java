package de.fu_berlin.inf.dpp;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
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

/**
 * @JTourBusStop 5, Some Basics:
 * 
 *               If you haven't already read about PicoContainer, stop and
 *               do so now (http://picocontainer.com/introduction.html).
 * 
 *               Saros uses PicoContainer to manage dependencies on our
 *               behalf. The SarosContext class encapsulates our usage of
 *               PicoContainer. It's a well documented class, so take a look
 *               at it.
 */

/**
 * Represents the application context. All components that are created and
 * initialized in this context are tied to the lifetime of the application, i.e
 * the components are only created once.
 * <p>
 * <b>Note:</b> The <code>create</code> and <code>dispose</code> methods are not
 * thread safe.
 * 
 * @author pcordes
 * @author srossbach
 */
public class SarosContext implements ISarosContext {

    private static final Logger LOG = Logger.getLogger(SarosContext.class);

    private static final String SAROS_DATA_DIRECTORY = ".saros";

    private static final String SAROS_XMPP_ACCOUNT_FILE = "config.dat";

    private final List<ISarosContextFactory> factories;
    /**
     * A caching container which holds all the singletons in Saros.
     */
    private final MutablePicoContainer container;

    private boolean initialized;
    private boolean disposed;

    public SarosContext(final List<ISarosContextFactory> factories,
        final ComponentMonitor componentMonitor) {
        this.factories = factories;

        /*
         * Ensure to use the caching characteristic otherwise we would create
         * multiple instances of components that should be only present once in
         * the context.
         */

        PicoBuilder builder = new PicoBuilder(new CompositeInjection(
            new ConstructorInjection(), new AnnotatedFieldInjection()))
            .withCaching().withLifecycle();

        if (componentMonitor != null)
            builder = builder.withMonitor(componentMonitor);

        container = builder.build();
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

    /**
     * Initialize this context and instantiates all components created by the
     * context factories. Does nothing if called more than once.
     * 
     * @see #dispose()
     */
    public void initialize() {

        if (initialized)
            return;

        LOG.info("initializing application context...");

        for (ISarosContextFactory factory : factories)
            factory.createComponents(container);

        container.addComponent(ISarosContext.class, this);

        initAccountStore(container.getComponent(XMPPAccountStore.class));

        installPacketExtensionProviders();

        XMPPUtils.setDefaultConnectionService(container
            .getComponent(XMPPConnectionService.class));

        /*
         * ensure all components are instantiated as some of them may have not
         * dependencies at all and so would never be instantiated although they
         * do critical work, e.g listening to several events
         */
        final List<Object> components = container.getComponents();

        if (LOG.isDebugEnabled()) {
            for (final Object component : components) {
                LOG.debug("created context component: "
                    + component.getClass().getName());
            }
        }

        initialized = true;

        LOG.info("successfully initialized application context");
    }

    /**
     * Disposes this context by disposing all components in this context. After
     * the context is disposed it can no longer be used. Does nothing if the
     * context is not initialized yet or already disposed.
     */
    public void dispose() {

        if (!initialized || disposed)
            return;

        LOG.info("disposing application context...");
        disposed = true;
        container.dispose();
        LOG.info("successfully disposed application context");
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
            LOG.warn("home directory not set, cannot save and load account data");
            return;
        }

        File sarosDataDir = new File(homeDirectory, SAROS_DATA_DIRECTORY);
        File accountFile = new File(sarosDataDir, SAROS_XMPP_ACCOUNT_FILE);

        store.setAccountFile(accountFile, System.getProperty("user.name"));
    }

    @Override
    @Deprecated
    public void initComponent(final Object component) {

        /*
         * it is unlikely that this method is called while creating or disposing
         * the context so this is sufficient for now
         */
        if (!initialized)
            throw new IllegalStateException("context is not initialized yet");

        if (disposed)
            throw new IllegalStateException("context is disposed");

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
    public <T> T getComponent(Class<T> clazz) {
        return container.getComponent(clazz);
    }

    @Override
    public boolean removeChildContainer(PicoContainer picoContainer) {
        return container.removeChildContainer(picoContainer);
    }

    @Override
    public MutablePicoContainer createSimpleChildContainer() {
        return container.makeChildContainer();
    }
}
