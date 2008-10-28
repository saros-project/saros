package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.concurrent.IDriverManager;
import de.fu_berlin.inf.dpp.net.JID;

public class DriverDocument implements IDriverManager {

    private static Logger logger = Logger.getLogger(DriverDocument.class);

    private final IPath editor;

    private final List<JID> currentDriver;

    public DriverDocument(IPath editor) {
	this.editor = editor;
	this.currentDriver = new Vector<JID>();
    }

    public IPath getEditor() {
	return this.editor;
    }

    public void addDriver(JID jid) {
	/* if driver not exists in list. */
	if (!isDriver(jid)) {
	    this.currentDriver.add(jid);
	} else {
	    DriverDocument.logger.debug("Driver " + jid
		    + " is already Driver for "
		    + this.editor.lastSegment().toString());
	}
    }

    public boolean isDriver(JID jid) {
	return this.currentDriver.contains(jid);
    }

    public boolean noDriver() {
	return this.currentDriver.isEmpty();
    }

    public void removeDriver(JID jid) {
	if (isDriver(jid)) {
	    this.currentDriver.remove(jid);
	} else {
	    DriverDocument.logger.warn("JID " + jid
		    + " is not driver for this document "
		    + this.editor.lastSegment().toString());
	}

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.concurrent.IDriverManager#exclusiveDriver()
     */
    public boolean exclusiveDriver() {
	if (this.currentDriver.size() > 1) {
	    return false;
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.concurrent.IDriverManager#getActiveDriver()
     */
    public List<JID> getActiveDriver() {
	return this.currentDriver;
    }
}
