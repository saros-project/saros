package de.fu_berlin.inf.dpp.activities;

import java.util.Collections;
import java.util.List;

import de.fu_berlin.inf.dpp.session.User;

public class QueueItem {
    /*
     * FIXME this class declared inside the ActivityHandler (which is not part
     * of the core yet) but used in the Jupiter Concurrent package. Find a
     * better home for that class !
     */

    public final List<User> recipients;
    public final IActivity activity;

    public QueueItem(List<User> recipients, IActivity activity) {
        this.recipients = recipients;
        this.activity = activity;
    }

    public QueueItem(User host, IActivity activity) {
        this(Collections.singletonList(host), activity);
    }
}
