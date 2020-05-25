package saros.concurrent.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import saros.activities.ChecksumActivity;
import saros.activities.JupiterActivity;
import saros.concurrent.jupiter.TransformationException;
import saros.concurrent.jupiter.internal.JupiterDocumentServer;
import saros.filesystem.IFile;
import saros.session.ISarosSession;
import saros.session.User;

/**
 * A JupiterServer manages Jupiter server instances for a number of users AND number of files.
 *
 * <p>(in contrast to a JupiterDocumentServer which only handles a single file)
 */
public class JupiterServer {

  /**
   * Jupiter server instance documents
   *
   * @host
   */
  private final HashMap<IFile, JupiterDocumentServer> concurrentDocuments = new HashMap<>();

  private final Set<User> currentClients = new HashSet<User>();

  private final ISarosSession sarosSession;

  public JupiterServer(final ISarosSession sarosSession) {
    this.sarosSession = sarosSession;
  }

  public synchronized void removeFile(final IFile file) {
    concurrentDocuments.remove(file);
  }

  public synchronized void addUser(final User user) {
    currentClients.add(user);

    for (final JupiterDocumentServer server : concurrentDocuments.values())
      server.addProxyClient(user);
  }

  public synchronized void removeUser(final User user) {
    currentClients.remove(user);

    for (final JupiterDocumentServer server : concurrentDocuments.values()) {
      server.removeProxyClient(user);
    }
  }

  /**
   * Retrieves the JupiterDocumentServer for a given file. If no JupiterDocumentServer exists for
   * this file, a new one is created and returned afterwards.
   *
   * @host
   */
  private synchronized JupiterDocumentServer getServer(final IFile file) {

    JupiterDocumentServer docServer = concurrentDocuments.get(file);

    if (docServer == null) {

      docServer = new JupiterDocumentServer(file);

      for (final User client : currentClients) {
        /*
         * Make sure that we only add clients that already have the
         * resources in question. Other clients that haven't accepted
         * the Project yet will be added later.
         */
        if (sarosSession.userHasProject(client, file.getReferencePoint())) {
          docServer.addProxyClient(client);
        }
      }

      docServer.addProxyClient(sarosSession.getHost());

      concurrentDocuments.put(file, docServer);
    }
    return docServer;
  }

  public synchronized void reset(final IFile file, final User user) {
    getServer(file).reset(user);
  }

  public synchronized Map<User, JupiterActivity> transform(final JupiterActivity activity)
      throws TransformationException {

    final JupiterDocumentServer docServer = getServer(activity.getResource());

    return docServer.transformJupiterActivity(activity);
  }

  public synchronized Map<User, ChecksumActivity> withTimestamp(final ChecksumActivity activity)
      throws TransformationException {

    final JupiterDocumentServer docServer = getServer(activity.getResource());

    return docServer.withTimestamp(activity);
  }
}
