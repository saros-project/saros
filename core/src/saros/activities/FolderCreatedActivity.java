package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import saros.filesystem.IFolder;
import saros.session.User;

/** An activity that represents the creation of a folder made by a user during a session. */
@XStreamAlias("folderCreated")
public class FolderCreatedActivity extends AbstractResourceActivity<IFolder>
    implements IFileSystemModificationActivity<IFolder> {

  public FolderCreatedActivity(final User source, final IFolder folder) {
    super(source, folder);

    if (folder == null) throw new IllegalArgumentException("folder must not be null");
  }

  @Override
  public void dispatch(IActivityReceiver receiver) {
    receiver.receive(this);
  }

  @Override
  public boolean isValid() {
    return super.isValid() && (getResource() != null);
  }

  @Override
  public String toString() {
    return "FolderCreatedActivity [folder=" + getResource() + "]";
  }
}
