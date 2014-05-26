package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.net.JID;

public class NetworkRequest implements Comparable<NetworkRequest> {

    protected JupiterActivity jupiterActivity;
    protected JID to;
    protected int delay;

    public NetworkRequest(JupiterActivity jupiterActivity, JID to, int delay) {
        this.jupiterActivity = jupiterActivity;
        this.to = to;
        this.delay = delay;
    }

    public JupiterActivity getJupiterActivity() {
        return jupiterActivity;
    }

    public JID getTo() {
        return to;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public int compareTo(NetworkRequest o) {
        return Integer.valueOf(delay).compareTo(o.delay);
    }

    @Override
    public boolean equals(Object another) {
        if (this == another)
            return true;

        if (another == null)
            return false;

        if (another instanceof NetworkRequest)
            return false;

        return this.delay == ((NetworkRequest) another).delay;
    }

    @Override
    // FIXME bad hash function
    public int hashCode() {
        return this.delay;
    }

}
