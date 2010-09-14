package de.fu_berlin.inf.dpp.activities.business;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.VCSActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * Activity for VCS operations like Switch, Update.
 */
public class VCSActivity extends AbstractActivity implements IResourceActivity {

    public enum Type {
        /**
         * Supported arguments:<br>
         * path: The path of the project to be connected. <br>
         * url: The repository root URL. <br>
         * directory: The path of the target dir relative to the repository
         * root. <br>
         * param1: The provider ID.
         */
        Connect,

        /**
         * path: The path of the project to be disconnected. <br>
         * Supported arguments:<br>
         * param1: If !=null, delete contents.
         */
        Disconnect,

        /**
         * Supported arguments:<br>
         * path: The path of the resource in the working directory. <br>
         * url: The URL of the target resource in the repo. <br>
         * param1: The revision of the target resource.
         */
        Switch,

        /**
         * Supported arguments:<br>
         * path: The path of the resource in the working directory. <br>
         * param1: The revision of the target resource.
         */
        Update,
    }

    protected Type type;
    protected String url;
    protected String directory;
    protected SPath path;
    protected String param1;

    public VCSActivity(Type type, User source, SPath path, String url,
        String directory, String param1) {
        super(source);
        this.type = type;
        this.path = path;
        this.url = url;
        this.directory = directory;
        this.param1 = param1;
    }

    public boolean dispatch(IActivityConsumer consumer) {
        return consumer.consume(this);
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        SPathDataObject sPathDataObject = path == null ? null : path
            .toSPathDataObject(sarosSession);
        return new VCSActivityDataObject(source.getJID(), getType(), url,
            sPathDataObject, directory, param1);
    }

    public static VCSActivity connect(ISarosSession sarosSession,
        IProject project, String url, String directory, String providerID) {
        User source = sarosSession.getLocalUser();
        SPath path = new SPath(project);
        return new VCSActivity(Type.Connect, source, path, url, directory,
            providerID);
    }

    public static VCSActivity disconnect(ISarosSession sarosSession,
        IProject project, boolean deleteContents) {
        User source = sarosSession.getLocalUser();
        SPath path = new SPath(project);
        String revision = deleteContents ? "" : null;
        return new VCSActivity(Type.Disconnect, source, path, null, null,
            revision);
    }

    public static VCSActivity update(ISarosSession sarosSession,
        IResource resource, String revision) {
        User source = sarosSession.getLocalUser();
        SPath path = new SPath(resource);
        return new VCSActivity(Type.Update, source, path, null, null, revision);
    }

    public static VCSActivity switch_(ISarosSession sarosSession,
        IResource resource, String url, String revision) {
        User source = sarosSession.getLocalUser();
        SPath path = new SPath(resource);
        return new VCSActivity(Type.Switch, source, path, url, null, revision);
    }

    public SPath getPath() {
        return path;
    }

    public Type getType() {
        return type;
    }

    public String getParam1() {
        return param1;
    }

    public String getURL() {
        return url;
    }

    public String getDirectory() {
        return directory;
    }

    @Override
    public String toString() {
        String result = "VCSActivity(type=" + type;
        if (type == Type.Disconnect)
            result += ", deleteContents=" + (param1 != null);
        else {
            result += ", path=" + path;
            if (type == Type.Connect || type == Type.Switch)
                result += ", url=" + url;
            if (type == Type.Connect)
                result += ", directory=" + directory;
            result += ", revision=" + param1;
        }
        result += ")";
        return result;
    }
}
