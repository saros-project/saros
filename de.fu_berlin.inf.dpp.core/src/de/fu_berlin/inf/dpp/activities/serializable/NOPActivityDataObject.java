package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.NOPActivity;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

@XStreamAlias("nopActivity")
public class NOPActivityDataObject extends AbstractActivityDataObject {

    private User target;
    private int id;

    public NOPActivityDataObject(User source, User target, int id) {
        super(source);

        this.target = target;
        this.id = id;
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession,
        IPathFactory pathFactory) {
        return new NOPActivity(getSource(), target, id);
    }
}
