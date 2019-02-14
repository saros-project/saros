package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.net.internal.BinaryXMPPExtension;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;

/**
 * An interface for intercepting packages that are about to send or dispatched via the {@link
 * DataTransferManager}
 */
public interface IPacketInterceptor {

  /**
   * This method is called before the {@link DataTransferManager} is dispatching the packet.
   *
   * @param extension
   * @return <code>true</code> if the packet should be dispatched, <code>false</code> if the packet
   *     should be dropped
   */
  public boolean receivedPacket(BinaryXMPPExtension extension);

  /**
   * This method is called before the {@link DataTransferManager} is sending the packet.
   *
   * @param connectID
   * @param description
   * @param payload
   * @return <code>true</code> if the packet should be send, <code>false</code> if the packet should
   *     be dropped
   */
  public boolean sendPacket(String connectID, TransferDescription description, byte[] payload);
}
