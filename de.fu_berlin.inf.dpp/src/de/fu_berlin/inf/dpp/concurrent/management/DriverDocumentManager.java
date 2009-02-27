package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

/**
 * This manager class handles driver event and the appropriate documents.
 * Additional this class is managing exclusive lock for temporary single driver
 * actions.
 * 
 * @host Only the host needs to manage JupiterDocuments in detail
 */
public class DriverDocumentManager {

    private static Logger logger = Logger
        .getLogger(DriverDocumentManager.class);

    /**
     * List of all drivers
     */
    private final List<JID> drivers = new Vector<JID>();

    /**
     * The drivers for each document
     */
    private final HashMap<IPath, DriverDocument> documents = new HashMap<IPath, DriverDocument>();

    private final ISharedProject project;

    public DriverDocumentManager(ISharedProject project) {
        this.project = project;
        Saros.getDefault().getSessionManager().addSessionListener(
            sessionListener);
        project.addListener(projectListener);
    }

    ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionEnded(ISharedProject session) {
            if (session == project) {
                project.removeListener(projectListener);
                Saros.getDefault().getSessionManager().removeSessionListener(
                    sessionListener);
            }
        }
    };

    ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void userLeft(JID user) {
            if (isDriver(user)) {
                /* remove driver status. */
                removeDriver(user);
            }
        }

        @Override
        public void roleChanged(User user, boolean replicated) {

            // TODO DriverDocumentManager needs the host to be a driver
            if (Saros.getDefault().getSessionManager().getSharedProject()
                .getHost().equals(user)) {

                assert assertRoles();
                return;
            }
            JID jid = user.getJID();
            if (isDriver(jid)) {
                removeDriver(jid);
            } else {
                addDriver(jid);
            }

            assert assertRoles();
        }
    };

    /**
     * @param jid
     *            JID of the driver
     * @return true if driver exists in active driver list, false otherwise
     */
    public boolean isDriver(JID jid) {

        assert jid != null;

        return this.drivers.contains(jid);
    }

    public List<JID> getDriversForDocument(IPath path) {
        if (this.documents.containsKey(path)) {
            return this.documents.get(path).getDrivers();
        }
        return new Vector<JID>();
    }

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
            + " to document " + path.lastSegment());
        /* add driver to new document. */
        DriverDocument doc = this.documents.get(path);
        if ((doc == null) || !this.documents.containsKey(path)) {
            DriverDocumentManager.logger.debug("New document creates for "
                + path.lastSegment());
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

        // TODO ConcurrentModificationException when called from setUserRole...
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
            DriverDocumentManager.logger
                .debug("no driver exists for document " + path.lastSegment()
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

    /**
     * @return true if the role information managed by this
     *         DriverDocumentManager is consistent with the information managed
     *         by the SharedProject.
     * 
     *         Caution: The host is always a driver for us
     */
    public boolean assertRoles() {

        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        if (project == null) {

            if (!drivers.isEmpty()) {
                logger.error("Project is null, but drivers is not empty");
                return false;
            } else {
                return true;
            }
        }

        for (JID driver : drivers) {

            if (project.getParticipant(driver) == null) {
                logger.error("Driver [" + driver.getBase()
                    + "] is not a participant in project");
                return false;
            }

            // If not host
            if (!project.getParticipant(driver).equals(project.getHost())) {
                // If not driver
                if (!project.getParticipant(driver).isDriver()) {
                    logger
                        .error("Host ["
                            + driver.getBase()
                            + "] is not a driver in SharedProject but in DriverDocumentManager");
                    return false;
                }
            }
        }

        for (User participant : project.getParticipants()) {

            if (participant.isDriver()
                && !drivers.contains(participant.getJID())) {
                logger
                    .error("User ["
                        + participant.getJID().getBase()
                        + "] is driver in SharedProject but not in DriverDocumentManager");
                return false;
            }

            if (participant.equals(project.getHost())) {
                // The host is always a driver
                if (!drivers.contains(participant.getJID())) {
                    logger.error("Host [" + participant.getJID().getBase()
                        + "] is not a driver in DriverDocumentManager");
                    return false;
                }
            }
        }

        return true;
    }

}
