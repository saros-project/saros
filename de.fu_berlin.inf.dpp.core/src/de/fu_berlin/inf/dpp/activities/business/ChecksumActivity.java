package de.fu_berlin.inf.dpp.activities.business;

import org.apache.commons.lang.ObjectUtils;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

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
     *            The current jupiterTimestamp for this document, may be
     *            <code>null</code>
     */
    public ChecksumActivity(User source, SPath path, long hash, long length,
        Timestamp jupiterTimestamp) {

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
        return new ChecksumActivity(getSource(), path, hash, length,
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
        return "ChecksumActivity(path: " + path + ", hash: " + hash
            + ", length: " + length + ", jupiterTimestamp: " + jupiterTimestamp
            + ")";
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
        result = prime * result + ObjectUtils.hashCode(path);
        result = prime * result + ObjectUtils.hashCode(jupiterTimestamp);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof ChecksumActivity))
            return false;

        ChecksumActivity other = (ChecksumActivity) obj;

        if (this.hash != other.hash)
            return false;
        if (this.length != other.length)
            return false;
        if (!ObjectUtils.equals(this.path, other.path))
            return false;
        if (!ObjectUtils.equals(this.jupiterTimestamp, other.jupiterTimestamp))
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
    public IActivityDataObject getActivityDataObject(
        ISarosSession sarosSession, IPathFactory pathFactory) {
        return new ChecksumActivityDataObject(getSource().getJID(),
            (path != null ? path.toSPathDataObject(sarosSession, pathFactory)
                : null), hash, length, jupiterTimestamp);
    }
}
