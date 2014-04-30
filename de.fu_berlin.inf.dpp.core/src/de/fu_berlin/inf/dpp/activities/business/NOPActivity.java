package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.NOPActivityDataObject;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

public final class NOPActivity extends AbstractActivity implements
    ITargetedActivity {

    private final User target;
    private final int id;

    public NOPActivity(User source, User target, int id) {
        super(source);

        if (target == null)
            throw new IllegalArgumentException("target must not be null");

        this.target = target;
        this.id = id;
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    @Override
    public IActivityDataObject getActivityDataObject(
        ISarosSession sarosSession, IPathFactory pathFactory) {
        return new NOPActivityDataObject(getSource(), target, id);
    }

    public int getID() {
        return id;
    }

    @Override
    public User getTarget() {
        return target;
    }
}
