package de.fu_berlin.inf.dpp.feedback;

import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * Abstract base class for a StatisticCollector which registers itself with a
 * StatisticManager and informs the StatisticManager at the end of a session of
 * new data via StatisticManager#processCollectedData(StatisticCollector).
 * 
 * @author Lisa Dohrmann
 */

public abstract class AbstractStatisticCollector {

    protected StatisticManager statisticManager;
    /**
     * The object that contains the gathered statistical information as simple
     * key/value pairs. It is automatically cleared on every session start and
     * filled on every session end.
     * 
     * @see #processGatheredData()
     */
    protected SessionStatistic data;

    /**
     * The listener gets notified on session start and session end. It clears
     * the previous data on session end and notifies the StatisticManager.
     */
    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            doOnSessionStart(newSarosSession);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            doOnSessionEnd(oldSarosSession);
            notifyCollectionCompleted();
            clearPreviousData();
        }
    };

    /**
     * The constructor that has to be called from all implementing classes. It
     * initializes the {@link SessionStatistic}, registers this collector with
     * the {@link StatisticManager} and adds a {@link #sessionListener}.
     * 
     * @param statisticManager
     * @param sessionManager
     */
    public AbstractStatisticCollector(StatisticManager statisticManager,
        SarosSessionManager sessionManager) {
        this.statisticManager = statisticManager;
        this.data = new SessionStatistic();

        statisticManager.registerCollector(this);
        sessionManager.addSarosSessionListener(sessionListener);
    }

    /**
     * Processes the collected data and then hands it to the
     * {@link StatisticManager}. This method is automatically called on session
     * end.
     */
    protected void notifyCollectionCompleted() {
        processGatheredData();
        statisticManager.addData(this, data);
    }

    /**
     * Ensures that all previously collected data is cleared. It is
     * automatically called on session end. After the collected data was handed
     * to the StatisticManager. <br>
     * Clients can override this method to add their own data to be cleared.
     * However they are supposed to call <code>super.clearPreviousData()</code>.
     */
    protected void clearPreviousData() {
        data.clear();
    }

    /**
     * Helper method that calculates the percentage of the given value from the
     * given total value.
     * 
     * @return value / totalValue * 100
     */
    protected int getPercentage(long value, long totalValue) {
        return (int) Math.round(((double) value / totalValue) * 100);
    }

    /**
     * Processes the gathered data, i.e. everything is stored in the
     * {@link #data} map and is afterwards ready to be fetched by the
     * {@link StatisticManager} <br>
     * <br>
     * NOTE: This method is automatically called by
     * {@link #notifyCollectionCompleted()}. Clients only have to implement the
     * method body.
     * 
     * @post the collected information is written to the {@link #data} map
     */
    protected abstract void processGatheredData();

    /**
     * Clients can add their code here that should be executed on session start. <br>
     * doOnSessionStart(ISarosSession) and
     * {@link #doOnSessionEnd(ISarosSession)} are guaranteed to be called in
     * matching pairs with the same project.
     */
    protected abstract void doOnSessionStart(ISarosSession sarosSession);

    /**
     * Clients can add their code here that should be executed on session end. <br>
     * {@link #doOnSessionStart(ISarosSession)} and
     * doOnSessionEnd(ISarosSession) are guaranteed to be called in matching
     * pairs with the same project.
     */
    protected abstract void doOnSessionEnd(ISarosSession sarosSession);

}