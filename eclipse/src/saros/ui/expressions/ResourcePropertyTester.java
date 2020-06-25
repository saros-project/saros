package saros.ui.expressions;

import java.util.Set;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import saros.SarosPluginContext;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ResourceConverter;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

/**
 * Adds tests to the {@link IResource}. Currently tests whether given {@link IResource} is part of
 * the {@link ISarosSession}.
 */
public class ResourcePropertyTester extends PropertyTester {

  @Inject private ISarosSessionManager sessionManager;

  public ResourcePropertyTester() {
    SarosPluginContext.initComponent(this);
  }

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

    if (!(receiver instanceof IResource)) return false;

    final ISarosSession session = sessionManager.getSession();

    if (session == null) return false;

    final IResource resource = (IResource) receiver;

    if ("isInSarosSession".equals(property)) {

      Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();
      saros.filesystem.IResource wrappedResource =
          ResourceConverter.convertToResource(sharedReferencePoints, resource);

      return session.isShared(wrappedResource);
    }

    return false;
  }
}
