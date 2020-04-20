package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import saros.filesystem.IFile;
import saros.session.User;

/** Subclass of FileActivity that allows the specification of targets. */
@XStreamAlias("targetedFileActivity")
public class TargetedFileActivity extends FileActivity implements ITargetedActivity {

  @XStreamAsAttribute private User target;

  /**
   * Generic constructor for {@link TargetedFileActivity}
   *
   * @param source the user who is the source (originator) of this Activity
   * @param target the target user to receive this Activity
   * @param type Type of this FileActivity (see {@link FileActivity.Type})
   * @param newFile where to save the data (if {@link Type#CREATED}), destination of a move (if
   *     {@link Type#MOVED}), file to remove (if {@link Type#REMOVED}); never <code>null</code>
   * @param oldFile if type is {@link Type#MOVED}, the file handle representing from where the file
   *     was moved (<code>null</code> otherwise)
   * @param content content of the file denoted by the path (only valid for {@link
   *     FileActivity.Type#CREATED} and {@link FileActivity.Type#MOVED})
   * @param encoding the encoding the content is encoded with or <code>null</code>
   * @param purpose purpose of this FileActivity (see {@link FileActivity.Purpose} )
   */
  public TargetedFileActivity(
      User source,
      User target,
      Type type,
      IFile newFile,
      IFile oldFile,
      byte[] content,
      String encoding,
      Purpose purpose) {

    super(source, type, purpose, newFile, oldFile, content, encoding);

    if (target == null) throw new IllegalArgumentException("target must not be null");

    this.target = target;
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (target != null);
  }

  @Override
  public User getTarget() {
    return target;
  }

  @Override
  public String toString() {
    return "TargetedFileActivity [target="
        + target
        + "purpose="
        + purpose
        + ", dst:path="
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
}
