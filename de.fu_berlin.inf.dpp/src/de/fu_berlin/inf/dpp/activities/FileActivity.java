package de.fu_berlin.inf.dpp.activities;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.net.JID;

public class FileActivity implements IActivity {
    public static enum Type {
	Created, Removed, Error
    };

    private String source;

    private final Type type;

    private final IPath path;

    /* exclusive file recipient for error file. */
    private JID errorRecipient;

    private InputStream inputStream;

    public FileActivity(Type type, IPath path) {
	this.type = type;
	this.path = path;
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

    public FileActivity(Type type, IPath path, InputStream in) {
	this(type, path);
	this.inputStream = in;
    }

    public IPath getPath() {
	return this.path;
    }

    public Type getType() {
	return this.type;
    }

    public JID getRecipient() {
	return this.errorRecipient;
    }

    /**
     * @return the inputStream for the contents of this file for incoming file
     *         creation activities. <code>null</code> otherwise.
     */
    public InputStream getContents() {
	return this.inputStream;
    }

    @Override
    public String toString() {
	return "FileActivity(type:" + this.type + ", path:" + this.path + ")";
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

    public String getSource() {
	return this.source;
    }

    public void setSource(String source) {
	this.source = source;
    }
}
