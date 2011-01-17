package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.net.JID;

public class NetworkRequest implements Comparable<NetworkRequest> {

    protected User from;

    protected JID to;

    protected JupiterActivity jupiterActivity;

    protected int delay;

    public NetworkRequest(User from, JID to, JupiterActivity jupiterActivity,
        int delay) {
        this.from = from;
        this.to = to;
        this.delay = delay;

        /* adaption to new JupiterActivity format. */
        if (jupiterActivity.getSource() == null) {
            this.jupiterActivity = new JupiterActivity(jupiterActivity
                .getTimestamp(), jupiterActivity.getOperation(), from,
                jupiterActivity.getPath());
        } else {
            this.jupiterActivity = jupiterActivity;
        }
    }

    public User getFrom() {
        return from;
    }

    public JID getTo() {
        return to;
    }

    public JupiterActivity getJupiterActivity() {
        return jupiterActivity;
    }

    public int compareTo(NetworkRequest o) {
        return Integer.valueOf(delay).compareTo(o.delay);
    }

    public int getDelay() {
        return delay;
    }
}
