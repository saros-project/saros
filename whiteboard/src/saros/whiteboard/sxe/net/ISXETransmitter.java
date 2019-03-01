package saros.whiteboard.sxe.net;

import java.io.IOException;
import org.eclipse.core.runtime.IProgressMonitor;
import saros.whiteboard.sxe.SXEController;
import saros.whiteboard.sxe.constants.SXEMessageType;

/**
 * Interface to offer some abstraction to the network layer.
 *
 * @author jurke
 */
public interface ISXETransmitter {

  /**
   * Send asynchronously a message to the peer provided by the SXEMessage
   *
   * @param msg
   */
  /*
   * note: has to be send asynchronously because should keep the GUI busy for
   * too long
   */
  public void sendAsync(SXEMessage msg);

  /**
   * Send a message to the peer provided by the SXEMessage and wait until the requested response has
   * arrived from this peer.
   *
   * @blocking
   * @param monitor
   * @param msg
   * @param awaitFor
   * @return the response message or <code>null</code> if the operation was canceled
   */
  public SXEMessage sendAndAwait(
      IProgressMonitor monitor, SXEMessage msg, SXEMessageType... awaitFor) throws IOException;

  // public SXEMessage receive(SubMonitor monitor, SXESession session, String
  // peer, SXEMessageType... awaitFor) throws IOException,
  // LocalCancellationException;

  /** registers the controller to receive records */
  public void installRecordReceiver(final SXEController controller);
}
