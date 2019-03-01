package saros.session.internal.timeout;

import org.apache.log4j.Logger;
import org.picocontainer.Startable;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.session.internal.ActivitySequencer;
import saros.session.internal.IActivitySequencerCallback;
import saros.util.ThreadUtils;

/**
 * Abstract base class that is already capable of detecting and handling network errors occurred in
 * the {@link ActivitySequencer} component.
 *
 * @author srossbach
 */
abstract class SessionTimeoutHandler implements Startable {

  private static final Logger LOG = Logger.getLogger(SessionTimeoutHandler.class);

  /** Join timeout when stopping this component */
  protected static final long TIMEOUT = 10000L;

  /**
   * Total timeout in milliseconds to remove a user(host) or stop the session(client) if no ping or
   * pong packet is received.
   */
  protected static final long PING_PONG_TIMEOUT =
      Long.getLong("saros.session.timeout.PING_PONG_TIMEOUT", 60L * 1000L * 5L);

  /** Update interval for sending and / or checking the status of ping and pong packets. */
  protected static final long PING_PONG_UPDATE_DELAY =
      Long.getLong("saros.session.timeout.PING_PONG_UPDATE_DELAY", 30000L);

  /** Current session the component is run with. */
  protected final ISarosSession session;

  protected final ISarosSessionManager sessionManager;

  protected final ITransmitter transmitter;
  protected final IReceiver receiver;

  /** Current id of the session. */
  protected final String currentSessionID;

  private final ActivitySequencer sequencer;

  private final IActivitySequencerCallback callback =
      new IActivitySequencerCallback() {
        @Override
        public void transmissionFailed(final JID jid) {
          handleNetworkError(jid, "tx");
        }
      };

  protected SessionTimeoutHandler(
      ISarosSession session,
      ISarosSessionManager sessionManager,
      ActivitySequencer sequencer,
      ITransmitter transmitter,
      IReceiver receiver) {
    this.session = session;
    this.sessionManager = sessionManager;
    this.sequencer = sequencer;
    this.transmitter = transmitter;
    this.receiver = receiver;
    this.currentSessionID = session.getID();
  }

  @Override
  public void start() {
    sequencer.setCallback(callback);
  }

  @Override
  public void stop() {
    sequencer.setCallback(null);
  }

  /**
   * Handles a network error by either stopping the session or removing the user from the session
   * depending on the state of the local user. This method returns immediately and performs its work
   * in the background.
   *
   * @param jid the {@linkplain JID} of the user
   * @param reason a reason why a network error occurred
   */
  protected final void handleNetworkError(final JID jid, final String reason) {

    String threadName = reason == null ? "" : reason;

    if (session.isHost()) {
      ThreadUtils.runSafeAsync(
          "dpp-kill-user-" + jid.getName() + "-" + threadName,
          LOG,
          new Runnable() {
            @Override
            public void run() {
              User user = session.getUser(jid);
              if (user != null) session.removeUser(user);
            }
          });
    } else {
      ThreadUtils.runSafeAsync(
          "dpp-kill-session-" + threadName,
          LOG,
          new Runnable() {
            @Override
            public void run() {
              sessionManager.stopSession(SessionEndReason.CONNECTION_LOST);
            }
          });
    }
  }
}
