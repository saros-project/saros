package de.fu_berlin.inf.dpp.activities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.xstream.UrlEncodingStringConverter;

@XStreamAlias("stopActivity")
public class StopActivity extends AbstractActivity {

    @XStreamAsAttribute
    @XStreamConverter(UrlEncodingStringConverter.class)
    protected String initiator;

    // the user who has to be locked / unlocked
    @XStreamAsAttribute
    @XStreamConverter(UrlEncodingStringConverter.class)
    protected String user;

    public enum Type {
        LOCKREQUEST, UNLOCKREQUEST
    }

    @XStreamAsAttribute
    protected Type type;

    public enum State {
        INITIATED, ACKNOWLEDGED
    }

    @XStreamAsAttribute
    protected State state;

    // a stop activity has a unique id
    @XStreamAsAttribute
    protected String stopActivityID;

    protected static Random random = new Random();

    public StopActivity(String source, JID initiator, JID user, Type type,
        State state) {

        super(source);
        this.initiator = initiator.toString();
        this.user = user.toString();
        this.state = state;
        this.type = type;
        this.stopActivityID = new SimpleDateFormat("HHmmssSS")
            .format(new Date())
            + random.nextLong();
    }

    public StopActivity(String source, JID initiator, JID user, Type type,
        State state, String stopActivityID) {

        this(source, initiator, user, type, state);
        this.stopActivityID = stopActivityID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
            + ((initiator == null) ? 0 : initiator.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result
            + ((stopActivityID == null) ? 0 : stopActivityID.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        StopActivity other = (StopActivity) obj;
        if (initiator == null) {
            if (other.initiator != null)
                return false;
        } else if (!initiator.equals(other.initiator))
            return false;
        if (state == null) {
            if (other.state != null)
                return false;
        } else if (!state.equals(other.state))
            return false;
        if (stopActivityID == null) {
            if (other.stopActivityID != null)
                return false;
        } else if (!stopActivityID.equals(other.stopActivityID))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }

    public JID getInitiator() {
        return new JID(initiator);
    }

    public boolean dispatch(IActivityReceiver receiver) {
        return receiver.receive(this);
    }

    public State getState() {
        return state;
    }

    public JID getUser() {
        return new JID(user);
    }

    public StopActivity generateAcknowledgment(String source) {
        return new StopActivity(source, new JID(initiator), new JID(user),
            type, State.ACKNOWLEDGED, stopActivityID);
    }

    public Type getType() {
        return type;
    }

    public String getActivityID() {
        return stopActivityID;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StopActivity (id: " + stopActivityID);
        sb.append(", type: " + type);
        sb.append(", state: " + state);
        sb.append(", initiator: " + initiator.toString());
        sb.append(", affected user: " + user.toString());
        sb.append(", src: " + getSource() + ")");
        return sb.toString();
    }
}