package de.fu_berlin.inf.dpp.net.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

/**
 * A connection pool with the ability to close all pooled connections when the pool is closed. After
 * construction the pool must be opened first.
 */
final class ConnectionPool {

  private static final Logger LOG = Logger.getLogger(ConnectionPool.class);

  private boolean isOpen;

  private final Map<String, IByteStreamConnection> pool =
      new HashMap<String, IByteStreamConnection>();

  /**
   * Opens the connection pool. After the connection pool is opened connections can be added,
   * removed or retrieved.
   */
  public synchronized void open() {
    isOpen = true;
  }

  /**
   * Closes the connection pool. All connection that currently exists in the pool will be closed.
   */
  public void close() {

    final Map<String, IByteStreamConnection> currentPoolCopy;

    synchronized (this) {
      if (!isOpen) return;

      isOpen = false;
      currentPoolCopy = new HashMap<String, IByteStreamConnection>(pool);
      pool.clear();
    }

    for (Entry<String, IByteStreamConnection> entry : currentPoolCopy.entrySet()) {
      final String id = entry.getKey();
      final IByteStreamConnection connection = entry.getValue();
      connection.close();

      LOG.debug("closed connection [id=" + id + "]: " + connection);
    }
  }

  /**
   * Returns the connection with the given id.
   *
   * @param id id of the connection
   * @return the connection associated with the id or <code>null</code> if no such connection exists
   *     or the pool is closed
   */
  public synchronized IByteStreamConnection get(final String id) {
    return pool.get(id);
  }

  /**
   * Adds the connection with the given id to the pool.
   *
   * @param id id of the connection
   * @param connection the connection to add
   * @return either the original connection if the pool is <b>closed</b> or the previous connection
   *     that was already added with the given id or <code>null</code> if no connection was added
   *     with the given id
   */
  public synchronized IByteStreamConnection add(
      final String id, final IByteStreamConnection connection) {

    if (!isOpen) return connection;

    return pool.put(id, connection);
  }

  /**
   * Removes the connection with the given id from the pool.
   *
   * @param id id of the connection
   * @return the connection associated with the id or <code>null</code> if no such connection exists
   *     or the pool is closed
   */
  public synchronized IByteStreamConnection remove(final String id) {
    if (!isOpen) return null;

    return pool.remove(id);
  }
}
