package de.fu_berlin.inf.dpp.propertyTesters;

import java.util.Set;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * Adds tests to the {@link IProject}. <br/>
 * Currently only tests whether given {@link IProject} is part of the
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
        if (receiver instanceof IProject) {
            IProject project = (IProject) receiver;
            if ("isInSarosSession".equals(property)) {
                ISarosSession sarosSession = sarosSessionManager
                    .getSarosSession();
                if (sarosSession != null) {
                    Set<IProject> projects = sarosSession.getProjects();
                    return projects.contains(project);
                }
            }
        }
        return false;
    }

}
