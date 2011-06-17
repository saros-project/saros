package de.fu_berlin.inf.dpp.propertyTesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * Adds tests to the {@link IResource}. <br/>
 * Currently tests whether given {@link IResource} is part of the
 * {@link ISarosSession}.
 */
public class ProjectPropertyTester extends PropertyTester {

    @Inject
    SarosSessionManager sarosSessionManager;

    public ProjectPropertyTester() {
        SarosPluginContext.initComponent(this);
    }

    public boolean test(Object receiver, String property, Object[] args,
        Object expectedValue) {
        if (receiver instanceof IResource) {
            IResource resource = (IResource) receiver;
            if ("isInSarosSession".equals(property)) {
                ISarosSession sarosSession = sarosSessionManager
                    .getSarosSession();
                if (sarosSession != null) {
                    if (resource instanceof IProject)
                        return sarosSession
                            .isCompletelyShared((IProject) resource);
                    return sarosSession.isShared(resource);
                }
            }
        }
        return false;
    }

}
