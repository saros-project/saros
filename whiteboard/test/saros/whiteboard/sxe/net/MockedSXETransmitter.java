package saros.whiteboard.sxe.net;

import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xmlpull.v1.XmlPullParserException;
import saros.ui.util.SWTUtils;
import saros.whiteboard.sxe.SXEController;
import saros.whiteboard.sxe.constants.SXEMessageType;
import saros.whiteboard.sxe.records.serializable.RecordDataObject;

public class MockedSXETransmitter implements ISXETransmitter {

  public static final Logger log = Logger.getLogger(MockedSXETransmitter.class);

  private final SXENetworkMock network;
  private SXEController localController;

  private final SXEMessageReader reader = new SXEMessageReader();
  private final SXEMessageWriter writer = new SXEMessageWriter();

  public MockedSXETransmitter(SXENetworkMock network) {
    this.network = network;
  }

  @Override
  public void sendAsync(SXEMessage msg) {
    String raw = writer.getSXEMessageAsString(msg);
    network.sendMessage(raw, msg.getTo());
  }

  @Override
  public SXEMessage sendAndAwait(
      IProgressMonitor monitor, SXEMessage msg, SXEMessageType... awaitFor) throws IOException {
    // TODO incorporate to enable proper invitation testing
    throw new UnsupportedOperationException();
  }

  @Override
  public void installRecordReceiver(SXEController controller) {
    localController = controller;
  }

  public void receiveRecords(String msg, String from) {

    try {
      SXEMessage message;
      message = reader.parseMessage(msg);
      message.setFrom(from);

      setSender(message.getRecords(), from);

      switch (message.getMessageType()) {
        case RECORDS:
          this.handleReceiveRecords(message);
          break;
        case STATE:
          this.handleReceiveState(message);
          break;
        default:
          // nothing
      }

    } catch (XmlPullParserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected void handleReceiveRecords(final SXEMessage message) {
    SWTUtils.runSafeSWTAsync(
        log,
        new Runnable() {

          @Override
          public void run() {
            localController.executeRemoteRecords(message);
          }
        });
  }

  protected void handleReceiveState(final SXEMessage message) {
    SWTUtils.runSafeSWTAsync(
        log,
        new Runnable() {

          @Override
          public void run() {
            localController.startSession(message);
          }
        });
  }

  protected void setSender(List<RecordDataObject> rdos, String sender) {
    for (RecordDataObject rdo : rdos) rdo.setSenderIfAbsent(sender);
  }
}
