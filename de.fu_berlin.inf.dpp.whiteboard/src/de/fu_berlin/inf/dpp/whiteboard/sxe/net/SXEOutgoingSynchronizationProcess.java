package de.fu_berlin.inf.dpp.whiteboard.sxe.net;

import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.SXEMessageType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.util.SXEUtils;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Base class for the inviter for the synchronization process during invitation.
 *
 * <p>Message flow:</br>
 *
 * <p>- send state-offer</br>
 *
 * <p>- wait for accept-state or refuse-state</br>
 *
 * <p>- if accepted send state </br>
 *
 * @author jurke
 */
public class SXEOutgoingSynchronizationProcess extends SXESynchronization {

  public static final Logger log = Logger.getLogger(SXEOutgoingSynchronizationProcess.class);

  public SXEOutgoingSynchronizationProcess(
      SXEController controller, ISXETransmitter sxe, String to) {
    super(controller, controller.getSession(), to);
  }

  public void start(IProgressMonitor monitor) {
    try {

      monitor.beginTask("Initializing Whiteboard session...", IProgressMonitor.UNKNOWN);

      log.debug(prefix() + " send state-offer to " + peer);

      /* send state-offer and wait for accept-state or refuse-state */

      SXEMessage msg = session.getNextMessage(SXEMessageType.STATE_OFFER, peer);

      SXEMessage answer =
          controller
              .getTransmitter()
              .sendAndAwait(monitor, msg, SXEMessageType.ACCEPT_STATE, SXEMessageType.REFUSE_STATE);

      if (answer == null) {
        log.debug(prefix() + "Whitebaord synchronization canceled locally");
        return;
      }

      if (answer.getMessageType() == SXEMessageType.ACCEPT_STATE) {

        log.debug(prefix() + peer + " accepted state-offer");

        try {
          List<IRecord> state =
              SWTUtils.runSWTSync(
                  new Callable<List<IRecord>>() {

                    @Override
                    public List<IRecord> call() throws Exception {
                      return controller.getDocumentRecord().getState();
                    }
                  });

          /* send state */

          msg = session.getNextMessage(SXEMessageType.STATE, peer);
          msg.setRecords(SXEUtils.toDataObjects(state));

          log.debug(prefix() + "Sending state to " + msg.getTo());

          log.debug("Send message: " + msg);
          controller.getTransmitter().sendAsync(msg);

          /*
           * TODO we might want to send an ack here to confirm that
           * everything went well (i.e. and add the user to the
           * session)
           */
          // sxe.sendAndAwait(monitor, msg, SXEMessageType.ACK_STATE);

        } catch (Exception e) {
          log.error("Error while synchronizing whiteboard state: " + e.getMessage());
        }

      } else if (answer.getMessageType() == SXEMessageType.REFUSE_STATE) {
        log.debug(prefix() + peer + " refused state offer");
      } else
        log.error(
            prefix()
                + "Receveived wrong message while synchronizing whiteboard state: "
                + answer.getMessageType());

    } catch (IOException e) {
      log.error(prefix() + "Timeout while synchronizing whiteboard state: " + e.getMessage());
    } catch (Exception e) {
      log.debug(prefix() + "Unexpected Exception in Whitebaord synchronization: " + e);
    } finally {
      monitor.done();
    }
  }
}
