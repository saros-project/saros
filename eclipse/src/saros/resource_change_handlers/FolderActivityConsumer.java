package saros.resource_change_handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.activities.IActivity;
import saros.filesystem.EclipseFolder;
import saros.repackaged.picocontainer.Startable;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;
import saros.util.FileUtils;

public final class FolderActivityConsumer extends AbstractActivityConsumer implements Startable {

  private static final Logger log = Logger.getLogger(FolderActivityConsumer.class);

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
      if (log.isTraceEnabled()) log.trace("executing folder activity: " + activity);

      resourceChangeListener.suspend();
      super.exec(activity);
    } finally {
      resourceChangeListener.resume();
    }
  }

  @Override
  public void receive(FolderCreatedActivity activity) {

    saros.filesystem.IFolder folderWrapper = activity.getResource();

    IFolder folder = ((EclipseFolder) folderWrapper).getDelegate();

    try {
      FileUtils.create(folder);
    } catch (CoreException e) {
      log.error("failed to execute folder activity: " + activity, e);
    }
  }

  @Override
  public void receive(FolderDeletedActivity activity) {

    saros.filesystem.IFolder folderWrapper = activity.getResource();

    IFolder folder = ((EclipseFolder) folderWrapper).getDelegate();

    try {
      if (folder.exists()) FileUtils.delete(folder);

    } catch (CoreException e) {
      log.error("failed to execute folder activity: " + activity, e);
    }
  }
}
