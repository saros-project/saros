package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

public class VCSActivity extends AbstractActivity {
    /*
     * Supported arguments Switch: path: Specifies the path of the local
     * resource. url: Specifies the path of the target resource in the repo.
     * revision: Specifies the revision of the target resource.
     */

    private Type type;
    private String revision;
    private String url;
    private SPath path;

    enum Type {
        Switch,
    }

    public VCSActivity(User source) {
        super(source);
    }

    public boolean dispatch(IActivityConsumer consumer) {
        return consumer.consume(this);
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return null;
    }

    public static VCSActivity switch_(User source, SPath path, String url,
        String revision) {
        VCSActivity result = new VCSActivity(source);
        result.setType(Type.Switch);
        result.setSPath(path);
        result.setURL(url);
        result.setRevision(revision);
        return result;
    }

    private void setType(Type type) {
        this.type = type;
    }

    private void setRevision(String revision) {
        this.revision = revision;
    }

    private void setURL(String url) {
        this.url = url;
    }

    private void setSPath(SPath path) {
        this.path = path;
    }

}
