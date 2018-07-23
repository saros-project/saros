package de.fu_berlin.inf.dpp.concurrent.management;

import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * A JupiterServer manages Jupiter server instances for a number of users AND number of paths.
 *
 * <p>(in contrast to a JupiterDocumentServer which only handles a single path)
 *
 * <p>Call {@link #addUser(User, Set)} with a user specific Set of resources unavailable for
 * processing of Jupiter Activities, enable processing with {@link #setResourceAvailable(User,
 * SPath)}.
 *
 * @host
 */
public class JupiterServer {
  private static final Logger log = Logger.getLogger(JupiterServer.class);

  /** Jupiter server instance documents */
  private final HashMap<SPath, JupiterDocumentServer> concurrentDocuments = new HashMap<>();

  /** current users and resources for which a user can not process Jupiter actions */
  private final HashMap<User, Set<SPath>> currentClientsAndUnavailableResources = new HashMap<>();

  private final ISarosSession sarosSession;

  protected JupiterServer(ISarosSession sarosSession) {
    this.sarosSession = sarosSession;
  }

  protected synchronized void removePath(SPath path) {
    concurrentDocuments.remove(path);
  }

  protected synchronized void addUser(User user, Set<SPath> resourcesUnavailable) {
    Set<SPath> resources = resourcesUnavailable;
    if (resourcesUnavailable == null) resources = Collections.emptySet();

    if (currentClientsAndUnavailableResources.containsKey(user)) {
      currentClientsAndUnavailableResources.get(user).addAll(resources);
    } else {
      currentClientsAndUnavailableResources.put(user, new HashSet<SPath>(resources));
    }

    concurrentDocuments.forEach(
        (resource, server) -> {
          if (!currentClientsAndUnavailableResources.get(user).contains(resource)) {
            server.addProxyClient(user);
          }
        });
  }

  protected synchronized void removeUser(final User user) {
    currentClientsAndUnavailableResources.remove(user);

    for (final JupiterDocumentServer server : concurrentDocuments.values()) {
      server.removeProxyClient(user);
    }
  }

  /**
   * Set a user resource available for activity processing and add to existing Jupiter documents.
   *
   * @host
   * @param user
   * @param resource
   */
  protected synchronized void setResourceAvailable(final User user, SPath resource) {
    if (!currentClientsAndUnavailableResources.containsKey(user)) {
      log.warn("User <" + user + "> is not registered!");
      return;
    }
    if (!currentClientsAndUnavailableResources.get(user).contains(resource)) {
      log.warn("User <" + user + "> has no unavailable resource <" + resource + "> registered!");
      return;
    }

    currentClientsAndUnavailableResources.get(user).remove(resource);

    if (concurrentDocuments.containsKey(resource)) {
      concurrentDocuments.get(resource).addProxyClient(user);
    }
  }

  /**
   * Retrieves the JupiterDocumentServer for a given path. If no JupiterDocumentServer exists for
   * this path, a new one is created and returned afterwards.
   *
   * @host
   */
  /*
   * TODO This solution currently just works for partial sharing by
   * coincidence. To really fix the issue we have to expand the
   * SarosSessionMapper to also track the resources and not just the projects
   * that are already shared for every user individually.
   */
  private synchronized JupiterDocumentServer getServer(final SPath path) {
    if (concurrentDocuments.containsKey(path)) {
      return concurrentDocuments.get(path);
    }

    final JupiterDocumentServer docServer = new JupiterDocumentServer(path);

    currentClientsAndUnavailableResources.forEach(
        (client, unavailableResources) -> {
          /*
           * Make sure that we only add clients that already have the resource
           * in question available. Other clients that haven't accepted the
           * Project yet will be added later.
           */
          if (sarosSession.userHasProject(client, path.getProject())
              && !unavailableResources.contains(path)) {
            docServer.addProxyClient(client);
          }
        });

    docServer.addProxyClient(sarosSession.getHost());

    concurrentDocuments.put(path, docServer);
    return docServer;
  }

  protected synchronized void reset(final SPath path, final User user) {
    if (currentClientsAndUnavailableResources.get(user).contains(path)) {
      getServer(path).removeProxyClient(user);
    } else {
      getServer(path).reset(user);
    }
  }

  protected synchronized Map<User, JupiterActivity> transform(final JupiterActivity activity)
      throws TransformationException {
    final JupiterDocumentServer docServer = getServer(activity.getPath());

    return docServer.transformJupiterActivity(activity);
  }

  protected synchronized Map<User, ChecksumActivity> withTimestamp(final ChecksumActivity activity)
      throws TransformationException {
    final JupiterDocumentServer docServer = getServer(activity.getPath());

    return docServer.withTimestamp(activity);
  }
}
