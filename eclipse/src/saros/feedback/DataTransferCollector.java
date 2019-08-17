package saros.feedback;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import saros.annotations.Component;
import saros.net.IReceiver;
import saros.net.ITransferListener;
import saros.net.ITransmitter;
import saros.net.stream.StreamMode;
import saros.session.ISarosSession;

/**
 * Collects information about the amount of data transfered with the different {@link StreamMode}s
 *
 * @author Christopher Oezbek
 */
@Component(module = "feedback")
public class DataTransferCollector extends AbstractStatisticCollector {

  private static final String KEY_TRANSFER_STATS = "data_transfer";

  private static final String TRANSFER_STATS_EVENT_SUFFIX = "number_of_events";

  /** Total size in KB */
  private static final String TRANSFER_STATS_SIZE_SUFFIX = "total_size_kb";

  /** Total size for transfers in milliseconds */
  private static final String TRANSFER_STATS_TIME_SUFFIX = "total_time_ms";

  /** Convenience value of total_size / total_time in KB/s */
  private static final String TRANSFER_STATS_THROUGHPUT_SUFFIX = "average_throughput_kbs";

  // we currently do not distinguish between sent and received data
  private static class TransferStatisticHolder {
    private long bytesTransferred;
    private long transferTime; // ms
    private int count;
  }

  private final Map<StreamMode, TransferStatisticHolder> statistic =
      new EnumMap<StreamMode, TransferStatisticHolder>(StreamMode.class);

  private final IReceiver receiver;
  private final ITransmitter transmitter;

  private final ITransferListener dataTransferlistener =
      new ITransferListener() {

        @Override
        public void sent(
            final StreamMode mode,
            final long sizeCompressed,
            final long sizeUncompressed,
            final long duration) {
          // see processGatheredData
          synchronized (DataTransferCollector.this) {
            TransferStatisticHolder holder = statistic.get(mode);

            if (holder == null) {
              holder = new TransferStatisticHolder();
              statistic.put(mode, holder);
            }

            holder.bytesTransferred += sizeCompressed;
            holder.transferTime += sizeUncompressed;
            holder.count++;

            // TODO how to handle overflow ?
          }
        }

        @Override
        public void received(
            final StreamMode mode,
            final long sizeCompressed,
            final long sizeUncompressed,
            final long duration) {
          // TODO differentiate the traffic
          sent(mode, sizeCompressed, sizeUncompressed, duration);
        }
      };

  public DataTransferCollector(
      StatisticManager statisticManager,
      ISarosSession session,
      IReceiver receiver,
      ITransmitter transmitter) {
    super(statisticManager, session);
    this.receiver = receiver;
    this.transmitter = transmitter;
  }

  @Override
  protected synchronized void processGatheredData() {

    for (final Entry<StreamMode, TransferStatisticHolder> entry : statistic.entrySet()) {

      final StreamMode mode = entry.getKey();
      final TransferStatisticHolder holder = entry.getValue();

      storeTransferStatisticForMode(
          mode.toString(), holder.count, holder.bytesTransferred, holder.transferTime);
    }
  }

  @Override
  protected void doOnSessionStart(ISarosSession sarosSession) {
    receiver.addTransferListener(dataTransferlistener);
    transmitter.addTransferListener(dataTransferlistener);
  }

  @Override
  protected void doOnSessionEnd(ISarosSession sarosSession) {
    receiver.removeTransferListener(dataTransferlistener);
    transmitter.removeTransferListener(dataTransferlistener);
  }

  private void storeTransferStatisticForMode(
      final String transferMode,
      final int transferEvents,
      final long totalSize,
      final long totalTransferTime) {

    data.put(KEY_TRANSFER_STATS, transferEvents, transferMode, TRANSFER_STATS_EVENT_SUFFIX);

    data.put(KEY_TRANSFER_STATS, totalSize / 1024, transferMode, TRANSFER_STATS_SIZE_SUFFIX);

    data.put(KEY_TRANSFER_STATS, totalTransferTime, transferMode, TRANSFER_STATS_TIME_SUFFIX);

    data.put(
        KEY_TRANSFER_STATS,
        totalSize * 1000.0 / 1024.0 / Math.max(1.0, totalTransferTime),
        transferMode,
        TRANSFER_STATS_THROUGHPUT_SUFFIX);
  }
}
