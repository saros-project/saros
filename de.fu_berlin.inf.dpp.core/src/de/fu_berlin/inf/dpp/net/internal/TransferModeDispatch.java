package de.fu_berlin.inf.dpp.net.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.ConnectionMode;
import de.fu_berlin.inf.dpp.net.xmpp.JID;

class TransferModeDispatch implements ITransferModeListener {

    private static final Logger log = Logger
        .getLogger(TransferModeDispatch.class);

    private final List<ITransferModeListener> listeners = new CopyOnWriteArrayList<ITransferModeListener>();

    void add(ITransferModeListener listener) {
        listeners.add(listener);
    }

    void remove(ITransferModeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public synchronized void transferFinished(JID jid, ConnectionMode mode,
        boolean incoming, long sizeTransferred, long sizeUncompressed,
        long transmissionMillisecs) {

        for (ITransferModeListener listener : listeners) {
            try {
                listener.transferFinished(jid, mode, incoming, sizeTransferred,
                    sizeUncompressed, transmissionMillisecs);
            } catch (RuntimeException e) {
                log.error("Listener crashed: ", e);
            }
        }
    }

    @Override
    public synchronized void transferModeChanged(JID jid, ConnectionMode mode) {
        for (ITransferModeListener listener : listeners) {
            try {
                listener.transferModeChanged(jid, mode);
            } catch (RuntimeException e) {
                log.error("Listener crashed: ", e);
            }
        }
    }
}