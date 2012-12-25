package de.fu_berlin.inf.dpp.net.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.NetTransferMode;

public class TransferModeDispatch implements ITransferModeListener {

    private static final Logger log = Logger
        .getLogger(TransferModeDispatch.class);

    private final List<ITransferModeListener> listeners = new CopyOnWriteArrayList<ITransferModeListener>();

    public void add(ITransferModeListener listener) {
        listeners.add(listener);
    }

    public void remove(ITransferModeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public synchronized void clear() {
        for (ITransferModeListener listener : listeners) {
            try {
                listener.clear();
            } catch (RuntimeException e) {
                log.error("Listener crashed: ", e);
            }
        }
    }

    @Override
    public synchronized void transferFinished(JID jid,
        NetTransferMode newMode, boolean incoming, long sizeTransferred,
        long sizeUncompressed, long transmissionMillisecs) {

        for (ITransferModeListener listener : listeners) {
            try {
                listener.transferFinished(jid, newMode, incoming,
                    sizeTransferred, sizeUncompressed,
                    transmissionMillisecs);
            } catch (RuntimeException e) {
                log.error("Listener crashed: ", e);
            }
        }
    }

    @Override
    public synchronized void connectionChanged(JID jid,
        IByteStreamConnection connection) {
        for (ITransferModeListener listener : listeners) {
            try {
                listener.connectionChanged(jid, connection);
            } catch (RuntimeException e) {
                log.error("Listener crashed: ", e);
            }
        }
    }
}