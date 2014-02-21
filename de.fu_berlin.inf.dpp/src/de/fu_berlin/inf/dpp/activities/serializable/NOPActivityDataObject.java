package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.NOPActivity;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

@XStreamAlias("nopActivity")
public class NOPActivityDataObject extends AbstractActivityDataObject {

    private JID target;
    private int id;

    public NOPActivityDataObject(JID source, JID target, int id) {
        super(source);

        this.target = target;
        this.id = id;
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession,
        IPathFactory pathFactory) {
        return new NOPActivity(sarosSession.getUser(source),
            sarosSession.getUser(target), id);
    }
}
