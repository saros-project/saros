package saros.context;

import java.io.File;
import java.util.List;
import org.apache.log4j.Logger;
import saros.account.XMPPAccountStore;
import saros.communication.extensions.ActivitiesExtension;
import saros.communication.extensions.CancelInviteExtension;
import saros.communication.extensions.CancelProjectNegotiationExtension;
import saros.communication.extensions.ConnectionEstablishedExtension;
import saros.communication.extensions.InfoExchangeExtension;
import saros.communication.extensions.InvitationAcceptedExtension;
import saros.communication.extensions.InvitationAcknowledgedExtension;
import saros.communication.extensions.InvitationCompletedExtension;
import saros.communication.extensions.InvitationOfferingExtension;
import saros.communication.extensions.InvitationParameterExchangeExtension;
import saros.communication.extensions.JoinSessionRejectedExtension;
import saros.communication.extensions.JoinSessionRequestExtension;
import saros.communication.extensions.KickUserExtension;
import saros.communication.extensions.LeaveSessionExtension;
import saros.communication.extensions.PingExtension;
import saros.communication.extensions.PongExtension;
import saros.communication.extensions.ProjectNegotiationMissingFilesExtension;
import saros.communication.extensions.ProjectNegotiationOfferingExtension;
import saros.communication.extensions.StartActivityQueuingRequest;
import saros.communication.extensions.StartActivityQueuingResponse;
import saros.communication.extensions.UserFinishedProjectNegotiationExtension;
import saros.communication.extensions.UserListExtension;
import saros.communication.extensions.UserListReceivedExtension;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.XMPPConnectionService;
import saros.repackaged.picocontainer.ComponentMonitor;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.repackaged.picocontainer.PicoBuilder;
import saros.repackaged.picocontainer.PicoContainer;
import saros.repackaged.picocontainer.injectors.AnnotatedFieldInjection;
import saros.repackaged.picocontainer.injectors.CompositeInjection;
import saros.repackaged.picocontainer.injectors.ConstructorInjection;
import saros.repackaged.picocontainer.injectors.Reinjector;

/**
 * @JTourBusStop 5, Some Basics:
 *
 * <p>If you haven't already read about PicoContainer, stop and do so now
 * (http://picocontainer.com/introduction.html).
 *
 * <p>Saros uses PicoContainer to manage dependencies on our behalf. The ContainerContext class
 * encapsulates our usage of PicoContainer. It's a well documented class, so take a look at it.
 */

/**
 * Represents the default context. It is up to the client to decide the lifetime of this context.
 *
 * <p>All components that are created and initialized in this context are only created once.
 *
 * <p><b>Note:</b> The <code>create</code> and <code>dispose</code> methods are not thread safe.
 *
 * <p><b>Restriction:</b> Instantiating the context <b>multiple</b> times is only allowed by using
 * different class loaders !
 *
 * @author pcordes
 * @author srossbach
 */
public class ContainerContext implements IContainerContext {

  private static final Logger log = Logger.getLogger(ContainerContext.class);

  private static final String SAROS_DATA_DIRECTORY = ".saros";

  private static final String SAROS_XMPP_ACCOUNT_FILE = "config.dat";

  private final List<IContextFactory> factories;
  /** A caching container which holds all the singletons in Saros. */
  private final MutablePicoContainer container;

  private boolean initialized;
  private boolean disposed;

  public ContainerContext(
      final List<IContextFactory> factories, final ComponentMonitor componentMonitor) {
    this.factories = factories;

    /*
     * Ensure to use the caching characteristic otherwise we would create
     * multiple instances of components that should be only present once in
     * the context.
     */

    PicoBuilder builder =
        new PicoBuilder(
                new CompositeInjection(new ConstructorInjection(), new AnnotatedFieldInjection()))
            .withCaching()
            .withLifecycle();

    if (componentMonitor != null) builder = builder.withMonitor(componentMonitor);

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
      // Info exchange extension used outside of Session
      Class.forName(InfoExchangeExtension.class.getName());

      // Session negotiation extensions
      Class.forName(CancelInviteExtension.class.getName());
      Class.forName(InvitationOfferingExtension.class.getName());
      Class.forName(InvitationParameterExchangeExtension.class.getName());
      Class.forName(InvitationAcknowledgedExtension.class.getName());
      Class.forName(InvitationAcceptedExtension.class.getName());
      Class.forName(InvitationCompletedExtension.class.getName());
      Class.forName(ConnectionEstablishedExtension.class.getName());

      // Project negotiation extensions
      Class.forName(CancelProjectNegotiationExtension.class.getName());
      Class.forName(ProjectNegotiationOfferingExtension.class.getName());
      Class.forName(ProjectNegotiationMissingFilesExtension.class.getName());

      // General session extensions
      Class.forName(ActivitiesExtension.class.getName());
      Class.forName(KickUserExtension.class.getName());
      Class.forName(UserListExtension.class.getName());
      Class.forName(LeaveSessionExtension.class.getName());
      Class.forName(UserListReceivedExtension.class.getName());
      Class.forName(StartActivityQueuingRequest.class.getName());
      Class.forName(StartActivityQueuingResponse.class.getName());
      Class.forName(UserFinishedProjectNegotiationExtension.class.getName());

      // Session extensions for Timeout-Handling during a session
      Class.forName(PingExtension.class.getName());
      Class.forName(PongExtension.class.getName());

      // Server extensions
      Class.forName(JoinSessionRequestExtension.class.getName());
      Class.forName(JoinSessionRejectedExtension.class.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Initialize this context and instantiates all components created by the context factories. Does
   * nothing if called more than once.
   *
   * @see #dispose()
   */
  public void initialize() {

    if (initialized) return;

    log.info("initializing context...");

    for (IContextFactory factory : factories) factory.createComponents(container);

    container.addComponent(IContainerContext.class, this);

    initAccountStore(container.getComponent(XMPPAccountStore.class));

    installPacketExtensionProviders();

    XMPPUtils.setDefaultConnectionService(container.getComponent(XMPPConnectionService.class));

    /*
     * ensure all components are instantiated as some of them may have not
     * dependencies at all and so would never be instantiated although they
     * do critical work, e.g listening to several events
     */
    final List<Object> components = container.getComponents();

    if (log.isDebugEnabled()) {
      for (final Object component : components) {
        log.debug("created context component: " + component.getClass().getName());
      }
    }

    container.start();
    initialized = true;

    log.info("successfully initialized context");
  }

  /**
   * Disposes this context by disposing all components in this context. After the context is
   * disposed it can no longer be used. Does nothing if the context is not initialized yet or
   * already disposed.
   */
  public void dispose() {

    if (!initialized || disposed) return;

    log.info("disposing context...");
    container.stop();
    disposed = true;
    container.dispose();
    log.info("successfully disposed context");
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

        if (directory.exists() && directory.isDirectory()) homeDirectory = userHome;
      }
    }

    if (homeDirectory == null) homeDirectory = System.getProperty("user.home");

    if (homeDirectory == null) {
      log.warn("home directory not set, cannot save and load account data");
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
    if (!initialized) throw new IllegalStateException("context is not initialized yet");

    if (disposed) throw new IllegalStateException("context is disposed");

    final MutablePicoContainer reinjectionContainer = container.makeChildContainer();

    try {
      reinjectionContainer.addComponent(component.getClass(), component);
      new Reinjector(reinjectionContainer)
          .reinject(component.getClass(), new AnnotatedFieldInjection());
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
  public MutablePicoContainer createChildContainer() {
    return container.makeChildContainer();
  }
}
