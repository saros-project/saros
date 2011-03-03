package de.fu_berlin.inf.dpp.feedback;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.util.Function;
import de.fu_berlin.inf.dpp.util.Pair;

/**
 * Collects information about the amount of data transfered with the different
 * {@link NetTransferMode}s
 * 
 * @author Christopher Oezbek
 */
@Component(module = "feedback")
public class DataTransferCollector extends AbstractStatisticCollector {

    public static class TransferEvent {

        protected NetTransferMode mode;

        protected boolean incoming;

        protected long sizeTransferred;

        protected long sizeUncompressed;

        protected long transmissionMillisecs;

        public TransferEvent(NetTransferMode newMode, boolean incoming,
            long sizeTransferred, long sizeUncompressed,
            long transmissionMillisecs) {
            this.mode = newMode;
            this.incoming = incoming;
            this.sizeTransferred = sizeTransferred;
            this.sizeUncompressed = sizeUncompressed;
            this.transmissionMillisecs = transmissionMillisecs;
        }

        public long getTransmissionMillisecs() {
            return transmissionMillisecs;
        }

        /**
         * Returns size of the transferred data for this {@link TransferEvent}
         * 
         * @return transferred data size in bytes
         */
        public long getTransferredSize() {
            return sizeTransferred;
        }

        /**
         * Transfered data may be uncompressed after reception. This function
         * returns the data size of the data of this {@link TransferEvent} after
         * decompression. If the data was not compressed, this value equals the
         * {@link #getTransferredSize()} call.
         * 
         * @return uncompressed data size in bytes
         */
        public long getUncompressedSize() {
            return sizeUncompressed;
        }

        public boolean isIncoming() {
            return incoming;
        }

        public NetTransferMode getMode() {
            return mode;
        }
    }

    protected DataTransferManager dataTransferManager;

    protected List<TransferEvent> transferEvents = new ArrayList<TransferEvent>();

    public DataTransferCollector(StatisticManager statisticManager,
        SarosSessionManager sessionManager,
        DataTransferManager dataTransferManager) {
        super(statisticManager, sessionManager);
        this.dataTransferManager = dataTransferManager;

        this.dataTransferManager.getTransferModeDispatch().add(
            new ITransferModeListener() {

                public void clear() {
                    // do nothing
                }

                public void transferFinished(JID jid, NetTransferMode newMode,
                    boolean incoming, long sizeTransferred,
                    long sizeUncompressed, long transmissionMillisecs) {
                    transferEvents.add(new TransferEvent(newMode, incoming,
                        sizeTransferred, sizeUncompressed,
                        transmissionMillisecs));
                }

                public void connectionChanged(JID jid,
                    IBytestreamConnection connection) {
                    // do nothing
                }
            });
    }

    @Override
    protected void processGatheredData() {

        // Group Events by NetTransferMode
        List<Pair<NetTransferMode, List<TransferEvent>>> transferClasses = Pair
            .partition(transferEvents,
                new Function<TransferEvent, NetTransferMode>() {
                    public NetTransferMode apply(TransferEvent u) {
                        return u.getMode();
                    }
                });

        // Compute summary statistics per class
        for (Pair<NetTransferMode, List<TransferEvent>> transferClass : transferClasses) {

            long totalSize = 0;
            long totalTransferTime = 0;
            int nTransferEvents = 0;
            for (TransferEvent event : transferClass.v) {
                totalSize += event.getTransferredSize();
                totalTransferTime += event.getTransmissionMillisecs();
                nTransferEvents++;
            }

            data.setTransferStatistic(transferClass.p.toString(),
                nTransferEvents, totalSize / 1024, totalTransferTime, totalSize
                    * 1000.0 / 1024.0 / Math.max(1.0, totalTransferTime));
        }
        transferEvents.clear();
    }

    @Override
    protected void doOnSessionEnd(ISarosSession sarosSession) {
        // Do nothing
    }

    @Override
    protected void doOnSessionStart(ISarosSession sarosSession) {
        // Do nothing
    }
}