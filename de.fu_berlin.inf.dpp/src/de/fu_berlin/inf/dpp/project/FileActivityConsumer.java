package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.EclipseReferencePointManager;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.util.FileUtils;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.picocontainer.Startable;

public class FileActivityConsumer extends AbstractActivityConsumer implements Startable {

  private static final Logger LOG = Logger.getLogger(FileActivityConsumer.class);

  private final ISarosSession session;
  private final SharedResourcesManager resourceChangeListener;
  private final EditorManager editorManager;
  private final EclipseReferencePointManager eclipseReferencePointManager;

  public FileActivityConsumer(
      final ISarosSession session,
      final SharedResourcesManager resourceChangeListener,
      final EditorManager editorManager,
      final EclipseReferencePointManager eclipseReferencePointManager) {

    this.session = session;
    this.resourceChangeListener = resourceChangeListener;
    this.editorManager = editorManager;
    this.eclipseReferencePointManager = eclipseReferencePointManager;
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
      if (LOG.isTraceEnabled()) LOG.trace("executing file activity: " + activity);

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
    } catch (CoreException e) {
      LOG.error("failed to execute file activity: " + activity, e);
    }
  }

  private void handleFileActivity(FileActivity activity) throws CoreException {

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

  private void handleFileRecovery(FileActivity activity) throws CoreException {
    SPath path = activity.getPath();

    LOG.debug("performing recovery for file: " + activity.getPath().getFullPath());

    /*
     * We have to save the editor or otherwise the internal buffer is not
     * flushed and so replacing the file on disk will have NO impact on the
     * actual content !
     */
    editorManager.saveLazy(path);

    boolean editorWasOpen = editorManager.isOpenEditor(path);

    if (editorWasOpen) editorManager.closeEditor(path);

    FileActivity.Type type = activity.getType();

    try {
      if (type == FileActivity.Type.CREATED) handleFileCreation(activity);
      else if (type == FileActivity.Type.REMOVED) handleFileDeletion(activity);
      else LOG.warn("performing recovery for type " + type + " is not supported");
    } finally {

      // TODO why does Jupiter not process the activities by itself ?

      /*
       * always reset Jupiter algorithm, because upon receiving that
       * activity, it was already reset on the host side
       */
      session.getConcurrentDocumentClient().reset(path);
    }

    if (editorWasOpen && type != FileActivity.Type.REMOVED) editorManager.openEditor(path, true);
  }

  private void handleFileMove(FileActivity activity) throws CoreException {

    SPath newPath = activity.getPath();
    SPath oldPath = activity.getOldPath();

    IReferencePoint newReferencePoint = newPath.getReferencePoint();
    IReferencePoint oldReferencePoint = oldPath.getReferencePoint();

    IPath newReferencePointRelativePath = newPath.getReferencePointRelativePath();
    IPath oldReferencePointRelativePath = oldPath.getReferencePointRelativePath();

    final IFile fileDestination =
        eclipseReferencePointManager.getFile(
            newReferencePoint, ResourceAdapterFactory.convertBack(newReferencePointRelativePath));

    final IFile fileToMove =
        eclipseReferencePointManager.getFile(
            oldReferencePoint, ResourceAdapterFactory.convertBack(oldReferencePointRelativePath));

    FileUtils.mkdirs(fileDestination);

    FileUtils.move(fileDestination.getFullPath(), fileToMove);

    if (activity.getContent() == null) return;

    handleFileCreation(activity);
  }

  private void handleFileDeletion(FileActivity activity) throws CoreException {
    SPath path = activity.getPath();

    IReferencePoint referencePoint = path.getReferencePoint();
    IPath referencePointRelativePath = path.getReferencePointRelativePath();

    final IFile file =
        eclipseReferencePointManager.getFile(
            referencePoint, ResourceAdapterFactory.convertBack(referencePointRelativePath));

    if (file.exists()) FileUtils.delete(file);
    else LOG.warn("could not delete file " + file + " because it does not exist");
  }

  private void handleFileCreation(FileActivity activity) throws CoreException {
    SPath path = activity.getPath();

    IReferencePoint referencePoint = path.getReferencePoint();
    IPath referencePointRelativePath = path.getReferencePointRelativePath();

    final IFile file =
        eclipseReferencePointManager.getFile(
            referencePoint, ResourceAdapterFactory.convertBack(referencePointRelativePath));

    final String encoding = activity.getEncoding();
    final byte[] newContent = activity.getContent();

    byte[] actualContent = null;

    if (file.exists()) actualContent = FileUtils.getLocalFileContent(file);

    if (!Arrays.equals(newContent, actualContent)) {
      FileUtils.writeFile(new ByteArrayInputStream(newContent), file);
    } else {
      LOG.debug("FileActivity " + activity + " dropped (same content)");
    }

    if (encoding != null) updateFileEncoding(encoding, file);
  }

  /**
   * Updates encoding of a file. A best effort is made to use the inherited encoding if available.
   * Does nothing if the file does not exist or the encoding to set is <code>null</code>
   *
   * @param encoding the encoding that should be used
   * @param file the file to update
   * @throws CoreException if setting the encoding failed
   */
  private static void updateFileEncoding(final String encoding, final IFile file)
      throws CoreException {

    if (encoding == null) return;

    if (!file.exists()) return;

    try {
      Charset.forName(encoding);
    } catch (Exception e) {
      LOG.warn(
          "encoding " + encoding + " for file " + file + " is not available on this platform", e);
      return;
    }

    String projectEncoding = null;
    String fileEncoding = null;

    try {
      projectEncoding = file.getProject().getDefaultCharset();
    } catch (CoreException e) {
      LOG.warn("could not determine project encoding for project " + file.getProject(), e);
    }

    try {
      fileEncoding = file.getCharset();
    } catch (CoreException e) {
      LOG.warn("could not determine file encoding for file " + file, e);
    }

    if (encoding.equals(fileEncoding)) {
      LOG.debug("encoding does not need to be changed for file: " + file);
      return;
    }

    // use inherited encoding if possible
    if (encoding.equals(projectEncoding)) {
      LOG.debug(
          "changing encoding for file "
              + file
              + " to use default project encoding: "
              + projectEncoding);
      file.setCharset(null, new NullProgressMonitor());
      return;
    }

    LOG.debug("changing encoding for file " + file + " to encoding: " + encoding);

    file.setCharset(encoding, new NullProgressMonitor());
  }

  private static IFile toEclipseIFile(de.fu_berlin.inf.dpp.filesystem.IFile file) {
    return (IFile) ResourceAdapterFactory.convertBack(file);
  }
}
