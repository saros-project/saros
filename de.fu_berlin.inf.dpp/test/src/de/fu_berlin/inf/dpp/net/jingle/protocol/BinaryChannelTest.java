package de.fu_berlin.inf.dpp.net.jingle.protocol;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.junit.Test;

import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.test.util.TestThread;

/**
 * @author coezbek
 */
public class BinaryChannelTest {

    /**
     * Field used exclusive by getTestArray to memoize the created TestArrays
     */
    private Map<Integer, WeakReference<byte[]>> memoMap = new HashMap<Integer, WeakReference<byte[]>>();

    /**
     * Construct a test array
     */
    public byte[] getTestArray(int size) {

        byte[] result;

        // First look if we still have a reference to the test array
        WeakReference<byte[]> memoizedTestArray = memoMap.get(size);
        if (memoizedTestArray != null) {
            result = memoizedTestArray.get();
            if (result != null)
                return result;
        }

        // Otherwise create it new
        result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = (byte) i;
        }
        memoMap.put(size, new WeakReference<byte[]>(result));
        return result;
    }

    /**
     * Run the main loop of the given binary channel and put all received
     * {@link IncomingTransferObject}s into the given queue.
     * 
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SarosCancellationException
     */
    protected void runBinaryChannelLoop(
        final BlockingQueue<IncomingTransferObject> queue, BinaryChannel channel)
        throws SarosCancellationException, IOException, ClassNotFoundException {

        while (true) {
            try {
                IncomingTransferObject ito = channel
                    .receiveIncomingTransferObject(SubMonitor
                        .convert(new NullProgressMonitor()));

                queue.add(ito);
            } catch (EOFException e) {
                return;
            }
        }

    }

    /**
     * Easy test with data being sent from client to server.
     * 
     * @throws Throwable
     */
    @Test
    public void testBinaryChannelClientToServer() throws Throwable {

        final ServerSocket server = new ServerSocket(4278);

        final BlockingQueue<IncomingTransferObject> serverQueue = new LinkedBlockingQueue<IncomingTransferObject>();
        final BlockingQueue<IncomingTransferObject> clientQueue = new LinkedBlockingQueue<IncomingTransferObject>();
        final BlockingQueue<Throwable> failureQueue = new LinkedBlockingQueue<Throwable>();

        final Socket[] serverSocket = new Socket[1];

        // Start the server in a separate thread
        TestThread serverMainLoop = new TestThread(failureQueue,
            new Runnable() {
                public void run() {
                    try {
                        serverSocket[0] = server.accept();

                        final BinaryChannel serverChannel = new BinaryChannel(
                            serverSocket[0], NetTransferMode.JINGLETCP);

                        runBinaryChannelLoop(serverQueue, serverChannel);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        serverMainLoop.start();

        // Connect to the server
        Socket clientSocket = new Socket("localhost", 4278);
        final BinaryChannel clientChannel = new BinaryChannel(clientSocket,
            NetTransferMode.JINGLETCP);

        // Start the clients main loop (for confirmations)
        TestThread clientMainLoop = new TestThread(failureQueue,
            new Runnable() {
                public void run() {
                    try {
                        runBinaryChannelLoop(clientQueue, clientChannel);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        clientMainLoop.start();

        // Start sending data to the server
        TestThread clientSender = new TestThread(failureQueue, new Runnable() {
            public void run() {

                for (int i = 0; i < 128; i++) {
                    try {
                        clientChannel.sendDirect(TransferDescription
                            .createFileTransferDescription(new JID(
                                "server@gmail.com"),
                                new JID("client@gmail.com"), new Path(
                                    "/test/hello.jpg"), "" + 4722),
                            getTestArray(i), SubMonitor
                                .convert(new NullProgressMonitor()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        clientSender.start();

        // Start sending data to the server
        TestThread serverReceiveThread = new TestThread(failureQueue,
            new Runnable() {
                public void run() {

                    try {
                        for (int i = 0; i < 128; i++) {
                            IncomingTransferObject ito = serverQueue.take();

                            assertTrue("" + i, Arrays.equals(getTestArray(i),
                                ito.accept(SubMonitor
                                    .convert(new NullProgressMonitor()))));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        serverReceiveThread.start();

        try {
            while (clientSender.isAlive() || serverReceiveThread.isAlive()) {

                Throwable t = failureQueue.poll(100, TimeUnit.MILLISECONDS);
                if (t != null)
                    throw t;
            }
            Throwable t = failureQueue.poll();
            if (t != null)
                throw t;
        } finally {
            clientChannel.dispose();
            serverSocket[0].close();
            server.close();
            clientSocket.close();
        }
    }

    /**
     * Test bidirectional sending at the same time
     */
    @Test
    public void testBinaryChannelBiDi() throws Throwable {

        final ServerSocket server = new ServerSocket(4278);

        final BlockingQueue<IncomingTransferObject> serverQueue = new LinkedBlockingQueue<IncomingTransferObject>();
        final BlockingQueue<IncomingTransferObject> clientQueue = new LinkedBlockingQueue<IncomingTransferObject>();
        final BlockingQueue<Throwable> failureQueue = new LinkedBlockingQueue<Throwable>();

        final Socket[] serverSocket = new Socket[1];

        // Start the server in a separate thread
        TestThread serverThread = new TestThread(failureQueue, new Runnable() {
            public void run() {

                try {
                    serverSocket[0] = server.accept();

                    final BinaryChannel serverChannel = new BinaryChannel(
                        serverSocket[0], NetTransferMode.JINGLETCP);

                    TestThread serverReceiveThread = new TestThread(
                        failureQueue, new Runnable() {
                            public void run() {
                                try {
                                    runBinaryChannelLoop(serverQueue,
                                        serverChannel);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    serverReceiveThread.start();

                    // Send uneven arrays to client
                    for (int i = 1; i < 128; i += 2) {
                        serverChannel.sendDirect(TransferDescription
                            .createFileTransferDescription(new JID(
                                "server@gmail.com"),
                                new JID("client@gmail.com"), new Path(
                                    "/test/hello.jpg"), "" + 4722),
                            getTestArray(i * 1024), SubMonitor
                                .convert(new NullProgressMonitor()));
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        serverThread.start();

        Socket clientSocket = new Socket("localhost", 4278);

        final BinaryChannel clientChannel = new BinaryChannel(clientSocket,
            NetTransferMode.JINGLETCP);

        // Start the clients main loop (for confirmations)
        TestThread clientMainLoop = new TestThread(failureQueue,
            new Runnable() {
                public void run() {
                    try {
                        runBinaryChannelLoop(clientQueue, clientChannel);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        clientMainLoop.start();

        // Start sending data to the server
        TestThread clientSender = new TestThread(failureQueue, new Runnable() {
            public void run() {

                // Send even arrays to server
                for (int i = 0; i < 128; i += 2) {
                    try {
                        clientChannel.sendDirect(TransferDescription
                            .createFileTransferDescription(new JID(
                                "server@gmail.com"),
                                new JID("client@gmail.com"), new Path(
                                    "/test/hello.jpg"), "" + 4722),
                            getTestArray(i * 1024), SubMonitor
                                .convert(new NullProgressMonitor()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        clientSender.start();

        TestThread clientReceiver = new TestThread(failureQueue,
            new Runnable() {
                public void run() {
                    try {
                        for (int i = 1; i < 128; i += 2) {
                            IncomingTransferObject ito;
                            ito = clientQueue.take();

                            assertTrue("" + i, Arrays.equals(
                                getTestArray(i * 1024), ito.accept(SubMonitor
                                    .convert(new NullProgressMonitor()))));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        clientReceiver.start();

        Thread serverReceiver = new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < 128; i += 2) {
                        IncomingTransferObject ito = serverQueue.take();

                        assertTrue("" + i, Arrays.equals(
                            getTestArray(i * 1024), ito.accept(SubMonitor
                                .convert(new NullProgressMonitor()))));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        serverReceiver.start();

        try {
            while (clientSender.isAlive() || serverThread.isAlive()
                || clientReceiver.isAlive() || serverReceiver.isAlive()) {

                Throwable t = failureQueue.poll(100, TimeUnit.MILLISECONDS);
                if (t != null)
                    throw t;

            }
            Throwable t = failureQueue.poll();
            if (t != null)
                throw t;
        } finally {
            clientChannel.dispose();
            serverSocket[0].close();
            clientSocket.close();
            server.close();
        }

    }

    /**
     * Test two people sending at the same time.
     * 
     * Currently crashing because testShutdown fails.
     */
    @Test
    public void testBinaryChannelMultiSend() throws Throwable {

        final ServerSocket server = new ServerSocket(4278);

        final BlockingQueue<IncomingTransferObject> serverQueue = new LinkedBlockingQueue<IncomingTransferObject>();
        final BlockingQueue<IncomingTransferObject> clientQueue = new LinkedBlockingQueue<IncomingTransferObject>();
        final BlockingQueue<Throwable> failureQueue = new LinkedBlockingQueue<Throwable>();

        final Socket[] serverSocket = new Socket[1];

        // Start the server in a separate thread
        TestThread serverMainLoop = new TestThread(failureQueue,
            new Runnable() {
                public void run() {

                    try {
                        serverSocket[0] = server.accept();

                        BinaryChannel serverChannel = new BinaryChannel(
                            serverSocket[0], NetTransferMode.JINGLETCP);

                        runBinaryChannelLoop(serverQueue, serverChannel);

                    } catch (Exception e1) {
                        fail("Server crashed");
                    }
                }
            });
        serverMainLoop.start();

        Socket clientSocket = new Socket("localhost", 4278);

        final BinaryChannel clientChannel = new BinaryChannel(clientSocket,
            NetTransferMode.JINGLETCP);

        // Start the clients main loop (for confirmations)
        TestThread clientMainLoop = new TestThread(failureQueue,
            new Runnable() {
                public void run() {
                    try {
                        runBinaryChannelLoop(clientQueue, clientChannel);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        clientMainLoop.start();

        // Start sending data to the server from thread 1:
        TestThread clientSendThread1 = new TestThread(failureQueue,
            new Runnable() {
                public void run() {

                    // Send even arrays to server
                    for (int i = 0; i < 512; i += 2) {
                        try {
                            clientChannel.sendDirect(TransferDescription
                                .createFileTransferDescription(new JID(
                                    "server@gmail.com"), new JID(
                                    "client@gmail.com"), new Path(
                                    "/test/hello.jpg"), "" + 4722),
                                getTestArray(i * 1024), SubMonitor
                                    .convert(new NullProgressMonitor()));
                        } catch (Exception e) {
                            throw new RuntimeException("Failed sending " + i, e);
                        }
                    }
                }
            });
        clientSendThread1.start();

        // Start sending data to the server from thread 2:
        TestThread clientSendThread2 = new TestThread(failureQueue,
            new Runnable() {
                public void run() {

                    // Send even arrays to server
                    for (int i = 1; i < 512; i += 2) {
                        try {
                            clientChannel.sendDirect(TransferDescription
                                .createFileTransferDescription(new JID(
                                    "server@gmail.com"), new JID(
                                    "client@gmail.com"), new Path(
                                    "/test/hello.jpg"), "" + 4722),
                                getTestArray(i * 1024), SubMonitor
                                    .convert(new NullProgressMonitor()));
                        } catch (Exception e) {
                            throw new RuntimeException("Failed sending " + i, e);
                        }
                    }
                }
            });
        clientSendThread2.start();

        TestThread serverReceiveThread = new TestThread(failureQueue,
            new Runnable() {
                public void run() {
                    try {
                        final boolean[] alreadyReceived = new boolean[512];

                        for (int i = 0; i < 512; i += 1) {
                            IncomingTransferObject ito;
                            ito = serverQueue.take();

                            byte[] received = ito.accept(SubMonitor
                                .convert(new NullProgressMonitor()));

                            int iOfReceived = received.length / 1024;
                            assertFalse(alreadyReceived[iOfReceived]);
                            assertTrue(Arrays.equals(
                                getTestArray(received.length), received));
                            alreadyReceived[iOfReceived] = true;
                        }

                        for (int i = 0; i < 512; i += 1) {
                            assertTrue("" + i, alreadyReceived[i]);
                        }
                    } catch (Exception e) {
                        fail("Server receive thread crashed");
                    }
                }
            });
        serverReceiveThread.start();

        try {
            while (clientSendThread1.isAlive() || clientSendThread2.isAlive()
                || clientSendThread2.isAlive() || serverReceiveThread.isAlive()) {

                Throwable t = failureQueue.poll(100, TimeUnit.MILLISECONDS);
                if (t != null)
                    throw t;
            }
        } finally {
            clientChannel.dispose();
            serverSocket[0].close();
            clientSocket.close();
            server.close();
        }
        Throwable t = failureQueue.poll();
        if (t != null)
            throw t;
    }

    public void pingOut() {
        long time = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            System.out.println("Delta: " + (System.currentTimeMillis() - time)
                + "ms");
            time = System.currentTimeMillis();
            System.out.flush();
        }
    }

    /**
     * Test for checking whether Shutdown works as intended. It does not because
     * there is a TODO in the BinaryChannel
     */
    @Test
    public void testShutdown() throws Throwable {

        final ServerSocket server = new ServerSocket(4278);

        final BlockingQueue<IncomingTransferObject> serverQueue = new LinkedBlockingQueue<IncomingTransferObject>();
        final BlockingQueue<IncomingTransferObject> clientQueue = new LinkedBlockingQueue<IncomingTransferObject>();
        final BlockingQueue<Throwable> failureQueue = new LinkedBlockingQueue<Throwable>();

        final AtomicReference<Socket> serverSocket = new AtomicReference<Socket>(
            null);
        final AtomicReference<BinaryChannel> serverChannel = new AtomicReference<BinaryChannel>(
            null);

        // Start the server in a separate thread
        TestThread serverMainLoop = createServerThread(server, serverQueue,
            failureQueue, serverSocket, serverChannel);
        serverMainLoop.start();

        // Connect to the server
        Socket clientSocket = new Socket("localhost", 4278);
        final BinaryChannel clientChannel = new BinaryChannel(clientSocket,
            NetTransferMode.JINGLETCP);

        // Start the clients main loop (for confirmations)
        TestThread clientMainLoop = createMainLoopThread(clientQueue,
            failureQueue, clientChannel);
        clientMainLoop.start();

        // Start sending data to the server
        TestThread clientSender = createSenderThread(failureQueue,
            clientChannel);
        clientSender.start();

        // Start receiving data from the client
        TestThread serverReceiveThread = createReceivingThread(serverQueue,
            failureQueue);
        serverReceiveThread.start();

        // Wait for sending to finish
        while (clientSender.isAlive() || serverReceiveThread.isAlive()) {
            Throwable t = failureQueue.poll(100, TimeUnit.MILLISECONDS);
            if (t != null)
                throw t;
        }

        assertTrue(clientChannel.isConnected());
        assertTrue(serverChannel.get().isConnected());

        // Now shutdown channel from client side
        clientChannel.dispose();

        while (clientMainLoop.isAlive() || serverMainLoop.isAlive()) {
            Throwable t = failureQueue.poll(100, TimeUnit.MILLISECONDS);
            if (t != null)
                throw t;
        }

        assertFalse(clientChannel.isConnected());
        assertFalse(serverChannel.get().isConnected());

        serverSocket.get().close();
        server.close();
        clientSocket.close();

        // No exceptions should still be pending at this point
        Throwable t = failureQueue.poll();
        if (t != null)
            throw t;
    }

    protected TestThread createServerThread(final ServerSocket server,
        final BlockingQueue<IncomingTransferObject> serverQueue,
        final BlockingQueue<Throwable> failureQueue,
        final AtomicReference<Socket> serverSocket,
        final AtomicReference<BinaryChannel> serverChannel) {

        TestThread serverMainLoop = new TestThread(failureQueue,
            new Runnable() {
                public void run() {
                    try {
                        serverSocket.set(server.accept());

                        serverChannel.set(new BinaryChannel(serverSocket.get(),
                            NetTransferMode.JINGLETCP));

                        runBinaryChannelLoop(serverQueue, serverChannel.get());

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        return serverMainLoop;
    }

    protected TestThread createMainLoopThread(
        final BlockingQueue<IncomingTransferObject> clientQueue,
        final BlockingQueue<Throwable> failureQueue,
        final BinaryChannel clientChannel) {

        TestThread clientMainLoop = new TestThread(failureQueue,
            new Runnable() {
                public void run() {
                    try {
                        runBinaryChannelLoop(clientQueue, clientChannel);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        return clientMainLoop;
    }

    protected TestThread createReceivingThread(
        final BlockingQueue<IncomingTransferObject> serverQueue,
        final BlockingQueue<Throwable> failureQueue) {

        TestThread serverReceiveThread = new TestThread(failureQueue,
            new Runnable() {
                public void run() {

                    try {
                        for (int i = 0; i < 128; i++) {
                            IncomingTransferObject ito = serverQueue.take();

                            assertTrue("" + i, Arrays.equals(getTestArray(i),
                                ito.accept(SubMonitor
                                    .convert(new NullProgressMonitor()))));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        return serverReceiveThread;
    }

    protected TestThread createSenderThread(
        final BlockingQueue<Throwable> failureQueue,
        final BinaryChannel clientChannel) {

        TestThread clientSender = new TestThread(failureQueue, new Runnable() {
            public void run() {

                for (int i = 0; i < 128; i++) {
                    try {
                        clientChannel.sendDirect(TransferDescription
                            .createFileTransferDescription(new JID(
                                "server@gmail.com"),
                                new JID("client@gmail.com"), new Path(
                                    "/test/hello.jpg"), "" + 4722),
                            getTestArray(i), SubMonitor
                                .convert(new NullProgressMonitor()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        return clientSender;
    }

}
