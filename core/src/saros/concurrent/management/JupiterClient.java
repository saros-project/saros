package saros.concurrent.management;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import saros.activities.ChecksumActivity;
import saros.activities.JupiterActivity;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.TransformationException;
import saros.concurrent.jupiter.internal.Jupiter;
import saros.filesystem.IFile;
import saros.session.ISarosSession;

/** A JupiterClient manages Jupiter client docs for a single user with several paths */
public class JupiterClient {

  protected ISarosSession sarosSession;

  public JupiterClient(ISarosSession sarosSession) {
    this.sarosSession = sarosSession;
  }

  /**
   * Jupiter instances for each local editor.
   *
   * @host and @client
   *     <p>Note: This needs to be a *Concurrent*HashMap, because it is periodically iterated by the
   *     HeartbeatDispatcher class.
   */
  private final ConcurrentHashMap<IFile, Jupiter> clientDocs = new ConcurrentHashMap<>();

  /** @host and @client */
  protected synchronized Jupiter get(IFile file) {
    return clientDocs.computeIfAbsent(file, (key) -> new Jupiter(true));
  }

  public synchronized Operation receive(JupiterActivity jupiterActivity)
      throws TransformationException {
    return get(jupiterActivity.getResource()).receiveJupiterActivity(jupiterActivity);
  }

  public synchronized boolean isCurrent(ChecksumActivity checksumActivity)
      throws TransformationException {

    return get(checksumActivity.getResource()).isCurrent(checksumActivity.getTimestamp());
  }

  public synchronized void reset(IFile file) {
    this.clientDocs.remove(file);
  }

  public synchronized void reset() {
    this.clientDocs.clear();
  }

  public synchronized JupiterActivity generate(TextEditActivity textEdit) {

    IFile file = textEdit.getResource();

    return get(file)
        .generateJupiterActivity(
            textEdit.toOperation(), sarosSession.getLocalUser(), new SPath(file));
  }

  /**
   * Given a checksum, this method will return a new ChecksumActivity with the timestamp set to the
   * VectorTime of the Jupiter algorithm used for managing the document addressed by the checksum.
   */
  public synchronized ChecksumActivity withTimestamp(ChecksumActivity checksumActivity) {

    return get(checksumActivity.getResource()).withTimestamp(checksumActivity);
  }

  // Package-private function for the HeartbeatDispatcher
  Map<IFile, Jupiter> getClientDocs() {
    return Collections.unmodifiableMap(clientDocs);
  }
}
