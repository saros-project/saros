package de.fu_berlin.inf.dpp.util;

import de.fu_berlin.inf.dpp.Saros;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.picocontainer.annotations.Inject;

/**
 * @author cordes
 */
public class EclipseHelper {

    @Inject
    protected Saros saros;

    public IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    public IPath getStateLocation() {
        return saros.getStateLocation();
    }
}
