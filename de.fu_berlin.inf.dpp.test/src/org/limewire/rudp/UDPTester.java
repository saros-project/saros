package org.limewire.rudp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.NumberFormat;

import org.limewire.nio.NIODispatcher;
import org.limewire.rudp.messages.RUDPMessageFactory;
import org.limewire.rudp.messages.impl.DefaultMessageFactory;
import org.limewire.util.AssertComparisons;

public class UDPTester {

    private static final int TIMEOUT = 10 * 1000;
    private static final int BLOCK_SIZE = 512;
    private static long startTime;
    private static long lastTime;
    
    private static UDPSelectorProvider udpSelectorProvider;
    
    /**
     * host:port
     */
    public static void main(String[] args) {
        if (args.length != 4)
            printHelp();
        
        String host = args[1];
        int port = Integer.parseInt(args[2]);
        int localPort = Integer.parseInt(args[3]);
        try {
            if ("-c".equals(args[0])) {
                startUDPServices(localPort);
                echoClient(host, port);
            } else if ("-s".equals(args[0])) {
                startUDPServices(localPort);
                echoServer(host, port);
            } else {
                printHelp();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void startUDPServices(int port) throws IOException {
        DefaultMessageDispatcher dispatcher = new DefaultMessageDispatcher();
        DefaultUDPService service = new DefaultUDPService(dispatcher);
        RUDPMessageFactory factory = new DefaultMessageFactory();
        udpSelectorProvider = new UDPSelectorProvider(new DefaultRUDPContext(
                factory, NIODispatcher.instance().getTransportListener(),
                service, new DefaultRUDPSettings()));
        UDPMultiplexor udpMultiplexor = udpSelectorProvider.openSelector();
        dispatcher.setUDPMultiplexor(udpMultiplexor);
        NIODispatcher.instance().registerSelector(udpMultiplexor, udpSelectorProvider.getUDPSocketChannelClass());
        
        service.start(port);
    }

    private static void printHelp() {
        System.err.println("usage: -c|-s host port localPort");
        System.exit(1);
    }

    public static void echoClient(String host, int port) throws IOException {
        Socket usock = udpSelectorProvider.openSocketChannel().socket();
        usock.connect(new InetSocketAddress(host, port));
        usock.setSoTimeout(TIMEOUT);
        
        OutputStream ostream = usock.getOutputStream();
        // InputStream istream = usock.getInputStream();

        startBandwidth();
        
        // ClientBlockReader reader = new ClientBlockReader(istream, numBlocks);
        // reader.start();
        try {
            // setup transfer data
            byte data[] = new byte[512];
            for (int i = 0; i < 512; i++)
                data[i] = (byte) (i % 256);

            long total = 0;
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                ostream.write(data, 0, 512);
                total += 512;
                updateBandwidth(total);
            }
        } finally {
            // try { reader.join(); } catch (InterruptedException ie){}
        }
    }

    public static void echoServer(String host, int port) throws IOException {
        Socket usock = udpSelectorProvider.openAcceptorSocketChannel().socket();
        usock.connect(new InetSocketAddress(host, port));
        usock.setSoTimeout(TIMEOUT);
        
        // OutputStream ostream = usock.getOutputStream();
        InputStream istream = usock.getInputStream();

        startBandwidth();

        long total = 0;
        byte[] data = new byte[BLOCK_SIZE];
        int len;
        for (int i = 0; i < Integer.MAX_VALUE; i += len) {
            len = istream.read(data);
            if (len == -1) {
                throw new EOFException();
            }
            
            total += len;

            if (len != 512)
                System.out.println("Abnormal data size: " + len + " loc: " + i);

            for (int j = 0; j < len; j++) {
                int btest = data[j] & 0xff;
                AssertComparisons.assertEquals(
                        "Read unexpected value at offset " + (i + j), btest,
                        (i + j) % 256);
            }
            
            updateBandwidth(total);

            // ostream.write(data, 0, len);
        }
    }

    private static void startBandwidth() {
        startTime = System.currentTimeMillis();
        lastTime = startTime;
    }

    private static void updateBandwidth(long total) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime > 1000) {
            long bandwidth = total / ((currentTime - startTime) / 1000);
            System.out.println("Average (bytes/s): " + NumberFormat.getInstance().format(bandwidth));
            lastTime = currentTime;
        }
    }

}
