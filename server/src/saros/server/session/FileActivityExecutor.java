package saros.server.session;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import org.apache.log4j.Logger;
import saros.activities.FileActivity;
import saros.activities.SPath;
import saros.filesystem.IFile;
import saros.repackaged.picocontainer.Startable;
import saros.server.editor.ServerEditorManager;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;

/** Executes FileActivities and performs actual filesystem operations. */
public class FileActivityExecutor extends AbstractActivityConsumer implements Startable {

  private static final Logger log = Logger.getLogger(FileActivityExecutor.class);

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
          log.warn("Unknown file activity type " + activity.getType());
          break;
      }
    } catch (IOException | IllegalCharsetNameException | UnsupportedCharsetException e) {
      log.error("Could not execute " + activity, e);
    }
  }

  private void executeFileCreation(FileActivity activity)
      throws IOException, IllegalCharsetNameException, UnsupportedCharsetException {

    IFile file = activity.getResource();
    file.create(new ByteArrayInputStream(activity.getContent()));

    String charset = activity.getEncoding();

    if (charset != null) {
      file.setCharset(charset);
    }
  }

  private void executeFileMove(FileActivity activity)
      throws IOException, IllegalCharsetNameException, UnsupportedCharsetException {

    IFile oldFile = activity.getOldResource();
    IFile newFile = activity.getResource();

    if (!oldFile.exists()) {
      log.warn(
          "Could not move file as it does not exist."
              + " source: "
              + oldFile
              + " destination: "
              + newFile);

      return;
    }

    byte[] activityContent = activity.getContent();

    InputStream contents;
    String charset;

    if (activityContent != null) {
      contents = new ByteArrayInputStream(activityContent);
      charset = activity.getEncoding();

    } else {
      contents = oldFile.getContents();
      charset = oldFile.getCharset();
    }

    newFile.create(contents);
    newFile.setCharset(charset);

    oldFile.delete();

    // only update if all previous operations are successful
    editorManager.updateMapping(oldFile, newFile);
  }

  private void executeFileRemoval(FileActivity activity) throws IOException {
    IFile file = activity.getResource();
    editorManager.closeEditor(new SPath(file));
    file.delete();
  }
}
