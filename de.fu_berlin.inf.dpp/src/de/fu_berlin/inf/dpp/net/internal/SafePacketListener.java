package de.fu_berlin.inf.dpp.net.internal;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.util.Utils;

/**
 * A packet listener which forwards calls to a another PacketListener, but
 * catches all exception which might have occur in the forwarded to
 * PacketListener and prints them to the log given in the constructor.
 * 
 * @pattern Proxy which adds the aspect of "safety"
 */
public class SafePacketListener implements PacketListener {

    /**
     * The {@link PacketListener} to forward all call to which are received by
     * this {@link PacketListener}
     */
    protected PacketListener toForwardTo;

    /**
     * The {@link Logger} to use for printing an error message when a
     * RuntimeException occurs when calling the {@link #toForwardTo}
     * {@link PacketListener}.
     */
    protected Logger log;

    public SafePacketListener(Logger log, PacketListener toForwardTo) {
        this.toForwardTo = toForwardTo;
        this.log = log;
    }

    public void processPacket(final Packet packet) {
        Utils.runSafeSync(log, new Runnable() {
            public void run() {
                toForwardTo.processPacket(packet);
            }
        });
    }

}
