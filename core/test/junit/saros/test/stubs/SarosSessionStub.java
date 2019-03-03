package saros.test.stubs;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import saros.activities.IActivity;
import saros.concurrent.management.ConcurrentDocumentClient;
import saros.concurrent.management.ConcurrentDocumentServer;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.net.xmpp.JID;
import saros.session.IActivityConsumer;
import saros.session.IActivityConsumer.Priority;
import saros.session.IActivityProducer;
import saros.session.ISarosSession;
import saros.session.ISessionListener;
import saros.session.User;
import saros.session.User.Permission;
import saros.synchronize.StopManager;

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
  public JID getResourceQualifiedJID(JID jid) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public List<User> getRemoteUsers() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public Set<IReferencePoint> getReferencePoints() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public String getReferencePointID(IReferencePoint referencePoint) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public IReferencePoint getReferencePoint(String referencePointID) {
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
  public void addUser(User user) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void addListener(ISessionListener listener) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void addSharedResources(
      IReferencePoint referencePoint, String referencePointID, List<IResource> dependentResources) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public List<IResource> getSharedResources() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public Map<IReferencePoint, List<IResource>> getReferencePointResourcesMapping() {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public boolean isCompletelyShared(IReferencePoint referencePoint) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public List<IResource> getSharedResources(IReferencePoint referencePoint) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void addReferencePointMapping(String referencePointID, IReferencePoint referencePoint) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void removeReferencePointMapping(String referencePointID, IReferencePoint referencePoint) {
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
  public void enableQueuing(IReferencePoint referencePoint) {
    throw new RuntimeException("Unexpected call to Stub");
  }

  @Override
  public void disableQueuing(IReferencePoint referencePoint) {
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
  public boolean userHasReferencePoint(User user, IReferencePoint referencePoint) {
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
