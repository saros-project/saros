package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.VCSActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * Activity for VCS operations like Switch.
 */
public class VCSActivity extends AbstractActivity implements IResourceActivity {
    /**
     * Supported arguments<br>
     * Switch: <br>
     * path: Specifies the path of the local resource. <br>
     * url: Specifies the path of the target resource in the repo. <br>
     * revision: Specifies the revision of the target resource.
     */

    public enum Type {
        Switch,
    }

    protected Type type;
    protected String url;
    protected SPath path;
    protected String revision;

    public VCSActivity(User source) {
        super(source);
    }

    public VCSActivity(Type type, User source, SPath path, String url,
        String revision) {
        super(source);
        this.type = type;
        this.path = path;
        this.url = url;
        this.revision = revision;
    }

    public boolean dispatch(IActivityConsumer consumer) {
        return consumer.consume(this);
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new VCSActivityDataObject(source.getJID(), getType(), url,
            path.toSPathDataObject(sarosSession), revision);
    }

    public static VCSActivity switch_(User source, SPath path, String url,
        String revision) {
        return new VCSActivity(Type.Switch, source, path, url, revision);
    }

    public SPath getPath() {
        return path;
    }

    public SPath getOldPath() {
        return null;
    }

    public Type getType() {
        return type;
    }

    public String getRevision() {
        return revision;
    }

    public String getURL() {
        return url;
    }

}
