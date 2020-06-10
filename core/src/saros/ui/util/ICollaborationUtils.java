package saros.ui.util;

import java.util.List;
import java.util.Set;
import saros.filesystem.IReferencePoint;
import saros.net.xmpp.JID;
import saros.session.ISarosSession;

/** Offers convenient methods for collaboration actions like sharing a reference point resources. */
public interface ICollaborationUtils {

  /**
   * Starts a new session and shares the given reference points with given contacts.<br>
   * Does nothing if a {@link ISarosSession session} is already running.
   *
   * @param referencePoints reference points to share
   * @param contacts which should be invited
   * @nonBlocking
   */
  public void startSession(Set<IReferencePoint> referencePoints, List<JID> contacts);

  /**
   * Leaves the currently running {@link ISarosSession}<br>
   * Does nothing if no {@link ISarosSession} is running.
   */
  public void leaveSession();

  /**
   * Adds the given reference points to the session.<br>
   * Does nothing if no {@link ISarosSession session} is running.
   *
   * @param referencePoints the reference points to add to the session
   * @nonBlocking
   */
  public void addReferencePointsToSession(Set<IReferencePoint> referencePoints);

  /**
   * Adds the given contacts to the session.<br>
   * Does nothing if no {@link ISarosSession session} is running.
   *
   * @param contacts which should be added to the session.
   * @nonBlocking
   */
  public void addContactsToSession(final List<JID> contacts);
}
