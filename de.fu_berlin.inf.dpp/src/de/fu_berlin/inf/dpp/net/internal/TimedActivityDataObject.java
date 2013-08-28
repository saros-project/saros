package de.fu_berlin.inf.dpp.net.internal;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.xstream.JIDConverter;

/**
 * A simple {@link IActivityDataObject} wrapper that adds a time stamp.
 * 
 * @author rdjemili
 */
@XStreamAlias("timedActivity")
public class TimedActivityDataObject implements
    Comparable<TimedActivityDataObject> {

    private final IActivityDataObject activityDataObject;

    @XStreamAsAttribute
    private final int sequenceNumber;

    /**
     * The JID of the user who sent this TimedActivityDataObject
     */
    @XStreamConverter(JIDConverter.class)
    private final JID sender;

    /**
     * Constructs a new TimedActivityDataObject.
     * 
     * @param activityDataObject
     *            the activityDataObject to wrap
     * @param sequenceNumber
     *            the sequence number that belongs to the activityDataObject.
     * 
     * @throws IllegalArgumentException
     *             if activityDataObject is <code>null</code> .
     */
    public TimedActivityDataObject(IActivityDataObject activityDataObject,
        JID sender, int sequenceNumber) {

        if (sender == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }

        if (activityDataObject == null) {
            throw new IllegalArgumentException("Activity cannot be null");
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
     * @return the sequence number of the activity.
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

    @Override
    public int compareTo(TimedActivityDataObject other) {
        if (other == null) {
            throw new NullPointerException();
        }
        return this.sequenceNumber - other.sequenceNumber;
    }
}
