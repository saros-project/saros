package de.fu_berlin.inf.dpp.activities;

import java.util.Vector;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * Activity for VCS operations like Switch, Update.
 */
@XStreamAlias("vcsActivity")
public class VCSActivity extends AbstractResourceActivity implements
    IFileSystemModificationActivity {

    public enum Type {
        /**
         * Supported arguments:<br>
         * path: The path of the project to be connected. <br>
         * url: The repository root URL. <br>
         * directory: The path of the target dir relative to the repository
         * root. <br>
         * param1: The provider ID.
         */
        CONNECT,

        /**
         * path: The path of the project to be disconnected. <br>
         * Supported arguments:<br>
         * param1: If !=null, delete contents.
         */
        DISCONNECT,

        /**
         * Supported arguments:<br>
         * path: The path of the resource in the working directory. <br>
         * url: The URL of the target resource in the repo. <br>
         * param1: The revision of the target resource.
         */
        SWITCH,

        /**
         * Supported arguments:<br>
         * path: The path of the resource in the working directory. <br>
         * param1: The revision of the target resource.
         */
        UPDATE,
    }

    protected String param1;
    protected String url;
    protected String directory;

    @XStreamAsAttribute
    protected Type type;

    public Vector<IResourceActivity> containedActivity = new Vector<IResourceActivity>();

    /**
     * 
     * @param source
     * @param type
     *            Represents the VCS action that takes place
     * @param path
     *            The path of the resource in the working directory
     * @param url
     *            The repository root url (for {@link Type#CONNECT});<br>
     *            the url of the target resource (for {@link Type#SWITCH});<br>
     *            <code>null</code> otherwise.
     * @param directory
     *            The path of the target directory relative to the root (for
     *            {@link Type#CONNECT});<br>
     *            <code>null</code> otherwise.
     * @param param1
     *            The provider id (for {@link Type#CONNECT});<br>
     *            the revision of the target resource (for {@link Type#SWITCH}
     *            and {@link Type#UPDATE});<br>
     *            the marker whether to delete the contents (delete
     *            <code>if (param1 != null)</code>, for {@link Type#DISCONNECT}
     *            ).
     */
    public VCSActivity(User source, Type type, SPath path, String url,
        String directory, String param1) {

        super(source, path);

        this.type = type;
        this.url = url;
        this.directory = directory;
        this.param1 = param1;
    }

    /**
     * Internal ctor used by the builder methods
     */
    private VCSActivity(Type type, ISarosSession sarosSession,
        IResource resource, String url, String directory, String param1) {
        this(sarosSession != null ? sarosSession.getLocalUser() : null, type,
            resource != null ? new SPath(resource) : null, url, directory,
            param1);
    }

    public static VCSActivity connect(ISarosSession sarosSession,
        IProject project, String url, String directory, String providerID) {
        return new VCSActivity(Type.CONNECT, sarosSession, project, url,
            directory, providerID);
    }

    public static VCSActivity disconnect(ISarosSession sarosSession,
        IProject project, boolean deleteContents) {
        String param1 = deleteContents ? "" : null;
        return new VCSActivity(Type.DISCONNECT, sarosSession, project, null,
            null, param1);
    }

    public static VCSActivity update(ISarosSession sarosSession,
        IResource resource, String revision) {
        return new VCSActivity(Type.UPDATE, sarosSession, resource, null, null,
            revision);
    }

    public static VCSActivity switch_(ISarosSession sarosSession,
        IResource resource, String url, String revision) {
        return new VCSActivity(Type.SWITCH, sarosSession, resource, url, null,
            revision);
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    /**
     * Represents the VCS action that takes place
     */
    public Type getType() {
        return type;
    }

    /**
     * Various purposes depending on {@link #getType()}
     * 
     * @return The provider id (for {@link Type#CONNECT});<br>
     *         the revision of the target resource (for {@link Type#SWITCH} and
     *         {@link Type#UPDATE});<br>
     *         the marker whether to delete the contents (delete
     *         <code>if (param1 != null)</code>, for {@link Type#DISCONNECT} ).
     */
    public String getParam1() {
        return param1;
    }

    /**
     * The repository url (or part of it), depends on {@link #getType()}
     * 
     * @return The repository root url (for {@link Type#CONNECT}), see
     *         {@link #getDirectory()} for the rest of the full path;<br>
     *         the url of the target resource (for {@link Type#SWITCH});<br>
     *         <code>null</code> otherwise.
     */
    public String getURL() {
        return url;
    }

    /**
     * The path of the target directory relative to the repository root
     * 
     * @return for {@link Type#CONNECT}: part of the URL, path relative to
     *         {@link #getURL()};<br>
     *         <code>null</code> otherwise.
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Returns <code>true</code> if executing this activity would implicitly
     * execute the <code>otherActivity</code>. In other words, if executing
     * <code>otherActivity</code> after this one would be a <code>null</code>
     * operation.<br>
     * Currently this method only checks {@link #getType() type} and
     * {@link #getPath() path}, not {@link #getURL() URL} and
     * {@link #getParam1() revision}.
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
        if (getType() == VCSActivity.Type.UPDATE
            && otherActivity instanceof VCSActivity
            && ((VCSActivity) otherActivity).getType() == VCSActivity.Type.SWITCH) {
            return false;
        }
        if (vcsPath.equals(otherPath)) {
            return otherActivity instanceof IFileSystemModificationActivity;
        }

        return true;
    }

    @Override
    public String toString() {
        String result = "VCSActivity(type: " + type;
        if (type == Type.DISCONNECT)
            result += ", deleteContents: " + (param1 != null);
        else {
            result += ", path: " + getPath();
            if (type == Type.CONNECT || type == Type.SWITCH)
                result += ", url: " + url;
            if (type == Type.CONNECT)
                result += ", directory: " + directory;
            result += ", revision: " + param1;
        }
        result += ")";
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ObjectUtils.hashCode(directory);
        result = prime * result + ObjectUtils.hashCode(param1);
        result = prime * result + ObjectUtils.hashCode(type);
        result = prime * result + ObjectUtils.hashCode(url);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof VCSActivity))
            return false;

        VCSActivity other = (VCSActivity) obj;

        if (this.type != other.type)
            return false;
        if (!ObjectUtils.equals(this.directory, other.directory))
            return false;
        if (!ObjectUtils.equals(this.param1, other.param1))
            return false;
        if (!ObjectUtils.equals(this.url, other.url))
            return false;

        return true;
    }
}
