package de.fu_berlin.inf.dpp.whiteboard.sxe;

import de.fu_berlin.inf.dpp.whiteboard.sxe.net.SXEMessage;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ChildRecordChangeCache;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Apart from maintaining the lists of listeners this class caches ChildRecordChangeCache during
 * record applying to notify a list of listeners at once (i.e. only after handling one message or
 * command)
 *
 * @author jurke
 */
public abstract class AbstractSXEMessageHandler implements ISXEMessageHandler {

  private final List<MessageListener> messageListeners = new ArrayList<MessageListener>();
  private final List<NotificationListener> notifyListeners = new ArrayList<NotificationListener>();

  private final HashSet<ChildRecordChangeCache> recordsToNotify =
      new HashSet<ChildRecordChangeCache>();

  @Override
  public void addMessageListener(MessageListener listener) {
    messageListeners.add(listener);
  }

  @Override
  public void removeMessageListener(MessageListener listener) {
    messageListeners.remove(listener);
  }

  @Override
  public void addNotificationListener(NotificationListener listener) {
    notifyListeners.add(listener);
  }

  @Override
  public void removeNotificationListener(NotificationListener listener) {
    notifyListeners.remove(listener);
  }

  protected void fireMessageSent(SXEMessage message) {
    for (MessageListener l : messageListeners) {
      l.sxeMessageSent(message);
    }
  }

  protected void fireRecordMessageApplied(SXEMessage message) {
    for (MessageListener l : messageListeners) {
      l.sxeRecordMessageApplied(message);
    }
  }

  protected void fireStateMessageApplied(SXEMessage message, ElementRecord root) {
    for (MessageListener l : messageListeners) {
      l.sxeStateMessageApplied(message, root);
    }
  }

  /*
   * "private" as it is critical to call fireAfterNotification() if
   * fireBeforeNotification() was called. Should not be accessed outside of
   * notifyLocalListeners().
   */
  private void fireBeforeNotification() {
    for (NotificationListener l : notifyListeners) {
      l.beforeNotification();
    }
  }

  private void fireAfterNotification() {
    for (NotificationListener l : notifyListeners) {
      l.afterNotificaion();
    }
  }

  public void addChildRecordChange(ChildRecordChangeCache changeSupport) {
    recordsToNotify.add(changeSupport);
  }

  /**
   * Will notify all listeners of all ChildRecordChangeCache that were cached.</br>
   *
   * <p>Records are responsible to properly inform this handler about executed changes.
   */
  public final void notifyLocalListeners() {
    fireBeforeNotification();
    try {
      for (ChildRecordChangeCache c : recordsToNotify) c.notifyListeners();
    } finally {
      recordsToNotify.clear();
      fireAfterNotification();
    }
  }
}
