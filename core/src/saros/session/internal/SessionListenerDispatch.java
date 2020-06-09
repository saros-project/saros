package saros.session.internal;

import java.util.concurrent.CopyOnWriteArrayList;
import saros.filesystem.IReferencePoint;
import saros.session.ISessionListener;
import saros.session.User;

/** {@link ISessionListener} implementation which can dispatch events to multiple listeners. */
public class SessionListenerDispatch implements ISessionListener {

  private final CopyOnWriteArrayList<ISessionListener> listeners =
      new CopyOnWriteArrayList<ISessionListener>();

  @Override
  public void permissionChanged(User user) {
    for (ISessionListener listener : listeners) listener.permissionChanged(user);
  }

  @Override
  public void userJoined(User user) {
    for (ISessionListener listener : listeners) listener.userJoined(user);
  }

  @Override
  public void userStartedQueuing(User user) {
    for (ISessionListener listener : listeners) listener.userStartedQueuing(user);
  }

  @Override
  public void userFinishedResourceNegotiation(User user) {
    for (ISessionListener listener : listeners) listener.userFinishedResourceNegotiation(user);
  }

  @Override
  public void userColorChanged(User user) {
    for (ISessionListener listener : listeners) listener.userColorChanged(user);
  }

  @Override
  public void userLeft(User user) {
    for (ISessionListener listener : listeners) listener.userLeft(user);
  }

  @Override
  public void referencePointAdded(IReferencePoint referencePoint) {
    for (ISessionListener listener : listeners) listener.referencePointAdded(referencePoint);
  }

  @Override
  public void referencePointRemoved(IReferencePoint referencePoint) {
    for (ISessionListener listener : listeners) listener.referencePointRemoved(referencePoint);
  }

  @Override
  public void resourcesAdded(IReferencePoint referencePoint) {
    for (ISessionListener listener : listeners) listener.resourcesAdded(referencePoint);
  }

  public void add(ISessionListener listener) {
    listeners.addIfAbsent(listener);
  }

  public void remove(ISessionListener listener) {
    listeners.remove(listener);
  }
}
