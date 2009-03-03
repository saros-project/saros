package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

public class FolderActivity extends AbstractActivity {
    public static enum Type {
        Created, Removed
    }

    private final Type type;

    private final IPath path;

    public FolderActivity(Type type, IPath path) {
        this.type = type;
        this.path = path;
    }

    public IPath getPath() {
        return this.path;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "FolderActivity(type:" + this.type + ",path:" + this.path + ")";
    }

    public boolean dispatch(IActivityReceiver receiver) {
        return receiver.receive(this);
    }
}
