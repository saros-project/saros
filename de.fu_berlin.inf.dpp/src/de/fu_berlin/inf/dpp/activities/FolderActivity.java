package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

public class FolderActivity implements IActivity {
	public static enum Type {
		Created, Removed
	};

	private Type type;

	private IPath path;

	public FolderActivity(Type type, IPath path) {
		this.type = type;
		this.path = path;
	}

	public IPath getPath() {
		return path;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "FolderActivity(type:" + type + ", path:" + path + ")";
	}
}
