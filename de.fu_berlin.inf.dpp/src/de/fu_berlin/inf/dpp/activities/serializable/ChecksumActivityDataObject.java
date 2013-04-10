package de.fu_berlin.inf.dpp.activities.serializable;

import org.picocontainer.annotations.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * A checksum activityDataObject is used to communicate checksums from the host
 * to the clients.
 * 
 * A checksum always relates to a certain file (given a path) and contains the
 * hash and length of the file.
 * 
 * To indicate that a file is missing on the host NON_EXISTING_DOC is used.
 * 
 * A checksum also may contain a JupiterTimestamp to indicate at which point of
 * time the checksum was created. A user can use this information to see whether
 * the checksum can be used to check for consistency or whether the local user
 * has already written additional text which invalidates the checksum.
 */
@XStreamAlias("checksumActivity")
public class ChecksumActivityDataObject extends
    AbstractProjectActivityDataObject {

    @XStreamAsAttribute
    protected long hash;

    @XStreamAsAttribute
    protected long length;

    @XStreamAsAttribute
    protected Timestamp jupiterTimestamp;

    /**
     * Constructor for a ChecksumActivityDataObject with no jupiterTimestamp set
     * (such is used when communicating with users who have
     * {@link Permission#READONLY_ACCESS})
     */
    public ChecksumActivityDataObject(JID source,
        SPathDataObject sPathDataObject, long hash, long length) {
        this(source, sPathDataObject, hash, length, null);
    }

    /**
     * Constructor for checksum activityDataObjects including a Timestamp (for
     * users who have {@link Permission#WRITE_ACCESS})
     */
    public ChecksumActivityDataObject(JID source,
        SPathDataObject sPathDataObject, long hash, long length,
        @Nullable Timestamp jupiterTimestamp) {

        super(source, sPathDataObject);
        this.hash = hash;
        this.length = length;
        this.jupiterTimestamp = jupiterTimestamp;
    }

    @Override
    public String toString() {
        return "Checksum(path:" + this.path + ",hash:" + hash + ",length:"
            + length + ",vectorTime:" + jupiterTimestamp + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (hash ^ (hash >>> 32));
        result = prime * result + (int) (length ^ (length >>> 32));
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        ChecksumActivityDataObject other = (ChecksumActivityDataObject) obj;
        if (hash != other.hash)
            return false;
        if (length != other.length)
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession) {
        return new ChecksumActivity(sarosSession.getUser(source),
            path.toSPath(sarosSession), hash, length, jupiterTimestamp);
    }
}
