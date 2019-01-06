package de.fu_berlin.inf.dpp.server.session;

import de.fu_berlin.inf.dpp.activities.FolderCreatedActivity;
import de.fu_berlin.inf.dpp.activities.FolderDeletedActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.server.editor.ServerEditorManager;
import de.fu_berlin.inf.dpp.server.filesystem.ServerFileImpl;
import de.fu_berlin.inf.dpp.server.filesystem.ServerFolderImpl;
import de.fu_berlin.inf.dpp.server.filesystem.ServerReferencePointManager;
import de.fu_berlin.inf.dpp.server.filesystem.ServerWorkspaceImpl;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

/** Executes FolderActivities and performs actual filesystem operations. */
public class FolderActivityExecutor extends AbstractActivityConsumer implements Startable {

  private static final Logger LOG = Logger.getLogger(FolderActivityExecutor.class);

  private final ISarosSession session;
  private final ServerEditorManager editorManager;
  private final ServerWorkspaceImpl workspace;

  /**
   * Creates a FolderActivityExecutor.
   *
   * @param session the current session
   * @param editorManager the editor manager
   */
  public FolderActivityExecutor(
      ISarosSession session, ServerEditorManager editorManager, ServerWorkspaceImpl workspace) {

    this.session = session;
    this.editorManager = editorManager;
    this.workspace = workspace;
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
  public void receive(FolderCreatedActivity activity) {
    try {
      executeFolderCreation(activity);
    } catch (IOException e) {
      LOG.error("Could not execute " + activity, e);
    }
  }

  @Override
  public void receive(FolderDeletedActivity activity) {
    try {
      executeFolderRemoval(activity);
    } catch (IOException e) {
      LOG.error("Could not execute " + activity, e);
    }
  }

  private void executeFolderCreation(FolderCreatedActivity activity) throws IOException {

    SPath path = activity.getPath();
    IPath referencePointRelativePath = path.getProjectRelativePath();

    IFolder folder = new ServerFolderImpl(workspace, referencePointRelativePath);

    folder.create(IResource.NONE, true);
  }

  private void executeFolderRemoval(FolderDeletedActivity activity) throws IOException {

    SPath path = activity.getPath();
    IPath referencePointRelativePath = path.getProjectRelativePath();

    IFolder folder = new ServerFolderImpl(workspace, referencePointRelativePath);

    folder.delete(IResource.NONE);
    editorManager.closeEditorsInFolder(path);
  }
}
