package de.fu_berlin.inf.dpp.net;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;

/**
 * The IChatManager contains the logic for appropriate chat transfer.
 * 
 * @author orieger
 * 
 */
public interface IChatManager extends PacketListener {

    public void setConnection(XMPPConnection connection, IReceiver receiver);

    public void setReceiver(IReceiver receiver);

    /**
     * status of chat connection.
     * 
     * @return
     */
    public boolean isConnected();

    public void sendRequest(Request request);
}
