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

/*
 * see {#link de.fu_berlin.inf.dpp.net.internal.DataTransferManager.ITransport} 
 */
public abstract class BytestreamTransport implements ITransport {

    private static final Logger log = Logger.getLogger(Socks5Transport.class);

    protected BytestreamManager manager;
    protected DataTransferManager dtm;

    /**
     * @param peer
     *            the JID of the user we'd like to connect
     * 
     * @return a new connection to peer
     */
    public IBytestreamConnection connect(final JID peer, SubMonitor progress)
        throws IOException, InterruptedException {

        progress.subTask("Try to initiate bytestream with " + toString());

        try {

            BinaryChannel channel = establishBinaryChannel(peer.toString(),
                progress);
            return new BinaryChannelConnection(peer, channel, dtm);

        } catch (XMPPException e) {
            throw new CausedIOException(e);
        }
    }

    public void disposeXMPPConnection() {
        if (manager != null) {
            manager.removeIncomingBytestreamListener(streamListener);
            // socks5ByteStreamManager.disableService();
            manager = null;
        }
    }

    /**
     * handles incoming requests and informs the DataTransferManager if a new
     * connection got established
     */
    protected BytestreamListener streamListener = new BytestreamListener() {

        public void incomingBytestreamRequest(BytestreamRequest request) {

            try {

                BinaryChannel channel = acceptRequest(request);

                if (channel == null)
                    return;

                JID peer = new JID(request.getFrom());

                dtm.connectionChanged(peer, new BinaryChannelConnection(peer,
                    channel, dtm));

            } catch (XMPPException e) {
                log.error(
                    "Socket crashed, no session for request established:", e);
            } catch (IOException e) {
                log.error(
                    "Socket crashed, no session for request established:", e);
            } catch (InterruptedException e) {
                log.debug("Interrupted while initiating new session.");
            }
        }
    };

    protected BinaryChannel establishBinaryChannel(String peer,
        SubMonitor progress) throws XMPPException, IOException,
        InterruptedException {
        BytestreamSession session = manager.establishSession(peer.toString());

        return new BinaryChannel(session, getDefaultNetTransferMode());
    }

    /**
     * 
     * @param request
     * @return BytestreamSession, null if failed or if answer for unidirectional
     *         connecting (to be overridden in subclasses)
     * @throws InterruptedException
     * @throws XMPPException
     * @throws IOException
     */
    protected BinaryChannel acceptRequest(BytestreamRequest request)
        throws XMPPException, InterruptedException, IOException {

        BytestreamSession session = request.accept();
        BinaryChannel channel = new BinaryChannel(session,
            getDefaultNetTransferMode());

        return channel;
    }

    public void prepareXMPPConnection(XMPPConnection connection,
        DataTransferManager dtm) {
        this.dtm = dtm;
        manager = getManager(connection);
        manager.addIncomingBytestreamListener(streamListener);
    }

    @Override
    public String toString() {
        return getDefaultNetTransferMode().getXEP();
    }

    abstract public NetTransferMode getDefaultNetTransferMode();

    abstract public BytestreamManager getManager(XMPPConnection connection);

}
