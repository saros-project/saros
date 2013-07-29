package de.fu_berlin.inf.dpp.activities.serializable;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

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
     * Constructor
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
    public ChecksumActivityDataObject(JID source, SPathDataObject path,
        long hash, long length, Timestamp jupiterTimestamp) {

        super(source, path);

        this.hash = hash;
        this.length = length;
        this.jupiterTimestamp = jupiterTimestamp;
    }

    @Override
    public String toString() {
        return "ChecksumActivityDO(path: " + getPath() + ", hash: " + hash
            + ", length: " + length + ", jupiterTimestamp: " + jupiterTimestamp
            + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (hash ^ (hash >>> 32));
        result = prime * result + (int) (length ^ (length >>> 32));
        result = prime * result + ObjectUtils.hashCode(jupiterTimestamp);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof ChecksumActivityDataObject))
            return false;

        ChecksumActivityDataObject other = (ChecksumActivityDataObject) obj;

        if (this.hash != other.hash)
            return false;
        if (this.length != other.length)
            return false;
        if (!ObjectUtils.equals(this.jupiterTimestamp, other.jupiterTimestamp))
            return false;

        return true;
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession) {
        return new ChecksumActivity(sarosSession.getUser(getSource()),
            (getPath() != null ? getPath().toSPath(sarosSession) : null), hash,
            length, jupiterTimestamp);
    }
}
