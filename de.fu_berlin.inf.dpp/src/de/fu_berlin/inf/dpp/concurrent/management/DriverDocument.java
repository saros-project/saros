package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.net.JID;

public class DriverDocument {

    private static Logger logger = Logger.getLogger(DriverDocument.class);

    private final IPath editor;

    private final List<JID> currentDrivers;

    public DriverDocument(IPath editor) {
        this.editor = editor;
        this.currentDrivers = new Vector<JID>();
    }

    public IPath getEditor() {
        return this.editor;
    }

    public void addDriver(JID jid) {
        /* if driver not exists in list. */
        if (!isDriver(jid)) {
            this.currentDrivers.add(jid);
        } else {
            DriverDocument.logger.debug("Driver " + jid
                + " is already Driver for "
                + this.editor.lastSegment().toString());
        }
    }

    public boolean isDriver(JID jid) {
        return this.currentDrivers.contains(jid);
    }

    public boolean noDriver() {
        return this.currentDrivers.isEmpty();
    }

    public void removeDriver(JID jid) {
        if (isDriver(jid)) {
            this.currentDrivers.remove(jid);
        } else {
            DriverDocument.logger.warn("JID " + jid
                + " is not driver for this document "
                + this.editor.lastSegment().toString());
        }

    }

    public boolean isExclusiveDriver() {
        return currentDrivers.size() == 1;
    }

    public List<JID> getDrivers() {
        return this.currentDrivers;
    }
}
