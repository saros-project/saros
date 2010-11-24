package de.fu_berlin.inf.dpp.net.internal;

import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;

/**
 * Data Object for (de)serializion of {@link TimedActivityDataObject}s with
 * {@link XStream}.
 */
@XStreamAlias("TimedActivities")
public class TimedActivities {

    @XStreamAsAttribute
    protected String sessionID;

    @XStreamImplicit
    protected List<TimedActivityDataObject> activities;

    /**
     * @param sessionID
     *            The session ID these timed activityDataObjects belong to.
     * @param activities
     *            The activityDataObjects wrapped by this container. Must not be
     *            null or an empty {@link List}.
     */
    public TimedActivities(String sessionID,
        List<TimedActivityDataObject> activities) {
        if (activities.isEmpty()) {
            throw new IllegalArgumentException("Activities must not be empty");
        }
        this.sessionID = sessionID;
        this.activities = activities;
    }

    public String getSessionID() {
        return sessionID;
    }

    public List<TimedActivityDataObject> getTimedActivities() {
        return activities;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((activities == null) ? 0 : activities.hashCode());
        result = prime * result
            + ((sessionID == null) ? 0 : sessionID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimedActivities other = (TimedActivities) obj;
        if (activities == null) {
            if (other.activities != null)
                return false;
        } else if (!activities.equals(other.activities))
            return false;
        if (sessionID == null) {
            if (other.sessionID != null)
                return false;
        } else if (!sessionID.equals(other.sessionID))
            return false;
        return true;
    }
}
