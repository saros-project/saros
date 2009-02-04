/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.project.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.concurrent.IConcurrentManager;
import de.fu_berlin.inf.dpp.concurrent.IDriverDocumentManager;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.concurrent.management.DriverDocumentManager;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.invitation.internal.OutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.net.IActivitySequencer;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.project.IActivityManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.InvitationDialog;

public class SharedProject implements ISharedProject {
    private static Logger log = Logger.getLogger(SharedProject.class.getName());

    private static final int REQUEST_ACTIVITY_ON_AGE = 5;
    protected static final int MILLIS_UPDATE = 1000;

    protected JID myID;

    protected ConcurrentHashMap<JID, User> participants = new ConcurrentHashMap<JID, User>();

    private final IProject project;

    private final List<ISharedProjectListener> listeners = new ArrayList<ISharedProjectListener>();

    private User host;

    private final ITransmitter transmitter;

    private IDriverDocumentManager driverManager;

    private final ActivitySequencer activitySequencer = new ActivitySequencer();

    private static final int MAX_USERCOLORS = 5;
    private FreeColors freeColors = null;

    /**
     * Constructor called for SharedProject of the host
     */
    public SharedProject(ITransmitter transmitter, IProject project, JID myID) {
        assert (transmitter != null && myID != null);

        this.transmitter = transmitter;

        this.freeColors = new FreeColors(MAX_USERCOLORS - 1);

        this.myID = myID;
        User user = new User(myID, 0);
        user.setUserRole(UserRole.DRIVER);
        this.host = user;

        this.participants.put(this.host.getJID(), this.host);

        /* add host to driver list. */
        this.activitySequencer.initConcurrentManager(
                IConcurrentManager.Side.HOST_SIDE, this.host, myID, this);

        /* init driver manager */
        this.driverManager = DriverDocumentManager.getInstance();
        addListener(this.driverManager);
        this.driverManager.addDriver(this.host.getJID());

        // activitySequencer.getConcurrentManager().addDriver(host.getJid());

        this.project = project;
        setProjectReadonly(false);
    }

    public SharedProject(ITransmitter transmitter, IProject project, JID myID, // guest
            JID hostID, int myColorID) {

        this.transmitter = transmitter;

        this.myID = myID;

        User host = new User(hostID, 0);
        host.setUserRole(UserRole.DRIVER);
        this.participants.put(hostID, host);
        this.participants.put(myID, new User(myID, myColorID));

        this.host = getParticipant(hostID);

        this.activitySequencer.initConcurrentManager(
                IConcurrentManager.Side.CLIENT_SIDE, this.host, myID, this);

        this.project = project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public Collection<User> getParticipants() {
        return this.participants.values();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public IActivitySequencer getSequencer() {
        return this.activitySequencer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public IActivityManager getActivityManager() {
        return this.activitySequencer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public void toggleDriverRole(User driver, boolean replicated) {

        assert driver != null;

        User myself = getParticipant(this.myID);

        /* if user has current role observer */
        if (driver.getUserRole() == UserRole.OBSERVER) {

            /* set new driver status in participant list of sharedProject. */
            driver.setUserRole(UserRole.DRIVER);

            /* set local file settings. */
            if (driver.equals(myself)) {
                setProjectReadonly(!isDriver());
            }

        } else {

            /* changed state form driver to observer */
            if (driver.getUserRole() == UserRole.DRIVER) {

                /* set the local driver state to observer */
                if (driver.equals(myself) && isDriver(myself)) {
                    setProjectReadonly(true);
                }

                /* set observer state. */
                driver.setUserRole(UserRole.OBSERVER);
            }
        }

        /* inform observer. */
        JID jid = driver.getJID();
        for (ISharedProjectListener listener : this.listeners) {
            listener.driverChanged(jid, replicated);
        }
    }

    public void removeDriver(User driver, boolean replicated) {
        /* set new observer status in participant list of sharedProject. */
        getParticipant(driver.getJID()).setUserRole(UserRole.OBSERVER);

        /**
         * communicate driver role change to listener.
         */
        JID jid = driver.getJID();
        for (ISharedProjectListener listener : this.listeners) {
            listener.driverChanged(jid, replicated);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public boolean isDriver() {
        // TODO: change driver status request to userrole request of
        // participient list.

        // HOST
        /* TODO: change to driver document manager. */
        // if (activitySequencer.getConcurrentManager() != null
        // && activitySequencer.getConcurrentManager().isHostSide()) {
        // return
        // activitySequencer.getConcurrentManager().isDriver(driver.getJid());
        // }
        // CLIENT
        return (getParticipant(this.myID).getUserRole() == UserRole.DRIVER);
        // return driver.getJid().equals(myID);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu_berlin.inf.dpp.project.ISharedProject#isDriver(de.fu_berlin.inf
     * .dpp.User)
     */
    public boolean isDriver(User user) {
        // HOST
        if (this.driverManager != null) {
            return this.driverManager.isDriver(user.getJID());
        }
        // CLIENT
        if (getParticipant(user.getJID()).getUserRole() == UserRole.DRIVER) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public User getHost() {
        return this.host;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public boolean isHost() {
        return this.host.getJID().equals(this.myID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject#exclusiveDriver()
     */
    public boolean exclusiveDriver() {
        return this.driverManager.exclusiveDriver();
    }

    public void addUser(User user) {

        if (this.participants.containsKey(user.getJID())) {
            log.warn("User " + user.getJID() + " added twice to SharedProject");
        }
        participants.putIfAbsent(user.getJID(), user);
        for (ISharedProjectListener listener : this.listeners) {
            listener.userJoined(user.getJID());
        }
        SharedProject.log.info("User " + user.getJID() + " joined session");
    }

    public void removeUser(User user) {
        if (this.participants.remove(user.getJID()) == null) {
            log.warn("Tried to remove user who was not in participants: "
                    + user.getJID());
            return;
        }
        if (isHost()) {
            returnColor(user.getColorID());
        }

        // TODO what is to do here if no driver exists anymore?

        for (ISharedProjectListener listener : this.listeners) {
            listener.userLeft(user.getJID());
        }

        SharedProject.log.info("User " + user.getJID() + " left session");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public IOutgoingInvitationProcess invite(JID jid, String description,
            boolean inactive, IInvitationUI inviteUI) {
        return new OutgoingInvitationProcess(this.transmitter, jid, this,
                description, inactive, inviteUI, getFreeColor());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void addListener(ISharedProjectListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void removeListener(ISharedProjectListener listener) {
        this.listeners.remove(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public IProject getProject() {
        return this.project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public FileList getFileList() throws CoreException {
        return new FileList(this.project);
    }

    public Timer flushTimer = new Timer(true);
    public Thread requestTransmitter = null;
    private static int queuedsince = 0;

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void start() {

        this.flushTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                
                if (SharedProject.this.participants.size() <= 1) {
                    SharedProject.this.activitySequencer.flush();
                } else {
                    List<TimedActivity> activities = SharedProject.this.activitySequencer
                            .flushTimed();

                    if (activities != null) {
                        SharedProject.this.transmitter.sendActivities(
                                SharedProject.this, activities);
                    }
                }

                // missing activities? (cant execute all)
                if (SharedProject.this.activitySequencer.getQueuedActivities() > 0) {
                    SharedProject.queuedsince++;

                    // if i am missing activities for REQUEST_ACTIVITY_ON_AGE
                    // seconds, ask all (because I dont know the origin)
                    // to send it to me again.
                    if (SharedProject.queuedsince >= SharedProject.REQUEST_ACTIVITY_ON_AGE) {

                        SharedProject.this.transmitter.sendRequestForActivity(
                                SharedProject.this,
                                SharedProject.this.activitySequencer
                                        .getTimestamp(), false);

                        SharedProject.queuedsince = 0;

                        // TODO: forever?
                    }

                } else {
                    SharedProject.queuedsince = 0;
                }
            }
        }, 0, SharedProject.MILLIS_UPDATE);

        stopped = false;
        
        /* 2. start thread for sending jupiter requests. */
        this.requestTransmitter = new Thread(new Runnable() {
            public void run() {
                while (!stopped && !Thread.interrupted()) {
                    sendRequest();
                }
            }
        });
        this.requestTransmitter.start();
    }

    // TODO Review sendRequest for InterruptedException and remove this flag.
    boolean stopped;
    
    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void stop() {
        this.flushTimer.cancel();
        this.requestTransmitter.interrupt();
        stopped = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public User getParticipant(JID jid) {
        return this.participants.get(jid);
    }

    public void startInvitation(final JID jid) {

        Shell shell = Display.getDefault().getActiveShell();

        if (searchUnsavedChangesInProject(false)) {
            if (MessageDialog
                    .openQuestion(
                            shell,
                            "Unsaved file modifications",
                            "Before inviting users and therefore synchronizing files, "
                                    + "this project needs to be saved to disk. "
                                    + "Do you want to save all unsaved files of this project now?")) {

                // save
                // PlatformUI.getWorkbench().saveAllEditors(false); // saves all
                // editors
                searchUnsavedChangesInProject(true);

            } else {
                return;
            }
        }

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                try {
                    Shell shell = Display.getDefault().getActiveShell();
                    Window iw = new InvitationDialog(shell, jid);
                    iw.open();
                } catch (Exception e) {
                    Saros
                            .getDefault()
                            .getLog()
                            .log(
                                    new Status(
                                            IStatus.ERROR,
                                            Saros.SAROS,
                                            IStatus.ERROR,
                                            "Error while running invitation helper",
                                            e));
                }
            }
        });

    }

    boolean searchUnsavedChangesInProject(boolean save) {
        FileList flist = null;

        try {
            flist = new FileList(getProject());
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        try {
            IWorkbenchWindow[] wbWindows = PlatformUI.getWorkbench()
                    .getWorkbenchWindows();
            for (IWorkbenchWindow window : wbWindows) {
                IWorkbenchPage activePage = window.getActivePage();
                IEditorReference[] editorRefs = activePage
                        .getEditorReferences();
                for (IEditorReference editorRef : editorRefs) {
                    if (editorRef.isDirty()
                            && (editorRef.getEditorInput() instanceof IFileEditorInput)) {

                        IPath fp = ((IFileEditorInput) editorRef
                                .getEditorInput()).getFile()
                                .getProjectRelativePath();

                        // is that dirty file in my project?
                        if (flist.getPaths().contains(fp)) {
                            if (save) {
                                editorRef.getEditor(false).doSave(null);
                            } else {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (CoreException e1) {
            System.out.println(e1.getMessage());
        }

        return false;
    }

    public void setProjectReadonly(final boolean readonly) {

        /* run project read only settings in progress monitor thread. */
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                        Display.getDefault().getActiveShell());
                try {
                    dialog.run(true, false, new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor) {
                            try {
                                Collection<IPath> paths = new FileList(
                                        SharedProject.this.project).getPaths();

                                monitor.beginTask("Project settings ... ",
                                        paths.size());

                                ResourceAttributes attributes = new ResourceAttributes();
                                attributes.setReadOnly(readonly);
                                attributes.setArchive(readonly);

                                for (IPath path : paths) {
                                    IFile file = getProject().getFile(
                                            path.makeAbsolute());
                                    if ((file != null) && file.exists()) {
                                        file.setResourceAttributes(attributes);
                                    }
                                    monitor.worked(1);
                                }
                            } catch (CoreException e) {
                                SharedProject.log.warn("", e);
                            } finally {
                                monitor.done();
                            }
                        }
                    });
                } catch (InvocationTargetException e) {
                    SharedProject.log.warn("", e);
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    SharedProject.log.warn("", e);
                    e.printStackTrace();
                }
            }
        });

    }

    public void sendRequest() {

        Request request;
        try {
            request = this.activitySequencer.getNextOutgoingRequest();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (isHost()) {

            /*
             * if jupiter server request to has to execute locally on host side.
             */
            if (request.getJID().equals(this.host.getJID())) {
                SharedProject.log
                        .debug("Send host request back for local execution: "
                                + request);
                this.activitySequencer.receiveRequest(request);
            } else {
                /* send operation to client. */
                SharedProject.log.debug("Send request to client: " + request
                        + request.getJID());
                this.transmitter.sendJupiterRequest(this, request, request
                        .getJID());
            }
        } else {
            SharedProject.log.debug("Send request to host : " + request);
            this.transmitter.sendJupiterRequest(this, request, this.host
                    .getJID());
        }
        // connection.sendOperation(new
        // NetworkRequest(this.jid,request.getJID(),request), 0);

    }

    public ConcurrentDocumentManager getConcurrentDocumentManager() {
        return (ConcurrentDocumentManager) this.activitySequencer
                .getConcurrentManager();

    }

    public int getFreeColor() {
        return freeColors.get();
    }

    public void returnColor(int colorID) {
        freeColors.add(colorID);
    }
}
