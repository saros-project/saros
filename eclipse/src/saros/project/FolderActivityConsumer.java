package saros.project;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.picocontainer.Startable;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.activities.IActivity;
import saros.activities.SPath;
import saros.filesystem.EclipseFolderImpl;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;
import saros.util.FileUtils;

public final class FolderActivityConsumer extends AbstractActivityConsumer implements Startable {

  private static final Logger LOG = Logger.getLogger(FolderActivityConsumer.class);

  private final ISarosSession session;
  private final SharedResourcesManager resourceChangeListener;

  public FolderActivityConsumer(
      final ISarosSession session, final SharedResourcesManager resourceChangeListener) {

    this.session = session;
    this.resourceChangeListener = resourceChangeListener;
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
    if (!(activity instanceof FolderCreatedActivity || activity instanceof FolderDeletedActivity))
      return;

    try {
      if (LOG.isTraceEnabled()) LOG.trace("executing folder activity: " + activity);

      resourceChangeListener.suspend();
      super.exec(activity);
    } finally {
      resourceChangeListener.resume();
    }
  }

  @Override
  public void receive(FolderCreatedActivity activity) {

    SPath path = activity.getPath();

    IFolder folder =
        ((EclipseFolderImpl) path.getProject().getFolder(path.getProjectRelativePath()))
            .getDelegate();

    try {
      FileUtils.create(folder);
    } catch (CoreException e) {
      LOG.error("failed to execute folder activity: " + activity, e);
    }
  }

  @Override
  public void receive(FolderDeletedActivity activity) {

    SPath path = activity.getPath();

    IFolder folder =
        ((EclipseFolderImpl) path.getProject().getFolder(path.getProjectRelativePath()))
            .getDelegate();

    try {
      if (folder.exists()) FileUtils.delete(folder);

    } catch (CoreException e) {
      LOG.error("failed to execute folder activity: " + activity, e);
    }
  }
}
