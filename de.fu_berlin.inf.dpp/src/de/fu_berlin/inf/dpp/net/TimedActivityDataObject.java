package de.fu_berlin.inf.dpp.net;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.serializable.FileActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;

/**
 * A simple {@link IActivityDataObject} wrapper that add an time stamp.
 * 
 * @author rdjemili
 */
@XStreamAlias("timedActivity")
public class TimedActivityDataObject implements
    Comparable<TimedActivityDataObject> {

    /** Sequence number for Activities that don't have to wait. */
    public static final int NO_SEQUENCE_NR = -1;
    /**
     * Unknown sequence number. It is illegal to use it in
     * {@link TimedActivityDataObject} instances.
     */
    public static final int UNKNOWN_SEQUENCE_NR = -2;

    protected final IActivityDataObject activityDataObject;

    @XStreamAsAttribute
    protected final int sequenceNumber;

    /** A "real" wall clock timestamp for this activityDataObject. */
    @XStreamOmitField
    protected long localTimestamp = 0;

    /**
     * The JID of the user who sent this TimedActivityDataObject
     */
    protected final JID sender;

    /**
     * Constructs a new TimedActivityDataObject.
     * 
     * @param activityDataObject
     *            the activityDataObject to wrap
     * @param sequenceNumber
     *            the sequence number that belongs to the activityDataObject.
     * 
     * @throws IllegalArgumentException
     *             if activityDataObject is <code>null</code> or the sequence
     *             number is {@link TimedActivityDataObject#UNKNOWN_SEQUENCE_NR}
     *             .
     */
    public TimedActivityDataObject(IActivityDataObject activityDataObject,
        JID sender, int sequenceNumber) {

        if (sender == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }
        if (activityDataObject == null) {
            throw new IllegalArgumentException("Activity cannot be null");
        }
        if (sequenceNumber == UNKNOWN_SEQUENCE_NR) {
            throw new IllegalArgumentException(
                "sequenceNumber must not be TimedActicity.UNKNOWN_SEQUENCE_NR");
        }

        this.activityDataObject = activityDataObject;
        this.sender = sender;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * @return the activityDataObject.
     */
    public IActivityDataObject getActivity() {
        return this.activityDataObject;
    }

    /**
     * @return the user who sent this time activityDataObject
     * 
     *         CAUTION: The Source of the activityDataObject contained in this
     *         timed activityDataObject might be somebody else.
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
        return "[" + this.sequenceNumber + ":" + this.activityDataObject + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
            * result
            + ((activityDataObject == null) ? 0 : activityDataObject.hashCode());
        result = prime * result + sequenceNumber;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (!(obj instanceof TimedActivityDataObject))
            return false;

        TimedActivityDataObject other = (TimedActivityDataObject) obj;
        return (other.sequenceNumber == this.sequenceNumber)
            && other.activityDataObject.equals(this.activityDataObject);
    }

    public int compareTo(TimedActivityDataObject other) {
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

    /**
     * Used for asserting that the given list contains no FileActivities which
     * create files
     */
    public static boolean containsNoFileCreationActivities(
        List<TimedActivityDataObject> timedActivities) {

        for (TimedActivityDataObject timedActivity : timedActivities) {

            IActivityDataObject activityDataObject = timedActivity
                .getActivity();

            if (activityDataObject instanceof FileActivityDataObject
                && ((FileActivityDataObject) activityDataObject).getType()
                    .equals(FileActivity.Type.Created)) {
                return false;
            }
        }
        return true;
    }
}
