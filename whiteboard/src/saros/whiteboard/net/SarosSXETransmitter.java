package de.fu_berlin.inf.dpp.whiteboard.net;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.SXEMessageType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.net.ISXETransmitter;
import de.fu_berlin.inf.dpp.whiteboard.sxe.net.SXEIncomingSynchronizationProcess;
import de.fu_berlin.inf.dpp.whiteboard.sxe.net.SXEMessage;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.serializable.RecordDataObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

/**
 * Uses Smack and Saros to establish the SXE communication.</br>
 *
 * <p>It maintains invitation and record listener as well as the extension provider.
 *
 * @author jurke
 */
public class SarosSXETransmitter implements ISXETransmitter {

  /** Max. waiting time for responses from other whiteboard instances */
  private static final long SXE_TIMEOUT = 30000L;

  /**
   * Interval to poll receiving
   *
   * @see XMPPTransmitter#receive(SubMonitor, PacketCollector, long, boolean)
   */
  private static final long SXE_TIMEOUT_INTERVAL = 500L;

  public static final Logger log = Logger.getLogger(SarosSXETransmitter.class);

  /* we don't want to block the GUI for sending */
  protected ExecutorService sendingDispatch =
      Executors.newSingleThreadExecutor(new NamedThreadFactory("Whiteboard-SXESending-Dispatch-"));

  private final SXEExtensionProvider provider = SXEExtensionProvider.getInstance();

  @Inject private ITransmitter transmitter;

  @Inject private IReceiver receiver;

  private final ISarosSession sarosSession;

  public SarosSXETransmitter(ISarosSession sarosSession) {
    SarosPluginContext.initComponent(this);
    this.sarosSession = sarosSession;
  }

  private PacketListener invitationListener;

  private PacketListener recordListener;

  @Override
  public void installRecordReceiver(final SXEController controller) {

    recordListener =
        new PacketListener() {
          @Override
          public void processPacket(Packet packet) {
            final SXEExtension extension =
                (SXEExtension) packet.getExtension(SXEMessage.SXE_TAG, SXEMessage.SXE_XMLNS);
            extension.getMessage().setFrom(packet.getFrom());
            /*
             * TODO consider the lastModifiedBy field: if not sent in the
             * record it may be set here like the sender
             */
            setSender(extension.getMessage().getRecords(), packet.getFrom());
            SWTUtils.runSafeSWTAsync(
                log,
                new Runnable() {

                  @Override
                  public void run() {
                    controller.executeRemoteRecords(extension.getMessage());
                  }
                });
          }
        };

    receiver.addPacketListener(
        recordListener, provider.getRecordsPacketFilter(controller.getSession()));
  }

  @Override
  public void sendAsync(final SXEMessage msg) {
    sendingDispatch.submit(
        ThreadUtils.wrapSafe(
            log,
            new Runnable() {

              @Override
              public void run() {
                sendWithoutDispatch(msg);
              }
            }));
  }

  protected void sendWithoutDispatch(SXEMessage msg) {
    SXEExtension extension = new SXEExtension();
    extension.setMessage(msg);

    try {
      if (msg.getTo() == null) {
        for (User u : sarosSession.getRemoteUsers()) {
          transmitter.send(u.getJID(), extension);
        }
      } else {
        JID jid = new JID(msg.getTo());
        transmitter.send(jid, extension);
      }
    } catch (Exception e) {
      log.error(prefix() + "sending whiteboard message failed because of an network error", e);
    }
  }

  @Override
  public synchronized SXEMessage sendAndAwait(
      IProgressMonitor monitor, SXEMessage msg, SXEMessageType... awaitFor) throws IOException {

    log.debug(
        prefix()
            + "send "
            + msg.getMessageType()
            + " to "
            + msg.getTo()
            + " waiting for "
            + Arrays.toString(awaitFor));

    Packet packet;

    PacketFilter filter = new SXEPacketFilter(msg.getSession(), msg.getTo(), awaitFor);

    PacketCollector collector = receiver.createCollector(filter);

    try {
      sendWithoutDispatch(msg);
      long startTime = System.currentTimeMillis();

      do {
        packet = collector.nextResult(SXE_TIMEOUT_INTERVAL);

        if (monitor.isCanceled()) return null;

        if (System.currentTimeMillis() - startTime > SXE_TIMEOUT)
          throw new IOException("received no response from " + msg.getTo());

      } while (packet == null);

    } finally {
      collector.cancel();
    }

    SXEMessage response =
        ((SXEExtension) packet.getExtension(SXEMessage.SXE_TAG, SXEMessage.SXE_XMLNS)).getMessage();
    response.setFrom(msg.getTo());

    return response;
  }

  //
  // @Override
  // public SXEMessage receive(SubMonitor monitor, SXESession session, String
  // peer,
  // SXEMessageType... awaitFor) throws IOException,
  // LocalCancellationException {
  //
  // PacketFilter filter = new SXEPacketFilter(session, peer, awaitFor);
  // SarosPacketCollector collector = transmitter.installReceiver(filter);
  //
  // Packet packet = transmitter.receive(monitor, collector, SXE_TIMEOUT,
  // true);
  //
  // SXEExtension extension =
  // (SXEExtension)packet.getExtension(SXEMessage.SXE_TAG,SXEMessage.SXE_XMLNS);
  // extension.getMessage().setFrom(peer);
  //
  // return extension.getMessage();
  // }

  /**
   * Registers the controller to receive state-offer messages and to start the invitation process
   */
  public void enableInvitation(final SXEController controller) {

    invitationListener =
        new PacketListener() {
          @Override
          public void processPacket(Packet packet) {
            SXEExtension extension =
                (SXEExtension) packet.getExtension(SXEMessage.SXE_TAG, SXEMessage.SXE_XMLNS);
            extension.getMessage().setFrom(packet.getFrom());

            SXEIncomingSynchronizationProcess inv =
                new SXEIncomingSynchronizationProcess(
                    controller, SarosSXETransmitter.this, extension.getMessage());

            inv.start();
          }
        };

    receiver.addPacketListener(invitationListener, provider.getInvitationPacketFilter());
  }

  protected void setSender(List<RecordDataObject> rdos, String sender) {
    for (RecordDataObject rdo : rdos) rdo.setSenderIfAbsent(sender);
  }

  /** Don's receive anymore records */
  public void disconnect() {
    receiver.removePacketListener(recordListener);
  }

  /** remove the extension provider and invitation */
  public void dispose() {
    disconnect();
    receiver.removePacketListener(invitationListener);
    sendingDispatch.shutdown();
  }

  protected String prefix() {
    return "SXE ";
  }
}
