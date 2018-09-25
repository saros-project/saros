package de.fu_berlin.inf.dpp.ui.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;

/**
 * Adds tests to the {@link IResource}. Currently tests whether given
 * {@link IResource} is part of the {@link ISarosSession}.
 */
public class ProjectPropertyTester extends PropertyTester {

    @Inject
    private ISarosSessionManager sessionManager;

    public ProjectPropertyTester() {
        SarosPluginContext.initComponent(this);
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args,
        Object expectedValue) {

        if (!(receiver instanceof IResource))
            return false;

        final ISarosSession session = sessionManager.getSession();

        if (session == null)
            return false;

        final IResource resource = (IResource) receiver;

        if ("isInSarosSession".equals(property)) {

            if (resource.getType() == IResource.PROJECT) {
                return session.isCompletelyShared(ResourceAdapterFactory
                    .create((IProject) resource));
            }

            return session.isShared(ResourceAdapterFactory.create(resource));
        }

        return false;
    }
}
