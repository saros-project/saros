package de.fu_berlin.inf.dpp.net;

import java.util.List;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.project.IActivityListener;

/**
 * The IActivitySequencer is responsible for making sure that activities are
 * sent and received in the right order.
 * 
 * @author rdjemili
 * 
 */
public interface IActivitySequencer extends IActivityListener {
	/**
	 * Gets all activities since last flush.
	 * 
	 * @return the activities that have accumulated since the last flush or
	 *         <code>null</code> if no activities are are available.
	 */
	public List<TimedActivity> flushTimed();

	/**
	 * Executes the list of timed activities in the right order.
	 * 
	 * @param activities
	 */
	public void exec(List<TimedActivity> activities);

	/**
	 * Executes given timed activity.
	 * 
	 * @param activity
	 */
	public void exec(TimedActivity activity);

	/**
	 * @return the log of flushed activities.
	 */
	public List<IActivity> getLog();

	/**
	 * @return the current timestamp.
	 */
	public int getTimestamp();
}
