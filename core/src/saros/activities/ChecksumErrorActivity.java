package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import saros.filesystem.IFile;
import saros.session.User;

/**
 * A Checksum Error is a notification sent to the host by a user who wants inconsistencies to be
 * recovered.
 *
 * <p>The host will reply with a ChecksumError of the same recoveryID after having sent the last
 * FileActivity (with {@link FileActivity#isRecovery()} being set related to this checksum recovery.
 */
@XStreamAlias("checksumErrorActivity")
public class ChecksumErrorActivity extends AbstractActivity implements ITargetedActivity {

  @XStreamAsAttribute private User target;

  @XStreamAsAttribute protected String recoveryID;

  @XStreamImplicit protected List<ResourceTransportWrapper<IFile>> files;

  public ChecksumErrorActivity(User source, User target, List<IFile> files, String recoveryID) {

    super(source);

    if (target == null) throw new IllegalArgumentException("target must not be null");

    this.target = target;
    this.files =
        files == null
            ? null
            : files.stream().map(ResourceTransportWrapper::new).collect(Collectors.toList());
    this.recoveryID = recoveryID;
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (target != null);
  }

  public List<IFile> getFiles() {
    return files == null
        ? null
        : files.stream().map(ResourceTransportWrapper::getResource).collect(Collectors.toList());
  }

  /** Each ChecksumError has a unique ID, which should be used to identify a recovery session */
  public String getRecoveryID() {
    return recoveryID;
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hashCode(files);
    result = prime * result + Objects.hashCode(recoveryID);
    result = prime * result + Objects.hashCode(target);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof ChecksumErrorActivity)) return false;

    ChecksumErrorActivity other = (ChecksumErrorActivity) obj;

    if (!Objects.equals(this.recoveryID, other.recoveryID)) return false;
    if (!Objects.equals(this.files, other.files)) return false;
    if (!Objects.equals(this.target, other.target)) return false;

    return true;
  }

  @Override
  public String toString() {
    return "ChecksumErrorActivity(src: "
        + getSource()
        + ", target: "
        + target
        + ", files: "
        + getFiles()
        + ", recoveryID: "
        + recoveryID
        + ")";
  }

  @Override
  public User getTarget() {
    return target;
  }
}
