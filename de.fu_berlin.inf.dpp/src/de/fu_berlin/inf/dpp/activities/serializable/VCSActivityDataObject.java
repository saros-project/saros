package de.fu_berlin.inf.dpp.activities.serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity.Type;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

@XStreamAlias("vcsActivity")
public class VCSActivityDataObject extends AbstractActivityDataObject implements
    IResourceActivityDataObject {

    protected String param1;
    protected String url;
    protected String directory;
    protected SPathDataObject path;
    @XStreamAsAttribute
    protected Type type;

    public VCSActivityDataObject(JID source) {
        super(source);
    }

    public VCSActivityDataObject(JID source, VCSActivity.Type type, String url,
        SPathDataObject path, String directory, String param1) {
        super(source);
        this.type = type;
        this.url = url;
        this.directory = directory;
        this.path = path;
        this.param1 = param1;
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        SPath sPath = path == null ? null : path.toSPath(sarosSession);
        User user = sarosSession == null ? null : sarosSession.getUser(source);
        return new VCSActivity(type, user, sPath, url, directory, param1);
    }

    public SPathDataObject getPath() {
        return path;
    }

    public SPathDataObject getOldPath() {
        return null;
    }
}
