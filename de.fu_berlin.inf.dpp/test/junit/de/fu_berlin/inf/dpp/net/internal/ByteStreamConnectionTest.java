package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.net.IPacketListener;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.net.packet.NOPPacket;
import de.fu_berlin.inf.dpp.net.packet.Packet;
import de.fu_berlin.inf.dpp.net.packet.PacketType;
import de.fu_berlin.inf.dpp.net.packet.TestPacket;
import de.fu_berlin.inf.dpp.test.util.TestThread;

public class ByteStreamConnectionTest {

    private static final int PIPE_BUFFER_SIZE = 1024 * 1024;

    private static class PipedBytestreamSession implements BytestreamSession {

        private InputStream in;
        private OutputStream out;

        public PipedBytestreamSession(PipedInputStream in, PipedOutputStream out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return in;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return out;
        }

        @Override
        public void close() throws IOException {
            in.close();
            out.close();
        }

        @Override
        public int getReadTimeout() throws IOException {
            return 0;
        }

        @Override
        public void setReadTimeout(int timeout) throws IOException {
            // NOP
        }
    }

    private static abstract class StreamConnectionListener implements
        IByteStreamConnectionListener {

        @Override
        public void connectionClosed(JID peer, IByteStreamConnection connection) {
            // NOP

        }

        @Override
        public void connectionChanged(JID peer,
            IByteStreamConnection connection, boolean incomingRequest) {
            // NOP
        }

        @Override
        public abstract void addIncomingTransferObject(
            IncomingTransferObject incomingTransferObject);
    }

    private BytestreamSession aliceSession;
    private BytestreamSession bobSession;
    private volatile TestThread testThread;

    private JID aliceJID = new JID("alice@baumeister");
    private JID bobJID = new JID("bob@baumeister");

    private PacketDispatcherImpl dispatcher;
    private volatile Packet packet;

    @Before
    public void setUp() throws IOException {
        PipedOutputStream aliceOut = new PipedOutputStream();
        PipedInputStream aliceIn = new PipedInputStream(PIPE_BUFFER_SIZE);

        PipedOutputStream bobOut = new PipedOutputStream();
        PipedInputStream bobIn = new PipedInputStream(PIPE_BUFFER_SIZE);

        aliceOut.connect(bobIn);
        aliceIn.connect(bobOut);

        aliceSession = new PipedBytestreamSession(aliceIn, aliceOut);
        bobSession = new PipedBytestreamSession(bobIn, bobOut);

        testThread = null;
        packet = null;
        dispatcher = new PacketDispatcherImpl();
    }

    @After
    public void tearDown() {
        dispatcher.stop();
    }

    @Test(expected = RemoteCancellationException.class)
    public void testCancellationOnLocalSite() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        ByteStreamConnection alice = new ByteStreamConnection(aliceSession,
            dispatcher, new StreamConnectionListener() {
                @Override
                public void addIncomingTransferObject(
                    final IncomingTransferObject incomingTransferObject) {
                    // NOP
                }
            }, NetTransferMode.SOCKS5_DIRECT, aliceJID, bobJID);

        ByteStreamConnection bob = new ByteStreamConnection(bobSession,
            dispatcher, new StreamConnectionListener() {
                @Override
                public void addIncomingTransferObject(
                    final IncomingTransferObject incomingTransferObject) {
                    testThread = new TestThread(new TestThread.Runnable() {
                        @Override
                        public void run() throws Exception {
                            incomingTransferObject
                                .accept(new NullProgressMonitor());
                        }
                    });
                    testThread.start();
                    latch.countDown();
                }
            }, NetTransferMode.SOCKS5_DIRECT, bobJID, aliceJID);

        TransferDescription description = new TransferDescription();

        NullProgressMonitor monitor = new NullProgressMonitor();
        monitor.setCanceled(true);

        try {
            alice.send(description, new byte[100], monitor);
        } catch (Exception e) {
            latch.await(1, TimeUnit.SECONDS);
            assertTrue(e instanceof LocalCancellationException);
            testThread.join(10000);
            testThread.verify();
        } finally {
            bob.close();
            alice.close();
        }
    }

    @Test(expected = LocalCancellationException.class)
    public void testCancellationOnRemoteSite() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        ByteStreamConnection alice = new ByteStreamConnection(aliceSession,
            dispatcher, new StreamConnectionListener() {
                @Override
                public void addIncomingTransferObject(
                    final IncomingTransferObject incomingTransferObject) {
                    // NOP
                }
            }, NetTransferMode.SOCKS5_DIRECT, aliceJID, bobJID);

        ByteStreamConnection bob = new ByteStreamConnection(bobSession,
            dispatcher, new StreamConnectionListener() {
                @Override
                public void addIncomingTransferObject(
                    final IncomingTransferObject incomingTransferObject) {
                    testThread = new TestThread(new TestThread.Runnable() {
                        @Override
                        public void run() throws Exception {
                            NullProgressMonitor monitor = new NullProgressMonitor();
                            monitor.setCanceled(true);
                            incomingTransferObject.accept(monitor);
                        }
                    });
                    testThread.start();
                    latch.countDown();
                }
            }, NetTransferMode.SOCKS5_DIRECT, bobJID, aliceJID);

        TransferDescription description = new TransferDescription();

        NullProgressMonitor monitor = new NullProgressMonitor();

        try {
            alice.send(description, new byte[100], monitor);
        } catch (Exception e) {
            latch.await(1, TimeUnit.SECONDS);
            assertTrue(e instanceof RemoteCancellationException);
            testThread.join(10000);
            testThread.verify();
        } finally {
            alice.close();
            bob.close();
        }
    }

    private volatile byte[] receivedBytes;

    @Test
    public void testFragmentationOnLargeDataToBeSend() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        ByteStreamConnection alice = new ByteStreamConnection(aliceSession,
            dispatcher, new StreamConnectionListener() {
                @Override
                public void addIncomingTransferObject(
                    final IncomingTransferObject incomingTransferObject) {
                    // NOP
                }
            }, NetTransferMode.SOCKS5_DIRECT, aliceJID, bobJID);

        ByteStreamConnection bob = new ByteStreamConnection(bobSession,
            dispatcher, new StreamConnectionListener() {
                @Override
                public void addIncomingTransferObject(
                    final IncomingTransferObject incomingTransferObject) {
                    testThread = new TestThread(new TestThread.Runnable() {
                        @Override
                        public void run() throws Exception {
                            receivedBytes = incomingTransferObject
                                .accept(new NullProgressMonitor());
                        }
                    });
                    testThread.start();
                    latch.countDown();
                }
            }, NetTransferMode.SOCKS5_DIRECT, bobJID, aliceJID);

        TransferDescription description = new TransferDescription();

        byte[] bytesToSend = new byte[512 * 1024];

        for (int i = 0; i < bytesToSend.length; i++)
            bytesToSend[i] = (byte) i;

        NullProgressMonitor monitor = new NullProgressMonitor();

        try {
            alice.send(description, bytesToSend, monitor);
            latch.await(1, TimeUnit.SECONDS);
            testThread.join(10000);
            testThread.verify();
        } finally {
            alice.close();
            bob.close();
        }

        assertTrue("fragmentation error",
            Arrays.equals(bytesToSend, receivedBytes));
    }

    @Test
    public void testSendPacket() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        IPacketListener listener = new IPacketListener() {

            @Override
            public void processPacket(Packet packet) {
                ByteStreamConnectionTest.this.packet = packet;
                latch.countDown();
            }
        };

        dispatcher.addPacketListener(listener, PacketType.NOP);

        ByteStreamConnection alice = new ByteStreamConnection(aliceSession,
            dispatcher, null, NetTransferMode.SOCKS5_DIRECT, aliceJID, bobJID);

        ByteStreamConnection bob = new ByteStreamConnection(bobSession,
            dispatcher, null, NetTransferMode.SOCKS5_DIRECT, bobJID, aliceJID);

        Packet packetToSend = new NOPPacket();

        try {
            alice.sendPacket(packetToSend);
            latch.await(1, TimeUnit.SECONDS);
        } finally {
            alice.close();
            bob.close();
        }

        assertTrue("no packet received", packet != null);
        assertEquals(aliceJID, packet.getSender());
        assertEquals(bobJID, packet.getReceiver());
    }

    @Test
    public void testSendFragmentedPacket() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        IPacketListener listener = new IPacketListener() {

            @Override
            public void processPacket(Packet packet) {
                ByteStreamConnectionTest.this.packet = packet;
                latch.countDown();
            }
        };

        dispatcher.addPacketListener(listener, PacketType.TEST);

        ByteStreamConnection alice = new ByteStreamConnection(aliceSession,
            dispatcher, null, NetTransferMode.SOCKS5_DIRECT, aliceJID, bobJID);

        ByteStreamConnection bob = new ByteStreamConnection(bobSession,
            dispatcher, null, NetTransferMode.SOCKS5_DIRECT, bobJID, aliceJID);

        byte[] bytesToSend = new byte[512 * 1024];

        for (int i = 0; i < bytesToSend.length; i++)
            bytesToSend[i] = (byte) i;

        Packet packetToSend = new TestPacket(bytesToSend);

        try {
            alice.sendPacket(packetToSend);
            latch.await(1, TimeUnit.SECONDS);
        } finally {
            alice.close();
            bob.close();
        }

        assertTrue("no packet received", packet != null);
        assertEquals(aliceJID, packet.getSender());
        assertEquals(bobJID, packet.getReceiver());
        assertTrue("fragmentation error",
            Arrays.equals(bytesToSend, ((TestPacket) packet).getData()));

    }
}
