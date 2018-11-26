package de.fu_berlin.inf.dpp.synchronize;

import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.StackTrace;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Logger;

/**
 * The StartHandle will be created and returned by the StopManager. This handle can be used exactly
 * once to signal the StopManager to un-stop (start) the remote user again.
 */
public class StartHandle {

  private static final Logger log = Logger.getLogger(StartHandle.class);

  protected StopManager stopManager;

  protected User user;

  protected String id;

  /** Each start handle may be only started once. This boolean guards this. */
  protected AtomicBoolean startCalled = new AtomicBoolean(false);

  /** Each start handle may be acknowledged once to have been started. */
  protected AtomicBoolean acknowledged = new AtomicBoolean(false);

  StartHandle(User user, StopManager stopManager, String id) {
    this.user = user;
    this.stopManager = stopManager;
    this.id = id;
  }

  /**
   * Notifies the StopManager, that the operation for which this StartHandle
   * was returned by a call to stop has finished.
   *
   * This method will return immediately and return true, if the stopped user
   * continued working or false if other StartHandles exist for this user,
   * which have not been called.
   *
   * @nonblocking
   *
   * @Throws IllegalStateException if start() is called twice on the same
   *         handle.
   *
   * @return <code>true</true> if this was the last StartHandle for the user.
   */
  public boolean start() {

    log.debug("Called start on " + user);

    if (!startCalled.compareAndSet(false, true))
      throw new IllegalStateException("start can only be called once per StartHandle");

    return stopManager.resumeStartHandle(this);
  }

  /**
   * @JTourBusStop 4, StopManager:
   *
   * <p>When a remote user has been stopped by StopManager#stop() a StartHandle will be returned.
   * Using the StartHandle#startAndAwait() method on the handle one can allow a remote user to make
   * modifications again.
   */

  /**
   * Notifies the StopManager, that the operation for which this StartHandle was returned by a call
   * to stop has finished.
   *
   * @return <code>true</code>, if the stopped user continued working or false if other StartHandles
   *     exist for this user, which have not been called.
   * @blocking waits until the blocked user acknowledged the start
   * @throws IllegalStateException if start() is called twice on the same handle.
   */
  public boolean startAndAwait() {

    log.debug("Called startAndAwait on " + user);

    if (!startCalled.compareAndSet(false, true))
      throw new IllegalStateException("start can only be called once per StartHandle");

    if (stopManager.resumeStartHandle(this)) return await();

    return false;
  }

  /**
   * Waits until the StartHandle is acknowledged or the waiting timed out.
   *
   * <p>Returns whether the handle was acknowledged or not
   */
  public boolean await() {

    long timeoutToExceed = System.currentTimeMillis() + StopManager.TIMEOUT;

    try {
      while (!acknowledged.get()
          && (System.currentTimeMillis() < timeoutToExceed)
          && user.isInSession()) Thread.sleep(stopManager.MILLISTOWAIT);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Code not designed to be interruptible", e);
    }

    return acknowledged.get();
  }

  /**
   * Get the stopped User.
   *
   * @return the stopped User
   */
  public User getUser() {
    return user;
  }

  /** @return the internal handle id */
  String getHandleID() {
    return id;
  }

  /** Remember that the stopped user has acknowledged this handle. */
  void acknowledge() {
    if (!acknowledged.compareAndSet(false, true)) {
      log.warn("Acknowledge should only be called once per handle", new StackTrace());
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((user == null) ? 0 : user.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    StartHandle other = (StartHandle) obj;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    if (user == null) {
      if (other.user != null) return false;
    } else if (!user.equals(other.user)) return false;
    return true;
  }

  @Override
  public String toString() {
    String out = "StartHandle (" + user + ", " + id + ", ";
    if (startCalled.get()) out += "not ";
    return out + "startable)";
  }
}
