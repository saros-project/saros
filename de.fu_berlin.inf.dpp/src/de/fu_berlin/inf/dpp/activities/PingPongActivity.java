package de.fu_berlin.inf.dpp.activities;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;

public class PingPongActivity extends AbstractActivity {

    public JID initiator;

    public DateTime departureTime;

    public PingPongActivity(String source) {
        super(source);
    }

    public PingPongActivity(JID sender, JID initiator, DateTime departureTime) {
        super(sender.toString());
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

    public static PingPongActivity create(User localUser) {
        return new PingPongActivity(localUser.getJID(), localUser.getJID(),
            new DateTime());
    }

    public IActivity createPong(User localUser) {
        return new PingPongActivity(localUser.getJID(), getInitiator(), this
            .getDepartureTime());
    }

    @Override
    public String toString() {
        return (getSource().equals(getInitiator().toString()) ? "Ping" : "Pong")
            + "Activity(initiator="
            + getInitiator()
            + ",departed="
            + departureTime.toString("HH:mm:ss,SSS") + ")";
    }

    public boolean dispatch(IActivityConsumer consumer) {
        return consumer.consume(this);
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }
}
