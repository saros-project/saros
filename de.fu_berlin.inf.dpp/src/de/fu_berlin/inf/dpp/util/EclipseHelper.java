package de.fu_berlin.inf.dpp.util;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * @author cordes
 */
public class EclipseHelper {

    public IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }
}
