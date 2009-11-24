package de.fu_berlin.inf.dpp.activities.business;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.PingPongActivityDataObject;

public class PingPongActivity extends AbstractActivity {

    public User initiator;

    public DateTime departureTime;

    // TODO Remove this constructor.
    public PingPongActivity(User source) {
        super(source);
    }

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
        return new PingPongActivity(localUser, getInitiator(), this
            .getDepartureTime());
    }

    @Override
    public String toString() {
        return (getSource().equals(getInitiator()) ? "Ping" : "Pong")
            + "Activity(initiator=" + getInitiator() + ",departed="
            + departureTime.toString("HH:mm:ss,SSS") + ")";
    }

    public boolean dispatch(IActivityConsumer consumer) {
        return consumer.consume(this);
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject() {
        return new PingPongActivityDataObject(source.getJID(), initiator
            .getJID(), departureTime);
    }
}
