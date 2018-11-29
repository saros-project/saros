package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.net.internal.BinaryChannelConnection.IDPool;
import de.fu_berlin.inf.dpp.net.stream.ByteStream;
import de.fu_berlin.inf.dpp.net.stream.StreamMode;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class BinaryChannelConnectionTest {

  private static final int PIPE_BUFFER_SIZE = 1024 * 1024;

  private static class PipedBytestreamSession implements ByteStream {

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

  private abstract static class StreamConnectionListener implements IByteStreamConnectionListener {

    @Override
    public void connectionClosed(String connectionIdentifier, IByteStreamConnection connection) {
      // NOP

    }

    @Override
    public void connectionChanged(
        String connectionIdentifier, IByteStreamConnection connection, boolean incomingRequest) {
      // NOP
    }

    @Override
    public abstract void receive(BinaryXMPPExtension extension);
  }

  private final JID aliceJID = new JID("alice@baumeister.de");

  private final JID bobJID = new JID("bob@baumeister.de");

  private ByteStream aliceStream;
  private ByteStream bobStream;

  @Before
  public void setUp() throws IOException {
    PipedOutputStream aliceOut = new PipedOutputStream();
    PipedInputStream aliceIn = new PipedInputStream(PIPE_BUFFER_SIZE);

    PipedOutputStream bobOut = new PipedOutputStream();
    PipedInputStream bobIn = new PipedInputStream(PIPE_BUFFER_SIZE);

    aliceOut.connect(bobIn);
    aliceIn.connect(bobOut);

    aliceStream = new PipedBytestreamSession(aliceIn, aliceOut);
    bobStream = new PipedBytestreamSession(bobIn, bobOut);
  }

  private volatile byte[] receivedBytes;

  @Test
  public void testCacheUpdates() throws Exception {

    final List<BinaryXMPPExtension> extensions = new ArrayList<BinaryXMPPExtension>();

    final CountDownLatch received = new CountDownLatch(2);

    BinaryChannelConnection alice =
        new BinaryChannelConnection(
            aliceJID,
            bobJID,
            "junit",
            aliceStream,
            StreamMode.SOCKS5_DIRECT,
            new StreamConnectionListener() {
              @Override
              public void receive(final BinaryXMPPExtension extension) {
                // NOP
              }
            });

    BinaryChannelConnection bob =
        new BinaryChannelConnection(
            bobJID,
            aliceJID,
            "junit",
            bobStream,
            StreamMode.SOCKS5_DIRECT,
            new StreamConnectionListener() {
              @Override
              public void receive(final BinaryXMPPExtension extension) {
                extensions.add(extension);
                received.countDown();
              }
            });

    alice.initialize();
    bob.initialize();

    final TransferDescription description = TransferDescription.newDescription();

    final byte[] bytesToSend = new byte[512];

    try {
      description.setNamespace("foo-namespace-0");
      description.setElementName("bar-0");

      alice.send(description, bytesToSend);

      description.setNamespace("foo-namespace-1");
      description.setElementName("bar-1");

      alice.send(description, bytesToSend);

      received.await(10000, TimeUnit.MILLISECONDS);
    } finally {
      alice.close();
      bob.close();
    }

    // send packet 0
    assertEquals("foo-namespace-0", extensions.get(0).getTransferDescription().getNamespace());

    assertEquals("bar-0", extensions.get(0).getTransferDescription().getElementName());

    assertEquals(
        aliceJID.toString(), extensions.get(0).getTransferDescription().getSender().toString());

    assertEquals(
        bobJID.toString(), extensions.get(0).getTransferDescription().getRecipient().toString());

    // send packet 1
    assertEquals("foo-namespace-1", extensions.get(1).getTransferDescription().getNamespace());

    assertEquals("bar-1", extensions.get(1).getTransferDescription().getElementName());

    assertEquals(
        aliceJID.toString(), extensions.get(1).getTransferDescription().getSender().toString());

    assertEquals(
        bobJID.toString(), extensions.get(1).getTransferDescription().getRecipient().toString());
  }

  @Test
  public void testFragmentationOnLargeDataToBeSend() throws Exception {

    final CountDownLatch received = new CountDownLatch(1);

    BinaryChannelConnection alice =
        new BinaryChannelConnection(
            aliceJID,
            bobJID,
            "junit",
            aliceStream,
            StreamMode.SOCKS5_DIRECT,
            new StreamConnectionListener() {
              @Override
              public void receive(final BinaryXMPPExtension extension) {
                // NOP
              }
            });

    BinaryChannelConnection bob =
        new BinaryChannelConnection(
            bobJID,
            aliceJID,
            "junit",
            bobStream,
            StreamMode.SOCKS5_DIRECT,
            new StreamConnectionListener() {
              @Override
              public void receive(final BinaryXMPPExtension extension) {
                receivedBytes = extension.getPayload();
                received.countDown();
              }
            });

    alice.initialize();
    bob.initialize();

    TransferDescription description = TransferDescription.newDescription();

    description.setNamespace("foo-namespace");
    description.setElementName("bar");
    description.setSender(new JID("sender@local"));
    description.setRecipient(new JID("receiver@local"));

    byte[] bytesToSend = new byte[512 * 1024];

    for (int i = 0; i < bytesToSend.length; i++) bytesToSend[i] = (byte) i;

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
  @Ignore(
      "this test consumes much CPU resources and should only executed manually when making changes")
  public void testFragmentationCleanup() throws Exception {

    long packetSize = 16 * 1024;
    long bytesToTransfer = (1L << 31L); // send 2 GB of data;

    long packetsToSend = bytesToTransfer / packetSize;

    packetsToSend++;

    final CountDownLatch received = new CountDownLatch((int) packetsToSend);

    BinaryChannelConnection alice =
        new BinaryChannelConnection(
            aliceJID,
            bobJID,
            "junit",
            aliceStream,
            StreamMode.SOCKS5_DIRECT,
            new StreamConnectionListener() {
              @Override
              public void receive(final BinaryXMPPExtension extension) {
                // NOP
              }
            });

    BinaryChannelConnection bob =
        new BinaryChannelConnection(
            bobJID,
            aliceJID,
            "junit",
            bobStream,
            StreamMode.SOCKS5_DIRECT,
            new StreamConnectionListener() {
              @Override
              public void receive(final BinaryXMPPExtension extension) {
                receivedBytes = extension.getPayload();
                received.countDown();
              }
            });

    alice.initialize();
    bob.initialize();

    TransferDescription description = TransferDescription.newDescription();

    description.setNamespace("foo-namespace");
    description.setElementName("bar");

    byte[] bytesToSend = new byte[(int) packetSize];

    for (int i = 0; i < bytesToSend.length; i++) bytesToSend[i] = (byte) i;

    try {
      for (int i = 0; i < packetsToSend - 1; i++) alice.send(description, bytesToSend);

      for (int i = 0; i < bytesToSend.length; i++) bytesToSend[i] = (byte) 0x7F;

      alice.send(description, bytesToSend);

      received.await(60000, TimeUnit.MILLISECONDS);
    } finally {
      alice.close();
      bob.close();
    }

    assertTrue("remote side crashed", received.getCount() == 0);

    assertArrayEquals("fragmentation error", bytesToSend, receivedBytes);
  }

  @Test
  public void testIDPool() {

    IDPool pool = new IDPool();

    for (int i = 0; i < 32; i++) assertEquals(i, pool.nextID());

    assertEquals(-1, pool.nextID());

    pool.freeID(31);
    pool.freeID(0);

    assertEquals(0, pool.nextID());
    assertEquals(31, pool.nextID());
    assertEquals(-1, pool.nextID());
  }
}
