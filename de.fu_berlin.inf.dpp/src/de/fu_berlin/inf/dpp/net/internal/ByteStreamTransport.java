package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.BytestreamListener;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Abstract skeleton for different transport methods.
 */
public abstract class ByteStreamTransport implements ITransport {

    private static final Logger LOG = Logger.getLogger(Socks5Transport.class);

    private BytestreamManager manager;
    private IByteStreamConnectionListener connectionListener;

    @Override
    public IByteStreamConnection connect(final JID peer) throws IOException,
        InterruptedException {

        LOG.debug("establishing bytestream session to " + peer);

        try {

            BinaryChannel channel = establishBinaryChannel(peer.toString());
            return new BinaryChannelConnection(peer, channel,
                connectionListener);

        } catch (XMPPException e) {
            throw new IOException(e);
        }
    }

    @Override
    public synchronized void uninitialize() {
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

        @Override
        public void incomingBytestreamRequest(BytestreamRequest request) {

            LOG.debug("received request to establish a " + getNetTransferMode()
                + " bytestream session to " + request.getFrom());

            try {

                BinaryChannel channel = acceptRequest(request);

                if (channel == null)
                    return;

                JID peer = new JID(request.getFrom());
                connectionListener.connectionChanged(peer,
                    new BinaryChannelConnection(peer, channel,
                        connectionListener), true);

            } catch (InterruptedException e) {
                /*
                 * do not interrupt here as this is called by SMACK and nobody
                 * knows how SMACK handle thread interruption
                 */
                LOG.warn("interrupted while establishing byte stream session to "
                    + request.getFrom());
            } catch (Exception e) {
                LOG.error("could not establish byte stream session to "
                    + request.getFrom(), e);
            }
        }
    };

    /**
     * Establishes a BinaryChannel to a peer.
     * 
     * @param peer
     * @return BinaryChannel to peer
     * @throws XMPPException
     * @throws IOException
     * @throws InterruptedException
     */
    protected BinaryChannel establishBinaryChannel(String peer)
        throws XMPPException, IOException, InterruptedException {

        BytestreamManager manager = getManager();

        if (manager == null)
            throw new IOException(this + " transport is not initialized");

        BytestreamSession session = manager.establishSession(peer.toString());

        return new BinaryChannel(session, getNetTransferMode());
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
     * @throws XMPPException
     * @throws IOException
     */
    protected BinaryChannel acceptRequest(BytestreamRequest request)
        throws XMPPException, IOException, InterruptedException {

        BytestreamSession session = request.accept();
        BinaryChannel channel = new BinaryChannel(session, getNetTransferMode());

        return channel;
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

    /**
     * @param connection
     * @return the configured BytestreamManager for the specialized transport
     *         method
     */
    abstract protected BytestreamManager createManager(Connection connection);

}
