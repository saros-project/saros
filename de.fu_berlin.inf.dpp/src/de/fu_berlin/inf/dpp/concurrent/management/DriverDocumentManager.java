package de.fu_berlin.inf.dpp.concurrent.management;

/**
 * this manager class handles driver event and the appropriate documents.
 * Additional this class is managing exclusive lock for temporary single driver
 * actions.
 */
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.concurrent.IDriverDocumentManager;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;

/**
 * 
 * @author orieger
 * 
 */
public class DriverDocumentManager implements IDriverDocumentManager,
    ISessionListener {

    private static Logger logger = Logger
        .getLogger(DriverDocumentManager.class);

    /** All drivers. Only used on host side. */
    private final List<JID> drivers;

    /* list of documents with appropriate drivers. */
    private final HashMap<IPath, DriverDocument> documents;

    private static DriverDocumentManager manager;

    /**
     * private constructor for singleton pattern.
     */
    private DriverDocumentManager() {
        this.drivers = new Vector<JID>();
        this.documents = new HashMap<IPath, DriverDocument>();
        Saros.getDefault().getSessionManager().addSessionListener(this);
    }

    /**
     * get instance of this singleton object
     * 
     * @return instance of DriverDocumentManager
     */
    public static DriverDocumentManager getInstance() {
        /* at first time, create new manager. */
        if (DriverDocumentManager.manager == null) {
            DriverDocumentManager.manager = new DriverDocumentManager();
        }
        return DriverDocumentManager.manager;
    }

    /**
     * @param jid
     *            JID of the driver
     * @return true if driver exists in active driver list, false otherwise
     */
    public boolean isDriver(JID jid) {
        if (jid != null) {
            return this.drivers.contains(jid);
        } else {
            logger.warn("jid null");
            return false;
        }
    }

    public List<JID> getDriversForDocument(IPath path) {
        if (this.documents.containsKey(path)) {
            return this.documents.get(path).getDrivers();
        }
        return new Vector<JID>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.concurrent.IDriverManager#getActiveDriver()
     */
    public List<JID> getDrivers() {
        return this.drivers;
    }

    public void addDriver(JID jid) {

        assert jid != null;

        if (!this.drivers.contains(jid)) {
            DriverDocumentManager.logger.debug("Add driver [" + jid.getBase()
                + "]");
            this.drivers.add(jid);
        } else {
            DriverDocumentManager.logger.warn("User [" + jid.getBase()
                + "] is already driver!");
        }
    }

    public void addDriverToDocument(IPath path, JID jid) {
        addDriver(jid);

        DriverDocumentManager.logger.debug("add activer driver " + jid
            + " to document " + path.lastSegment().toString());
        /* add driver to new document. */
        DriverDocument doc = this.documents.get(path);
        if ((doc == null) || !this.documents.containsKey(path)) {
            DriverDocumentManager.logger.debug("New document creates for "
                + path.lastSegment().toString());
            /* create new instance of this documents. */
            doc = new DriverDocument(path);
            this.documents.put(path, doc);
        }
        if (!doc.isDriver(jid)) {
            doc.addDriver(jid);
        }
    }

    public void removeDriver(JID jid) {
        DriverDocumentManager.logger.debug("remove driver " + jid);

        /* remove driver from all documents */
        for (IPath path : this.documents.keySet()) {
            removeDriverFromDocument(path, jid);
        }

        this.drivers.remove(jid);

    }

    /**
     * remove driver from document and delete empty documents.
     * 
     * @param path
     *            of the document
     * @param jid
     *            removable driver
     */
    private void removeDriverFromDocument(IPath path, JID jid) {
        DriverDocument doc = this.documents.get(path);
        if (doc.isDriver(jid)) {
            doc.removeDriver(jid);
        }
        /* check for other driver or delete if no other driver exists. */
        if (doc.noDriver()) {
            DriverDocumentManager.logger.debug("no driver exists for document "
                + path.lastSegment().toString()
                + ". Document delete from driver manager.");
            /* delete driver document. */
            this.documents.remove(doc.getEditor());
        }
    }

    /**
     * new driver activity received and has to be managed.
     * 
     * @param activity
     */
    public void receiveActivity(IActivity activity) {
        JID jid = new JID(activity.getSource());

        /* if user is an active driver */
        if (isDriver(jid)) {

            /* editor activities. */
            if (activity instanceof EditorActivity) {
                EditorActivity edit = (EditorActivity) activity;

                DriverDocumentManager.logger.debug("receive activity from "
                    + jid + ": " + edit);

                /* editor has activated. */
                if (edit.getType() == EditorActivity.Type.Activated) {
                    /* add driver to new document. */
                    addDriverToDocument(edit.getPath(), jid);
                }
                /* editor has closed. */
                if (edit.getType() == EditorActivity.Type.Closed) {
                    /* remove driver */
                    if (this.documents.containsKey(edit.getPath())) {

                        removeDriverFromDocument(edit.getPath(), jid);

                    } else {
                        DriverDocumentManager.logger
                            .warn("No driver document exists for "
                                + edit.getPath());
                    }
                }
            }
            if (activity instanceof FileActivity) {
                FileActivity file = (FileActivity) activity;
                /* if file has been removed, delete appropriate driver document. */
                if (file.getType() == FileActivity.Type.Removed) {
                    this.documents.remove(file.getPath());
                }
            }
        } else {
            DriverDocumentManager.logger.debug("JID " + jid
                + " isn't an active driver.");
        }
    }

    public void roleChanged(JID user, boolean replicated) {

        // TODO DriverDocumentManager needs the host to be a driver
        if (Saros.getDefault().getSessionManager().getSharedProject().getHost()
            .equals(user)) {
            return;
        }

        if (isDriver(user)) {
            removeDriver(user);
        } else {
            addDriver(user);
        }

    }

    public void userJoined(JID user) {
        // nothing to do

    }

    public void userLeft(JID user) {
        if (isDriver(user)) {
            /* remove driver status. */
            removeDriver(user);
        }
    }

    public void invitationReceived(IIncomingInvitationProcess invitation) {
        // ignore
    }

    public void sessionEnded(ISharedProject session) {
        drivers.clear();
    }

    public void sessionStarted(ISharedProject session) {

        // when this is called the host is already active Driver, remove all
        // other
        if (drivers.size() > 1) {
            for (JID participant : drivers) {
                if (!participant.equals(session.getHost().getJID())) {
                    drivers.remove(participant);
                }
            }
        }
    }

}
