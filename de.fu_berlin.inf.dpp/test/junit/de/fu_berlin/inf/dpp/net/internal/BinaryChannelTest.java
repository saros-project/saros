package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.test.util.TestThread;

public class BinaryChannelTest {

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

    @Test(expected = RemoteCancellationException.class)
    public void testCancellationOnLocalSite() throws Exception {
        BinaryChannel alice = new BinaryChannel(aliceSession,
            NetTransferMode.SOCKS5_DIRECT);

        BinaryChannelConnection bob = new BinaryChannelConnection(new JID(
            "bob@baumeister.de"), new BinaryChannel(bobSession,
            NetTransferMode.SOCKS5_DIRECT), new StreamConnectionListener() {
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
            }
        });

        TransferDescription description = new TransferDescription();

        NullProgressMonitor monitor = new NullProgressMonitor();
        monitor.setCanceled(true);

        try {
            alice.send(description, new byte[100], monitor);
        } catch (Exception e) {
            // if you remove this sleep testThread will be always null
            Thread.sleep(1000);
            assertTrue(e instanceof LocalCancellationException);
        } finally {
            bob.close();
        }

        testThread.join();
        testThread.verify();
    }

    @Test(expected = LocalCancellationException.class)
    public void testCancellationOnRemoteSite() throws Exception {

        BinaryChannelConnection alice = new BinaryChannelConnection(new JID(
            "alice@baumeister.de"), new BinaryChannel(aliceSession,
            NetTransferMode.SOCKS5_DIRECT), new StreamConnectionListener() {
            @Override
            public void addIncomingTransferObject(
                final IncomingTransferObject incomingTransferObject) {
                // NOP
            }
        });

        BinaryChannelConnection bob = new BinaryChannelConnection(new JID(
            "bob@baumeister.de"), new BinaryChannel(bobSession,
            NetTransferMode.SOCKS5_DIRECT), new StreamConnectionListener() {
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
            }
        });

        TransferDescription description = new TransferDescription();

        NullProgressMonitor monitor = new NullProgressMonitor();

        try {
            alice.send(description, new byte[100], monitor);
        } catch (Exception e) {
            assertTrue(e instanceof RemoteCancellationException);
        } finally {
            alice.close();
            bob.close();
        }

        testThread.join();
        testThread.verify();
    }

    private volatile byte[] receivedBytes;

    @Test
    public void testFragmentationOnLargeDataToBeSend() throws Exception {
        BinaryChannelConnection alice = new BinaryChannelConnection(new JID(
            "alice@baumeister.de"), new BinaryChannel(aliceSession,
            NetTransferMode.SOCKS5_DIRECT), new StreamConnectionListener() {
            @Override
            public void addIncomingTransferObject(
                final IncomingTransferObject incomingTransferObject) {
                // NOP
            }
        });

        BinaryChannelConnection bob = new BinaryChannelConnection(new JID(
            "bob@baumeister.de"), new BinaryChannel(bobSession,
            NetTransferMode.SOCKS5_DIRECT), new StreamConnectionListener() {
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
            }
        });

        TransferDescription description = new TransferDescription();

        byte[] bytesToSend = new byte[512 * 1024];

        for (int i = 0; i < bytesToSend.length; i++)
            bytesToSend[i] = (byte) i;

        NullProgressMonitor monitor = new NullProgressMonitor();

        try {
            alice.send(description, bytesToSend, monitor);
        } finally {
            alice.close();
            bob.close();
        }

        testThread.join();
        testThread.verify();

        assertTrue("fragmentation error",
            Arrays.equals(bytesToSend, receivedBytes));
    }
}
