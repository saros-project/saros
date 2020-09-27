package saros.lsp.monitoring.remote;

import saros.activities.ProgressActivity;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.monitoring.ProgressMonitor;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.remote.IRemoteProgressIndicator;
import saros.net.util.XMPPUtils;
import saros.session.User;

/** Implementation of {@link IRemoteProgressIndicator}. */
public class LspRemoteProgressIndicator implements IRemoteProgressIndicator {

  private final ProgressMonitor progressMonitor;
  private String remoteProgressID;
  private User remoteUser;

  public LspRemoteProgressIndicator(
      final ISarosLanguageClient client, String remoteProgressID, User remoteUser) {
    this.remoteProgressID = remoteProgressID;
    this.remoteUser = remoteUser;
    this.progressMonitor = new ProgressMonitor(client);
  }

  @Override
  public String getRemoteProgressID() {
    return this.remoteProgressID;
  }

  @Override
  public User getRemoteUser() {
    return this.remoteUser;
  }

  @Override
  public void start() {
    final String nickname =
        XMPPUtils.getNickname(null, this.remoteUser.getJID(), this.remoteUser.getJID().toString());
    this.progressMonitor.beginTask(nickname, IProgressMonitor.UNKNOWN);
  }

  @Override
  public void stop() {
    this.progressMonitor.done();
  }

  @Override
  public void handleProgress(final ProgressActivity activity) {
    this.progressMonitor.setSize(activity.getWorkTotal());
    this.progressMonitor.subTask(activity.getTaskName());
    this.progressMonitor.worked(activity.getWorkCurrent());
  }
}
