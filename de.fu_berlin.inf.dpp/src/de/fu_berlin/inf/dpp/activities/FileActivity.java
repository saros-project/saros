package de.fu_berlin.inf.dpp.activities;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.net.JID;

public class FileActivity implements IActivity {
    public static enum Type {
	Created, Error, Removed
    };

    /* exclusive file recipient for error file. */
    private JID errorRecipient;

    private InputStream inputStream;

    private final IPath path;

    private String source;

    private final Type type;

    public FileActivity(Type type, IPath path) {
	this.type = type;
	this.path = path;
    }

    public FileActivity(Type type, IPath path, InputStream in) {
	this(type, path);
	this.inputStream = in;
    }

    /**
     * construtor to send file activity to an exclusive recipient.
     * 
     * @param type
     * @param path
     * @param to
     */
    public FileActivity(Type type, IPath path, JID to) {
	this(type, path);
	this.errorRecipient = to;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof FileActivity) {
	    FileActivity activity = (FileActivity) obj;

	    return (getPath().equals(activity.getPath()) && getType().equals(
		    activity.getType()));
	}

	return false;
    }

    /**
     * @return the inputStream for the contents of this file for incoming file
     *         creation activities. <code>null</code> otherwise.
     */
    public InputStream getContents() {
	return this.inputStream;
    }

    public IPath getPath() {
	return this.path;
    }

    public JID getRecipient() {
	return this.errorRecipient;
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
	return "FileActivity(type:" + this.type + ", path:" + this.path + ")";
    }
}
