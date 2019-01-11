package de.fu_berlin.inf.dpp.whiteboard.sxe.net;

import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.SXEMessageType;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Base class for the invitee for the synchronization process during invitation *
 *
 * <p>Message flow:</br>
 *
 * <p>- send accept-state</br>
 *
 * <p>- wait for state</br>
 *
 * @author jurke
 */
public class SXEIncomingSynchronizationProcess extends SXESynchronization {

  public static final Logger log = Logger.getLogger(SXEIncomingSynchronizationProcess.class);

  public SXEIncomingSynchronizationProcess(
      SXEController controller, ISXETransmitter sxe, SXEMessage stateOfferMessage) {
    super(controller, stateOfferMessage.getSession(), stateOfferMessage.getFrom());
    assert (stateOfferMessage.getMessageType() == SXEMessageType.STATE_OFFER);
  }

  /*
   * TODO: At the moment started after receiving the state-offer, however,
   * should start with connect message after session negotiation see SXE
   * XEP-0284 but Saros used to lack a convenient place to send the connect
   *
   * (non-Javadoc)
   *
   * @see
   * de.fu_berlin.inf.dpp.whiteboard.sxe.net.SXESynchronization#start(org.
   * eclipse.core.runtime.IProgressMonitor)
   */
  public void start() {
    /*
     * Has to run in another thread because else this would block Smack
     * dispatching listener thread when awaiting answers (could not process
     * incoming messages, deadlock)
     */
    ThreadUtils.runSafeAsync(
        "dpp-wb-sync-inc",
        log,
        new Runnable() {

          @Override
          public void run() {
            try {

              log.debug(prefix() + "invitation from " + peer + " received");

              SWTUtils.runSafeSWTSync(
                  log,
                  new Runnable() {

                    @Override
                    public void run() {
                      if (!controller.switchToConnectingState(session)) {
                        log.debug(
                            prefix()
                                + "Received state offer while in "
                                + controller.getState()
                                + " state");
                        SXEMessage msg = session.getNextMessage(SXEMessageType.REFUSE_STATE, peer);
                        controller.getTransmitter().sendAsync(msg);
                        return;
                      }
                    }
                  });

              SXEMessage msg = session.getNextMessage(SXEMessageType.ACCEPT_STATE, peer);

              log.debug(prefix() + "queue incoming records from now");

              final SXEMessage stateMessage =
                  controller
                      .getTransmitter()
                      .sendAndAwait(new NullProgressMonitor(), msg, SXEMessageType.STATE);

              if (stateMessage == null) {
                log.debug(prefix() + "Whitebaord synchronization canceled locally");
                return;
              }

              log.debug(prefix() + "state received");

              SWTUtils.runSafeSWTSync(
                  log,
                  new Runnable() {

                    @Override
                    public void run() {
                      controller.startSession(stateMessage);
                    }
                  });

              // TODO send ack? Note: is not included in SXE

            } catch (IOException e) {
              log.error(
                  prefix() + "Timeout while synchronizing whiteboard state: " + e.getMessage());
            } catch (Exception e) {
              log.debug(prefix() + "Unexpected Exception in Whitebaord synchronization: " + e);
            }
          }
        });
  }

  @Override
  protected String prefix() {
    return "SXE(" + session.getSessionId() + ") ";
  }
}
