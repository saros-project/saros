package saros.feedback;

import org.picocontainer.Startable;
import saros.session.ISarosSession;

/**
 * Abstract base class for a StatisticCollector which registers itself with a StatisticManager and
 * informs the StatisticManager at the end of a session of new data via
 * StatisticManager#processCollectedData(StatisticCollector).
 *
 * @author Lisa Dohrmann
 */
public abstract class AbstractStatisticCollector implements Startable {

  protected final ISarosSession sarosSession;

  protected StatisticManager statisticManager;
  /**
   * The object that contains the gathered statistical information as simple key/value pairs. It is
   * automatically cleared on every session start and filled on every session end.
   *
   * @see #processGatheredData()
   */
  protected SessionStatistic data;

  @Override
  public void start() {
    doOnSessionStart(sarosSession);
  }

  @Override
  public void stop() {
    doOnSessionEnd(sarosSession);
    notifyCollectionCompleted();
  }

  /**
   * The constructor that has to be called from all implementing classes. It initializes the {@link
   * SessionStatistic}, registers this collector with the {@link StatisticManager}.
   *
   * @param statisticManager
   * @param sarosSession
   */
  public AbstractStatisticCollector(StatisticManager statisticManager, ISarosSession sarosSession) {
    this.statisticManager = statisticManager;
    this.data = new SessionStatistic();
    this.sarosSession = sarosSession;

    statisticManager.registerCollector(this);
  }

  /**
   * Processes the collected data and then hands it to the {@link StatisticManager}. This method is
   * automatically called on session end.
   */
  protected void notifyCollectionCompleted() {
    processGatheredData();
    statisticManager.addData(this, data);
  }

  /**
   * Helper method that calculates the percentage of the given value from the given total value.
   *
   * @return value / totalValue * 100 or 0 if totalValue = 0
   */
  protected int getPercentage(long value, long totalValue) {
    if (totalValue == 0) return 0;

    return (int) Math.round(((double) value / totalValue) * 100);
  }

  /**
   * Processes the gathered data, i.e. everything is stored in the {@link #data} map and is
   * afterwards ready to be fetched by the {@link StatisticManager} <br>
   * <br>
   * NOTE: This method is automatically called by {@link #notifyCollectionCompleted()}. Clients only
   * have to implement the method body.
   *
   * @post the collected information is written to the {@link #data} map
   */
  protected abstract void processGatheredData();

  /**
   * Clients can add their code here that should be executed on session start. <br>
   * doOnSessionStart(ISarosSession) and {@link #doOnSessionEnd(ISarosSession)} are guaranteed to be
   * called in matching pairs with the same project.
   */
  protected abstract void doOnSessionStart(ISarosSession sarosSession);

  /**
   * Clients can add their code here that should be executed on session end. <br>
   * {@link #doOnSessionStart(ISarosSession)} and doOnSessionEnd(ISarosSession) are guaranteed to be
   * called in matching pairs with the same project.
   */
  protected abstract void doOnSessionEnd(ISarosSession sarosSession);
}
