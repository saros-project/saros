package saros.concurrent.watchdog;

import saros.activities.SPath;

/**
 * Represents a checksum of a document in the workspace. It consists of the document's
 * project-relative path, the content length and the content's string hash code.
 */
public class DocumentChecksum {

  /**
   * The return value of {@link #getLength()} and {@link #getHash()} if the checksum's associated
   * document is not available (i.e., the document doesn't exist).
   */
  public static final int NOT_AVAILABLE = -1;

  private SPath path;
  private int length;
  private int hash;
  private boolean dirty;

  /**
   * Creates a new DocumentChecksum.
   *
   * @param path the document's project-relative path
   */
  public DocumentChecksum(SPath path) {
    this.path = path;
    this.dirty = true;
  }

  /**
   * Returns the project-relative path of the checksum's associated document.
   *
   * @return document path
   */
  public SPath getPath() {
    return path;
  }

  /**
   * Returns the length of the associated document's content.
   *
   * <p>If the document's content is not available ({@link #update} was not called yet or called
   * with <code>null</code> the last time), {@link #NOT_AVAILABLE} is returned.
   *
   * @return document content length, or {@link #NOT_AVAILABLE} if the document doesn't exist
   */
  public int getLength() {
    return length;
  }

  /**
   * Returns the hash code of the associated document's content.
   *
   * <p>If the document's content is not available ({@link #update} was not called yet or called
   * with <code>null</code> the last time), {@link #NOT_AVAILABLE} is returned.
   *
   * @return document content hash, or {@link #NOT_AVAILABLE} if not available, or {@link
   *     #NOT_AVAILABLE} if not available
   */
  public int getHash() {
    return hash;
  }

  /**
   * Returns whether the checksum (more specifically, what was calculated for {@link #getHash()} and
   * {@link #getLength()} in the last call to {@link #update(String)}) is out-of-sync with the
   * associated document's current content.
   *
   * <p>Note that this determines whether {@link #update(String)} actually does any checksum
   * calculations, so make sure to call {@link #markDirty()} to notify the checksum whenever the
   * document changes.
   *
   * <p>A DocumentChecksum is automatically marked as dirty when it created.
   *
   * @return <code>true</code> if the checksum is out-of-sync, <code>false</code> if it is
   *     up-to-date
   */
  public boolean isDirty() {
    return dirty;
  }

  /**
   * Tells the checksum that its content length and hash code are out-of-date because the associated
   * document changed.
   */
  public void markDirty() {
    dirty = true;
  }

  /**
   * Recalculates the checksum's content hash code and updates the returned content length, provided
   * the checksum is marked as {@link #isDirty() dirty}. If not, it does nothing.
   *
   * <p>This method resets the checksum to be non-dirty.
   *
   * @param documentContent the document's current content, or <code>null</code> if the document is
   *     does not exist locally
   */
  public void update(String documentContent) {
    if (!dirty) return;

    if (documentContent == null) {
      length = hash = NOT_AVAILABLE;
    } else {
      length = documentContent.length();
      hash = documentContent.hashCode();
    }

    dirty = false;
  }

  @Override
  public String toString() {
    return path.toString() + " [" + this.length + "," + this.hash + "]";
  }
}
