package de.fu_berlin.inf.dpp.activities.serializable;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.IActivityDataObjectConsumer;
import de.fu_berlin.inf.dpp.activities.IActivityDataObjectReceiver;
import de.fu_berlin.inf.dpp.net.JID;

public class PingPongActivityDataObject extends AbstractActivityDataObject {

    public JID initiator;

    public DateTime departureTime;

    public PingPongActivityDataObject(JID source) {
        super(source);
    }

    public PingPongActivityDataObject(JID sender, JID initiator,
        DateTime departureTime) {
        super(sender);
        this.initiator = initiator;
        this.departureTime = departureTime;
    }

    public Duration getRoundtripTime() {
        return new Duration(departureTime, new DateTime());
    }

    public JID getInitiator() {
        return initiator;
    }

    public DateTime getDepartureTime() {
        return departureTime;
    }

    public static PingPongActivityDataObject create(User localUser) {
        return new PingPongActivityDataObject(localUser.getJID(), localUser
            .getJID(), new DateTime());
    }

    public IActivityDataObject createPong(User localUser) {
        return new PingPongActivityDataObject(localUser.getJID(),
            getInitiator(), this.getDepartureTime());
    }

    @Override
    public String toString() {
        return (getSource().equals(getInitiator()) ? "Ping" : "Pong")
            + "Activity(initiator=" + getInitiator() + ",departed="
            + departureTime.toString("HH:mm:ss,SSS") + ")";
    }

    public boolean dispatch(IActivityDataObjectConsumer consumer) {
        return consumer.consume(this);
    }

    public void dispatch(IActivityDataObjectReceiver receiver) {
        receiver.receive(this);
    }
}
