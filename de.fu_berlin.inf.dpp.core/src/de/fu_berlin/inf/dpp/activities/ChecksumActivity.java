package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.session.User;
import org.apache.commons.lang3.ObjectUtils;

/**
 * A ChecksumActivity is used to communicate checksums from the host to the clients.
 *
 * <p>A ChecksumActivity always relates to a certain file (given a path) and contains the hash and
 * length of the file.
 *
 * <p>To indicate that a file is missing on the host NON_EXISTING_DOC is used.
 *
 * <p>A ChecksumActivity also may contain a {@link Timestamp} to indicate at which point of time the
 * checksum was created. A user can use this information to see whether the checksum can be used to
 * check for consistency or whether the local user has already written additional text which
 * invalidates the checksum.
 */
@XStreamAlias("checksumActivity")
public class ChecksumActivity extends AbstractResourceActivity {

  /** Constant used for representing a missing file */
  public static final int NON_EXISTING_DOC = -1;

  @XStreamAsAttribute protected final long hash;

  @XStreamAsAttribute protected final long length;

  @XStreamAsAttribute protected final Timestamp jupiterTimestamp;

  /**
   * Constructor for ChecksumActivities. Timestamp can be null. ChecksumActivities created by the
   * watchdog don't have access to the JupiterClients and therefore create an Activity without
   * timestamp. Timestamps will be added later by the ConcurrentDocumentClient /-Server (by creating
   * a new Activity with a timestamp)
   *
   * @param source The User that created this activity
   * @param path The SPath pointing to the document
   * @param hash The hashcode of the document
   * @param length The length of the document
   * @param jupiterTimestamp The current jupiterTimestamp for this document, may be <code>null
   *     </code>
   */
  public ChecksumActivity(
      User source, SPath path, long hash, long length, Timestamp jupiterTimestamp) {

    super(source, path);

    this.hash = hash;
    this.length = length;
    this.jupiterTimestamp = jupiterTimestamp;
  }

  /** Returns a copy of the ChecksumActivity with a new {@link Timestamp}. */
  public ChecksumActivity withTimestamp(Timestamp jupiterTimestamp) {
    return new ChecksumActivity(getSource(), getPath(), hash, length, jupiterTimestamp);
  }

  @Override
  public String toString() {
    return "ChecksumActivity(path: "
        + getPath()
        + ", hash: "
        + hash
        + ", length: "
        + length
        + ", jupiterTimestamp: "
        + jupiterTimestamp
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
    result = prime * result + ObjectUtils.hashCode(jupiterTimestamp);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof ChecksumActivity)) return false;

    ChecksumActivity other = (ChecksumActivity) obj;

    if (this.hash != other.hash) return false;
    if (this.length != other.length) return false;
    if (!ObjectUtils.equals(this.jupiterTimestamp, other.jupiterTimestamp)) return false;

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
}
