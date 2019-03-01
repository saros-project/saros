package saros.concurrent.jupiter.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import saros.activities.ChecksumActivity;
import saros.activities.JupiterActivity;
import saros.activities.SPath;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.Timestamp;
import saros.concurrent.jupiter.TransformationException;
import saros.session.User;

/**
 * The JupiterDocumentServer is the host side component managing all server Jupiter instances.
 *
 * <p>TODO [CO] Document and review this class
 */
public class JupiterDocumentServer {

  private static final Logger LOG = Logger.getLogger(JupiterDocumentServer.class);

  /** List of proxy clients. */
  private final HashMap<User, Jupiter> proxies = new HashMap<User, Jupiter>();

  private final SPath editor;

  /**
   * Create a new JupiterDocument (server-side) representing the document identified by the given
   * SPath
   */
  public JupiterDocumentServer(SPath path) {
    this.editor = path;
  }

  public synchronized void addProxyClient(final User user) {
    if (!proxies.containsKey(user)) proxies.put(user, new Jupiter(false));
  }

  public synchronized boolean removeProxyClient(final User user) {
    return proxies.remove(user) != null;
  }

  // FIXME why is this not synchronized ?!
  public Map<User, JupiterActivity> transformJupiterActivity(final JupiterActivity activity)
      throws TransformationException {

    final Map<User, JupiterActivity> result = new HashMap<User, JupiterActivity>();

    final User source = activity.getSource();

    // 1. Use JupiterClient of sender to transform JupiterActivity
    final Jupiter sourceProxy = proxies.get(source);

    /*
     * TODO maybe just silently add a proxy ? currently the project is
     * registered before decompression so it is possible to start working on
     * the files during this phase and this is why this can return null
     */

    if (sourceProxy == null)
      throw new IllegalStateException("no proxy client registered for user: " + source);

    final Operation op = sourceProxy.receiveJupiterActivity(activity);

    // 2. Generate outgoing JupiterActivities for all other clients and the
    // host
    for (final Entry<User, Jupiter> entry : proxies.entrySet()) {

      final User user = entry.getKey();

      // Skip sender
      if (user.equals(source)) continue;

      final Jupiter remoteProxy = entry.getValue();

      final JupiterActivity transformed = remoteProxy.generateJupiterActivity(op, source, editor);

      result.put(user, transformed);
    }

    return result;
  }

  public synchronized void updateVectorTime(final User source, final User dest) {
    final Jupiter proxy = proxies.get(source);

    if (proxy == null) {
      LOG.error("no proxy found for user: " + source);
      return;
    }

    try {
      Timestamp ts = proxy.getTimestamp();
      proxies
          .get(dest)
          .updateVectorTime(new JupiterVectorTime(ts.getComponents()[1], ts.getComponents()[0]));
    } catch (TransformationException e) {
      LOG.error("error during update vector time for user: " + dest, e);
    }
  }

  public synchronized void reset(final User user) {
    if (removeProxyClient(user)) addProxyClient(user);
  }

  public Map<User, ChecksumActivity> withTimestamp(final ChecksumActivity activity)
      throws TransformationException {

    final Map<User, ChecksumActivity> result = new HashMap<User, ChecksumActivity>();

    final User source = activity.getSource();

    // 1. Verify that this checksum can still be sent to others...
    final Jupiter sourceProxy = proxies.get(source);

    final boolean isCurrent = sourceProxy.isCurrent(activity.getTimestamp());

    if (!isCurrent) return result; // Checksum is no longer valid => discard

    // 2. Put timestamp into all resulting checksums
    for (final Entry<User, Jupiter> entry : proxies.entrySet()) {

      final User user = entry.getKey();

      // Skip sender
      if (user.equals(source)) continue;

      final Jupiter remoteProxy = entry.getValue();

      ChecksumActivity timestamped = activity.withTimestamp(remoteProxy.getTimestamp());

      result.put(user, timestamped);
    }

    return result;
  }
}
