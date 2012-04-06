package de.fu_berlin.inf.dpp.net.internal;

import java.util.EnumMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.IPacketDispatcher;
import de.fu_berlin.inf.dpp.net.IPacketListener;
import de.fu_berlin.inf.dpp.net.packet.Packet;
import de.fu_berlin.inf.dpp.net.packet.PacketType;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;

/**
 * @author Stefan Rossbach
 */
public class PacketDispatcherImpl implements IPacketDispatcher {

    private static final Logger LOG = Logger
        .getLogger(PacketDispatcherImpl.class);

    private static class PacketListenerHolder implements
        Comparable<PacketListenerHolder> {
        private IPacketListener listener;
        private Priority priority;

        public PacketListenerHolder(IPacketListener listener, Priority priority) {
            this.listener = listener;
            this.priority = priority;
        }

        public IPacketListener getListener() {
            return listener;
        }

        @Override
        public int compareTo(PacketListenerHolder other) {

            return 0 - Integer.valueOf(this.priority.getPriority()).compareTo(
                Integer.valueOf(other.priority.getPriority()));
        }

        @Override
        public boolean equals(Object object) {
            return object != null
                && listener.equals(((PacketListenerHolder) object).listener);
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }

    private Map<PacketType, PriorityQueue<PacketListenerHolder>> listeners = new EnumMap<PacketType, PriorityQueue<PacketListenerHolder>>(
        PacketType.class);

    private ExecutorService executorService;

    public PacketDispatcherImpl() {
        this(Executors.newSingleThreadExecutor(new NamedThreadFactory(
            "Packet-Dispatcher-")));
    }

    public PacketDispatcherImpl(ExecutorService executorService) {
        this.executorService = executorService;
    }

    void stop() {
        if (executorService.isShutdown() || executorService.isTerminated()) {
            LOG.warn("packet dispatcher is not running");
            return;
        }

        executorService.shutdown();
    }

    @Override
    public void dispatch(final Packet packet) {

        if (executorService.isShutdown() || executorService.isTerminated())
            throw new IllegalStateException(
                "packet dispatcher is not running or is terminated");

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                dispatchPacket(packet);
            }
        });
    }

    @Override
    public void dispatchAndWait(final Packet packet, long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException {

        if (executorService.isShutdown() || executorService.isTerminated())
            throw new IllegalStateException(
                "packet dispatcher is not running or is terminated");

        Future<?> future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                dispatchPacket(packet);
            }
        });

        try {
            future.get(timeout, unit);
        } catch (ExecutionException e) {
            LOG.debug("error while waiting for dispatching of packet " + packet
                + ", " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void addPacketListener(IPacketListener listener,
        PacketType... types) {
        addPacketListener(listener, Priority.NORMAL, types);
    }

    @Override
    public synchronized void addPacketListener(IPacketListener listener,
        Priority priority, PacketType... types) {
        for (PacketType type : types) {
            PriorityQueue<PacketListenerHolder> packetListeners = listeners
                .get(type);
            if (packetListeners == null) {
                packetListeners = new PriorityQueue<PacketListenerHolder>();
                listeners.put(type, packetListeners);
            }
            packetListeners.add(new PacketListenerHolder(listener, priority));
        }
    }

    @Override
    public synchronized void removePacketListener(IPacketListener listener,
        PacketType... types) {

        if (types.length == 0) {
            for (PriorityQueue<PacketListenerHolder> packetListeners : listeners
                .values())
                packetListeners
                    .remove(new PacketListenerHolder(listener, null));

            return;
        }

        for (PacketType type : types) {
            PriorityQueue<PacketListenerHolder> packetListeners = listeners
                .get(type);

            if (packetListeners == null)
                continue;

            packetListeners.remove(new PacketListenerHolder(listener, null));
        }
    }

    private synchronized void dispatchPacket(Packet packet) {
        PriorityQueue<PacketListenerHolder> packetListeners = listeners
            .get(packet.getType());
        if (packetListeners == null)
            return;

        for (PacketListenerHolder listenerHolder : packetListeners) {
            try {
                listenerHolder.getListener().processPacket(packet);
            } catch (Exception e) {
                LOG.error("error while dispatching packet to packet listener "
                    + listenerHolder.getListener() + " :" + e.getMessage(), e);
            }
        }
    }
}
