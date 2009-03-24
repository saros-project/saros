package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.activities.IActivity;

/**
 * A simple {@link IActivity} wrapper that add an time stamp.
 * 
 * @author rdjemili
 */
public class TimedActivity implements Comparable<TimedActivity> {

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

        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null");

        this.activity = activity;
        this.timestamp = timestamp;
    }

    /**
     * @return the activity.
     */
    public IActivity getActivity() {
        return this.activity;
    }

    /**
     * @return the source of the activity.
     */
    public String getSource() {
        return this.activity.getSource();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((activity == null) ? 0 : activity.hashCode());
        result = prime * result + timestamp;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (!(obj instanceof TimedActivity))
            return false;

        TimedActivity other = (TimedActivity) obj;
        return other.activity.equals(this.activity)
            && (other.timestamp == this.timestamp);
    }

    public int compareTo(TimedActivity other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return this.timestamp - other.timestamp;
    }
}
