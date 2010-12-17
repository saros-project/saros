package de.fu_berlin.inf.dpp.activities.business;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.PingPongActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

public class PingPongActivity extends AbstractActivity {

    protected final User initiator;

    protected final DateTime departureTime;

    public PingPongActivity(User source, User initiator, DateTime departureTime) {

        super(source);
        this.initiator = initiator;
        this.departureTime = departureTime;
    }

    public Duration getRoundtripTime() {
        return new Duration(departureTime, new DateTime());
    }

    public User getInitiator() {
        return initiator;
    }

    public DateTime getDepartureTime() {
        return departureTime;
    }

    public static PingPongActivity create(User localUser) {
        return new PingPongActivity(localUser, localUser, new DateTime());
    }

    public IActivity createPong(User localUser) {
        return new PingPongActivity(localUser, getInitiator(),
            this.getDepartureTime());
    }

    @Override
    public String toString() {
        return (getSource().equals(getInitiator()) ? "Ping" : "Pong")
            + "Activity(initiator=" + getInitiator() + ",departed="
            + departureTime.toString("HH:mm:ss,SSS") + ")";
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new PingPongActivityDataObject(source.getJID(),
            initiator.getJID(), departureTime);
    }
}
