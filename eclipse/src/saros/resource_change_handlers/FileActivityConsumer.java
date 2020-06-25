package saros.resource_change_handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import saros.activities.FileActivity;
import saros.activities.IActivity;
import saros.editor.EditorManager;
import saros.filesystem.ResourceConverter;
import saros.repackaged.picocontainer.Startable;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;
import saros.util.FileUtils;

public class FileActivityConsumer extends AbstractActivityConsumer implements Startable {

  private static final Logger log = Logger.getLogger(FileActivityConsumer.class);

  private final ISarosSession session;
  private final SharedResourcesManager resourceChangeListener;
  private final EditorManager editorManager;

  public FileActivityConsumer(
      final ISarosSession session,
      final SharedResourcesManager resourceChangeListener,
      final EditorManager editorManager) {

    this.session = session;
    this.resourceChangeListener = resourceChangeListener;
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
  public void exec(IActivity activity) {
    if (!(activity instanceof FileActivity)) return;

    try {
      if (log.isTraceEnabled()) log.trace("executing file activity: " + activity);

      resourceChangeListener.suspend();
      super.exec(activity);
    } finally {
      resourceChangeListener.resume();
    }
  }

  @Override
  public void receive(FileActivity activity) {
    try {
      handleFileActivity(activity);

    } catch (CoreException
        | IOException
        | UnsupportedCharsetException
        | IllegalCharsetNameException e) {

      log.error("failed to execute file activity: " + activity, e);
    }
  }

  private void handleFileActivity(FileActivity activity)
      throws CoreException, IOException, IllegalCharsetNameException, UnsupportedCharsetException {

    if (activity.isRecovery()) {
      handleFileRecovery(activity);
      return;
    }

    // TODO check if we should open / close existing editors here too
    switch (activity.getType()) {
      case CREATED:
        handleFileCreation(activity);
        break;
      case REMOVED:
        handleFileDeletion(activity);
        break;
      case MOVED:
        handleFileMove(activity);
        break;
    }
  }

  private void handleFileRecovery(FileActivity activity)
      throws CoreException, IOException, IllegalCharsetNameException, UnsupportedCharsetException {

    saros.filesystem.IFile file = activity.getResource();

    log.debug("performing recovery for file: " + file);

    /*
     * We have to save the editor or otherwise the internal buffer is not
     * flushed and so replacing the file on disk will have NO impact on the
     * actual content !
     */
    editorManager.saveLazy(file);

    boolean editorWasOpen = editorManager.isOpenEditor(file);

    if (editorWasOpen) editorManager.closeEditor(file);

    FileActivity.Type type = activity.getType();

    try {
      if (type == FileActivity.Type.CREATED) handleFileCreation(activity);
      else if (type == FileActivity.Type.REMOVED) handleFileDeletion(activity);
      else log.warn("performing recovery for type " + type + " is not supported");
    } finally {

      // TODO why does Jupiter not process the activities by itself ?

      /*
       * always reset Jupiter algorithm, because upon receiving that
       * activity, it was already reset on the host side
       */
      session.getConcurrentDocumentClient().reset(file);
    }

    if (editorWasOpen && type != FileActivity.Type.REMOVED) editorManager.openEditor(file, true);
  }

  private void handleFileMove(FileActivity activity)
      throws CoreException, IOException, IllegalCharsetNameException, UnsupportedCharsetException {

    final IFile fileDestination = ResourceConverter.getDelegate(activity.getResource());

    final IFile fileToMove = ResourceConverter.getDelegate(activity.getOldResource());

    FileUtils.mkdirs(fileDestination);

    FileUtils.move(fileDestination.getFullPath(), fileToMove);

    if (activity.getContent() == null) return;

    handleFileCreation(activity);
  }

  private void handleFileDeletion(FileActivity activity) throws CoreException {
    saros.filesystem.IFile fileWrapper = activity.getResource();

    editorManager.closeEditor(fileWrapper, false);

    final IFile file = ResourceConverter.getDelegate(fileWrapper);

    if (file.exists()) FileUtils.delete(file);
    else log.warn("could not delete file " + file + " because it does not exist");
  }

  private void handleFileCreation(FileActivity activity)
      throws CoreException, IOException, IllegalCharsetNameException, UnsupportedCharsetException {

    saros.filesystem.IFile sarosFile = activity.getResource();

    final IFile file = ResourceConverter.getDelegate(sarosFile);

    final String encoding = activity.getEncoding();
    final byte[] newContent = activity.getContent();

    byte[] actualContent = null;

    if (file.exists()) actualContent = FileUtils.getLocalFileContent(file);

    if (!Arrays.equals(newContent, actualContent)) {
      FileUtils.writeFile(new ByteArrayInputStream(newContent), file);
    } else {
      log.debug("FileActivity " + activity + " dropped (same content)");
    }

    if (encoding != null) sarosFile.setCharset(encoding);
  }
}
