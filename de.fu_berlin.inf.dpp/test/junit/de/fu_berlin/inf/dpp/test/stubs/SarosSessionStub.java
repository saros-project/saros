package de.fu_berlin.inf.dpp.test.stubs;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.SubMonitor;
import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SharedProject;

public class SarosSessionStub implements ISarosSession {

    private boolean hasWriteAccess;
    private boolean useVersionControl;

    public void setUseVersionControl(boolean b) {
        useVersionControl = b;
    }

    public void setWriteAccess(boolean b) {
        hasWriteAccess = b;
    }

    public void userInvitationCompleted(User user) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean useVersionControl() {
        return useVersionControl;
    }

    public void stop() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public void start() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setPermission(User user, Permission permission) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void sendActivity(User recipient, IActivity activity) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void sendActivity(List<User> recipient, IActivity activity) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void returnColor(int colorID) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void removeUser(User user) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void removeListener(ISharedProjectListener listener) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void removeActivityProvider(IActivityProvider provider) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isShared(IProject project) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public boolean isHost() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public boolean hasExclusiveWriteAccess() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public boolean hasWriteAccess() {
        return hasWriteAccess;
    }

    public void initiatePermissionChange(User user, Permission newPermission,
        SubMonitor progress) throws CancellationException, InterruptedException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public User getUser(JID jid) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public ITransmitter getTransmitter() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public SharedProject getSharedProject(IProject project) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public DateTime getSessionStart() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public ActivitySequencer getSequencer() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public Saros getSaros() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public JID getResourceQualifiedJID(JID jid) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public List<User> getRemoteUsers() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public List<User> getRemoteUsersWithReadOnlyAccess() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public Set<IProject> getProjects() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public String getProjectID(IProject project) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public IProject getProject(String projectID) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public Collection<User> getParticipants() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public List<User> getUsersWithReadOnlyAccess() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public User getLocalUser() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public User getHost() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public int getFreeColor() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public List<User> getUsersWithWriteAccess() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public ConcurrentDocumentServer getConcurrentDocumentServer() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public ConcurrentDocumentClient getConcurrentDocumentClient() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public void exec(List<IActivityDataObject> activityDataObjects) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void addUser(User user) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void addSharedProject(IProject project, String projectID) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void addListener(ISharedProjectListener listener) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void addActivityProvider(IActivityProvider provider) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void activityCreated(IActivity activity) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public List<SharedProject> getSharedProjects() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public void synchronizeUserList(ITransmitter transmitter, JID peer,
        String invitationID, SubMonitor monitor)
        throws SarosCancellationException {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public void execQueuedActivities() {
        throw new RuntimeException("Unexpected call to Stub");
    }
}