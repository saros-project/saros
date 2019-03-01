package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Arrays;
import org.apache.commons.lang3.ObjectUtils;
import saros.session.User;

@XStreamAlias("fileActivity")
public class FileActivity extends AbstractResourceActivity
    implements IFileSystemModificationActivity {

  /**
   * Enumeration used to distinguish file activities which are caused as part of a consistency
   * recovery and those used as regular activities.
   */
  public static enum Purpose {
    ACTIVITY,
    RECOVERY;
  }

  public static enum Type {
    /** The file was created or modified, but the path stayed the same. */
    CREATED,
    /** The file was deleted. */
    REMOVED,
    /** The path of the file changed. The content might have changed, too. */
    MOVED
  }

  protected final SPath oldPath;

  @XStreamAsAttribute protected final Type type;

  @XStreamAsAttribute protected final Purpose purpose;

  @XStreamAsAttribute protected String encoding;

  protected final byte[] content;

  /**
   * Generic constructor for {@link FileActivity}s
   *
   * @param source the user who is the source (originator) of this Activity
   * @param newPath where to save the data (if {@link Type#CREATED}), destination of a move (if
   *     {@link Type#MOVED}), file to remove (if {@link Type#REMOVED}); never <code>null</code>
   * @param oldPath if type is {@link Type#MOVED}, the path from where the file was moved (<code>
   *     null</code> otherwise)
   * @param content content of the file denoted by the path (only valid for {@link Type#CREATED} and
   *     {@link Type#MOVED})
   * @param encoding the encoding the content is encoded with or <code>null</code>
   */
  public FileActivity(
      User source,
      Type type,
      Purpose purpose,
      SPath newPath,
      SPath oldPath,
      byte[] content,
      String encoding) {

    super(source, newPath);

    if (type == null) throw new IllegalArgumentException("type must not be null");
    if (purpose == null) throw new IllegalArgumentException("purpose must not be null");
    if (newPath == null) throw new IllegalArgumentException("newPath must not be null");

    switch (type) {
      case CREATED:
        if (content == null || oldPath != null) throw new IllegalArgumentException();
        break;
      case REMOVED:
        if (content != null || oldPath != null) throw new IllegalArgumentException();
        break;
      case MOVED:
        if (oldPath == null) throw new IllegalArgumentException();
        break;
    }

    this.type = type;
    this.oldPath = oldPath;
    this.content = content;
    this.encoding = encoding;
    this.purpose = purpose;
  }

  @Override
  public boolean isValid() {
    /* oldPath == null is only unexpected for Type.MOVED */
    return super.isValid() && (getPath() != null) && (oldPath != null || type != Type.MOVED);
  }

  /** Returns the old/source path in case this Activity represents a moving of files. */
  public SPath getOldPath() {
    return oldPath;
  }

  public Type getType() {
    return type;
  }

  /**
   * @return the content of this file or <code>null</code> if not available
   *     <p><b>Important:</b> the content of the array must <b>not</b> be changed
   */
  public byte[] getContent() {
    return content;
  }

  /**
   * Returns the encoding the content is encoded with.
   *
   * @return the encoding or <code>null</code> if it is not available
   */
  public String getEncoding() {
    return encoding;
  }

  @Override
  public String toString() {
    return "FileActivity [dst:path="
        + getPath()
        + ", src:path="
        + (oldPath == null ? "N/A" : oldPath)
        + ", type="
        + type
        + ", encoding="
        + (encoding == null ? "N/A" : encoding)
        + ", content="
        + (content == null ? "0" : content.length)
        + " byte(s)]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(content);
    result = prime * result + ObjectUtils.hashCode(oldPath);
    result = prime * result + ObjectUtils.hashCode(type);
    result = prime * result + ObjectUtils.hashCode(purpose);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (!super.equals(obj)) return false;

    if (!(obj instanceof FileActivity)) return false;

    FileActivity other = (FileActivity) obj;

    if (type != other.type) return false;

    if (purpose != other.purpose) return false;

    if (!ObjectUtils.equals(oldPath, other.oldPath)) return false;

    if (!Arrays.equals(content, other.content)) return false;

    return ObjectUtils.equals(encoding, other.encoding);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  public boolean isRecovery() {
    return Purpose.RECOVERY.equals(purpose);
  }
}
