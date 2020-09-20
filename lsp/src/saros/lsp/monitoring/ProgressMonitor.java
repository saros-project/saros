package saros.lsp.monitoring;

import java.util.UUID;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.client.dto.ProgressParams;
import saros.lsp.extensions.client.dto.WorkDoneProgressBegin;
import saros.lsp.extensions.client.dto.WorkDoneProgressCreateParams;
import saros.lsp.extensions.client.dto.WorkDoneProgressEnd;
import saros.lsp.extensions.client.dto.WorkDoneProgressReport;
import saros.monitoring.IProgressMonitor;

/** Implementation of {@link IProgressMonitor}. */
public class ProgressMonitor implements IProgressMonitor {

  private ISarosLanguageClient client;

  private String token;

  private String taskName;

  private String subTask;

  private boolean canceled;

  private int size;

  public ProgressMonitor(ISarosLanguageClient client) {
    this.client = client;
    this.token = UUID.randomUUID().toString();
  }

  @Override
  public void done() {
    this.endProgress("Done");
  }

  @Override
  public void subTask(String name) {

    this.subTask = name;
  }

  @Override
  public void setTaskName(String name) {

    this.taskName = name;
  }

  @Override
  public void worked(int amount) {

    if (this.canceled) {
      throw new UnsupportedOperationException();
    }

    this.reportProgress(amount);
  }

  @Override
  public void setCanceled(boolean canceled) {

    if (this.canceled && !canceled) {
      throw new UnsupportedOperationException();
    }

    if (canceled) {
      this.endProgress("Cancelled");
    }

    this.canceled = canceled;
  }

  @Override
  public boolean isCanceled() {
    return this.canceled;
  }

  @Override
  public void beginTask(String name, int size) {

    if (this.canceled) {
      throw new UnsupportedOperationException();
    }

    this.setSize(size);

    this.setTaskName(name);
    this.createProgress();
    this.beginProgress(this.taskName);
  }

  public void setSize(int size) {
    this.size = size;
  }

  private void createProgress() {
    WorkDoneProgressCreateParams c = new WorkDoneProgressCreateParams(this.token);

    this.client.createProgress(c);
  }

  private void beginProgress(String title) {

    ProgressParams<WorkDoneProgressBegin> p =
        new ProgressParams<WorkDoneProgressBegin>(
            this.token, new WorkDoneProgressBegin(title, null, 0, false));

    this.client.progress(p);
  }

  private void reportProgress(int amount) {

    ProgressParams<WorkDoneProgressReport> p =
        new ProgressParams<WorkDoneProgressReport>(
            this.token,
            new WorkDoneProgressReport(
                this.subTask, (int) Math.round(amount / (double) this.size * 100), false));

    this.client.progress(p);
  }

  private void endProgress(String message) {

    ProgressParams<WorkDoneProgressEnd> p =
        new ProgressParams<WorkDoneProgressEnd>(this.token, new WorkDoneProgressEnd(message));

    this.client.progress(p);
  }
}
