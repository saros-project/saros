package saros.server.session;

import java.io.IOException;
import org.apache.log4j.Logger;
import saros.activities.FileActivity;
import saros.activities.SPath;
import saros.repackaged.picocontainer.Startable;
import saros.server.editor.ServerEditorManager;
import saros.server.filesystem.ServerReferencePointManager;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;

/** Executes FileActivities and performs actual filesystem operations. */
public class FileActivityExecutor extends AbstractActivityConsumer implements Startable {

  private static final Logger LOG = Logger.getLogger(FileActivityExecutor.class);

  private final ISarosSession session;
  private final ServerEditorManager editorManager;
  private final ServerReferencePointManager serverReferencePointManager;

  /**
   * Creates a FileActivityExecutor.
   *
   * @param session the current session
   * @param editorManager the editor manager to update the file mapping on a file move
   */
  public FileActivityExecutor(
      ISarosSession session,
      ServerEditorManager editorManager,
      ServerReferencePointManager serverReferencePointManager) {

    this.session = session;
    this.editorManager = editorManager;
    this.serverReferencePointManager = serverReferencePointManager;
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
    SPath pathToFile = activity.getPath();
    byte[] content = activity.getContent();

    serverReferencePointManager.createFile(pathToFile, content);
  }

  private void executeFileMove(FileActivity activity) throws IOException {
    SPath oldPath = activity.getOldPath();
    SPath newPath = activity.getPath();
    byte[] content = activity.getContent();

    serverReferencePointManager.moveResource(oldPath, newPath, content);
    // only update if all previous operations are successful
    editorManager.updateMapping(oldPath, newPath);
  }

  private void executeFileRemoval(FileActivity activity) throws IOException {
    SPath path = activity.getPath();
    editorManager.closeEditor(path);

    serverReferencePointManager.deleteFile(path);
  }
}
