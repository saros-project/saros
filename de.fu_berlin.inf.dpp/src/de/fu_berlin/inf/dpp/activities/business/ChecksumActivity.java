package de.fu_berlin.inf.dpp.activities.business;

import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * A ChecksumActivity is used to communicate checksums from the host to the
 * clients.
 * 
 * A ChecksumActivity always relates to a certain file (given a path) and
 * contains the hash and length of the file.
 * 
 * To indicate that a file is missing on the host NON_EXISTING_DOC is used.
 * 
 * A ChecksumActivity also may contain a {@link Timestamp} to indicate at which
 * point of time the checksum was created. A user can use this information to
 * see whether the checksum can be used to check for consistency or whether the
 * local user has already written additional text which invalidates the
 * checksum.
 */
public class ChecksumActivity extends AbstractActivity implements
    IResourceActivity {

    /**
     * Constant used for representing a missing file
     */
    public static final int NON_EXISTING_DOC = -1;

    protected final SPath path;
    protected final long hash;
    protected final long length;
    protected final Timestamp jupiterTimestamp;

    /**
     * Constructor for ChecksumActivities. Timestamp can be null.
     * ChecksumActivities created by the watchdog don't have access to the
     * JupiterClients and therefore create an Activity without timestamp.
     * Timestamps will be added later by the ConcurentDocumentClient /-Server
     * (by creating a new Activity with a timestamp)
     * 
     * @param source
     *            The User that created this activity
     * @param path
     *            The SPath pointing to the document
     * @param hash
     *            The hashcode of the document
     * @param length
     *            The length of the document
     * @param jupiterTimestamp
     *            The current jupiterTimestamp for this document (nullable)
     */
    public ChecksumActivity(User source, SPath path, long hash, long length,
        @Nullable Timestamp jupiterTimestamp) {

        super(source);
        this.path = path;
        this.hash = hash;
        this.length = length;
        this.jupiterTimestamp = jupiterTimestamp;
    }

    /**
     * Create a ChecksumActivity which indicates that the file is missing on the
     * host.
     */
    public static ChecksumActivity missing(User source, SPath path) {
        return new ChecksumActivity(source, path, NON_EXISTING_DOC,
            NON_EXISTING_DOC, null);
    }

    /**
     * Returns a copy of the ChecksumActivity with a new {@link Timestamp}.
     */
    public ChecksumActivity withTimestamp(Timestamp jupiterTimestamp) {
        return new ChecksumActivity(source, path, hash, length,
            jupiterTimestamp);
    }

    /**
     * Returns the path this checksum is about.
     */
    @Override
    public SPath getPath() {
        return this.path;
    }

    @Override
    public String toString() {
        return "Checksum(path:" + this.path + ",hash:" + hash + ",length:"
            + length + ",vectorTime:" + jupiterTimestamp + ")";
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
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
        ChecksumActivity other = (ChecksumActivity) obj;
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

    public Timestamp getTimestamp() {
        return jupiterTimestamp;
    }

    public long getLength() {
        return length;
    }

    public long getHash() {
        return hash;
    }

    public boolean existsFile() {
        return !(this.length == NON_EXISTING_DOC && this.hash == NON_EXISTING_DOC);
    }

    @Override
    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new ChecksumActivityDataObject(source.getJID(),
            path.toSPathDataObject(sarosSession), hash, length,
            jupiterTimestamp);
    }
}
