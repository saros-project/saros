package saros.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import saros.activities.IActivity;
import saros.activities.IResourceActivity;
import saros.annotations.Component;
import saros.editor.EditorManager;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.filesystem.ResourceConverter;
import saros.observables.FileReplacementInProgressObservable;
import saros.repackaged.picocontainer.Startable;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.AbstractActivityProducer;
import saros.session.ISarosSession;
import saros.synchronize.Blockable;
import saros.synchronize.StopManager;

/**
 * This manager is responsible for handling all resource changes that aren't handled by the
 * EditorManager, that is for changes that aren't done by entering text in a text editor. It
 * produces and consumes file and folder activities.
 *
 * <p>TODO Extract AbstractActivityProducer/Consumer functionality in another classes
 * ResourceActivityProducer/Consumer, rename to SharedResourceChangeListener.
 */
/*
 * For a good introduction to Eclipse's resource change notification mechanisms
 * see
 * http://www.eclipse.org/articles/Article-Resource-deltas/resource-deltas.html
 */
@Component(module = "core")
public class SharedResourcesManager extends AbstractActivityProducer
    implements IResourceChangeListener, Startable {
  /** The {@link IResourceChangeEvent}s we're going to register for. */
  /*
   * haferburg: We're really only interested in
   * IResourceChangeEvent.POST_CHANGE events. I don't know why other events
   * were tracked, so I removed them.
   *
   * We're definitely not interested in PRE_REFRESH, refreshes are only
   * interesting when they result in an actual change, in which case we will
   * receive a POST_CHANGE event anyways.
   *
   * We also don't need PRE_CLOSE, since we'll also get a POST_CHANGE and
   * still have to test project.isOpen().
   *
   * We might want to add PRE_DELETE if the user deletes a shared reference point base resource
   * though.
   */
  private static final int INTERESTING_EVENTS = IResourceChangeEvent.POST_CHANGE;

  private static final Logger log = Logger.getLogger(SharedResourcesManager.class);

  /**
   * If the StopManager has paused the session, the SharedResourcesManager doesn't react to resource
   * changes.
   */
  private boolean pause = false;

  private final ISarosSession sarosSession;

  private final StopManager stopManager;

  /**
   * Should return <code>true</code> while executing resource changes to avoid an infinite resource
   * event loop.
   */
  @Inject private FileReplacementInProgressObservable fileReplacementInProgressObservable;

  private final EditorManager editorManager;

  private final Blockable stopManagerListener =
      new Blockable() {
        @Override
        public void unblock() {
          SharedResourcesManager.this.pause = false;
        }

        @Override
        public void block() {
          SharedResourcesManager.this.pause = true;
        }
      };

  @Override
  public void start() {
    sarosSession.addActivityProducer(this);
    stopManager.addBlockable(stopManagerListener);
    ResourcesPlugin.getWorkspace().addResourceChangeListener(this, INTERESTING_EVENTS);
  }

  @Override
  public void stop() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    stopManager.removeBlockable(stopManagerListener);
    sarosSession.removeActivityProducer(this);
  }

  public SharedResourcesManager(
      ISarosSession sarosSession, EditorManager editorManager, StopManager stopManager) {
    this.sarosSession = sarosSession;
    this.editorManager = editorManager;
    this.stopManager = stopManager;
  }

  /** This method is called from Eclipse when changes to resource are detected */
  @Override
  public void resourceChanged(IResourceChangeEvent event) {

    /*
     * FIXME this is REAL GARBAGE ! This is sometimes set by the
     * IncomingResourceNegotiation. So when it is set every change in an
     * already shared reference point is just SILENTLY IGNORED !!!
     */
    if (fileReplacementInProgressObservable.isReplacementInProgress()) return;

    if (pause) {
      logPauseWarning(event);
      return;
    }

    // Creations, deletions, modifications of files and folders.
    if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
      handlePostChange(event);
    } else {
      log.warn("cannot handle event type : " + event);
    }
  }

  /*
   * FIXME this will lockout everything. File changes made in the meantime
   * from another background job are not recognized. See AddMultipleFilesTest
   * STF test which fails randomly.
   */

  /**
   * Suspends every listening to file changes.
   *
   * @deprecated error prone as the Eclipse Workspace can be accessed concurrently
   */
  @Deprecated
  void suspend() {
    fileReplacementInProgressObservable.startReplacement();
  }

  /**
   * Resumes every listening to file changes.
   *
   * @deprecated error prone as the Eclipse Workspace can be accessed concurrently
   */
  @Deprecated
  void resume() {
    fileReplacementInProgressObservable.replacementDone();
  }

  private void handlePostChange(IResourceChangeEvent event) {

    if (!sarosSession.hasWriteAccess()) {
      return;
    }

    IResourceDelta delta = event.getDelta();

    if (log.isTraceEnabled()) {
      IJobManager jobManager = Job.getJobManager();
      Job currentJob = jobManager.currentJob();
      log.trace(
          "received resource change event caused by job  ='"
              + (currentJob == null ? "N/A" : currentJob.getName())
              + "'");
    }

    if (delta == null) {
      log.error("unexpected empty delta in resource change event: " + event);
      return;
    }

    if (log.isTraceEnabled())
      log.trace("received resource delta '" + delta + "' contains:\n" + deltaToString(delta));

    assert delta.getResource() instanceof IWorkspaceRoot;

    final List<IResourceActivity<? extends IResource>> resourceActivities = new ArrayList<>();

    Map<IProject, Set<IReferencePoint>> projectReferencePointMap = createProjectReferencePointMap();

    for (IResourceDelta projectDelta : delta.getAffectedChildren()) {

      assert projectDelta.getResource() instanceof IProject;

      IProject project = (IProject) projectDelta.getResource();

      if (isProjectOpenedDelta(projectDelta)) {
        if (log.isDebugEnabled()) {
          log.debug("ignoring delta changes for project " + project + " as it was only opened");
        }

        continue;
      }

      Set<IReferencePoint> projectReferencePoints = projectReferencePointMap.get(project);

      if (projectReferencePoints == null) {
        continue;
      }

      for (IReferencePoint referencePoint : projectReferencePoints) {
        ReferencePointResourceDeltaVisitor referencePointResourceDeltaVisitor =
            new ReferencePointResourceDeltaVisitor(sarosSession, editorManager, referencePoint);

        IContainer referencePointDelegate = ResourceConverter.getDelegate(referencePoint);

        IResourceDelta referencePointDelta =
            projectDelta.findMember(referencePointDelegate.getProjectRelativePath());

        if (referencePointDelta == null) {
          continue;
        }

        try {
          /*
           * There is some magic involved here. The ReferencePointResourceDeltaVisitor
           * will ignore changed files that are currently opened in an
           * editor to prevent transmitting the whole file content of the
           * modified file.
           *
           * FIXME document this behavior in the ReferencePointResourceDeltaVisitor !
           */
          referencePointDelta.accept(referencePointResourceDeltaVisitor, IContainer.INCLUDE_HIDDEN);

        } catch (CoreException e) {
          // cannot be thrown by our custom visitor
          log.warn(
              this.getClass().getSimpleName() + " is not supposed to throw a CoreException", e);
        }

        resourceActivities.addAll(referencePointResourceDeltaVisitor.getActivities());
      }
    }

    if (log.isTraceEnabled()) {
      log.trace(
          "generated resource activities for current resource delta '"
              + delta
              + "' : "
              + resourceActivities);
    }

    /*
     * TODO for every activity have to synchronize on the GUI thread, maybe
     * offer a bulk method ?
     */
    for (final IActivity activity : resourceActivities) fireActivity(activity);
  }

  private Map<IProject, Set<IReferencePoint>> createProjectReferencePointMap() {
    Map<IProject, Set<IReferencePoint>> projectReferencePointMap = new HashMap<>();

    for (IReferencePoint referencePoint : sarosSession.getReferencePoints()) {
      IProject referencePointProject = ResourceConverter.getDelegate(referencePoint).getProject();

      Set<IReferencePoint> projectReferencePoints =
          projectReferencePointMap.computeIfAbsent(referencePointProject, (key) -> new HashSet<>());

      projectReferencePoints.add(referencePoint);
    }

    return projectReferencePointMap;
  }

  /**
   * Returns whether the resource delta describes a project being opened.
   *
   * @param resourceDelta the resource delta to check
   * @return whether the resource delta describes a project being opened
   * @see IResourceDelta#ADDED
   * @see IResourceDelta#OPEN
   */
  private boolean isProjectOpenedDelta(IResourceDelta resourceDelta) {
    if ((resourceDelta.getKind() & IResourceDelta.ADDED) == IResourceDelta.ADDED) {
      return false;
    }

    IProject project = resourceDelta.getResource().getAdapter(IProject.class);

    if (project == null) {
      return false;
    }

    boolean isOpenDelta = (resourceDelta.getFlags() & IResourceDelta.OPEN) == IResourceDelta.OPEN;

    return isOpenDelta && project.isOpen();
  }

  /*
   * coezbek: This warning is misleading! The consistency recovery process
   * might cause IResourceChangeEvents (which do not need to be replicated)
   * [Added in branches/10.2.26.r2028, the commit message claims "Improved
   * logging of ResourceChanges while paused".]
   *
   * haferburg: When is this even called? We don't get here while this class
   * executes any activity. We can only get here when pause is true, but not
   * fileReplacementInProgressObservable. Also, why add a misleading warning
   * in the first place??
   */
  private void logPauseWarning(IResourceChangeEvent event) {
    if (event.getType() == IResourceChangeEvent.POST_CHANGE) {

      IResourceDelta delta = event.getDelta();
      if (delta == null) {
        log.error(
            "Resource changed while paused"
                + " but unexpected empty delta in "
                + "SharedResourcesManager: "
                + event);
        return;
      }

      log.warn("Resource changed while paused:\n" + deltaToString(delta));
    } else {
      log.error("Unexpected event type in in logPauseWarning: " + event);
    }
  }

  private String deltaToString(IResourceDelta delta) {
    ToStringResourceDeltaVisitor visitor = new ToStringResourceDeltaVisitor();
    try {
      delta.accept(
          visitor,
          IContainer.INCLUDE_PHANTOMS
              | IContainer.INCLUDE_HIDDEN
              | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
    } catch (CoreException e) {
      log.error("ToStringResourceDelta visitor crashed", e);
      return "";
    }
    return visitor.toString();
  }
}
