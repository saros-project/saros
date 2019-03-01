package saros.concurrent.management;

import java.util.HashMap;
import saros.activities.ChecksumActivity;
import saros.activities.JupiterActivity;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.TransformationException;
import saros.concurrent.jupiter.internal.Jupiter;
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
   */
  protected final HashMap<SPath, Jupiter> clientDocs = new HashMap<SPath, Jupiter>();

  /** @host and @client */
  protected synchronized Jupiter get(SPath path) {

    Jupiter clientDoc = this.clientDocs.get(path);
    if (clientDoc == null) {
      clientDoc = new Jupiter(true);
      this.clientDocs.put(path, clientDoc);
    }
    return clientDoc;
  }

  public synchronized Operation receive(JupiterActivity jupiterActivity)
      throws TransformationException {
    return get(jupiterActivity.getPath()).receiveJupiterActivity(jupiterActivity);
  }

  public synchronized boolean isCurrent(ChecksumActivity checksumActivity)
      throws TransformationException {

    return get(checksumActivity.getPath()).isCurrent(checksumActivity.getTimestamp());
  }

  public synchronized void reset(SPath path) {
    this.clientDocs.remove(path);
  }

  public synchronized void reset() {
    this.clientDocs.clear();
  }

  public synchronized JupiterActivity generate(TextEditActivity textEdit) {

    SPath path = textEdit.getPath();
    return get(path)
        .generateJupiterActivity(textEdit.toOperation(), sarosSession.getLocalUser(), path);
  }

  /**
   * Given a checksum, this method will return a new ChecksumActivity with the timestamp set to the
   * VectorTime of the Jupiter algorithm used for managing the document addressed by the checksum.
   */
  public synchronized ChecksumActivity withTimestamp(ChecksumActivity checksumActivity) {

    return get(checksumActivity.getPath()).withTimestamp(checksumActivity);
  }
}
