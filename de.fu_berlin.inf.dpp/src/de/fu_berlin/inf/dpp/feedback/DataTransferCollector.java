package de.fu_berlin.inf.dpp.feedback;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ITransferModeListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
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

        protected long size;

        protected long transmissionMillisecs;

        public TransferEvent(NetTransferMode newMode, boolean incoming,
            long size, long transmissionMillisecs) {
            this.mode = newMode;
            this.incoming = incoming;
            this.size = size;
            this.transmissionMillisecs = transmissionMillisecs;
        }

        public long getTransmissionMillisecs() {
            return transmissionMillisecs;
        }

        public long getSize() {
            return size;
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
        SessionManager sessionManager, DataTransferManager dataTransferManager) {
        super(statisticManager, sessionManager);
        this.dataTransferManager = dataTransferManager;

        this.dataTransferManager.getTransferModeDispatch().add(
            new ITransferModeListener() {

                public void clear() {
                    // do nothing
                }

                public void transferFinished(JID jid, NetTransferMode newMode,
                    boolean incoming, long size, long transmissionMillisecs) {
                    transferEvents.add(new TransferEvent(newMode, incoming,
                        size, transmissionMillisecs));
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
                totalSize += event.getSize();
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
    protected void doOnSessionEnd(ISharedProject project) {
        // Do nothing
    }

    @Override
    protected void doOnSessionStart(ISharedProject project) {
        // Do nothing
    }
}