package de.fu_berlin.inf.dpp.concurrent.management;

import org.eclipse.core.runtime.IPath;

/**
 * This Class represents a checksum of an document. It contains the path, the
 * length and the hash code of the document.
 * 
 * @author chjacob
 */
public class DocumentChecksum {

    // the path to the concurrent document
    private IPath path;

    // the length of the document
    private int length;

    // the hash code of the document
    private int hash;

    public DocumentChecksum(IPath path, int length, int hash) {
	this.path = path;
	this.length = length;
	this.hash = hash;
    }

    public IPath getPath() {
	return path;
    }

    public void setPath(IPath path) {
	this.path = path;
    }

    public int getLength() {
	return length;
    }

    public void setLength(int length) {
	this.length = length;
    }

    public int getHash() {
	return hash;
    }

    public void setHash(int hash) {
	this.hash = hash;
    }
}