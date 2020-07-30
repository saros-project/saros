package saros.filesystem.checksum;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ResourceConverter;
import saros.repackaged.picocontainer.Startable;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

/**
 * Bridge class that maps Eclipse Resource change events to unique identifiers by retrieving the
 * absolute path relative to the workspace and converting the path to a unique string.
 *
 * @author Stefan Rossbach
 */
public class FileContentNotifierBridge
    implements IFileContentChangedNotifier, IResourceChangeListener, Startable {

  private static final Logger log = Logger.getLogger(FileContentNotifierBridge.class);

  private final ISarosSessionManager sarosSessionManager;

  private CopyOnWriteArrayList<IFileContentChangedListener> fileContentChangedListeners =
      new CopyOnWriteArrayList<IFileContentChangedListener>();

  public FileContentNotifierBridge(ISarosSessionManager sarosSessionManager) {
    this.sarosSessionManager = sarosSessionManager;
  }

  @Override
  public void start() {
    ResourcesPlugin.getWorkspace()
        .addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
  }

  @Override
  public void stop() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
  }

  @Override
  public void resourceChanged(IResourceChangeEvent event) {
    if (event.getDelta() == null) return;

    Deque<IResourceDelta> stack = new LinkedList<IResourceDelta>();

    stack.addAll(Arrays.asList(event.getDelta().getAffectedChildren()));

    while (!stack.isEmpty()) {
      IResourceDelta delta = stack.pop();
      stack.addAll(Arrays.asList(delta.getAffectedChildren()));

      if (delta.getResource().getType() == IResource.FILE) {

        // TODO check the Eclipse API to ignore more events
        if (delta.getKind() == IResourceDelta.NO_CHANGE) continue;

        if ((delta.getKind() == IResourceDelta.CHANGED)
            && (delta.getFlags() == IResourceDelta.MARKERS)) continue;

        final IFile file = delta.getResource().getAdapter(IFile.class);

        final ISarosSession sarosSession = sarosSessionManager.getSession();
        if (sarosSession == null) {
          if (log.isTraceEnabled()) {
            log.trace("Ignoring resource change without a running session for file " + file);
          }
          return;
        }

        Set<IReferencePoint> sharedReferencePoints = sarosSession.getReferencePoints();
        saros.filesystem.IFile fileWrapper =
            ResourceConverter.convertToFile(sharedReferencePoints, file);

        if (fileWrapper == null) {
          if (log.isTraceEnabled()) {
            log.trace("Ignoring resource change for non-shared file " + file);
          }
          return;
        }

        for (IFileContentChangedListener listener : fileContentChangedListeners) {
          try {
            listener.fileContentChanged(fileWrapper);
          } catch (RuntimeException e) {
            log.error("internal error in listener: " + listener, e);
          }
        }
      }
    }
  }

  @Override
  public void addFileContentChangedListener(IFileContentChangedListener listener) {
    fileContentChangedListeners.add(listener);
  }

  @Override
  public void removeFileContentChangedListener(IFileContentChangedListener listener) {
    fileContentChangedListeners.remove(listener);
  }
}
