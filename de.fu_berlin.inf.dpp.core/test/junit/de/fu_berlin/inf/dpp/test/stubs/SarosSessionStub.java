package de.fu_berlin.inf.dpp.test.stubs;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.IActivityProducer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;

public class SarosSessionStub implements ISarosSession {

  private boolean hasWriteAccess;

  public void setWriteAccess(boolean b) {
    hasWriteAccess = b;
  }

  @Override
  public void start() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void removeUser(User user) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void removeListener(ISessionListener listener) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public boolean isShared(IResource resource) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public boolean isHost() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public boolean hasWriteAccess() {
    return hasWriteAccess;
  }

  @Override
  public void changePermission(User user, Permission newPermission)
      throws CancellationException, InterruptedException {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public User getUser(JID jid) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public IPreferenceStore getUserProperties(User user) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public JID getResourceQualifiedJID(JID jid) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public List<User> getRemoteUsers() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public Set<IProject> getProjects() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public String getProjectID(IProject project) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public IProject getProject(String projectID) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public List<User> getUsers() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public User getLocalUser() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public User getHost() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public ConcurrentDocumentServer getConcurrentDocumentServer() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public ConcurrentDocumentClient getConcurrentDocumentClient() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void exec(List<IActivity> activities) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void addUser(User user, IPreferenceStore properties) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void addListener(ISessionListener listener) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void addSharedResources(
      IProject project, String projectID, List<IResource> dependentResources) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public List<IResource> getSharedResources() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public HashMap<IProject, List<IResource>> getProjectResourcesMapping() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public boolean isCompletelyShared(IProject project) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public List<IResource> getSharedResources(IProject project) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void addProjectMapping(String projectID, IProject project) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void removeProjectMapping(String projectID, IProject project) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public StopManager getStopManager() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void kickUser(User user) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void changeColor(int colorID) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public Set<Integer> getUnavailableColors() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void enableQueuing(IProject project) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void disableQueuing(IProject project) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void userStartedQueuing(User user) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void userFinishedProjectNegotiation(User user) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public boolean userHasProject(User user, IProject project) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public String getID() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void addActivityProducer(IActivityProducer producer) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void removeActivityProducer(IActivityProducer producer) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void addActivityConsumer(IActivityConsumer consumer, Priority priority) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void removeActivityConsumer(IActivityConsumer consumer) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public <T> T getComponent(Class<T> key) {
    throw new RuntimeException("Unexpected call to Stub");
  }
}
