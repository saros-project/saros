package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

public class ResourceRemoveActivity implements IActivity {
    private IPath path;
    
    public ResourceRemoveActivity(IPath path) {
        this.path = path;
    }
    
    public IPath getPath() {
        return path;
    }
}
