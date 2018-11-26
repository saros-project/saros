package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.fu_berlin.inf.dpp.session.User;

/** An activity that represents the creation of a folder made by a user during a session. */
@XStreamAlias("folderCreated")
public class FolderCreatedActivity extends AbstractResourceActivity
    implements IFileSystemModificationActivity {

  public FolderCreatedActivity(final User source, final SPath path) {
    super(source, path);

    if (path == null) throw new IllegalArgumentException("path must not be null");
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (getPath() != null);
  }

  @Override
  public String toString() {
    return "FolderCreatedActivity [path=" + getPath() + "]";
  }
}
