package de.fu_berlin.inf.dpp.net;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.fu_berlin.inf.dpp.net.packet.Packet;
import de.fu_berlin.inf.dpp.net.packet.PacketType;

/**
 * TODO IFSPEC
 * 
 * @author Stefan Rossbach
 */
public interface IPacketDispatcher {

    public enum Priority {

        LOWEST(-3),

        LOWER(-2),

        LOW(-1),

        NORMAL(0),

        HIGH(1),

        HIGHER(2),

        HIGHEST(3);

        private final int priority;

        private Priority(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    public void dispatch(final Packet packet);

    public void dispatchAndWait(final Packet packet, long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException;

    public void addPacketListener(IPacketListener listener, PacketType... types);

    public void removePacketListener(IPacketListener listener,
        PacketType... types);

    public void addPacketListener(IPacketListener listener, Priority priority,
        PacketType... types);
}