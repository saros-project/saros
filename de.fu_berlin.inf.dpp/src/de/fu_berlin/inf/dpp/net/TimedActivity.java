package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.activities.IActivity;

/**
 * A simple {@link IActivity} wrapper that add an time stamp.
 * 
 * @author rdjemili
 */
public class TimedActivity {
	private IActivity activity;

	private int timestamp;

	/**
	 * Constructs a new TimedActivity.
	 * 
	 * @param activity
	 *            the activity.
	 * @param timestamp
	 *            the timestamp that belongs to the activity.
	 */
	public TimedActivity(IActivity activity, int timestamp) {
		this.activity 	= activity;
		this.timestamp 	= timestamp;
	}

	/**
	 * @return the activity.
	 */
	public IActivity getActivity() {
		return activity;
	}
	
	/**
	 * @return the timestamp of the activiy.
	 */
	public int getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "[" + timestamp + ":" + activity + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TimedActivity))
			return false;

		TimedActivity other = (TimedActivity) obj;
		return other.activity.equals(activity) && other.timestamp == timestamp;
	}
}
