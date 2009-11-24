package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.net.JID;

public class NetworkRequest {

    private User from;

    private JID to;

    private JupiterActivity jupiterActivity;

    public NetworkRequest(User from, JID to, JupiterActivity jupiterActivity) {
        this.from = from;
        this.to = to;
        /* adaption to new JupiterActivity format. */
        if (jupiterActivity.getSource() == null) {
            this.jupiterActivity = new JupiterActivity(jupiterActivity
                .getTimestamp(), jupiterActivity.getOperation(), from,
                jupiterActivity.getEditorPath());
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
}
