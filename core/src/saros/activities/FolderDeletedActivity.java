package saros.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import saros.concurrent.management.ConcurrentDocumentClient;
import saros.concurrent.management.ConcurrentDocumentServer;
import saros.filesystem.IFolder;
import saros.session.User;

/**
 * An activity that represents the deletion of a folder made by a user during a session.
 *
 * <p><b>NOTE:</b> Any resource that is contained in the deleted folder should have been processed
 * separately before dispatching the folder deletion activity. This is important to allow the other
 * session participants to clean up the state of all deleted child resources. Furthermore, the
 * explicit handling of deleted child resources is required by the {@link ConcurrentDocumentServer}
 * and {@link ConcurrentDocumentClient}.
 */
@XStreamAlias("folderDeleted")
public class FolderDeletedActivity extends AbstractResourceActivity
    implements IFileSystemModificationActivity {

  public FolderDeletedActivity(final User source, final IFolder folder) {
    super(source, new SPath(folder));

    if (folder == null) throw new IllegalArgumentException("path must not be null");
  }

  @Override
  public IFolder getResource() {
    return getPath().getFolder();
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
    return "FolderDeletedActivity [path=" + getPath() + "]";
  }
}
