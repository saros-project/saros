package saros.ui.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.filesystem.ResourceAdapterFactory;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

/**
 * Adds tests to the {@link IResource}. Currently tests whether given {@link IResource} is part of
 * the {@link ISarosSession}.
 */
public class ProjectPropertyTester extends PropertyTester {

  @Inject private ISarosSessionManager sessionManager;

  public ProjectPropertyTester() {
    SarosPluginContext.initComponent(this);
  }

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

    if (!(receiver instanceof IResource)) return false;

    final ISarosSession session = sessionManager.getSession();

    if (session == null) return false;

    final IResource resource = (IResource) receiver;

    if ("isInSarosSession".equals(property)) {

      if (resource.getType() == IResource.PROJECT) {
        return session.isCompletelyShared(ResourceAdapterFactory.create((IProject) resource));
      }

      return session.isShared(ResourceAdapterFactory.create(resource));
    }

    return false;
  }
}
