package saros.negotiation;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Packet;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.RemoteCancellationException;
import saros.exceptions.SarosCancellationException;
import saros.monitoring.IProgressMonitor;
import saros.negotiation.NegotiationTools.CancelLocation;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.PacketCollector;
import saros.net.xmpp.JID;

/**
 * Abstract base class for implementing specific types of message exchanges within the Saros
 * protocol. It offers a few utility methods, as well as an interface for coordinated cancellation
 * of negotiations.
 *
 * @author srossbach
 */
abstract class Negotiation {

  public enum Status {
    OK,
    CANCEL,
    REMOTE_CANCEL,
    ERROR,
    REMOTE_ERROR
  }

  private static final Logger log = Logger.getLogger(Negotiation.class);

  private final String id;

  // FIMXE make this final (do not obtain the JID during the Negotiation !)
  private JID peer;

  protected final ITransmitter transmitter;

  protected final IReceiver receiver;

  private IProgressMonitor monitorToObserve;

  private boolean isRemoteCancellation;

  private boolean isLocalCancellation;

  private SarosCancellationException cancellationCause;

  private String errorMessage;

  private boolean terminated;

  private volatile NegotiationListener listener;

  private Status exitStatus;

  private final List<CancelListener> cancelListeners = new CopyOnWriteArrayList<CancelListener>();

  /**
   * Creates a Negotiation.
   *
   * @param id unique ID of the negotiation
   * @param peer JID of the peer to negotiate with
   * @param transmitter transmitter used for sending negotiation messages
   * @param receiver receiver used for receiving negotiation messages
   */
  protected Negotiation(
      String id, JID peer, final ITransmitter transmitter, final IReceiver receiver) {
    this.id = id;
    this.peer = peer;
    this.transmitter = transmitter;
    this.receiver = receiver;
  }

  /**
   * Returns the unique ID of this negotiation. It is shared between the local and the remote peer
   * instance of the negotiation.
   *
   * @return negotiation ID
   */
  public String getID() {
    return id;
  }

  /**
   * Returns the JID of the peer with which the negotiation takes place.
   *
   * @return peer JID
   */
  public JID getPeer() {
    return peer;
  }

  /**
   * Changes the peer to negotiate with.
   *
   * @param peer new peer JID
   * @deprecated The peer JID should remain constant during the negotiation. Code using this method
   *     should be changed to find out the correct peer JID before the negotiation starts.
   */
  @Deprecated
  protected void setPeer(JID peer) {
    this.peer = peer;
  }

  /**
   * Sets a {@linkplain NegotiationListener negotiation listener} for the negotiation.
   *
   * @param listener the listener that should be notified
   */
  public final void setNegotiationListener(final NegotiationListener listener) {
    this.listener = listener;
  }

  /**
   * Returns the error message if the exit status of the negotiation was either {@link Status#ERROR}
   * or {@link Status#REMOTE_ERROR}.
   *
   * @return the error message
   */
  public synchronized String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Returns the next packet from a collector.
   *
   * @param collector the collector to monitor
   * @param timeout the amount of time to wait for the next packet (in milliseconds)
   * @return the collected packet or <code>null</code> if no packet was received during the timeout
   *     period
   * @throws SarosCancellationException if the process was canceled
   */
  protected final Packet collectPacket(PacketCollector collector, long timeout)
      throws SarosCancellationException {

    Packet packet = null;

    for (long timeLeft = timeout; timeLeft > 0; timeLeft -= 1000) {
      checkCancellation(CancelOption.NOTIFY_PEER);
      if ((packet = collector.nextResult(1000)) != null) break;
    }

    return packet;
  }

  /**
   * This method is called after {@link #terminate} decides to perform a cleanup because the
   * negotiation was canceled. Implementing classes should try a maximum effort to revert all the
   * changes that were made before the negotiation was aborted.
   */
  protected abstract void executeCancellation();

  /**
   * This method is called after {@link #terminate} decides to cancel the negotiation. It is up to
   * the implementing class to forward this notification.
   *
   * @param cancellationCause the cause of the cancellation
   */
  protected abstract void notifyCancellation(SarosCancellationException cancellationCause);

  /**
   * Registers a monitor which should be observed to determine the status of a local cancellation of
   * the negotiation.
   *
   * @param monitor the monitor to observer
   * @see #isLocalCancellation
   * @see #checkCancellation(CancelOption)
   */
  protected final synchronized void observeMonitor(IProgressMonitor monitor) {
    monitorToObserve = monitor;
  }

  /**
   * Checks the current cancellation status of this negotiation. If a local cancellation request is
   * detected this method will invoke {@link #localCancel(String errorMessage, CancelOption
   * cancelOption)} with <code>null</code> as errorMessage argument.
   *
   * @param cancelOption the cancel option to use when a local cancellation was set
   * @throws SarosCancellationException if the negotiation should be canceled
   */
  protected final synchronized void checkCancellation(CancelOption cancelOption)
      throws SarosCancellationException {

    if (isLocalCancellation()) localCancel(null, cancelOption);
    else if (!isRemoteCancellation()) return;

    assert (cancellationCause != null);

    throw cancellationCause;
  }

  /**
   * Informs the negotiation that it should cancel its operation as soon as possible. Calling this
   * method multiple times will <b>NOT</b> override the error message or the cancel option. This
   * method will also have <b>NO</b> effect if a remote cancellation request was already executed.
   *
   * @param errorMessage the reason to cancel the execution in case of an error or <code>null</code>
   *     if this is a normal cancel request
   * @param cancelOption either {@link CancelOption#NOTIFY_PEER} to inform the remote side of the
   *     cancellation or {@link CancelOption#DO_NOT_NOTIFY_PEER}
   * @return <code>true</code> if this request was the first cancel request, <code>false</code>
   *     otherwise
   * @see #remoteCancel
   * @see #checkCancellation
   * @see #notifyCancellation
   */
  public boolean localCancel(String errorMessage, CancelOption cancelOption) {
    synchronized (this) {
      if (!cancel(new LocalCancellationException(errorMessage, cancelOption))) return false;

      isLocalCancellation = true;
    }

    log.debug(
        "negotiation "
            + this
            + " was canceled by the local side, error: "
            + (errorMessage == null ? "none" : errorMessage));

    notifyCancellationListeners(CancelLocation.LOCAL, errorMessage);
    return true;
  }

  /**
   * Informs the negotiation that it should cancel its operation as soon as possible. Calling this
   * method multiple times will <b>NOT</b> override the error message. This method will also have
   * <b>NO</b> effect if a local cancellation request was already executed.
   *
   * @param errorMessage the reason to cancel the execution in case of an error or <code>null</code>
   *     if this is a cancel abort request
   * @return <code>true</code> if this request was the first cancel request, <code>false</code>
   *     otherwise
   * @see #localCancel
   * @see #checkCancellation
   * @see #notifyCancellation
   */
  public boolean remoteCancel(String errorMessage) {
    synchronized (this) {
      if (!cancel(new RemoteCancellationException(errorMessage))) return false;

      isRemoteCancellation = true;
    }

    log.debug(
        "negotiation "
            + this
            + " was canceled by the remote side, error: "
            + (errorMessage == null ? "none" : errorMessage));

    notifyCancellationListeners(CancelLocation.REMOTE, errorMessage);

    return true;
  }

  /**
   * Adds the given listener for cancellation events to this negotiation.
   *
   * @param listener the listener to add
   */
  public final void addCancelListener(final CancelListener listener) {
    cancelListeners.add(listener);
  }

  /**
   * Removes the given listener for cancellation events from this negotiation.
   *
   * @param listener the listener to remove
   */
  public final void removeCancelListener(final CancelListener listener) {
    cancelListeners.add(listener);
  }

  /**
   * Returns if this negotiation should be canceled.
   *
   * @return <code>true</code> this negotiation should be canceled, <code>false</code> otherwise
   */
  public final synchronized boolean isCanceled() {
    return isRemoteCancellation || isLocalCancellation;
  }

  /**
   * Returns if the negotiation should be canceled because of a local cancellation request
   *
   * @return <code>true</code> if cancellation is requested on the local side, <code>false</code>
   *     otherwise
   */
  public final synchronized boolean isLocalCancellation() {
    return !isRemoteCancellation
        && ((monitorToObserve != null && monitorToObserve.isCanceled()) || isLocalCancellation);
  }

  /**
   * Returns if the negotiation should be canceled because of a remote cancellation request.
   *
   * @return <code>true</code> if cancellation is requested on the remote side, <code>false</code>
   *     otherwise
   */
  public final synchronized boolean isRemoteCancellation() {
    return isRemoteCancellation;
  }

  /**
   * Terminates the negotiation. This method may be called multiple times but only the <b>first</b>
   * call will be taken into account. If the negotiation was canceled in the meantime it will invoke
   * {@link #notifyCancellation} and {@link #executeCancellation} in this order.
   *
   * @param exception The exception to analyze or <code>null</code>. If the negotiation had already
   *     been canceled by a {@link #localCancel} or a {@link #remoteCancel} call the exception will
   *     be ignored. If the exception is <code>null</code> then the exit status will be determined
   *     on former {@link #localCancel} or {@link #remoteCancel} calls.
   * @return the {@link Status} of the termination
   */
  protected final Status terminate(Exception exception) {

    final SarosCancellationException cause;
    final Status status;

    synchronized (this) {
      Status lastExitStatus = null;

      // allow multiple calls to log exceptions
      if (terminated) lastExitStatus = exitStatus;

      exitStatus = Status.OK;

      if (exception == null) exception = cancellationCause;

      String error = exception == null ? null : exception.getMessage();

      if (exception instanceof LocalCancellationException) {
        localCancel(
            exception.getMessage(), ((LocalCancellationException) exception).getCancelOption());

        exitStatus = error == null ? Status.CANCEL : Status.ERROR;
      } else if (exception instanceof RemoteCancellationException) {
        remoteCancel(exception.getMessage());
        exitStatus = error == null ? Status.REMOTE_CANCEL : Status.REMOTE_ERROR;

      } else if (exception instanceof IOException) {
        log.error(this + " I/O error occurred", exception);

        String errorMsg = "I/O failure";

        if (exception.getMessage() != null) errorMsg += ": " + exception.getMessage();

        localCancel(errorMsg, CancelOption.NOTIFY_PEER);
        exitStatus = Status.ERROR;
      } else if (exception != null) {
        log.error(this + " unknown error", exception);
        String errorMsg = "Unknown error: " + exception;

        if (exception.getMessage() != null) errorMsg = exception.getMessage();

        localCancel(errorMsg, CancelOption.NOTIFY_PEER);
        exitStatus = Status.ERROR;
      }

      if (terminated) {
        exitStatus = lastExitStatus;
        return exitStatus;
      }

      if (exitStatus != Status.OK) errorMessage = generateErrorMessage();

      status = exitStatus;
      cause = cancellationCause;
      terminated = true;
    }

    /*
     * must notify the listener here or otherwise calling
     * SessionManager.stopSession in the executeCancellation method will
     * block because the SessionManager will wait until the negotiation is
     * terminated (which would not be the case at this moment)
     */

    final NegotiationListener currentListener = listener;

    if (currentListener != null) notifyTerminated(listener);

    assert status != null;

    if (status != Status.OK) {
      assert cause != null;
      notifyCancellation(cause);
      log.debug("executing cancellation for negotiation " + this);
      executeCancellation();
    }

    log.debug("negotiation " + this + " exit status: " + status);
    return exitStatus;
  }

  /**
   * Informs the listener, that the negotiation is terminated. Otherwise, the SessionManager would
   * block the execution and wait until the negotiation is terminated
   *
   * @param listener to notify
   */
  protected abstract void notifyTerminated(NegotiationListener listener);

  /**
   * @return <code>true</code> if this was the first processed cancellation, <code>false</code>
   *     otherwise
   */
  private synchronized boolean cancel(SarosCancellationException cause) {
    if (monitorToObserve != null) monitorToObserve.setCanceled(true);

    if (isCanceled()) return false;

    cancellationCause = cause;

    return true;
  }

  // TODO: move to UI
  private synchronized String generateErrorMessage() {
    String generatedErrorMessage = null;

    if (cancellationCause == null) return null;

    assert (cancellationCause != null);

    String exceptionMessage = cancellationCause.getMessage();

    if (cancellationCause instanceof LocalCancellationException) {
      if (exceptionMessage != null) {
        generatedErrorMessage =
            "Invitation was canceled locally" + " because of an error: " + exceptionMessage;
        log.error("canceled negotiation " + this + ", error: " + exceptionMessage);
      } else {
        log.debug("negotiation " + this + " was canceled manually by the local user");
      }

    } else if (cancellationCause instanceof RemoteCancellationException) {
      if (exceptionMessage != null) {
        generatedErrorMessage =
            "Invitation was canceled by the remote user "
                + " because of an error on his/her side: "
                + exceptionMessage;

        log.error(
            "canceled negotiation "
                + this
                + " because the remote side encountered an error: "
                + exceptionMessage);

      } else {
        log.debug("negotiation " + this + " was canceled manually by the remote side");
      }
    } else {
      log.error(
          "unexpected exception: " + cancellationCause.getClass().getName(), cancellationCause);
    }

    return generatedErrorMessage;
  }

  private final void notifyCancellationListeners(
      final CancelLocation location, final String message) {
    for (final CancelListener cancelListener : cancelListeners)
      cancelListener.canceled(location, message);
  }
}
