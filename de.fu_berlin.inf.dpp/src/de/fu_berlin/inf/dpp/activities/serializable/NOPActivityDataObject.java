package de.fu_berlin.inf.dpp.activities.serializable;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.NOPActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

public class NOPActivityDataObject extends AbstractActivityDataObject {

    private JID target;
    private int id;

    public NOPActivityDataObject(JID source, JID target, int id) {
        super(source);

        this.target = target;
        this.id = id;
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession) {
        return new NOPActivity(sarosSession.getUser(source),
            sarosSession.getUser(target), id);
    }
}
