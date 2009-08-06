package de.fu_berlin.inf.dpp.net.jingle;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.util.Util;

public class DispatchingJingleFileTransferListener implements
    IJingleFileTransferListener {

    private static final Logger log = Logger
        .getLogger(DispatchingJingleFileTransferListener.class.getName());

    protected Set<IJingleFileTransferListener> listeners = new LinkedHashSet<IJingleFileTransferListener>();

    public DispatchingJingleFileTransferListener(ExecutorService executor) {
        this.jingleDispatch = executor;
    }

    /**
     * This executor is used to decouple the reading from the ObjectInputStream
     * and the notification of the listeners. Thus we can continue reading, even
     * while the DataTransferManager is handling our data.
     */
    protected ExecutorService jingleDispatch;

    public void incomingData(final TransferDescription data,
        final NetTransferMode mode, final byte[] content, final long size,
        final long transferDuration) {

        jingleDispatch.submit(Util.wrapSafe(log, new Runnable() {
            public void run() {
                for (IJingleFileTransferListener listener : listeners) {
                    listener.incomingData(data, mode, content, content.length,
                        transferDuration);
                }
            }
        }));
    }

    public void incomingDescription(final TransferDescription data,
        final NetTransferMode connectionType) {

        jingleDispatch.submit(Util.wrapSafe(log, new Runnable() {
            public void run() {
                for (IJingleFileTransferListener listener : listeners) {
                    listener.incomingDescription(data, connectionType);
                }
            }
        }));

    }

    public void transferFailed(final TransferDescription data,
        final NetTransferMode connectionType, final Exception e) {
        jingleDispatch.submit(Util.wrapSafe(log, new Runnable() {
            public void run() {
                for (IJingleFileTransferListener listener : listeners) {
                    listener.transferFailed(data, connectionType, e);
                }
            }
        }));
    }

    public void add(IJingleFileTransferListener jingleListener) {
        if (!listeners.contains(jingleListener)) {
            listeners.add(jingleListener);
        }
    }

    public void remove(IJingleFileTransferListener jingleListener) {
        listeners.remove(jingleListener);
    }

}
