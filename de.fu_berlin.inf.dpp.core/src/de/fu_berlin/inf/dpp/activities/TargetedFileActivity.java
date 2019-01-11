package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import de.fu_berlin.inf.dpp.session.User;

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
   * @param newPath where to save the data (if {@link FileActivity.Type#CREATED}), destination of a
   *     move (if {@link FileActivity.Type#MOVED}), file to remove (if {@link
   *     FileActivity.Type#REMOVED}); never <code>null</code>
   * @param oldPath if type is {@link FileActivity.Type#MOVED}, the path from where the file was
   *     moved (<code>null</code> otherwise)
   * @param content content of the file denoted by the path (only valid for {@link
   *     FileActivity.Type#CREATED} and {@link FileActivity.Type#MOVED})
   * @param encoding the encoding the content is encoded with or <code>null</code>
   * @param purpose purpose of this FileActivity (see {@link FileActivity.Purpose} )
   */
  public TargetedFileActivity(
      User source,
      User target,
      Type type,
      SPath newPath,
      SPath oldPath,
      byte[] content,
      String encoding,
      Purpose purpose) {

    super(source, type, purpose, newPath, oldPath, content, encoding);

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
