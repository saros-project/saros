package saros.ui.decorators;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource.Type;
import saros.filesystem.ResourceConverter;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;

/**
 * Decorates shared base resources and their files.
 *
 * @see ILightweightLabelDecorator
 */
/*
 * http://help.eclipse.org/helios/index.jsp?topic=/org.eclipse.platform.doc.isv/
 * guide/workbench_advext_decorators.htm
 *
 * If your plug-in needs to manipulate the label text in addition to the icon,
 * or if the type of icon is determined dynamically, you can use a
 * non-declarative lightweight decorator. In this case, an implementation class
 * that implements ILightweightLabelDecorator must be defined. The designated
 * class is responsible for supplying a prefix, suffix, and overlay image at
 * runtime which are applied to the label. The mechanics of concatenating the
 * prefix and suffix with the label text and performing the overlay are handled
 * by the workbench code in a background thread. Thus, any work performed by
 * your plug-in in its ILightweightLabelDecorator implementation must be
 * UI-thread safe.
 */
@Component(module = "eclipse")
public final class SharedResourceDecorator implements ILightweightLabelDecorator {

  private static final Logger log = Logger.getLogger(SharedResourceDecorator.class);

  private static final ImageDescriptor SHARED_RESOURCE_DESCRIPTOR =
      ImageManager.getImageDescriptor("icons/ovr16/shared.png"); // NON-NLS-1

  private final List<ILabelProviderListener> listeners =
      new CopyOnWriteArrayList<ILabelProviderListener>();

  @Inject private ISarosSessionManager sessionManager;

  private volatile ISarosSession session;

  private volatile IReferencePoint lastDecoratedReferencePoint;

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
          session.addListener(sessionListener);
          SharedResourceDecorator.this.session = session;
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          session.removeListener(sessionListener);
          SharedResourceDecorator.this.session = null;
          log.debug("clearing decoration for all shared resources");
          updateDecoratorsAsync(null); // update all labels

          lastDecoratedReferencePoint = null;
        }
      };

  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void resourcesAdded(IReferencePoint referencePoint) {
          log.debug("updating decoration for all shared resources");
          updateDecoratorsAsync(null); // update all labels
        }
      };

  public SharedResourceDecorator() {
    SarosPluginContext.initComponent(this);
    session = sessionManager.getSession();
    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  @Override
  public void dispose() {
    sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
  }

  @Override
  public void decorate(Object element, IDecoration decoration) {
    // make a copy as this value might change while decorating
    ISarosSession currentSession = session;

    if (currentSession == null) return;

    IResource resource = (IResource) element;

    saros.filesystem.IResource resourceWrapper = getSarosResource(resource);

    if (resourceWrapper == null || !currentSession.isShared(resourceWrapper)) return;

    decoration.addOverlay(SharedResourceDecorator.SHARED_RESOURCE_DESCRIPTOR, IDecoration.TOP_LEFT);

    if (resourceWrapper.getType() == Type.REFERENCE_POINT) {
      decoration.addSuffix(Messages.SharedBaseResourceDecorator_shared);
    }
  }

  @Override
  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  @Override
  public void addListener(ILabelProviderListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(ILabelProviderListener listener) {
    listeners.remove(listener);
  }

  private void updateDecoratorsAsync(final IResource[] resources) {
    SWTUtils.runSafeSWTAsync(
        log,
        new Runnable() {
          @Override
          public void run() {
            LabelProviderChangedEvent event =
                new LabelProviderChangedEvent(SharedResourceDecorator.this, resources);

            for (ILabelProviderListener listener : listeners) {
              listener.labelProviderChanged(event);
            }
          }
        });
  }

  /**
   * Returns the Saros resource object for the given Eclipse resource object.
   *
   * <p>Tries to use the last used reference point as the first guess of which reference point to
   * use. Updates the last used reference point if the resource belongs to a different reference
   * point.
   *
   * @param resource the Eclipse resource object
   * @return the Saros resource object for the given Eclipse resource object or <code>null</code> if
   *     the resource is not part of a shared reference point
   */
  private saros.filesystem.IResource getSarosResource(IResource resource) {
    IReferencePoint currentLastUsedReferencePoint = lastDecoratedReferencePoint;

    if (currentLastUsedReferencePoint != null) {
      saros.filesystem.IResource resourceWrapper =
          ResourceConverter.convertToResource(currentLastUsedReferencePoint, resource);

      if (resourceWrapper != null) {
        return resourceWrapper;
      }
    }

    Set<IReferencePoint> referencePoints = session.getReferencePoints();
    saros.filesystem.IResource resourceWrapper =
        ResourceConverter.convertToResource(referencePoints, resource);

    if (resourceWrapper != null) {
      lastDecoratedReferencePoint = resourceWrapper.getReferencePoint();
    }

    return resourceWrapper;
  }
}
