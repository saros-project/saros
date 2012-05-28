package de.fu_berlin.inf.dpp.activities.business;

import java.util.Collections;
import java.util.List;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * A wrapper around an IActivity to send it to a specific list of users.
 */
public class TargetedActivityWrapper implements ITargetedActivity {

    private final List<User> users;
    private final IActivity activity;

    public TargetedActivityWrapper(User user, IActivity activity) {
        this.users = Collections.singletonList(user);
        this.activity = activity;
    }

    public TargetedActivityWrapper(List<User> users, IActivity activity) {
        this.users = users;
        this.activity = activity;
    }

    @Override
    public List<User> getRecipients() {
        return users;
    }

    @Override
    public User getSource() {
        return activity.getSource();
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        activity.dispatch(receiver);
    }

    @Override
    public boolean equals(Object obj) {
        return activity.equals(obj);
    }

    @Override
    public int hashCode() {
        return activity.hashCode();
    }

    @Override
    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return activity.getActivityDataObject(sarosSession);
    }
}
