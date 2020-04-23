package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Arrays;
import java.util.Objects;
import saros.filesystem.IFile;
import saros.session.User;

@XStreamAlias("fileActivity")
public class FileActivity extends AbstractResourceActivity<IFile>
    implements IFileSystemModificationActivity<IFile> {

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

  private final ResourceTransportWrapper<IFile> oldFileWrapper;

  @XStreamAsAttribute protected final Type type;

  @XStreamAsAttribute protected final Purpose purpose;

  @XStreamAsAttribute protected String encoding;

  protected final byte[] content;

  /**
   * Generic constructor for {@link FileActivity}s
   *
   * @param source the user who is the source (originator) of this Activity
   * @param newFile where to save the data (if {@link Type#CREATED}), destination of a move (if
   *     {@link Type#MOVED}), file to remove (if {@link Type#REMOVED}); never <code>null</code>
   * @param oldFile if type is {@link Type#MOVED}, the file handle representing from where the file
   *     was moved (<code>null</code> otherwise)
   * @param content content of the file (only valid for {@link Type#CREATED} and {@link Type#MOVED})
   * @param encoding the encoding for the file or <code>null</code> if (and only if) the type is
   *     {@link Type#MOVED} and the content is <code>null</code> or the type is {@link Type#REMOVED}
   */
  public FileActivity(
      User source,
      Type type,
      Purpose purpose,
      IFile newFile,
      IFile oldFile,
      byte[] content,
      String encoding) {

    super(source, newFile);

    if (type == null) throw new IllegalArgumentException("type must not be null");
    if (purpose == null) throw new IllegalArgumentException("purpose must not be null");
    if (newFile == null) throw new IllegalArgumentException("newFile must not be null");

    switch (type) {
      case CREATED:
        if (content == null || oldFile != null) throw new IllegalArgumentException();
        break;
      case REMOVED:
        if (content != null || oldFile != null) throw new IllegalArgumentException();
        break;
      case MOVED:
        if (oldFile == null) throw new IllegalArgumentException();
        break;
    }

    if (encoding == null && (type == Type.CREATED || (type == Type.MOVED && content != null))) {
      throw new IllegalArgumentException(
          "Encoding must be passed if type is created and/or binary content is passed");
    }

    this.type = type;
    this.oldFileWrapper = oldFile != null ? new ResourceTransportWrapper<>(oldFile) : null;
    this.content = content;
    this.encoding = encoding;
    this.purpose = purpose;
  }

  /**
   * Returns the old/source file in case this Activity represents a file move.
   *
   * @return the old/source file in case this Activity represents a file move, <code>null</code>
   *     otherwise
   */
  public IFile getOldResource() {
    return oldFileWrapper != null ? oldFileWrapper.getResource() : null;
  }

  @Override
  public boolean isValid() {
    /* oldFileWrapper == null is only unexpected for Type.MOVED */
    return super.isValid()
        && (getResource() != null)
        && (oldFileWrapper != null || type != Type.MOVED);
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
    return "FileActivity [dst:file="
        + getResource()
        + ", src:file="
        + (oldFileWrapper == null ? "N/A" : oldFileWrapper.getResource())
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
    result = prime * result + Objects.hashCode(oldFileWrapper);
    result = prime * result + Objects.hashCode(type);
    result = prime * result + Objects.hashCode(purpose);
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

    if (!Objects.equals(oldFileWrapper, other.oldFileWrapper)) return false;

    if (!Arrays.equals(content, other.content)) return false;

    return Objects.equals(encoding, other.encoding);
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  public boolean isRecovery() {
    return Purpose.RECOVERY.equals(purpose);
  }
}
