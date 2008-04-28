package de.fu_berlin.inf.dpp.activities;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;

public class FileActivity implements IActivity {
	public static enum Type {
		Created, Removed
	};

	private String source;
	
	private Type type;

	private IPath path;

	private InputStream inputStream;

	public FileActivity(Type type, IPath path) {
		this.type = type;
		this.path = path;
	}

	public FileActivity(Type type, IPath path, InputStream in) {
		this(type, path);
		inputStream = in;
	}

	public IPath getPath() {
		return path;
	}

	public Type getType() {
		return type;
	}

	/**
	 * @return the inputStream for the contents of this file for incoming file
	 *         creation activities. <code>null</code> otherwise.
	 */
	public InputStream getContents() {
		return inputStream;
	}

	@Override
	public String toString() {
		return "FileActivity(type:" + type + ", path:" + path + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FileActivity) {
			FileActivity activity = (FileActivity) obj;

			return (getPath().equals(activity.getPath()) && getType().equals(activity.getType()));
		}

		return false;
	}

	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
