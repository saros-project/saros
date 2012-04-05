package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.IPacketDispatcher.Priority;
import de.fu_berlin.inf.dpp.net.IPacketListener;
import de.fu_berlin.inf.dpp.net.packet.NOPPacket;
import de.fu_berlin.inf.dpp.net.packet.Packet;
import de.fu_berlin.inf.dpp.net.packet.PacketType;
import de.fu_berlin.inf.dpp.net.packet.VersionRequestPacket;
import de.fu_berlin.inf.dpp.net.packet.VersionResponsePacket;

public class PacketDispatcherTest {

    private static class FakeExecutorService extends AbstractExecutorService {

        @Override
        public boolean awaitTermination(long arg0, TimeUnit arg1)
            throws InterruptedException {
            return false;
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public void shutdown() {
            //
        }

        @Override
        public List<Runnable> shutdownNow() {
            return null;
        }

        @Override
        public void execute(Runnable runnable) {
            runnable.run();
        }

    }

    private ExecutorService executorService = new FakeExecutorService();

    private List<Packet> capturedPackets = new ArrayList<Packet>();

    private IPacketListener listener = new IPacketListener() {

        @Override
        public void processPacket(Packet packet) {
            capturedPackets.add(packet);
        }
    };

    private IPacketListener defectListener = new IPacketListener() {

        @Override
        public void processPacket(Packet packet) {
            throw new RuntimeException();
        }
    };

    private PacketDispatcherImpl dispatcher;

    @Before
    public void setUp() {
        dispatcher = new PacketDispatcherImpl(executorService);
    }

    @After
    public void tearDown() {
        dispatcher.stop();
        capturedPackets.clear();
    }

    @Test(expected = IllegalStateException.class)
    public void testDispatchPacketWithoutRunningDispatcher() {
        // make code coverage happy
        dispatcher = new PacketDispatcherImpl();

        dispatcher.stop();
        dispatcher.dispatch(new NOPPacket());
    }

    @Test(expected = IllegalStateException.class)
    public void testDispatchPacketAndWaitWithoutRunningDispatcher()
        throws Exception {
        // make code coverage happy
        dispatcher = new PacketDispatcherImpl();

        dispatcher.stop();
        dispatcher.dispatchAndWait(new NOPPacket(), 200, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("null")
    @Test
    public void testListenerWithSinglePacketTypeRegisterd() throws Exception {
        dispatcher.addPacketListener(listener, PacketType.NOP);
        dispatcher.dispatch(new VersionRequestPacket());
        dispatcher.dispatch(new NOPPacket());

        assertFalse(capturedPackets.isEmpty());
        Packet packet = capturedPackets.remove(0);

        if (packet == null)
            fail("recevied no packet");

        assertEquals(PacketType.NOP, packet.getType());

        if (!capturedPackets.isEmpty())
            fail("recevied packet that was not registered by the listener: "
                + capturedPackets.get(0).getType());

    }

    @SuppressWarnings("null")
    @Test
    public void testListenerWithMultiPacketTypeRegisterd() throws Exception {
        dispatcher.addPacketListener(listener, PacketType.NOP,
            PacketType.VERSION_REQUEST);
        dispatcher.dispatch(new NOPPacket());
        dispatcher.dispatch(new VersionResponsePacket());
        dispatcher.dispatch(new VersionRequestPacket());

        assertFalse(capturedPackets.isEmpty());
        Packet packet = capturedPackets.remove(0);

        if (packet == null)
            fail("recevied no packet");

        assertEquals(PacketType.NOP, packet.getType());

        assertFalse(capturedPackets.isEmpty());
        packet = capturedPackets.remove(0);

        if (packet == null)
            fail("recevied no packet");

        assertEquals(PacketType.VERSION_REQUEST, packet.getType());

        if (!capturedPackets.isEmpty())
            fail("recevied packet that was not registered by the listener: "
                + capturedPackets.get(0).getType());

    }

    @SuppressWarnings("null")
    @Test
    public void testListenerWithUnregisteringAllPacketTypes() throws Exception {
        dispatcher.addPacketListener(listener, PacketType.NOP,
            PacketType.VERSION_REQUEST);
        dispatcher.dispatch(new NOPPacket());
        dispatcher.dispatch(new VersionResponsePacket());
        dispatcher.dispatch(new VersionRequestPacket());

        assertFalse(capturedPackets.isEmpty());
        Packet packet = capturedPackets.remove(0);

        if (packet == null)
            fail("recevied no packet");

        assertEquals(PacketType.NOP, packet.getType());

        dispatcher.removePacketListener(listener);
        dispatcher.dispatch(new VersionRequestPacket());
        dispatcher.dispatch(new NOPPacket());

        assertFalse(capturedPackets.isEmpty());
        packet = capturedPackets.remove(0);

        if (packet == null)
            fail("recevied no packet");

        assertEquals(PacketType.VERSION_REQUEST, packet.getType());

        if (!capturedPackets.isEmpty())
            fail("recevied packet that was not registered by the listener: "
                + capturedPackets.get(0).getType());

    }

    @SuppressWarnings("null")
    @Test
    public void testListenerWithUnregisteringOnePacketType() throws Exception {
        dispatcher.addPacketListener(listener, PacketType.NOP,
            PacketType.VERSION_REQUEST);

        dispatcher.dispatch(new NOPPacket());
        dispatcher.dispatch(new VersionResponsePacket());
        dispatcher.dispatch(new VersionRequestPacket());

        assertFalse(capturedPackets.isEmpty());
        Packet packet = capturedPackets.remove(0);

        if (packet == null)
            fail("recevied no packet");

        assertEquals(PacketType.NOP, packet.getType());

        dispatcher.removePacketListener(listener, PacketType.NOP);
        dispatcher.dispatch(new VersionRequestPacket());
        dispatcher.dispatch(new NOPPacket());

        assertFalse(capturedPackets.isEmpty());
        packet = capturedPackets.remove(0);

        if (packet == null)
            fail("recevied no packet");

        assertEquals(PacketType.VERSION_REQUEST, packet.getType());

        assertFalse(capturedPackets.isEmpty());
        packet = capturedPackets.remove(0);

        if (packet == null)
            fail("recevied no packet");

        assertEquals(PacketType.VERSION_REQUEST, packet.getType());

        if (!capturedPackets.isEmpty())
            fail("recevied packet that was not registered by the listener: "
                + capturedPackets.get(0).getType());

    }

    @SuppressWarnings("null")
    @Test
    public void testListenerWithUnregisteringNotRegisteredBeforPacketType()
        throws Exception {
        dispatcher.addPacketListener(listener, PacketType.NOP);
        dispatcher.dispatch(new NOPPacket());
        dispatcher.dispatch(new VersionRequestPacket());
        dispatcher.removePacketListener(listener, PacketType.VERSION_REQUEST);
        dispatcher.dispatch(new VersionRequestPacket());
        dispatcher.dispatch(new NOPPacket());

        assertFalse(capturedPackets.isEmpty());
        Packet packet = capturedPackets.remove(0);

        if (packet == null)
            fail("recevied no packet");

        assertEquals(PacketType.NOP, packet.getType());

        assertFalse(capturedPackets.isEmpty());

        packet = capturedPackets.remove(0);

        if (packet == null)
            fail("recevied no packet");

        assertEquals(PacketType.NOP, packet.getType());

        if (!capturedPackets.isEmpty())
            fail("recevied packet that was not registered by the listener: "
                + capturedPackets.get(0).getType());
    }

    @SuppressWarnings("null")
    @Test
    public void testDispatcherMustNotThrowAnExceptionAndContinueWorkingOnOtherListeners() {
        dispatcher.addPacketListener(listener, PacketType.NOP);
        dispatcher.addPacketListener(defectListener, Priority.HIGHEST,
            PacketType.NOP);

        dispatcher.dispatch(new NOPPacket());

        assertFalse(capturedPackets.isEmpty());
        Packet packet = capturedPackets.remove(0);

        if (packet == null)
            fail("recevied no packet");

        assertEquals(PacketType.NOP, packet.getType());
    }

    private int value = 0;
    private IPacketListener highListener = new IPacketListener() {

        @Override
        public void processPacket(Packet packet) {
            value = 1;
        }
    };

    private IPacketListener lowListener = new IPacketListener() {

        @Override
        public void processPacket(Packet packet) {
            value <<= 1;
        }
    };

    @Test
    public void testDispatcherMustCallListenersByTheirPriority() {
        value = 0;
        dispatcher.addPacketListener(lowListener, PacketType.NOP);
        dispatcher.addPacketListener(highListener, Priority.HIGHEST,
            PacketType.NOP);

        dispatcher.dispatch(new NOPPacket());

        assertEquals(1 << 1, value);
    }

    private long sleepTime;

    private IPacketListener delayListener = new IPacketListener() {

        @Override
        public void processPacket(Packet packet) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    };

    @Test
    public void testWaitUntilPacketIsDipatched() throws Exception {
        sleepTime = 0;
        dispatcher = new PacketDispatcherImpl();
        dispatcher.addPacketListener(delayListener, PacketType.NOP);
        dispatcher
            .dispatchAndWait(new NOPPacket(), 1000, TimeUnit.MILLISECONDS);

    }

    @Test(expected = TimeoutException.class)
    public void testWaitUntilPacketIsDipatchedWithExeedingTheTimeout()
        throws Exception {
        sleepTime = 500;
        dispatcher = new PacketDispatcherImpl();
        dispatcher.addPacketListener(delayListener, PacketType.NOP);
        dispatcher.dispatchAndWait(new NOPPacket(), 200, TimeUnit.MILLISECONDS);
    }
}
