package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

public class FolderActivity implements IActivity {
    public static enum Type {
	Created, Removed
    };

    private final IPath path;

    private String source;

    private final Type type;

    public FolderActivity(Type type, IPath path) {
	this.type = type;
	this.path = path;
    }

    public IPath getPath() {
	return this.path;
    }

    public String getSource() {
	return this.source;
    }

    public Type getType() {
	return this.type;
    }

    public void setSource(String source) {
	this.source = source;

    }

    @Override
    public String toString() {
	return "FolderActivity(type:" + this.type + ", path:" + this.path + ")";
    }
}
