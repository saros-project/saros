package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.BytestreamListener;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.ITransport;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.util.CausedIOException;

/**
 * Abstract skeleton for different transport methods
 */
public abstract class BytestreamTransport implements ITransport {

    private static final Logger log = Logger.getLogger(Socks5Transport.class);

    protected BytestreamManager manager;
    protected IBytestreamConnectionListener connectionListener;

    /**
     * @param peer
     *            the JID of the user we'd like to connect
     * 
     * @return a new connection to peer
     */
    public IBytestreamConnection connect(final JID peer, SubMonitor progress)
        throws IOException, InterruptedException {

        progress.subTask("Try to initiate bytestream with " + toString());

        BinaryChannel channel = null;

        try {

            channel = establishBinaryChannel(peer.toString(), progress);
            return new BinaryChannelConnection(peer, channel,
                connectionListener);

        } catch (XMPPException e) {
            throw new CausedIOException(e);
        }
    }

    /**
     * Disposes the transport method. Doesn't close running connections (to be
     * done by DataTransferManager).
     */
    public void disposeXMPPConnection() {
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

            log.info("Received request to establish a "
                + getDefaultNetTransferMode() + " bytestream connection from "
                + request.getFrom());

            try {

                BinaryChannel channel = acceptRequest(request);

                if (channel == null)
                    return;

                JID peer = new JID(request.getFrom());

                connectionListener.connectionChanged(peer,
                    new BinaryChannelConnection(peer, channel,
                        connectionListener));

            } catch (InterruptedException e) {
                log.debug("Interrupted while initiating new session.");
            } catch (Exception e) {
                log.error(
                    "Socket crashed, no session for request established: ", e);
            }
        }
    };

    /**
     * Establishes a BinaryChannel to a peer.
     * 
     * @param peer
     * @param progress
     * @return BinaryChannel to peer
     * @throws XMPPException
     * @throws IOException
     * @throws InterruptedException
     */
    protected BinaryChannel establishBinaryChannel(String peer,
        SubMonitor progress) throws XMPPException, IOException,
        InterruptedException {
        BytestreamSession session = manager.establishSession(peer.toString());

        return new BinaryChannel(session, getDefaultNetTransferMode());
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
        throws InterruptedException, Exception {

        BytestreamSession session = request.accept();
        BinaryChannel channel = new BinaryChannel(session,
            getDefaultNetTransferMode());

        return channel;
    }

    public void prepareXMPPConnection(XMPPConnection connection,
        IBytestreamConnectionListener listener) {
        this.connectionListener = listener;
        manager = getManager(connection);
        manager.addIncomingBytestreamListener(streamListener);
    }

    @Override
    public String toString() {
        return getDefaultNetTransferMode().getXEP();
    }

    abstract public NetTransferMode getDefaultNetTransferMode();

    /**
     * @param connection
     * @return The configured BytestreamManager for the specialized transport
     *         method
     */
    abstract protected BytestreamManager getManager(XMPPConnection connection);

}
