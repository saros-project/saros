package saros.lsp.filesystem;

import java.io.IOException;
import saros.exceptions.OperationCanceledException;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.filesystem.IWorkspaceRunnable;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.monitoring.ProgressMonitor;

/** Saros language server implementation of {@link IWorkspace}. */
public class LspWorkspace implements IWorkspace {

  private ISarosLanguageClient client;

  public LspWorkspace(ISarosLanguageClient client) {
    this.client = client;
  }

  @Override
  public void run(IWorkspaceRunnable runnable) throws IOException, OperationCanceledException {

    run(runnable, null);
  }

  @Override
  public void run(IWorkspaceRunnable runnable, IResource[] resources)
      throws IOException, OperationCanceledException {

    synchronized (this) {
      runnable.run(new ProgressMonitor(this.client));
    }
  }
}
