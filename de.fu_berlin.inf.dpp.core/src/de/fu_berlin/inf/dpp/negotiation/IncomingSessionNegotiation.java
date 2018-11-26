package de.fu_berlin.inf.dpp.negotiation;

import de.fu_berlin.inf.dpp.communication.extensions.ConnectionEstablishedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationCompletedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationParameterExchangeExtension;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.negotiation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.preferences.PreferenceStore;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Packet;

/*
 * IMPORTANT: All messages in the cancellation exception are SHOWN to the end user !
 */
public class IncomingSessionNegotiation extends SessionNegotiation {

  private static Logger LOG = Logger.getLogger(IncomingSessionNegotiation.class);

  private final String remoteVersion;

  private boolean running;

  private PacketCollector invitationDataExchangeCollector;
  private PacketCollector invitationAcknowledgedCollector;

  private final IConnectionManager connectionManager;

  private final String sessionID;

  public IncomingSessionNegotiation( //
      final JID peer, //
      final String negotiationID, //
      final String sessionID, //
      final String remoteVersion, //
      final String description, //
      final ISarosSessionManager sessionManager, //
      final SessionNegotiationHookManager hookManager, //
      final IConnectionManager connectionManager, //
      final ITransmitter transmitter, //
      final IReceiver receiver //
      ) {

    super(negotiationID, peer, description, sessionManager, hookManager, transmitter, receiver);

    this.sessionID = sessionID;
    this.remoteVersion = remoteVersion;

    this.connectionManager = connectionManager;
  }

  /*
   * TODO local/remoteCancel and terminate should not be called inside the
   * monitor
   */
  @Override
  public synchronized boolean remoteCancel(String errorMsg) {
    if (!super.remoteCancel(errorMsg)) return false;

    if (!running) terminate(null);

    return true;
  }

  /*
   * TODO local/remoteCancel and terminate should not be called inside the
   * monitor
   */
  @Override
  public synchronized boolean localCancel(String errorMsg, CancelOption cancelOption) {
    if (!super.localCancel(errorMsg, cancelOption)) return false;

    if (!running) terminate(null);

    return true;
  }

  @Override
  protected void executeCancellation() {
    /*
     * make sure we can receive negotiations again because they are rejected
     * until we explicitly call this method even if we do not start any
     * session at all !
     */
    sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
  }

  /** Returns the Saros version of the remote side. */
  public String getRemoteVersion() {
    return remoteVersion;
  }

  public Status accept(IProgressMonitor monitor) {
    LOG.debug(this + " : invitation accepted");

    monitor.beginTask("Joining session...", IProgressMonitor.UNKNOWN);

    synchronized (this) {
      running = true;
    }

    // the negotiation should not be canceled manually !
    // observeMonitor(monitor);

    Exception exception = null;

    createCollectors();

    try {
      checkCancellation(CancelOption.NOTIFY_PEER);

      /**
       * @JTourBusStop 8, Invitation Process:
       *
       * <p>This method is called by the JoinSessionWizard after the user clicked on "Finish"
       * (indicating that he is willing to join the session).
       *
       * <p>(4b) Send acceptance to host.
       *
       * <p>(5a) Create "wishlist" with session's parameters (e.g. preferred color) and send it.
       *
       * <p>(6b) Wait for host's response.
       *
       * <p>(7) Initialize the session and related components (e.g. chat, color management) with the
       * parameters as defined by the host.
       *
       * <p>(8) Establish a connection to the host (e.g Socks5)
       *
       * <p>(9) Start the session accordingly, inform the host and wait for his final
       * acknowledgement (which indicates, that this client has been successfully added to the
       * session and will receive activities from now on).
       */
      sendInvitationAccepted();

      InvitationParameterExchangeExtension clientSessionPreferences;
      clientSessionPreferences = createClientSessionPreferences();

      sendSessionPreferences(clientSessionPreferences, monitor);

      InvitationParameterExchangeExtension actualSessionParameters;
      actualSessionParameters = awaitActualSessionParameters(monitor);

      initializeSession(actualSessionParameters, monitor);

      /*
       * TODO This is very fragile and needs a better design. We must
       * connect before we start our session otherwise a component that
       * try to immediately send an activity or something over the wire
       * will trigger the ClientSessionTimeoutHandler which will just
       * terminate the session !
       */
      monitor.setTaskName("Establishing connection...");

      connectionManager.connect(ISarosSession.SESSION_CONNECTION_ID, getPeer());

      sendConnectionEstablished();

      startSession(monitor);

      sendInvitationCompleted(monitor);

      awaitFinalAcknowledgement(monitor);
    } catch (Exception e) {
      exception = e;
    } finally {
      monitor.done();
      deleteCollectors();
    }

    return terminate(exception);
  }

  /**
   * Informs the session host that the user has established a connection (e.g Socks5) to the host.
   */
  private void sendConnectionEstablished() {
    LOG.debug(this + " : sending connection established confirmation");

    transmitter.sendPacketExtension(
        getPeer(),
        ConnectionEstablishedExtension.PROVIDER.create(
            new ConnectionEstablishedExtension(getID())));
  }

  /** Informs the session host that the user has accepted the invitation. */
  private void sendInvitationAccepted() {
    LOG.debug(this + " : sending invitation accepted confirmation");

    transmitter.sendPacketExtension(
        getPeer(),
        InvitationAcceptedExtension.PROVIDER.create(new InvitationAcceptedExtension(getID())));
  }

  /**
   * Creates session negotiation data that may contain some default settings that should be
   * recognized by the host during the negotiation.
   */
  private InvitationParameterExchangeExtension createClientSessionPreferences() {
    InvitationParameterExchangeExtension parameters =
        new InvitationParameterExchangeExtension(getID());

    for (ISessionNegotiationHook hook : hookManager.getHooks()) {
      Map<String, String> clientPreferences = hook.tellClientPreferences();
      parameters.saveHookSettings(hook, clientPreferences);
    }

    return parameters;
  }

  /**
   * Sends session negotiation data that should be recognized by the host during the negotiation.
   */
  private void sendSessionPreferences(
      InvitationParameterExchangeExtension parameters, IProgressMonitor monitor) {

    LOG.debug(this + " : sending session negotiation data");

    monitor.setTaskName("Sending session configuration data...");
    transmitter.sendPacketExtension(
        getPeer(), InvitationParameterExchangeExtension.PROVIDER.create(parameters));
  }

  /**
   * Waits for the actual parameters which are sent back by the host. They contain information that
   * must be used on session start.
   */
  private InvitationParameterExchangeExtension awaitActualSessionParameters(
      IProgressMonitor monitor) throws SarosCancellationException {

    LOG.debug(this + " : waiting for host's session negotiation configuration data");

    monitor.setTaskName("Waiting for remote session configuration data...");

    Packet packet = collectPacket(invitationDataExchangeCollector, PACKET_TIMEOUT);

    if (packet == null)
      throw new LocalCancellationException(
          getPeer() + " does not respond. (Timeout)", CancelOption.DO_NOT_NOTIFY_PEER);

    InvitationParameterExchangeExtension parameters;
    parameters = InvitationParameterExchangeExtension.PROVIDER.getPayload(packet);

    if (parameters == null)
      throw new LocalCancellationException(
          getPeer() + " sent malformed data", CancelOption.DO_NOT_NOTIFY_PEER);

    LOG.debug(this + " : received host's session parameters");

    return parameters;
  }

  /**
   * Initializes some components that rely on the session parameters as defined by the host. This
   * includes MUC settings and color settings. Finally the session is created locally (but not
   * started yet).
   */
  private void initializeSession(
      InvitationParameterExchangeExtension parameters, IProgressMonitor monitor) {
    LOG.debug(this + " : initializing session");

    monitor.setTaskName("Initializing session...");

    IPreferenceStore hostPreferences = new PreferenceStore();
    IPreferenceStore clientPreferences = new PreferenceStore();

    for (ISessionNegotiationHook hook : hookManager.getHooks()) {
      Map<String, String> settings = parameters.getHookSettings(hook);
      hook.applyActualParameters(settings, hostPreferences, clientPreferences);

      if (settings == null) continue;
    }

    sarosSession =
        sessionManager.joinSession(
            sessionID, parameters.getSessionHost(), hostPreferences, clientPreferences);
  }

  /** Starts the Saros session */
  private void startSession(IProgressMonitor monitor) {
    LOG.debug(this + " : starting session");

    monitor.setTaskName("Starting session...");

    /*
     * TODO: Wait until all of the activities in the queue (which arrived
     * during the invitation) are processed and notify the host only after
     * that.
     */

    sessionManager.sessionStarting(sarosSession);
    sarosSession.start();
    sessionManager.sessionStarted(sarosSession);

    LOG.debug(this + " : invitation has completed successfully");
  }

  /** Signal the host that the session invitation is complete (from the client's perspective). */
  private void sendInvitationCompleted(IProgressMonitor monitor) throws IOException {
    transmitter.send(
        ISarosSession.SESSION_CONNECTION_ID,
        getPeer(),
        InvitationCompletedExtension.PROVIDER.create(new InvitationCompletedExtension(getID())));

    LOG.debug(this + " : invitation complete confirmation sent");
  }

  /**
   * Wait for the final acknowledgment by the host which indicates that this client has been added
   * to the session and will receive activities from now on. The waiting for the acknowledgment of
   * the host cannot be canceled and therefore the packet timeout should not set be higher than 1
   * minute !
   */
  private void awaitFinalAcknowledgement(IProgressMonitor monitor)
      throws SarosCancellationException {
    monitor.setTaskName("Awaiting current session data...");

    if (collectPacket(invitationAcknowledgedCollector, PACKET_TIMEOUT) == null)
      throw new LocalCancellationException(
          getPeer() + " does not respond. (Timeout)", CancelOption.DO_NOT_NOTIFY_PEER);
  }

  private void createCollectors() {
    invitationAcknowledgedCollector =
        receiver.createCollector(InvitationAcknowledgedExtension.PROVIDER.getPacketFilter(getID()));

    invitationDataExchangeCollector =
        receiver.createCollector(
            InvitationParameterExchangeExtension.PROVIDER.getPacketFilter(getID()));
  }

  private void deleteCollectors() {
    invitationAcknowledgedCollector.cancel();
    invitationDataExchangeCollector.cancel();
  }

  @Override
  public String toString() {
    return "ISN [remote side: " + getPeer() + "]";
  }
}
