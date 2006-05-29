
package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

public class ResourceAddActivity implements IActivity {
    private IPath       path;

    public ResourceAddActivity(IPath path) {
        this.path = path;
    }
    
    public IPath getPath() {
        return path;
    }
}
