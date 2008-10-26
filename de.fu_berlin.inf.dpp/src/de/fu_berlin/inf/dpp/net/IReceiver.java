package de.fu_berlin.inf.dpp.net;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/**
 * The IReceiver contains methods to process incoming packets.
 * 
 * @author orieger
 * 
 */
public interface IReceiver extends PacketListener {

    /**
     * process jupiter request.
     * 
     * @param packet
     */
    public void processRequest(Packet packet);
}
