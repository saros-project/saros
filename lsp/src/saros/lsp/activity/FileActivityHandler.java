package saros.lsp.activity;

import saros.activities.FileActivity;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;

/**
 * The ActivityHandler is responsible for receiving activities regarding file/folder creations and
 * deletions and applying them to the local workspace.
 */
public class FileActivityHandler extends AbstractActivityConsumer {

  public FileActivityHandler(final ISarosSession session) {
    session.addActivityConsumer(this, Priority.ACTIVE);
  }

  @Override
  public void receive(final FileActivity fileActivity) {}

  @Override
  public void receive(final FolderCreatedActivity folderCreatedActivity) {}

  @Override
  public void receive(final FolderDeletedActivity folderDeletedActivity) {}
}
