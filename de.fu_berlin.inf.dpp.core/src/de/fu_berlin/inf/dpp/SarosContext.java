package de.fu_berlin.inf.dpp;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
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
 * Represents the application context. All components created in this context
 * are tied to the lifetime of the application, i.e the components are only
 * created once.
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
         * Ensure to use the caching characteristic otherwise we would create
         * multiple instances of components that should be only present once in
         * the context.
         */

        PicoBuilder picoBuilder = new PicoBuilder(new CompositeInjection(
            new ConstructorInjection(), new AnnotatedFieldInjection()))
            .withCaching().withLifecycle();

        if (componentMonitor != null)
            picoBuilder = picoBuilder.withMonitor(componentMonitor);

        container = picoBuilder.build();

        for (ISarosContextFactory factory : factories)
            factory.createComponents(container);

        container.addComponent(ISarosContext.class, this);

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
