package de.fu_berlin.inf.dpp.net;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import de.fu_berlin.inf.dpp.activities.IActivity;

/**
 * A simple {@link IActivity} wrapper that add an time stamp.
 * 
 * @author rdjemili
 */
@XStreamAlias("timedActivity")
public class TimedActivity implements Comparable<TimedActivity> {

    /** Sequence number for Activities that don't have to wait. */
    public static final int NO_SEQUENCE_NR = -1;
    /**
     * Unknown sequence number. It is illegal to use it in {@link TimedActivity}
     * instances.
     */
    public static final int UNKNOWN_SEQUENCE_NR = -2;

    protected final IActivity activity;

    @XStreamAsAttribute
    protected final int sequenceNumber;

    /** A "real" wall clock timestamp for this activity. */
    @XStreamOmitField
    protected long localTimestamp = 0;

    /**
     * The JID of the user who sent this TimedActivity
     */
    protected final JID sender;

    /**
     * Constructs a new TimedActivity.
     * 
     * @param activity
     *            the activity to wrap
     * @param sequenceNumber
     *            the sequence number that belongs to the activity.
     * 
     * @throws IllegalArgumentException
     *             if activity is <code>null</code> or the sequence number is
     *             {@link TimedActivity#UNKNOWN_SEQUENCE_NR}.
     */
    public TimedActivity(IActivity activity, JID sender, int sequenceNumber) {

        if (sender == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null");
        }
        if (sequenceNumber == UNKNOWN_SEQUENCE_NR) {
            throw new IllegalArgumentException(
                "sequenceNumber must not be TimedActicity.UNKNOWN_SEQUENCE_NR");
        }

        this.activity = activity;
        this.sender = sender;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * @return the activity.
     */
    public IActivity getActivity() {
        return this.activity;
    }

    /**
     * @return the user who sent this time activity
     * 
     *         CAUTION: The Source of the activity contained in this timed
     *         activity might be somebody else.
     */
    public JID getSender() {
        return this.sender;
    }

    /**
     * @return the sequence number of the activiy.
     */
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    @Override
    public String toString() {
        return "[" + this.sequenceNumber + ":" + this.activity + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((activity == null) ? 0 : activity.hashCode());
        result = prime * result + sequenceNumber;
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
        return (other.sequenceNumber == this.sequenceNumber)
            && other.activity.equals(this.activity);
    }

    public int compareTo(TimedActivity other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return this.sequenceNumber - other.sequenceNumber;
    }

    public void setLocalTimestamp(long localTimestamp) {
        this.localTimestamp = localTimestamp;
    }

    public long getLocalTimestamp() {
        return localTimestamp;
    }
}
