package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.activities.FolderCreatedActivity;
import de.fu_berlin.inf.dpp.activities.FolderDeletedActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.EclipseReferencePointManager;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.util.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.picocontainer.Startable;

public final class FolderActivityConsumer extends AbstractActivityConsumer implements Startable {

  private static final Logger LOG = Logger.getLogger(FolderActivityConsumer.class);

  private final ISarosSession session;
  private final SharedResourcesManager resourceChangeListener;
  private final EclipseReferencePointManager eclipseReferencePointManager;

  public FolderActivityConsumer(
      final ISarosSession session,
      final SharedResourcesManager resourceChangeListener,
      final EclipseReferencePointManager eclipseReferencePointManager) {

    this.session = session;
    this.resourceChangeListener = resourceChangeListener;
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

    IReferencePoint referencePoint = path.getReferencePoint();
    IPath referencePointRelativePath = path.getReferencePointRelativePath();

    IFolder folder =
        eclipseReferencePointManager.getFolder(
            referencePoint, ResourceAdapterFactory.convertBack(referencePointRelativePath));

    try {
      FileUtils.create(folder);
    } catch (CoreException e) {
      LOG.error("failed to execute folder activity: " + activity, e);
    }
  }

  @Override
  public void receive(FolderDeletedActivity activity) {

    SPath path = activity.getPath();

    IReferencePoint referencePoint = path.getReferencePoint();
    IPath referencePointRelativePath = path.getReferencePointRelativePath();

    IFolder folder =
        eclipseReferencePointManager.getFolder(
            referencePoint, ResourceAdapterFactory.convertBack(referencePointRelativePath));

    try {
      if (folder.exists()) FileUtils.delete(folder);

    } catch (CoreException e) {
      LOG.error("failed to execute folder activity: " + activity, e);
    }
  }
}
