package de.fu_berlin.inf.dpp.activities.serializable;

import java.util.Vector;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity.Type;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

@XStreamAlias("vcsActivity")
public class VCSActivityDataObject extends AbstractProjectActivityDataObject
    implements IResourceActivityDataObject {

    protected String param1;
    protected String url;
    protected String directory;
    protected SPathDataObject path;
    @XStreamAsAttribute
    protected Type type;
    public Vector<IResourceActivityDataObject> containedActivity;

    public VCSActivityDataObject(JID source, VCSActivity.Type type, String url,
        SPathDataObject path, String directory, String param1,
        Vector<IResourceActivityDataObject> containedActivity) {
        super(source);
        this.type = type;
        this.url = url;
        this.directory = directory;
        this.path = path;
        this.param1 = param1;
        this.containedActivity = containedActivity;
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        SPath sPath = path == null ? null : path.toSPath(sarosSession);
        User user = sarosSession == null ? null : sarosSession.getUser(source);
        final VCSActivity vcsActivity = new VCSActivity(type, user, sPath, url,
            directory, param1);
        vcsActivity.containedActivity.ensureCapacity(containedActivity.size());
        for (IResourceActivityDataObject ado : containedActivity) {
            vcsActivity.containedActivity.add((IResourceActivity) ado
                .getActivity(sarosSession));
        }
        return vcsActivity;
    }

    @Override
    public SPathDataObject getPath() {
        return path;
    }

    public SPathDataObject getOldPath() {
        return null;
    }
}
