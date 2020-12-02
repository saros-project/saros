package saros.lsp.ui;

import java.util.concurrent.Executors;
import saros.synchronize.UISynchronizer;

/** Implementation of {@link UISynchronizer}. */
public class UISynchronizerImpl implements UISynchronizer {

  @Override
  public void asyncExec(Runnable runnable) {

    Executors.newCachedThreadPool()
        .submit(
            () -> {
              runnable.run();

              return null;
            });
  }

  @Override
  public void syncExec(Runnable runnable) {

    runnable.run();
  }

  @Override
  public boolean isUIThread() {
    return false;
  }
}
