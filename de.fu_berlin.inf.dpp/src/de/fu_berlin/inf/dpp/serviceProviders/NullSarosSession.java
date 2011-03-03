package de.fu_berlin.inf.dpp.serviceProviders;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
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

    public Collection<User> getParticipants() {
        log.warn("unexpected method call");
        return null;
    }

    public List<User> getRemoteUsers() {
        log.warn("unexpected method call");
        return null;
    }

    public void initiatePermissionChange(User user, Permission newPermission,
        SubMonitor progress) throws CancellationException, InterruptedException {
        log.warn("unexpected method call");
    }

    public void setPermission(User user, Permission permission) {
        log.warn("unexpected method call");
    }

    public void userInvitationCompleted(User user) {
        log.warn("unexpected method call");
    }

    public boolean hasWriteAccess() {
        log.warn("unexpected method call");
        return false;
    }

    public User getHost() {
        log.warn("unexpected method call");
        return null;
    }

    public boolean isHost() {
        log.warn("unexpected method call");
        return false;
    }

    public void addUser(User user) {
        log.warn("unexpected method call");
    }

    public void removeUser(User user) {
        log.warn("unexpected method call");
    }

    public void addListener(ISharedProjectListener listener) {
        log.warn("unexpected method call");
    }

    public void removeListener(ISharedProjectListener listener) {
        log.warn("unexpected method call");
    }

    public Set<IProject> getProjects() {
        log.warn("unexpected method call");
        return null;
    }

    public ActivitySequencer getSequencer() {
        log.warn("unexpected method call");
        return null;
    }

    public void start() {
        log.warn("unexpected method call");
    }

    public void stop() {
        log.warn("unexpected method call");
    }

    public User getUser(JID jid) {
        log.warn("unexpected method call");
        return null;
    }

    public JID getResourceQualifiedJID(JID jid) {
        log.warn("unexpected method call");
        return null;
    }

    public User getLocalUser() {
        log.warn("unexpected method call");
        return null;
    }

    public boolean hasExclusiveWriteAccess() {
        log.warn("unexpected method call");
        return false;
    }

    public ConcurrentDocumentServer getConcurrentDocumentServer() {
        log.warn("unexpected method call");
        return null;
    }

    public ConcurrentDocumentClient getConcurrentDocumentClient() {
        log.warn("unexpected method call");
        return null;
    }

    public Saros getSaros() {
        log.warn("unexpected method call");
        return null;
    }

    public int getFreeColor() {
        log.warn("unexpected method call");
        return 0;
    }

    public void returnColor(int colorID) {
        log.warn("unexpected method call");

    }

    public void exec(List<IActivityDataObject> activityDataObjects) {
        log.warn("unexpected method call");

    }

    public void activityCreated(IActivity activity) {
        log.warn("unexpected method call");

    }

    public void sendActivity(List<User> recipient, IActivity activity) {
        log.warn("unexpected method call");

    }

    public void sendActivity(User recipient, IActivity activity) {
        log.warn("unexpected method call");

    }

    public void addActivityProvider(IActivityProvider provider) {
        log.warn("unexpected method call");

    }

    public void removeActivityProvider(IActivityProvider provider) {
        log.warn("unexpected method call");

    }

    public List<User> getUsersWithWriteAccess() {
        log.warn("unexpected method call");
        return null;
    }

    public List<User> getUsersWithReadOnlyAccess() {
        log.warn("unexpected method call");
        return null;
    }

    public List<User> getRemoteUsersWithReadOnlyAccess() {
        log.warn("unexpected method call");
        return null;
    }

    public DateTime getSessionStart() {
        log.warn("unexpected method call");
        return null;
    }

    public boolean isShared(IProject project) {
        log.warn("unexpected method call");
        return false;
    }

    public boolean useVersionControl() {
        log.warn("unexpected method call");
        return false;
    }

    public SharedProject getSharedProject(IProject project) {
        log.warn("unexpected method call");
        return null;
    }

    public String getProjectID(IProject project) {
        log.warn("unexpected method call");
        return null;
    }

    public IProject getProject(String projectID) {
        log.warn("unexpected method call");
        return null;
    }

    public void addSharedProject(IProject project, String projectID) {
        log.warn("unexpected method call");

    }

    public List<SharedProject> getSharedProjects() {
        log.warn("unexpected method call");
        return null;
    }

    public void synchronizeUserList(ITransmitter transmitter, JID peer,
        String invitationID, SubMonitor monitor)
        throws SarosCancellationException {
        log.warn("unexpected method call");

    }

}
