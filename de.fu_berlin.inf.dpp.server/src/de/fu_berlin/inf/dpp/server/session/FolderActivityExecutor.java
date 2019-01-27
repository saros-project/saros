package de.fu_berlin.inf.dpp.server.session;

import de.fu_berlin.inf.dpp.activities.FolderCreatedActivity;
import de.fu_berlin.inf.dpp.activities.FolderDeletedActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.server.editor.ServerEditorManager;
import de.fu_berlin.inf.dpp.server.filesystem.ServerFolderImpl;
import de.fu_berlin.inf.dpp.server.filesystem.ServerPathFactoryImpl;
import de.fu_berlin.inf.dpp.server.filesystem.ServerReferencePointManager;
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
  private final ServerReferencePointManager serverReferencePointManager;

  /**
   * Creates a FolderActivityExecutor.
   *
   * @param session the current session
   * @param editorManager the editor manager
   */
  public FolderActivityExecutor(
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

    IFolder folder = getFolder(path);

    folder.create(IResource.NONE, true);
  }

  private void executeFolderRemoval(FolderDeletedActivity activity) throws IOException {

    SPath path = activity.getPath();

    IFolder folder = getFolder(path);

    folder.delete(IResource.NONE);
    editorManager.closeEditorsInFolder(path);
  }

  private IFolder getFolder(SPath path) {
    IReferencePoint referencePoint = path.getReferencePoint();
    IPath referencePointRelativePath = path.getReferencePointRelativePath();

    File file = serverReferencePointManager.get(referencePoint);
    ServerPathFactoryImpl pathFactory = new ServerPathFactoryImpl();

    IPath referencePointPath = pathFactory.fromString(file.getPath());

    return new ServerFolderImpl(referencePointPath, referencePointRelativePath);
  }
}
