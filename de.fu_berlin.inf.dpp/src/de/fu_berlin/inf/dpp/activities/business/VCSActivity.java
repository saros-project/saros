package de.fu_berlin.inf.dpp.activities.business;

import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IResourceActivityDataObject;
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

    public Vector<IResourceActivity> containedActivity = new Vector<IResourceActivity>();

    public VCSActivity(Type type, User source, SPath path, String url,
        String directory, String param1) {
        super(source);
        this.type = type;
        this.path = path;
        this.url = url;
        this.directory = directory;
        this.param1 = param1;
    }

    public static VCSActivity connect(ISarosSession sarosSession,
        IProject project, String url, String directory, String providerID) {
        return new VCSActivity(Type.Connect, sarosSession, project, url,
            directory, providerID);
    }

    public static VCSActivity disconnect(ISarosSession sarosSession,
        IProject project, boolean deleteContents) {
        String param1 = deleteContents ? "" : null;
        return new VCSActivity(Type.Disconnect, sarosSession, project, null,
            null, param1);
    }

    public static VCSActivity update(ISarosSession sarosSession,
        IResource resource, String revision) {
        return new VCSActivity(Type.Update, sarosSession, resource, null, null,
            revision);
    }

    public static VCSActivity switch_(ISarosSession sarosSession,
        IResource resource, String url, String revision) {
        return new VCSActivity(Type.Switch, sarosSession, resource, url, null,
            revision);
    }

    private VCSActivity(Type type, ISarosSession sarosSession,
        IResource resource, String url, String directory, String param1) {
        this(type, sarosSession != null ? sarosSession.getLocalUser() : null,
            resource != null ? new SPath(resource) : null, url, directory,
            param1);
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        SPathDataObject sPathDataObject = path == null ? null : path
            .toSPathDataObject(sarosSession);
        Vector<IResourceActivityDataObject> ados = new Vector<IResourceActivityDataObject>(
            containedActivity.size());
        for (IResourceActivity a : containedActivity) {
            ados.add((IResourceActivityDataObject) a
                .getActivityDataObject(sarosSession));
        }
        return new VCSActivityDataObject(source.getJID(), getType(), url,
            sPathDataObject, directory, param1, ados);
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

    /**
     * Returns true if executing this activity would implicitely execute the
     * otherActivity. In other words, if executing otherActivity after this one
     * would be a null operation.<br>
     * Currently this method only checks type and path, not URL and revision.
     */
    public boolean includes(IResourceActivity otherActivity) {
        if (otherActivity == null || getPath() == null
            || otherActivity.getPath() == null)
            return false;
        IPath vcsPath = getPath().getFullPath();
        IPath otherPath = otherActivity.getPath().getFullPath();
        if (!vcsPath.isPrefixOf(otherPath))
            return false;
        // An update can't include a switch.
        if (getType() == VCSActivity.Type.Update
            && otherActivity instanceof VCSActivity
            && ((VCSActivity) otherActivity).getType() == VCSActivity.Type.Switch) {
            return false;
        }
        if (vcsPath.equals(otherPath)) {
            return otherActivity instanceof FileActivity
                || otherActivity instanceof FolderActivity;
        }

        return true;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
            + ((directory == null) ? 0 : directory.hashCode());
        result = prime * result + ((param1 == null) ? 0 : param1.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        VCSActivity other = (VCSActivity) obj;
        if (directory == null) {
            if (other.directory != null)
                return false;
        } else if (!directory.equals(other.directory))
            return false;
        if (param1 == null) {
            if (other.param1 != null)
                return false;
        } else if (!param1.equals(other.param1))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (type != other.type)
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

}
