package saros.net.stun.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import saros.net.stun.IStunService;
import saros.net.util.NetworkingUtils;

public final class StunServiceImpl implements IStunService {

  private static final Logger log = Logger.getLogger(StunServiceImpl.class);

  private boolean isDirectConnection = false;

  private Set<InetSocketAddress> publicIPAddresses = new HashSet<InetSocketAddress>();

  @Override
  public synchronized boolean isDirectConnectionAvailable() {
    return isDirectConnection;
  }

  @Override
  public synchronized Collection<InetSocketAddress> getPublicIpAddresses() {
    return new ArrayList<InetSocketAddress>(publicIPAddresses);
  }

  @Override
  public Collection<InetSocketAddress> discover(String stunAddress, int stunPort, int timeout) {

    if (stunAddress == null) throw new NullPointerException("STUN address is null");

    if (stunPort == 0) stunPort = DEFAULT_STUN_PORT;

    if (stunPort <= 0 || stunPort >= 65536)
      throw new IllegalArgumentException("stun port is not in range of 1 - 65535");

    synchronized (this) {
      publicIPAddresses.clear();
      isDirectConnection = false;
    }

    List<InetAddress> localInetAddresses;
    InetAddress stunInetAddress = null;

    try {
      stunInetAddress = InetAddress.getByName(stunAddress);
      localInetAddresses = NetworkingUtils.getAllNonLoopbackLocalIPAddresses(false);

    } catch (IOException e) {
      log.error(
          "error retrieving local IP addresses or STUN Server IP address: " + e.getMessage(), e);
      return new ArrayList<InetSocketAddress>();
    }

    List<Thread> discoveryThreads = new ArrayList<Thread>();

    for (InetAddress address : localInetAddresses) {
      Thread discoveryThread =
          new Thread(new StunDiscovery(address, stunInetAddress, stunPort, timeout));

      discoveryThreads.add(discoveryThread);
      discoveryThread.setName("StunDiscovery-" + address.getHostAddress());
      discoveryThread.start();
    }

    while (!discoveryThreads.isEmpty()) {
      try {
        Thread discoveryThread = discoveryThreads.remove(0);
        discoveryThread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }

    return getPublicIpAddresses();
  }

  /** Thread class for performing STUN discovery to retrieve public IP addresses */
  private class StunDiscovery implements Runnable {

    private static final short BINDING_REQUEST = 0x0001;
    private static final short BINDING_RESPONSE = 0x0101;

    private static final short CHANGE_REQUEST = 0x0003;
    private static final int MAGIC_COOKIE = 0x2112A442;

    private static final short MAPPED_ADDRESS = 0x0001;
    private static final short XOR_MAPPED_ADDRESS = 0x0020;
    private static final short ERROR_CODE = 0x0009;

    private static final short IP4_FAMILY = 0x0001;
    private static final short IP6_FAMILY = 0x0002;

    private static final int MINIMUM_IP6_MTU_SIZE = 1280;

    private static final int STUN_HEADER_SIZE = 20;

    private InetAddress localAddress;
    private InetAddress stunAddress;
    private int stunPort;
    private int timeout;

    private Random random = new Random();

    public StunDiscovery(
        InetAddress localAddress, InetAddress stunAddress, int stunPort, int timeout) {
      this.localAddress = localAddress;
      this.stunAddress = stunAddress;
      this.stunPort = stunPort;
      this.timeout = timeout;
    }

    @Override
    public void run() {

      InetSocketAddress publicInetAddress = null;

      try {
        publicInetAddress =
            performStunDiscovery(
                new InetSocketAddress(stunAddress, stunPort),
                new InetSocketAddress(localAddress, 0),
                timeout);
      } catch (IOException e) {
        log.error("an error occurred while performing a STUN discovery: " + e.getMessage(), e);
        return;
      } catch (Exception e) {
        log.error(
            "an internal error occurred while performing a STUN discovery: " + e.getMessage(), e);
        return;
      }

      if (publicInetAddress.getAddress().isAnyLocalAddress()) return;

      synchronized (StunServiceImpl.this) {
        if (publicInetAddress.getAddress().equals(localAddress)) isDirectConnection = true;

        log.debug(
            "discovered public WAN-IP: "
                + publicInetAddress.getAddress().getHostAddress()
                + " through interface "
                + localAddress.getHostAddress());
        publicIPAddresses.add(publicInetAddress);
      }
    }

    /**
     * Performs a stun discovery over UDP using RFC 5389. Should be compatible with RFC 3489 too.
     *
     * @param stunServer the address of the stun server
     * @param localAddress the local address that the UDP should bound to
     * @param timeout how long this method should wait for a response from the stun server
     * @return the public IP address and port or a wildcard address if the discovery failed
     * @throws IOException if an I/O error occurs
     */
    private InetSocketAddress performStunDiscovery(
        SocketAddress stunServer, SocketAddress localAddress, int timeout) throws IOException {

      Thread senderThread = null;

      InetAddress mappedInetAddress = null;
      InetAddress xorMappedInetAddress = null;
      int port = 0;

      int transactionId0 = random.nextInt();
      int transactionId1 = random.nextInt();
      int transactionId2 = random.nextInt();

      final DatagramSocket socket = new DatagramSocket(localAddress);

      // see STUN HEADER: http://tools.ietf.org/html/rfc5389#page-10

      try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataOutputStream dout = new DataOutputStream(out);
        dout.writeShort(BINDING_REQUEST);
        dout.writeShort(8); // CHANGE_REQUEST SIZE
        dout.writeInt(MAGIC_COOKIE);
        dout.writeInt(transactionId0);
        dout.writeInt(transactionId1);
        dout.writeInt(transactionId2);

        /*
         * this should not be send using RFC 5389, because we will get a
         * response error. Normally a server should send the public IP
         * even if an error occurs, so it is fine for now
         */
        dout.writeShort(CHANGE_REQUEST);
        dout.writeShort(4);
        dout.writeInt(0);
        dout.close();

        socket.connect(stunServer);
        socket.setSoTimeout(timeout);

        byte[] requestData = out.toByteArray();

        final DatagramPacket packet = new DatagramPacket(requestData, requestData.length);

        final CountDownLatch responseReceived = new CountDownLatch(1);

        // we are using UDP, and since there is no guarantee that these
        // packets ever reach their destination we have to resent them
        senderThread =
            new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    long sendDelay = 500;
                    while (!Thread.currentThread().isInterrupted()) {
                      try {
                        log.trace("sending STUN request");
                        socket.send(packet);
                        if (responseReceived.await(sendDelay, TimeUnit.MILLISECONDS)) break;

                        sendDelay *= 2;
                      } catch (IOException e) {
                        if (!Thread.currentThread().isInterrupted())
                          log.error("error sending STUN request", e);

                        break;
                      } catch (InterruptedException e) {
                        break;
                      }
                    }
                  }
                });

        senderThread.setName("StunSender-" + localAddress);
        senderThread.start();

        DatagramPacket response =
            new DatagramPacket(new byte[MINIMUM_IP6_MTU_SIZE], MINIMUM_IP6_MTU_SIZE);

        byte[] responseData = null;

        int retries = 10;

        while (retries-- > 0) {
          socket.receive(response);
          responseData = response.getData();

          if (responseData.length <= STUN_HEADER_SIZE) {
            log.warn("received STUN response with invalid header");
            continue;
          }

          // compare the transaction ID / Magic cookie
          for (int i = 4; i < STUN_HEADER_SIZE; i++) {
            if (requestData[i] != responseData[i]) {
              log.warn("received STUN response with invalid transaction id");
              continue;
            }
          }

          break;
        }

        responseReceived.countDown();
        senderThread.interrupt();
        socket.close();

        if (retries == 0) return new InetSocketAddress((InetAddress) null, 0);

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(responseData));

        short responseCode = in.readShort();

        int attributesLength = in.readUnsignedShort();

        log.trace("received STUN response, payload length is: " + attributesLength + " bytes");

        byte[] transactionId = new byte[16];
        in.read(transactionId);

        if (responseCode != BINDING_RESPONSE)
          log.warn(
              "received bad STUN response code from server: 0x"
                  + Integer.toHexString(responseCode & 0xFFFF));

        while (attributesLength > 4) {

          short code = in.readShort();
          int length = in.readUnsignedShort();

          attributesLength -= length + 4;

          log.trace(
              "processing STUN value code: 0x"
                  + Integer.toHexString(code & 0xFFFF)
                  + ", length: "
                  + length);

          if (attributesLength < 0) {
            log.warn(
                "STUN response code 0x" + Integer.toHexString(code & 0xFFFF) + " is corrupted");
            break;
          }

          boolean isXorMapped = false;
          switch (code) {
            case XOR_MAPPED_ADDRESS:
              isXorMapped = true;
              // $FALL-THROUGH$
            case MAPPED_ADDRESS:
              short ipFamily = in.readShort();

              port = in.readUnsignedShort();

              length -= 4;

              if (isXorMapped) {
                port ^= MAGIC_COOKIE >>> 16;
              }

              byte[] inetAddress;

              if (ipFamily == IP4_FAMILY) inetAddress = new byte[4];
              else if (ipFamily == IP6_FAMILY) inetAddress = new byte[16];
              else {
                log.warn(
                    "received unknown IP family value: 0x"
                        + Integer.toHexString(ipFamily & 0xFFFF));
                skipFully(in, length);
                continue;
              }

              in.read(inetAddress);

              if (isXorMapped) {

                xorBytes(inetAddress, 0, MAGIC_COOKIE, false);

                if (ipFamily == IP6_FAMILY) {
                  xorBytes(inetAddress, 4, transactionId0, false);
                  xorBytes(inetAddress, 8, transactionId1, false);
                  xorBytes(inetAddress, 12, transactionId2, false);
                }
              }

              if (isXorMapped) xorMappedInetAddress = InetAddress.getByAddress(inetAddress);
              else mappedInetAddress = InetAddress.getByAddress(inetAddress);

              break;

            case ERROR_CODE:
              int errorNumber = in.readInt();

              errorNumber = (((errorNumber >>> 8) & 0x7) * 100) + (errorNumber & 0xFF);

              length -= 4;

              byte[] errorMessageBytes = new byte[length];
              in.readFully(errorMessageBytes);
              processError(errorNumber, new String(errorMessageBytes, "UTF-8"));
              break;

            default:
              log.trace("skipping STUN value with code: 0x" + Integer.toHexString(code & 0xFFFF));
              skipFully(in, length);
          }
        }
      } catch (SocketTimeoutException e) {
        log.warn(
            "received no response from STUN server "
                + stunServer
                + " at local address "
                + localAddress);
      } finally {
        if (senderThread != null && senderThread.isAlive()) senderThread.interrupt();

        socket.close();
      }

      return xorMappedInetAddress == null
          ? new InetSocketAddress(mappedInetAddress, port)
          : new InetSocketAddress(xorMappedInetAddress, port);
    }

    private void skipFully(InputStream in, long length) throws IOException {
      while (length > 0) {
        length -= in.skip(length);
      }
    }

    private void xorBytes(byte[] bytes, int offset, int value, boolean isHostOrder) {
      if (isHostOrder) {
        bytes[offset + 0] ^= ((value & 0x000000FF) >>> 0);
        bytes[offset + 1] ^= ((value & 0x0000FF00) >>> 8);
        bytes[offset + 2] ^= ((value & 0x00FF0000) >>> 16);
        bytes[offset + 3] ^= ((value & 0xFF000000) >>> 24);
      } else {
        bytes[offset + 3] ^= ((value & 0x000000FF) >>> 0);
        bytes[offset + 2] ^= ((value & 0x0000FF00) >>> 8);
        bytes[offset + 1] ^= ((value & 0x00FF0000) >>> 16);
        bytes[offset + 0] ^= ((value & 0xFF000000) >>> 24);
      }
    }

    private boolean processError(int code, String message) {
      switch (code) {
        case 400:
          log.error("400 (Bad Request): The request was malformed." + " [" + message + "]");
          return false;

        case 401:
          log.error(
              "401 (Unauthorized): The Binding Request did not contain a MESSAGE-INTEGRITY attribute."
                  + " ["
                  + message
                  + "]");
          return false;
        case 420:
          log.error(
              "420 (Unknown Attribute): The server did not understand a mandatory attribute in the request."
                  + " ["
                  + message
                  + "]");
          return false;
        case 430:
          log.error(
              "430 (Stale Credentials): The Binding Request did contain a MESSAGE-INTEGRITY attribute, but it used a shared secret that has expired."
                  + " ["
                  + message
                  + "]");
          return false;
        case 431:
          log.error(
              "431 (Integrity Check Failure): The Binding Request contained a MESSAGE-INTEGRITY attribute, but the HMAC failed verification."
                  + " ["
                  + message
                  + "]");
          return false;
        case 432:
          log.error(
              "432 (Missing Username): The Binding Request contained a MESSAGE-INTEGRITY attribute, but not a USERNAME attribute."
                  + " ["
                  + message
                  + "]");
          return false;
        case 433:
          log.error(
              "433 (Use TLS): The Shared Secret request has to be sent over TLS, but was not received over TLS."
                  + " ["
                  + message
                  + "]");
          return false;
        case 500:
          log.error(
              "500 (Server Error): The server has suffered a temporary error."
                  + " ["
                  + message
                  + "]");
          return true;
        case 600:
          log.error(
              "600 (Global Failure:) The server is refusing to fulfill the request."
                  + " ["
                  + message
                  + "]");
          return false;
      }
      return false;
    }
  }
}
