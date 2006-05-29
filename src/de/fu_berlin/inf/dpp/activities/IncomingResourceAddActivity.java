package de.fu_berlin.inf.dpp.activities;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;

public class IncomingResourceAddActivity implements IActivity {
    private final IPath path;
    private final InputStream in;

    public IncomingResourceAddActivity(IPath path, InputStream in) {
        this.path = path;
        this.in = in;
    }

    public InputStream getContents() {
        return in;
    }

    public IPath getPath() {
        return path;
    }
}
