package de.fu_berlin.inf.dpp.server.synchronize;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import org.apache.log4j.Logger;

/**
 * Server implementation of the {@link UISynchronizer} interface. As the server has no actual "UI
 * thread", this implementation creates a dedicated thread playing the UI thread's role instead.
 */
@Component(module = "server")
public class ServerUISynchronizerImpl implements UISynchronizer {

  private static final Logger LOG = Logger.getLogger(ServerUISynchronizerImpl.class);

  private ExecutorService executor;
  private Thread virtualUIThread;

  /** Initializes the ServerUISynchronizerImpl. */
  public ServerUISynchronizerImpl() {
    executor =
        Executors.newSingleThreadExecutor(
            new ThreadFactory() {
              @Override
              public Thread newThread(Runnable r) {
                virtualUIThread = new Thread(r, "dpp-srv-exec-context");
                return virtualUIThread;
              }
            });
  }

  @Override
  public void asyncExec(Runnable runnable) {
    executor.execute(runnable);
  }

  @Override
  public void syncExec(Runnable runnable) {
    if (Thread.currentThread() == virtualUIThread) {
      runnable.run();
      return;
    }
    try {
      Future<?> f = executor.submit(runnable);
      f.get();
    } catch (InterruptedException e) {
      LOG.warn("interrupted while waiting for runnable " + runnable + " to finish execution");
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      LOG.error("execution of runnable " + runnable + " failed", e);
    }
  }

  @Override
  public boolean isUIThread() {
    return Thread.currentThread() == virtualUIThread;
  }
}
