package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.BytestreamListener;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;

import de.fu_berlin.inf.dpp.net.IPacketDispatcher;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;

/**
 * Abstract skeleton for different transport methods
 */
public abstract class ByteStreamTransport implements ITransport {

    private static final Logger log = Logger.getLogger(Socks5Transport.class);

    protected BytestreamManager manager;
    protected IByteStreamConnectionListener connectionListener;
    protected IPacketDispatcher dispatcher;
    protected JID localJID;

    /**
     * @param remoteJID
     *            the JID of the user we'd like to connect
     * 
     * @return a new connection to peer
     */
    @Override
    public IByteStreamConnection connect(JID remoteJID, SubMonitor progress)
        throws IOException, InterruptedException {

        progress.subTask("Try to initiate bytestream with " + toString());

        try {

            return establishBinaryChannel(remoteJID, progress);

        } catch (XMPPException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void initializeTransport(Connection connection, JID localJID,
        IPacketDispatcher dispatcher, IByteStreamConnectionListener listener) {
        this.connectionListener = listener;
        this.dispatcher = dispatcher;
        this.localJID = localJID;
        manager = getManager(connection);
        manager.addIncomingBytestreamListener(streamListener);
    }

    /**
     * Disposes the transport method. Doesn't close running connections (to be
     * done by DataTransferManager).
     */
    @Override
    public void disposeTransport() {
        if (manager != null) {
            manager.removeIncomingBytestreamListener(streamListener);
            manager = null;
        }
    }

    /**
     * Handles incoming requests and informs the IBytestreamConnectionListener
     * if a new connection got established
     */
    protected BytestreamListener streamListener = new BytestreamListener() {

        public void incomingBytestreamRequest(BytestreamRequest request) {

            log.info("Received request to establish a " + getTransportMode()
                + " bytestream connection from " + request.getFrom());

            try {

                ByteStreamConnection channel = acceptRequest(request);

                if (channel == null)
                    return;

                JID remoteJID = new JID(request.getFrom());

                connectionListener.connectionChanged(remoteJID, channel, true);

            } catch (InterruptedException e) {
                log.debug("Interrupted while initiating new session.");
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                log.error(
                    "Socket crashed, no session for request established: ", e);
            }
        }
    };

    /**
     * Establishes a BinaryChannel to a peer.
     * 
     * @param remoteJID
     * @param progress
     * @return BinaryChannel to peer
     * @throws XMPPException
     * @throws IOException
     * @throws InterruptedException
     */
    protected ByteStreamConnection establishBinaryChannel(JID remoteJID,
        SubMonitor progress) throws XMPPException, IOException,
        InterruptedException {
        BytestreamSession session = manager.establishSession(remoteJID
            .toString());

        return new ByteStreamConnection(session, dispatcher,
            connectionListener, getTransportMode(), localJID, remoteJID);
    }

    /**
     * Handles a BytestreamRequest requests and returns a BinaryChannel. If null
     * is returned the request is handled in different manner (i.e. see {#link
     * Socks5Transport})
     * 
     * 
     * @param request
     * @return BinaryChannel or null if handled in different manner
     * @throws InterruptedException
     * @throws IOException
     */
    protected ByteStreamConnection acceptRequest(BytestreamRequest request)
        throws InterruptedException, IOException {

        try {
            JID remoteJID = new JID(request.getFrom());
            BytestreamSession session = request.accept();
            return new ByteStreamConnection(session, dispatcher,
                connectionListener, getTransportMode(), localJID, remoteJID);
        } catch (XMPPException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    abstract public NetTransferMode getTransportMode();

    /**
     * @param connection
     * @return The configured BytestreamManager for the specialized transport
     *         method
     */
    abstract protected BytestreamManager getManager(Connection connection);

    @Override
    public String toString() {
        return getTransportMode().getXEP();
    }

}
