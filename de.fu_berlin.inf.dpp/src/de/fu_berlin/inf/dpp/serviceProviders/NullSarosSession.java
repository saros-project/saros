package de.fu_berlin.inf.dpp.serviceProviders;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.session.IActivityProvider;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.synchronize.StopManager;

/**
 * Instances of this class signify a non existing {@link ISarosSession}.
 * <p>
 * It is <b>ONLY</b> intended to be used for activity/enablement reasons in
 * plugin.xml as the <a
 * href="http://wiki.eclipse.org/Command_Core_Expressions">Eclipse Core
 * Expressions</a> do not support null values.
 */
public class NullSarosSession implements ISarosSession {

    private Logger log = Logger.getLogger(NullSarosSession.class);

    @Override
    public List<User> getUsers() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public List<User> getRemoteUsers() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public void initiatePermissionChange(User user, Permission newPermission)
        throws CancellationException, InterruptedException {
        log.warn("unexpected method call");
    }

    @Override
    public void setPermission(User user, Permission permission) {
        log.warn("unexpected method call");
    }

    @Override
    public boolean hasWriteAccess() {
        log.warn("unexpected method call");
        return false;
    }

    @Override
    public User getHost() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public boolean isHost() {
        log.warn("unexpected method call");
        return false;
    }

    @Override
    public void addUser(User user) {
        log.warn("unexpected method call");
    }

    @Override
    public void removeUser(User user) {
        log.warn("unexpected method call");
    }

    @Override
    public void addListener(ISharedProjectListener listener) {
        log.warn("unexpected method call");
    }

    @Override
    public void removeListener(ISharedProjectListener listener) {
        log.warn("unexpected method call");
    }

    @Override
    public Set<IProject> getProjects() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public void start() {
        log.warn("unexpected method call");
    }

    @Override
    public User getUser(JID jid) {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public JID getResourceQualifiedJID(JID jid) {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public User getLocalUser() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public boolean hasExclusiveWriteAccess() {
        log.warn("unexpected method call");
        return false;
    }

    @Override
    public ConcurrentDocumentServer getConcurrentDocumentServer() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public ConcurrentDocumentClient getConcurrentDocumentClient() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public void exec(List<IActivityDataObject> activityDataObjects) {
        log.warn("unexpected method call");

    }

    public void sendActivity(List<User> recipient, IActivity activity) {
        log.warn("unexpected method call");

    }

    @Override
    public void addActivityProvider(IActivityProvider provider) {
        log.warn("unexpected method call");

    }

    @Override
    public void removeActivityProvider(IActivityProvider provider) {
        log.warn("unexpected method call");

    }

    @Override
    public List<User> getUsersWithWriteAccess() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public List<User> getUsersWithReadOnlyAccess() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public List<User> getRemoteUsersWithReadOnlyAccess() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public boolean isShared(IResource resource) {
        log.warn("unexpected method call");
        return false;
    }

    @Override
    public boolean useVersionControl() {
        log.warn("unexpected method call");
        return false;
    }

    @Override
    public String getProjectID(IProject project) {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public IProject getProject(String projectID) {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public void addSharedResources(IProject project, String projectID,
        List<IResource> dependentResources) {
        log.warn("unexpected method call");

    }

    @Override
    public List<IResource> getSharedResources() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public HashMap<IProject, List<IResource>> getProjectResourcesMapping() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public boolean isCompletelyShared(IProject project) {
        log.warn("unexpected method call");
        return false;
    }

    public void stopQueue() {
        log.warn("unexpected method call");
    }

    public void startQueue() {
        log.warn("unexpected method call");
    }

    @Override
    public List<IResource> getSharedResources(IProject project) {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public void addProjectOwnership(String projectID, IProject project,
        JID ownerJID) {
        log.warn("unexpected method call");
    }

    @Override
    public StopManager getStopManager() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public void removeProjectOwnership(String projectID, IProject project,
        JID ownerJID) {
        log.warn("unexpected method call");
    }

    @Override
    public void kickUser(User user) {
        log.warn("unexpected method call");
    }

    @Override
    public void changeColor(int colorID) {
        log.warn("unexpected method call");
    }

    @Override
    public Set<Integer> getUnavailableColors() {
        log.warn("unexpected method call");
        return null;
    }

    @Override
    public void enableQueuing(String projectId) {
        log.warn("unexpected method call");
    }

    @Override
    public void disableQueuing() {
        log.warn("unexpected method call");
    }

    @Override
    public void userStartedQueuing(User user) {
        log.warn("unexpected method call");
    }

    @Override
    public void userFinishedProjectNegotiation(User user) {
        log.warn("unexpected method call");

    }

    @Override
    public boolean userHasProject(User user, IProject project) {
        log.warn("unexpected method call");
        return false;
    }

    @Override
    public String getID() {
        log.warn("unexpected method call");
        return null;
    }

}
