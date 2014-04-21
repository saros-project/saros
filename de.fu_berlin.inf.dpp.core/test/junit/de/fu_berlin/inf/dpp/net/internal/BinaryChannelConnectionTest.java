package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smackx.bytestreams.BytestreamSession;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;

public class BinaryChannelConnectionTest {

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
        public void connectionClosed(String connectionIdentifier, JID peer,
            IByteStreamConnection connection) {
            // NOP

        }

        @Override
        public void connectionChanged(String connectionIdentifier, JID peer,
            IByteStreamConnection connection, boolean incomingRequest) {
            // NOP
        }

        @Override
        public abstract void receive(BinaryXMPPExtension extension);
    }

    private BytestreamSession aliceSession;
    private BytestreamSession bobSession;

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
    }

    private volatile byte[] receivedBytes;

    @Test
    public void testFragmentationOnLargeDataToBeSend() throws Exception {

        final CountDownLatch received = new CountDownLatch(1);

        BinaryChannelConnection alice = new BinaryChannelConnection(new JID(
            "alice@baumeister.de"), "junit", aliceSession,
            NetTransferMode.SOCKS5_DIRECT, new StreamConnectionListener() {
                @Override
                public void receive(final BinaryXMPPExtension extension) {
                    // NOP
                }
            });

        BinaryChannelConnection bob = new BinaryChannelConnection(new JID(
            "bob@baumeister.de"), "junit", bobSession,
            NetTransferMode.SOCKS5_DIRECT, new StreamConnectionListener() {
                @Override
                public void receive(final BinaryXMPPExtension extension) {
                    receivedBytes = extension.getPayload();
                    received.countDown();
                }
            });

        alice.initialize();
        bob.initialize();

        TransferDescription description = TransferDescription
            .createCustomTransferDescription();

        byte[] bytesToSend = new byte[512 * 1024];

        for (int i = 0; i < bytesToSend.length; i++)
            bytesToSend[i] = (byte) i;

        try {
            alice.send(description, bytesToSend);
            received.await(10000, TimeUnit.MILLISECONDS);
        } finally {
            alice.close();
            bob.close();
        }

        assertTrue("no bytes were received", received.getCount() == 0);

        assertArrayEquals("fragmentation error", bytesToSend, receivedBytes);
    }

    @Test
    @Ignore("this test consumes much CPU resources and should only executed manually when making changes")
    public void testFragmentationCleanup() throws Exception {

        long packetSize = 16 * 1024;
        long bytesToTransfer = (1L << 31L); // send 2 GB of data;

        long packetsToSend = bytesToTransfer / packetSize;

        packetsToSend++;

        final CountDownLatch received = new CountDownLatch((int) packetsToSend);

        BinaryChannelConnection alice = new BinaryChannelConnection(new JID(
            "alice@baumeister.de"), "junit", aliceSession,
            NetTransferMode.SOCKS5_DIRECT, new StreamConnectionListener() {
                @Override
                public void receive(final BinaryXMPPExtension extension) {
                    // NOP
                }
            });

        BinaryChannelConnection bob = new BinaryChannelConnection(new JID(
            "bob@baumeister.de"), "junit", bobSession,
            NetTransferMode.SOCKS5_DIRECT, new StreamConnectionListener() {
                @Override
                public void receive(final BinaryXMPPExtension extension) {
                    receivedBytes = extension.getPayload();
                    received.countDown();
                }
            });

        TransferDescription description = TransferDescription
            .createCustomTransferDescription();

        byte[] bytesToSend = new byte[(int) packetSize];

        for (int i = 0; i < bytesToSend.length; i++)
            bytesToSend[i] = (byte) i;

        try {
            for (int i = 0; i < packetsToSend - 1; i++)
                alice.send(description, bytesToSend);

            for (int i = 0; i < bytesToSend.length; i++)
                bytesToSend[i] = (byte) 0x7F;

            alice.send(description, bytesToSend);

            received.await(60000, TimeUnit.MILLISECONDS);
        } finally {
            alice.close();
            bob.close();
        }

        assertTrue("remote side crashed", received.getCount() == 0);

        assertArrayEquals("fragmentation error", bytesToSend, receivedBytes);

    }
}
