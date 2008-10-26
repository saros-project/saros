package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.activities.IActivity;

/**
 * A simple {@link IActivity} wrapper that add an time stamp.
 * 
 * @author rdjemili
 */
public class TimedActivity {
    private final IActivity activity;

    private final int timestamp;

    /**
     * Constructs a new TimedActivity.
     * 
     * @param activity
     *            the activity.
     * @param timestamp
     *            the timestamp that belongs to the activity.
     */
    public TimedActivity(IActivity activity, int timestamp) {
	this.activity = activity;
	this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof TimedActivity)) {
	    return false;
	}

	TimedActivity other = (TimedActivity) obj;
	return other.activity.equals(this.activity)
		&& (other.timestamp == this.timestamp);
    }

    /**
     * @return the activity.
     */
    public IActivity getActivity() {
	return this.activity;
    }

    /**
     * @return the timestamp of the activiy.
     */
    public int getTimestamp() {
	return this.timestamp;
    }

    @Override
    public String toString() {
	return "[" + this.timestamp + ":" + this.activity + "]";
    }
}
