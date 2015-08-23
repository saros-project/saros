/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.NOPActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.communication.extensions.ActivitiesExtension;
import de.fu_berlin.inf.dpp.communication.extensions.KickUserExtension;
import de.fu_berlin.inf.dpp.communication.extensions.LeaveSessionExtension;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.misc.xstream.SPathConverter;
import de.fu_berlin.inf.dpp.misc.xstream.UserConverter;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.preferences.Preferences;
import de.fu_berlin.inf.dpp.project.SharedResourcesManager;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityHandlerCallback;
import de.fu_berlin.inf.dpp.session.IActivityListener;
import de.fu_berlin.inf.dpp.session.IActivityProducer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionContextFactory;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.session.internal.ActivityHandler;
import de.fu_berlin.inf.dpp.session.internal.ActivityQueuer;
import de.fu_berlin.inf.dpp.session.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.session.internal.PermissionManager;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * TODO Review if SarosSession, ConcurrentDocumentManager, ActivitySequencer all
 * honor start() and stop() semantics.
 */
public final class SarosSession implements ISarosSession {

    private static final Logger log = Logger.getLogger(SarosSession.class);

    @Inject
    private UISynchronizer synchronizer;

    /* Dependencies */

    @Inject
    private ITransmitter transmitter;

    @Inject
    private XMPPConnectionService connectionService;

    @Inject
    private Preferences preferences;

    @Inject
    private IConnectionManager connectionManager;

    private final IPathFactory pathFactory;

    private final ISarosContext sarosContext;

    private final ConcurrentDocumentClient concurrentDocumentClient;

    private final ConcurrentDocumentServer concurrentDocumentServer;

    private final ActivityHandler activityHandler;

    private final CopyOnWriteArrayList<IActivityProducer> activityProducers = new CopyOnWriteArrayList<IActivityProducer>();

    private final CopyOnWriteArrayList<IActivityConsumer> activityConsumers = new CopyOnWriteArrayList<IActivityConsumer>();

    /* Instance fields */
    private final User localUser;

    private final ConcurrentHashMap<JID, User> participants = new ConcurrentHashMap<JID, User>();

    private final SharedProjectListenerDispatch listenerDispatch = new SharedProjectListenerDispatch();

    private final User hostUser;

    private final SarosProjectMapper projectMapper;

    private boolean useVersionControl = true;

    // KARL HELD YOU ARE MY WTF GUY !!!
    private List<IResource> selectedResources = new ArrayList<IResource>();

    private final MutablePicoContainer sessionContainer;

    private final StopManager stopManager;

    private final ChangeColorManager changeColorManager;

    private final PermissionManager permissionManager;

    private final ActivitySequencer activitySequencer;

    private final UserInformationHandler userListHandler;

    private final String sessionID;

    private boolean started = false;
    private boolean stopped = false;

    private final ActivityQueuer activityQueuer;

    private final Object componentAccessLock = new Object();

    // HACK to be able to move most parts to core
    private final SharedResourcesManager resourceManager;

    private final IActivityListener activityListener = new IActivityListener() {

        /**
         * @JTourBusStop 5, Activity sending, Forwarding the IActivity:
         * 
         *               This is where the SarosSession will receive the
         *               activity. This listener it is not part of the
         *               ISarosSession interface to avoid misuse.
         */
        @Override
        public void created(final IActivity activity) {
            if (activity == null)
                throw new NullPointerException("activity is null");

            activityHandler.handleOutgoingActivities(Collections
                .singletonList(activity));
        }
    };

    private final IActivityHandlerCallback activityCallback = new IActivityHandlerCallback() {

        @Override
        public void send(List<User> recipients, IActivity activity) {
            sendActivity(recipients, activity);
        }

        @Override
        public void execute(IActivity activity) {
            /**
             * @JTourBusStop 10, Activity sending, Local Execution, first
             *               dispatch:
             * 
             *               Afterwards, every registered ActivityConsumer is
             *               informed about the remote activity that should be
             *               executed locally.
             */
            for (IActivityConsumer consumer : activityConsumers) {
                consumer.exec(activity);
                updatePartialSharedResources(activity);
            }
        }
    };

    private SPathConverter pathConverter;
    private UserConverter userConverter;

    // FIXME those parameter passing feels strange, find a better way
    /**
     * Constructor for host.
     */
    public SarosSession(final String id, String nickname, int colorID,
        ISarosContext sarosContext) {

        this(id, sarosContext, /* unused */null, colorID, /* unused */
        -1, nickname, /* unused */null);
    }

    /**
     * Constructor for client.
     */
    public SarosSession(final String id, JID hostJID, String clientNickname,
        String hostNickname, int clientColorID, int hostColorID,
        ISarosContext sarosContext) {

        this(id, sarosContext, hostJID, clientColorID, hostColorID,
            clientNickname, hostNickname);
    }

    @Override
    public void addSharedResources(IProject project, String projectID,
        List<IResource> dependentResources) {
        if (!isCompletelyShared(project) && dependentResources != null) {
            for (IResource resource : dependentResources) {
                if (resource.getType() == IResource.FOLDER) {
                    addMembers(resource);
                }
            }
            if (selectedResources != null) {
                selectedResources.removeAll(dependentResources);
                dependentResources.addAll(selectedResources);
                selectedResources.clear();
            }
        }

        if (!projectMapper.isShared(project)) {
            projectMapper.addProject(projectID, project,
                dependentResources != null);

            if (dependentResources != null)
                projectMapper.addResources(project, dependentResources);

            // HACK
            resourceManager.projectAdded(project);
        } else {
            if (dependentResources == null)
                // upgrade the project to a completely shared project
                projectMapper.addProject(projectID, project, false);
            else
                projectMapper.addResources(project, dependentResources);
        }
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<User>(participants.values());
    }

    @Override
    public List<User> getRemoteUsersWithReadOnlyAccess() {
        List<User> result = new ArrayList<User>();
        for (User user : getUsers()) {
            if (user.isLocal())
                continue;
            if (user.hasReadOnlyAccess())
                result.add(user);
        }
        return result;
    }

    @Override
    public List<User> getUsersWithReadOnlyAccess() {
        List<User> result = new ArrayList<User>();
        for (User user : getUsers()) {
            if (user.hasReadOnlyAccess())
                result.add(user);
        }
        return result;
    }

    @Override
    public List<User> getUsersWithWriteAccess() {
        List<User> result = new ArrayList<User>();
        for (User user : getUsers()) {
            if (user.hasWriteAccess())
                result.add(user);
        }
        return result;
    }

    public List<User> getRemoteUsersWithWriteAccess() {
        List<User> result = new ArrayList<User>();
        for (User user : getUsers()) {
            if (user.isLocal())
                continue;
            if (user.hasWriteAccess())
                result.add(user);
        }
        return result;
    }

    @Override
    public List<User> getRemoteUsers() {
        List<User> result = new ArrayList<User>();
        for (User user : getUsers()) {
            if (user.isRemote())
                result.add(user);
        }
        return result;
    }

    @Override
    public boolean userHasProject(User user, IProject project) {
        return projectMapper.userHasProject(user, project);
    }

    @Override
    public void initiatePermissionChange(final User user,
        final Permission newPermission) throws CancellationException,
        InterruptedException {

        if (!localUser.isHost())
            throw new IllegalStateException(
                "only the host can initiate permission changes");

        permissionManager.initiatePermissionChange(user, newPermission,
            synchronizer);
    }

    @Override
    public void setPermission(final User user, final Permission permission) {

        if (user == null || permission == null)
            throw new IllegalArgumentException();

        synchronizer.syncExec(ThreadUtils.wrapSafe(log, new Runnable() {
            @Override
            public void run() {
                user.setPermission(permission);
                listenerDispatch.permissionChanged(user);
            }
        }));

        log.info("user " + user + " is now a " + permission);
    }

    @Override
    public String getID() {
        return sessionID;
    }

    @Override
    public User getHost() {
        return hostUser;
    }

    @Override
    public boolean isHost() {
        return localUser.isHost();
    }

    @Override
    public boolean hasWriteAccess() {
        return localUser.hasWriteAccess();
    }

    @Override
    public boolean hasExclusiveWriteAccess() {
        if (!hasWriteAccess()) {
            return false;
        }
        for (User user : getUsers()) {
            if (user.isRemote() && user.hasWriteAccess()) {
                return false;
            }
        }
        return true;
    }

    /*
     * FIXME only accept a JID or create a method session.createUser to ensure
     * proper initialization etc. of User objects !
     */
    @Override
    public void addUser(final User user) {

        // TODO synchronize this method !

        JID jid = user.getJID();

        if (!jid.isResourceQualifiedJID())
            throw new IllegalArgumentException("network id of user " + user
                + " is not unique, resource part of JID is missing");

        user.setInSession(true);

        if (participants.putIfAbsent(jid, user) != null) {
            log.error("user " + user + " added twice to SarosSession",
                new StackTrace());
            throw new IllegalArgumentException();
        }

        /*
         * 
         * as long as we do not know when something is send to someone this will
         * always produce errors ... swapping synchronizeUserList and userJoined
         * can produce different results
         */

        if (isHost()) {

            activitySequencer.registerUser(user);

            List<User> timedOutUsers = userListHandler.synchronizeUserList(
                getUsers(), null, getRemoteUsers());

            if (!timedOutUsers.isEmpty()) {
                activitySequencer.unregisterUser(user);
                participants.remove(jid);
                // FIXME do not throw a runtime exception here
                throw new RuntimeException(
                    "could not synchronize user list, following users did not respond: "
                        + StringUtils.join(timedOutUsers, ", "));
            }
        }

        synchronizer.syncExec(ThreadUtils.wrapSafe(log, new Runnable() {
            @Override
            public void run() {
                listenerDispatch.userJoined(user);
            }
        }));

        log.info("user " + user + " joined session");
    }

    @Override
    public void userStartedQueuing(final User user) {

        log.info("user " + user
            + " started queuing projects and can receive IResourceActivities");

        /**
         * Updates the projects for the given user, so that host knows that he
         * can now send ever Activity
         */
        projectMapper.addMissingProjectsToUser(user);

        synchronizer.syncExec(ThreadUtils.wrapSafe(log, new Runnable() {
            @Override
            public void run() {
                listenerDispatch.userStartedQueuing(user);
            }
        }));
    }

    @Override
    public void userFinishedProjectNegotiation(final User user) {

        log.info("user " + user
            + " now has Projects and can process IResourceActivities");

        synchronizer.syncExec(ThreadUtils.wrapSafe(log, new Runnable() {
            @Override
            public void run() {
                listenerDispatch.userFinishedProjectNegotiation(user);
            }
        }));

        if (isHost()) {

            JID jid = user.getJID();
            /**
             * This informs all participants, that a user is now able to process
             * IResourceActivities. After receiving this message the
             * participants will send their awareness information.
             */
            userListHandler.sendUserFinishedProjectNegotiation(
                getRemoteUsers(), jid);
        }
    }

    @Override
    public void removeUser(final User user) {
        synchronized (this) {
            if (!user.isInSession()) {
                log.warn("user " + user
                    + " is already or is currently removed from the session");
                return;
            }

            user.setInSession(false);
        }

        JID jid = user.getJID();
        if (participants.remove(jid) == null) {
            log.error("tried to remove user " + user
                + " who was never added to the session");
            return;
        }

        activitySequencer.unregisterUser(user);

        projectMapper.userLeft(user);

        List<User> currentRemoteUsers = getRemoteUsers();

        if (isHost() && !currentRemoteUsers.isEmpty()) {

            List<User> timedOutUsers = userListHandler.synchronizeUserList(
                null, Collections.singletonList(user), currentRemoteUsers);

            if (!timedOutUsers.isEmpty()) {
                log.error("could not synchronize user list properly, following users did not respond: "
                    + StringUtils.join(timedOutUsers, ", "));
            }
        }

        synchronizer.syncExec(ThreadUtils.wrapSafe(log, new Runnable() {
            @Override
            public void run() {
                listenerDispatch.userLeft(user);
            }
        }));

        // TODO what is to do here if no user with write access exists anymore?

        // Disconnect bytestream connection when user leaves session to
        // prevent idling connection when not needed anymore.
        connectionManager.closeConnection(ISarosSession.SESSION_CONNECTION_ID,
            jid);

        log.info("user " + user + " left session");
    }

    @Override
    public void kickUser(final User user) {

        if (!isHost())
            throw new IllegalStateException(
                "only the host can kick users from the current session");

        if (user.equals(getLocalUser()))
            throw new IllegalArgumentException(
                "the local user cannot kick itself out of the session");

        try {
            transmitter.send(SESSION_CONNECTION_ID, user.getJID(),
                KickUserExtension.PROVIDER
                    .create(new KickUserExtension(getID())));
        } catch (IOException e) {
            log.warn("could not kick user "
                + user
                + " from the session because the connection to the user is already lost");
        }

        removeUser(user);
    }

    @Override
    public void addListener(ISharedProjectListener listener) {
        listenerDispatch.add(listener);
    }

    @Override
    public void removeListener(ISharedProjectListener listener) {
        listenerDispatch.remove(listener);
    }

    @Override
    public Set<IProject> getProjects() {
        return projectMapper.getProjects();
    }

    // FIXME synchronization
    @Override
    public void start() {
        if (started || stopped) {
            throw new IllegalStateException();
        }

        sessionContainer.start();

        for (User user : getRemoteUsers())
            activitySequencer.registerUser(user);

        // TODO Pull that out
        pathConverter = new SPathConverter(this, pathFactory);
        ActivitiesExtension.PROVIDER.registerConverter(pathConverter);

        userConverter = new UserConverter(this);
        ActivitiesExtension.PROVIDER.registerConverter(userConverter);

        synchronized (componentAccessLock) {
            started = true;
        }
    }

    /**
     * Stops this session and performing cleanup as necessary. All remote users
     * will also be notified about the local session stop.
     * 
     * @throws IllegalStateException
     *             if the session is already stopped or was not started at all
     */
    // FIXME synchronization
    public void stop() {
        if (!started || stopped) {
            throw new IllegalStateException();
        }

        synchronized (componentAccessLock) {
            stopped = true;
        }

        sarosContext.removeChildContainer(sessionContainer);
        sessionContainer.stop();
        sessionContainer.dispose();

        List<User> usersToNotify;

        if (isHost())
            usersToNotify = getRemoteUsers();
        else
            usersToNotify = Collections.singletonList(getHost());

        for (User user : usersToNotify) {
            try {
                transmitter.send(SESSION_CONNECTION_ID, user.getJID(),
                    LeaveSessionExtension.PROVIDER
                        .create(new LeaveSessionExtension(getID())));
            } catch (IOException e) {
                log.warn("failed to notify user " + user
                    + " about local session stop", e);
            }
        }

        for (User user : getRemoteUsers())
            connectionManager.closeConnection(
                ISarosSession.SESSION_CONNECTION_ID, user.getJID());

        // TODO Pull that out
        ActivitiesExtension.PROVIDER.unregisterConverter(pathConverter);
        ActivitiesExtension.PROVIDER.unregisterConverter(userConverter);
    }

    @Override
    public User getUser(JID jid) {

        if (jid == null)
            throw new IllegalArgumentException("jid is null");

        if (jid.isBareJID())
            throw new IllegalArgumentException(
                "JID is not resource qualified: " + jid);

        User user = participants.get(jid);

        if (user == null || !user.getJID().strictlyEquals(jid))
            return null;

        return user;
    }

    @Override
    public JID getResourceQualifiedJID(JID jid) {

        if (jid == null)
            throw new IllegalArgumentException("jid is null");

        User user = participants.get(jid);

        if (user == null)
            return null;

        return user.getJID();
    }

    @Override
    public User getLocalUser() {
        return localUser;
    }

    @Override
    public ConcurrentDocumentClient getConcurrentDocumentClient() {
        return concurrentDocumentClient;
    }

    @Override
    public ConcurrentDocumentServer getConcurrentDocumentServer() {
        if (!isHost())
            throw new IllegalStateException(
                "the session is running in client mode");

        return concurrentDocumentServer;
    }

    /**
     * @JTourBusStop 7, Activity sending, Incoming activities:
     * 
     *               The ActivitySequencer will call this function for
     *               activities received over the Network Layer.
     * 
     */

    @Override
    public void exec(List<IActivity> activities) {
        final List<IActivity> valid = new ArrayList<IActivity>();

        // Check every incoming activity for validity
        for (IActivity activity : activities) {
            if (activity.isValid())
                valid.add(activity);
            else
                log.error("could not handle incoming activity: " + activity);
        }

        List<IActivity> processed = activityQueuer.process(valid);
        activityHandler.handleIncomingActivities(processed);
    }

    /*
     * FIXME most (if not all checks) to send or not activities should be
     * handled by the activity handler and not here !
     */
    private void sendActivity(final List<User> recipients,
        final IActivity activity) {
        if (recipients == null)
            throw new IllegalArgumentException();

        if (activity == null)
            throw new IllegalArgumentException();
        /*
         * If we don't have any sharedProjects don't send File-, Folder- or
         * EditorActivities.
         */
        if (projectMapper.size() == 0
            && (activity instanceof EditorActivity
                || activity instanceof FolderActivity || activity instanceof FileActivity)) {
            return;
        }

        // avoid sending of unwanted editor related activities

        if (activity instanceof IResourceActivity
            && (activity instanceof TextSelectionActivity
                || activity instanceof ViewportActivity || activity instanceof JupiterActivity)) {
            IResourceActivity resActivity = (IResourceActivity) activity;
            if (!isShared(resActivity.getPath().getResource()))
                return;
        }

        boolean send = true;
        // handle FileActivities and FolderActivities to update ProjectMapper
        if (activity instanceof FolderActivity
            || activity instanceof FileActivity) {
            send = updatePartialSharedResources(activity);
        }

        if (!send)
            return;

        try {
            activitySequencer.sendActivity(recipients, activity);
        } catch (IllegalArgumentException e) {
            log.warn("could not serialize activity: " + activity, e);
        }
    }

    /**
     * Method to update the project mapper when changes on shared files oder
     * folders happened.
     * 
     * @param activity
     *            {@link FileActivity} or {@link FolderActivity} to handle
     * @return <code>true</code> if the activity should be send to the user,
     *         <code>false</code> otherwise
     */
    private boolean updatePartialSharedResources(IActivity activity) {
        if (!(activity instanceof FileActivity)
            && !(activity instanceof FolderActivity))
            return true;

        if (activity instanceof FileActivity) {
            FileActivity fileActivity = ((FileActivity) activity);
            SPath path = fileActivity.getPath();
            IFile file = path.getFile();

            if (file == null)
                return true;

            IProject project = file.getProject();

            switch (fileActivity.getType()) {
            case CREATED:
                if (!file.exists())
                    return true;

                if (projectMapper.isPartiallyShared(project))
                    projectMapper.addResources(project,
                        Collections.singletonList(file));
                break;
            case REMOVED:
                if (!isShared(file))
                    return false;

                if (projectMapper.isPartiallyShared(project))
                    projectMapper.removeResources(project,
                        Collections.singletonList(file));

                break;
            case MOVED:
                IFile oldFile = fileActivity.getOldPath().getFile();
                if (oldFile == null || !isShared(oldFile))
                    return false;

                if (projectMapper.isPartiallyShared(project)) {
                    projectMapper.removeAndAddResources(project,
                        Collections.singletonList(oldFile),
                        Collections.singletonList(file));
                }

                break;
            }
        } else if (activity instanceof FolderActivity) {
            FolderActivity folderActivity = ((FolderActivity) activity);
            IFolder folder = folderActivity.getPath().getFolder();

            if (folder == null)
                return true;

            IProject project = folder.getProject();

            switch (folderActivity.getType()) {
            case CREATED:
                if (projectMapper.isPartiallyShared(project)
                    && isShared(folder.getParent()))
                    projectMapper.addResources(project,
                        Collections.singletonList(folder));
                break;
            case REMOVED:
                if (!isShared(folder))
                    return false;

                if (projectMapper.isPartiallyShared(project))
                    projectMapper.removeResources(project,
                        Collections.singletonList(folder));
            }
        }
        return true;
    }

    @Override
    public void addActivityProducer(IActivityProducer producer) {
        if (activityProducers.addIfAbsent(producer))
            producer.addActivityListener(activityListener);
    }

    @Override
    public void removeActivityProducer(IActivityProducer producer) {
        if (activityProducers.remove(producer))
            producer.removeActivityListener(activityListener);
    }

    @Override
    public void addActivityConsumer(IActivityConsumer consumer) {
        activityConsumers.addIfAbsent(consumer);
    }

    @Override
    public void removeActivityConsumer(IActivityConsumer consumer) {
        activityConsumers.remove(consumer);
    }

    @Override
    public boolean isShared(IResource resource) {
        return projectMapper.isShared(resource);
    }

    @Override
    public List<IResource> getSharedResources() {
        return projectMapper.getPartiallySharedResources();
    }

    /**
     * Recursively add non-shared resources
     * 
     * @param resource
     *            of type {@link IResource#FOLDER} or {@link IResource#FILE}
     */
    private void addMembers(IResource resource) {
        if (isShared(resource))
            return;

        selectedResources.add(resource);

        if (resource.getType() == IResource.FOLDER) {
            List<IResource> childResources = null;
            try {
                childResources = Arrays.asList(((IContainer) resource)
                    .members());
            } catch (IOException e) {
                log.error("Can't get children of folder " + resource, e);
                return;
            }

            for (IResource childResource : childResources) {
                addMembers(childResource);
            }
        }
    }

    @Override
    public boolean useVersionControl() {
        /*
         * It is not possible to enable version control support during a
         * session.
         */
        if (!useVersionControl)
            return false;
        return useVersionControl = preferences.useVersionControl();
    }

    @Override
    public String getProjectID(IProject project) {
        return projectMapper.getID(project);
    }

    @Override
    public IProject getProject(String projectID) {
        return projectMapper.getProject(projectID);
    }

    @Override
    public Map<IProject, List<IResource>> getProjectResourcesMapping() {
        return projectMapper.getProjectResourceMapping();
    }

    @Override
    public List<IResource> getSharedResources(IProject project) {
        return projectMapper.getProjectResourceMapping().get(project);
    }

    @Override
    public boolean isCompletelyShared(IProject project) {
        return projectMapper.isCompletelyShared(project);
    }

    @Override
    public void addProjectMapping(String projectID, IProject project) {
        if (projectMapper.getProject(projectID) == null) {
            projectMapper.addProject(projectID, project, true);
            // HACK
            resourceManager.projectAdded(project);
        }
    }

    @Override
    public void removeProjectMapping(String projectID, IProject project) {
        if (projectMapper.getProject(projectID) != null) {
            projectMapper.removeProject(projectID);
            // HACK
            resourceManager.projectRemoved(project);
        }
    }

    @Override
    public Object getComponent(Object key) {
        /*
         * Ensure that we return null when the session is about to start or stop
         * because the MutablePicoContainer#start/stop/dispose method is
         * synchronized and may cause a deadlock if the method is called from
         * the UI thread while a component may call #syncExec inside the start
         * or stop methods.
         */
        synchronized (componentAccessLock) {
            if (stopped || !started)
                return null;

            return sessionContainer.getComponent(key);
        }
    }

    @Override
    public StopManager getStopManager() {
        return stopManager;
    }

    @Override
    public void changeColor(int colorID) {
        changeColorManager.changeColorID(colorID);
    }

    @Override
    public Set<Integer> getUnavailableColors() {
        return changeColorManager.getUsedColorIDs();
    }

    @Override
    public void enableQueuing(IProject project) {
        activityQueuer.enableQueuing(project);
    }

    @Override
    public void disableQueuing() {
        activityQueuer.disableQueuing();
        // send us a dummy activity to ensure the queues get flushed
        sendActivity(Collections.singletonList(localUser), new NOPActivity(
            localUser, localUser, 0));
    }

    private SarosSession(final String id, ISarosContext context, JID host,
        int localColorID, int hostColorID, String localNickname,
        String hostNickname) {

        context.initComponent(this);

        this.pathFactory = context.getComponent(IPathFactory.class);

        this.sessionID = id;
        this.projectMapper = new SarosProjectMapper();
        this.activityQueuer = new ActivityQueuer();
        this.sarosContext = context;

        // FIXME that should be passed in !
        JID localUserJID = connectionService.getJID();

        assert localUserJID != null;

        localUser = new User(localUserJID, localNickname, host == null, true,
            localColorID, localColorID);

        localUser.setInSession(true);

        if (host == null) {
            hostUser = localUser;
            participants.put(hostUser.getJID(), hostUser);
        } else {
            hostUser = new User(host, hostNickname, true, false, hostColorID,
                hostColorID);
            hostUser.setInSession(true);
            participants.put(hostUser.getJID(), hostUser);
            participants.put(localUser.getJID(), localUser);
        }

        sessionContainer = context.createSimpleChildContainer();
        sessionContainer.addComponent(ISarosSession.class, this);
        sessionContainer.addComponent(IActivityHandlerCallback.class,
            activityCallback);

        ISarosSessionContextFactory factory = context
            .getComponent(ISarosSessionContextFactory.class);
        factory.createComponents(this, sessionContainer);

        // Force the creation of the components added to the session container.
        sessionContainer.getComponents();

        // HACK
        resourceManager = sessionContainer
            .getComponent(SharedResourcesManager.class);

        concurrentDocumentServer = sessionContainer
            .getComponent(ConcurrentDocumentServer.class);

        concurrentDocumentClient = sessionContainer
            .getComponent(ConcurrentDocumentClient.class);

        activityHandler = sessionContainer.getComponent(ActivityHandler.class);

        stopManager = sessionContainer.getComponent(StopManager.class);

        changeColorManager = sessionContainer
            .getComponent(ChangeColorManager.class);

        permissionManager = sessionContainer
            .getComponent(PermissionManager.class);

        activitySequencer = sessionContainer
            .getComponent(ActivitySequencer.class);

        userListHandler = sessionContainer
            .getComponent(UserInformationHandler.class);

        // ensure that the container uses caching
        assert sessionContainer.getComponent(ActivityHandler.class) == sessionContainer
            .getComponent(ActivityHandler.class) : "container is wrongly configured - no cache support";
    }

    /**
     * This method is only meant to be used by unit tests to verify the cleanup
     * of activity producers and consumers.
     * 
     * @return the size of the internal activity producer collection
     */
    boolean hasActivityProducers() {
        return !activityProducers.isEmpty();
    }

    /**
     * This method is only meant to be used by unit tests to verify the cleanup
     * of activity producers and consumers.
     * 
     * @return the size of the internal activity consumer collection
     */
    boolean hasActivityConsumers() {
        return !activityConsumers.isEmpty();
    }
}
