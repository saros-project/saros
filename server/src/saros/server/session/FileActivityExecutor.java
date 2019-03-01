package saros.server.session;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;
import saros.activities.FileActivity;
import saros.activities.SPath;
import saros.filesystem.IFile;
import saros.filesystem.IResource;
import saros.server.editor.ServerEditorManager;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;

/** Executes FileActivities and performs actual filesystem operations. */
public class FileActivityExecutor extends AbstractActivityConsumer implements Startable {

  private static final Logger LOG = Logger.getLogger(FileActivityExecutor.class);

  private final ISarosSession session;
  private final ServerEditorManager editorManager;

  /**
   * Creates a FileActivityExecutor.
   *
   * @param session the current session
   * @param editorManager the editor manager to update the file mapping on a file move
   */
  public FileActivityExecutor(ISarosSession session, ServerEditorManager editorManager) {

    this.session = session;
    this.editorManager = editorManager;
  }

  @Override
  public void start() {
    session.addActivityConsumer(this, Priority.ACTIVE);
  }

  @Override
  public void stop() {
    session.removeActivityConsumer(this);
  }

  @Override
  public void receive(FileActivity activity) {
    try {
      switch (activity.getType()) {
        case CREATED:
          executeFileCreation(activity);
          break;
        case MOVED:
          executeFileMove(activity);
          break;
        case REMOVED:
          executeFileRemoval(activity);
          break;
        default:
          LOG.warn("Unknown file activity type " + activity.getType());
          break;
      }
    } catch (IOException e) {
      LOG.error("Could not execute " + activity, e);
    }
  }

  private void executeFileCreation(FileActivity activity) throws IOException {
    IFile file = activity.getPath().getFile();
    file.create(new ByteArrayInputStream(activity.getContent()), true);
  }

  private void executeFileMove(FileActivity activity) throws IOException {
    SPath oldPath = activity.getOldPath();
    IFile oldFile = oldPath.getFile();
    SPath newPath = activity.getPath();
    IFile newFile = newPath.getFile();
    oldFile.move(activity.getPath().getFullPath(), true);
    byte[] content = activity.getContent();
    if (content != null) {
      newFile.setContents(new ByteArrayInputStream(content), true, true);
    }
    // only update if all previous operations are successful
    editorManager.updateMapping(oldPath, newPath);
  }

  private void executeFileRemoval(FileActivity activity) throws IOException {
    SPath path = activity.getPath();
    IFile file = path.getFile();
    editorManager.closeEditor(path);
    file.delete(IResource.NONE);
  }
}
