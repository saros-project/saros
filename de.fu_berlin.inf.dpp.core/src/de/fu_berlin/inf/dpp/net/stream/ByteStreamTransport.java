package de.fu_berlin.inf.dpp.net.stream;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.BytestreamListener;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;

import de.fu_berlin.inf.dpp.net.internal.BinaryChannelConnection;
import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnection;
import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;

/**
 * Abstract skeleton for different transport methods.
 */
public abstract class ByteStreamTransport implements ITransport {

    private static final Logger LOG = Logger.getLogger(Socks5Transport.class);

    private BytestreamManager manager;
    private IByteStreamConnectionListener connectionListener;

    @Override
    public IByteStreamConnection connect(final String connectionID,
        final JID peer) throws IOException, InterruptedException {

        if (connectionID == null)
            throw new NullPointerException("connectionID is null");

        if (peer == null)
            throw new NullPointerException("peer is null");

        if (connectionID.isEmpty())
            throw new IllegalArgumentException(
                "connection id must not be empty");

        if (connectionID.contains(String
            .valueOf(ITransport.SESSION_ID_DELIMITER)))
            throw new IllegalArgumentException(
                "connection id must not contain '"
                    + ITransport.SESSION_ID_DELIMITER + "'");

        LOG.debug("establishing bytestream connection to " + peer);

        try {
            return establishBinaryChannel(connectionID, peer.toString());
        } catch (XMPPException e) {
            throw new IOException(e);
        }
    }

    @Override
    public synchronized void uninitialize() {
        if (manager != null) {
            manager.removeIncomingBytestreamListener(streamListener);
            manager = null;
            connectionListener = null;
        }
    }

    /**
     * Handles incoming requests and informs the IBytestreamConnectionListener
     * if a new connection got established
     */
    protected BytestreamListener streamListener = new BytestreamListener() {

        @Override
        public void incomingBytestreamRequest(BytestreamRequest request) {

            LOG.debug("received request to establish a  bytestream connection to "
                + request.getFrom() + " [" + this + "]");

            try {

                IByteStreamConnection connection = acceptRequest(request);

                if (connection == null)
                    return;

                JID peer = new JID(request.getFrom());

                IByteStreamConnectionListener listener = getConnectionListener();

                if (listener == null) {
                    LOG.warn("closing bytestream connection " + connection
                        + " because transport " + this
                        + " was uninitilized during connection establishment");
                    connection.close();
                    return;
                }

                listener.connectionChanged(connection.getConnectionID(), peer,
                    connection, true);

            } catch (InterruptedException e) {
                /*
                 * do not interrupt here as this is called by SMACK and nobody
                 * knows how SMACK handle thread interruption
                 */
                LOG.warn("interrupted while establishing bytestream connection to "
                    + request.getFrom());
            } catch (Exception e) {
                LOG.error("could not establish bytestream connection to "
                    + request.getFrom(), e);
            }
        }
    };

    /**
     * Establishes a IByteStreamConnection to a peer.
     * 
     * @param peer
     * @return IByteStreamConnection to peer
     * @throws XMPPException
     * @throws IOException
     * @throws InterruptedException
     */
    protected IByteStreamConnection establishBinaryChannel(
        String connectionIdentifier, String peer) throws XMPPException,
        IOException, InterruptedException {

        BytestreamManager currentManager = getManager();
        IByteStreamConnectionListener listener = getConnectionListener();

        if (currentManager == null || listener == null)
            throw new IOException(this + " transport is not initialized");

        return new BinaryChannelConnection(new JID(peer), connectionIdentifier,
            new XMPPByteStreamAdapter(currentManager.establishSession(peer,
                connectionIdentifier)), getNetTransferMode(), listener);
    }

    /**
     * Handles a BytestreamRequest requests and returns a IByteStreamConnection.
     * If null is returned the request is handled in different manner (i.e. see
     * {#link Socks5Transport})
     * 
     * 
     * @param request
     * @return IByteStreamConnection or null if handled in different manner
     * @throws InterruptedException
     * @throws XMPPException
     * @throws IOException
     */
    protected IByteStreamConnection acceptRequest(BytestreamRequest request)
        throws XMPPException, IOException, InterruptedException {

        IByteStreamConnectionListener listener = getConnectionListener();

        if (listener == null)
            throw new IOException(this + " transport is not initialized");

        return new BinaryChannelConnection(new JID(request.getFrom()),
            request.getSessionID(),
            new XMPPByteStreamAdapter(request.accept()), getNetTransferMode(),
            listener);
    }

    @Override
    public synchronized void initialize(Connection connection,
        IByteStreamConnectionListener listener) {
        this.connectionListener = listener;
        manager = createManager(connection);
        manager.addIncomingBytestreamListener(streamListener);
    }

    protected final synchronized BytestreamManager getManager() {
        return manager;
    }

    protected final synchronized IByteStreamConnectionListener getConnectionListener() {
        return connectionListener;
    }

    /**
     * @param connection
     * @return the configured BytestreamManager for the specialized transport
     *         method
     */
    abstract protected BytestreamManager createManager(Connection connection);

    abstract protected ConnectionMode getNetTransferMode();
}
