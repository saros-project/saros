package de.fu_berlin.inf.dpp.net.jingle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;
import org.limewire.nio.NIODispatcher;
import org.limewire.rudp.DefaultRUDPContext;
import org.limewire.rudp.DefaultRUDPSettings;
import org.limewire.rudp.DefaultUDPService;
import org.limewire.rudp.RudpMessageDispatcher;
import org.limewire.rudp.UDPMultiplexor;
import org.limewire.rudp.UDPSelectorProvider;
import org.limewire.rudp.messages.RUDPMessageFactory;
import org.limewire.rudp.messages.impl.DefaultMessageFactory;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferData.FileTransferType;

/**
 * This class implements a file transfer session with jingle.
 * 
 * Jingle is a XMPP-extension with id XEP-0166. Documentation can be found at
 * http://xmpp.org/extensions/xep-0166.html .
 * 
 * This implementation uses UDP as transport protocol. To ensure no data loss
 * the RUDP implementation from the Limewire project are used.
 * 
 * Documentation for the RUDP component from limewire can be found at:
 * http://wiki.limewire.org/index.php?title=Javadocs .
 * 
 * @author chjacob
 * 
 */
public class JingleFileTransferSession extends JingleMediaSession {

    private class Receive extends Thread {

        private ObjectInputStream input;

        public Receive(ObjectInputStream ii) {
            this.input = ii;
        }

        public void run() {
            try {

                while (true) {
                    logger.debug("waiting on port " + localPort);

                    /* get number of file to be transfer. */
                    int fileNumber;

                    fileNumber = input.readInt();
                    logger.debug("incoming file number: " + fileNumber);

                    for (int i = 0; i < fileNumber; i++) {

                        /* receive file data */
                        JingleFileTransferData data = (JingleFileTransferData) input
                                .readObject();

                        if (data.type == FileTransferType.FILELIST_TRANSFER) {
                            logger.debug("received file List");
                            logger.debug(data.file_list_content);
                            /* inform listener. */
                            for (IJingleFileTransferListener listener : listeners) {
                                listener.incomingFileList(
                                        data.file_list_content, data.sender);
                            }

                        } else if (data.type == FileTransferType.RESOURCE_TRANSFER) {
                            logger.debug("received resource "
                                    + data.file_project_path);
                            for (IJingleFileTransferListener listener : listeners) {
                                listener.incomingResourceFile(data,
                                        new ByteArrayInputStream(data.content));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.info("receive-thread interrupted");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private static Logger logger = Logger
            .getLogger(JingleFileTransferSession.class);

    private Receive udpReceiveThread;
    private JingleFileTransferData[] transferList;
    private Set<IJingleFileTransferListener> listeners;
    private UDPSelectorProvider udpSelectorProvider;
    private Socket udpSocket;
    private ObjectOutputStream udpObjectOutputStream;
    private ObjectInputStream udpObjectInputStream;
    private JID remoteJid;
    private String ip;
    private int localPort;
    private int remotePort;

    /**
     * TODO CJ: write javadoc
     * 
     * @param payloadType
     * @param remote
     * @param local
     * @param mediaLocator
     * @param jingleSession
     * @param transferData
     * @param listeners
     */
    public JingleFileTransferSession(PayloadType payloadType,
            TransportCandidate remote, TransportCandidate local,
            String mediaLocator, JingleSession jingleSession,
            JingleFileTransferData[] transferData, JID remoteJid,
            Set<IJingleFileTransferListener> listeners) {
        super(payloadType, remote, local, mediaLocator, jingleSession);

        this.remoteJid = remoteJid;
        this.transferList = transferData;
        this.listeners = listeners;
        logger.debug("JingleFileTransferSesseion created " + local.getLocalIp()
                + "/" + local.getIp() + ":" + local.getPort() + " <-> "
                + remote.getLocalIp() + "/" + remote.getIp() + ":"
                + remote.getPort());
        initialize();
    }

    /**
     * Initialization of the session. It tries to create sockets for both, TCP
     * and UDP. The UDP Socket is a reliable implementation from the Limewire
     * project. Documentation can be found at http://wiki.limewire.org.
     */
    @Override
    public void initialize() {

        if (this.getLocal().getSymmetric() != null) {
            ip = this.getLocal().getIp();
            localPort = getFreePort();
            remotePort = this.getLocal().getSymmetric().getPort();

        } else {
            ip = this.getRemote().getIp();
            localPort = this.getLocal().getPort();
            remotePort = this.getRemote().getPort();
        }

        // create RUDP service
        RudpMessageDispatcher dispatcher = new RudpMessageDispatcher();
        DefaultUDPService service = new DefaultUDPService(dispatcher);
        RUDPMessageFactory factory = new DefaultMessageFactory();
        udpSelectorProvider = new UDPSelectorProvider(new DefaultRUDPContext(
                factory, NIODispatcher.instance().getTransportListener(),
                service, new DefaultRUDPSettings()));
        UDPMultiplexor udpMultiplexor = udpSelectorProvider.openSelector();
        dispatcher.setUDPMultiplexor(udpMultiplexor);
        NIODispatcher.instance().registerSelector(udpMultiplexor,
                udpSelectorProvider.getUDPSocketChannelClass());
        try {
            service.start(localPort);
        } catch (IOException e) {
            logger.debug("Failed to create RUDP service");
        }

        // server side
        if (!getJingleSession().getInitiator().equals(
                getJingleSession().getConnection().getUser())) {
            try {
                Socket usock = udpSelectorProvider.openAcceptorSocketChannel()
                        .socket();
                usock.setSoTimeout(0);
                usock.connect(new InetSocketAddress(InetAddress.getByName(ip),
                        remotePort));
                usock.setKeepAlive(true);
                JingleFileTransferSession.this.udpSocket = usock;
                JingleFileTransferSession.this.udpObjectOutputStream = new ObjectOutputStream(
                        udpSocket.getOutputStream());
                JingleFileTransferSession.this.udpObjectInputStream = new ObjectInputStream(
                        udpSocket.getInputStream());
                informListenersAboutConnection("UDP");
            } catch (IOException e) {
                logger.debug("Failed to listen with UDP");
            }
        } else { // client sides

            try { // to create a udp socket
                Socket usock = udpSelectorProvider.openSocketChannel().socket();
                usock.setSoTimeout(0);
                usock.setKeepAlive(true);
                usock.connect(new InetSocketAddress(InetAddress.getByName(ip),
                        remotePort));
                this.udpSocket = usock;
                this.udpObjectOutputStream = new ObjectOutputStream(udpSocket
                        .getOutputStream());
                this.udpObjectInputStream = new ObjectInputStream(udpSocket
                        .getInputStream());
                logger.debug("successfully connected with UDP");
                informListenersAboutConnection("UDP");
                logger.debug("JingleFileTransferSesseion initialized");
            } catch (UnknownHostException e1) {
                logger.debug("Invalid IP-address of jingle remote (UDP)");
            } catch (IOException e1) {
                logger.debug("Failed to connect with UDP");
                try {
                    this.getJingleSession().terminate();
                } catch (XMPPException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void informListenersAboutConnection(String protocol) {
        for (IJingleFileTransferListener listener : listeners) {
            listener.connected(protocol, ip);
        }
    }

    /**
     * This method is called from the JingleFileTransferManager to send files
     * with this session. This method tries to transmit the files with UDP/RUDP.
     * When this fails it throws an JingleSessionException.
     * 
     * @throws JingleSessionException
     */
    public void sendFiles(JingleFileTransferData[] transferData)
            throws JingleSessionException {

        this.transferList = transferData;

        if (udpSocket != null) {
            logger.debug("sending with UDP to " + ip + ":" + remotePort);
            try {
                logger.debug("sending with UDP..");
                transmit(udpObjectOutputStream);
                return;
            } catch (IOException e) {
                logger.debug("sending with UDP failed, use IBB instead..", e);
            }
        }
        throw new JingleSessionException("Failed to send files with Jingle");
    }

    /**
     * This method is called from Jingle when a jingle session is established. A
     * new thread for receiving is started.
     */
    @Override
    public void startReceive() {

        logger.debug("start receiving");

        if (udpSocket != null && udpObjectInputStream != null) {
            this.udpReceiveThread = new Receive(udpObjectInputStream);
            this.udpReceiveThread.start();
        }
    }

    /**
     * This method is called from Jingle when a jingle session is established.
     * This method tries to transmit the files with UDP/RUDP. When this fails,
     * the listeners are informed about the failed try.
     */
    @Override
    public void startTrasmit() {
        logger.debug("JingleFileTransferSesseion: start transmitting");

        if (transferList == null)
            return;

        if (udpSocket != null) {
            try {
                logger.debug("sending with UDP..");
                transmit(udpObjectOutputStream);
                return;
            } catch (IOException e) {
                logger.warn("sending with UDP failed, use UDP instead..", e);
            }
        }
        if (transferList.length > 0) {
            for (IJingleFileTransferListener listener : listeners) {
                listener.failedToSendFileListWithJingle(remoteJid,
                        transferList[0]);
            }
        }
    }

    private synchronized void transmit(ObjectOutputStream oo)
            throws IOException {
        assert (oo != null);

        oo.writeInt(transferList.length);
        oo.flush();
        logger.debug("sent transfer number : " + transferList.length);

        for (JingleFileTransferData data : transferList) {

            /* send data */
            oo.writeObject(data);
            oo.flush();
            logger.debug("sent data for : " + data.file_project_path);

        }
        transferList = null;
    }

    @Override
    public void stopReceive() {
        logger.debug("JingleFileTransferSesseion: stop receiving");
        if (udpReceiveThread != null)
            udpReceiveThread.interrupt();
    }

    @Override
    public void stopTrasmit() {
        logger.debug("JingleFileTransferSesseion: stop transmitting");
        try {
            if (udpSocket != null) {
                udpObjectOutputStream.close();
                udpObjectInputStream.close();
                udpSocket.close();
            }
        } catch (IOException e) {
            logger.debug("Failed to close all sockets");
        }
    }

    @Override
    public void setTrasmit(boolean active) {
        logger.debug("JingleFileTransferSesseion: set transmit to " + active);
        // TODO CJ: What have to do here?
    }

    /**
     * Obtain a free port we can use.
     * 
     * @return A free port number.
     */
    protected int getFreePort() {
        ServerSocket ss;
        int freePort = 0;

        for (int i = 0; i < 10; i++) {
            freePort = (int) (10000 + Math.round(Math.random() * 10000));
            freePort = freePort % 2 == 0 ? freePort : freePort + 1;
            try {
                ss = new ServerSocket(freePort);
                freePort = ss.getLocalPort();
                ss.close();
                return freePort;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ss = new ServerSocket(0);
            freePort = ss.getLocalPort();
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return freePort;
    }
}
