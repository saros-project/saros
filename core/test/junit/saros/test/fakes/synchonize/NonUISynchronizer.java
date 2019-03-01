package saros.test.fakes.synchonize;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.picocontainer.Disposable;
import org.picocontainer.Startable;
import saros.synchronize.UISynchronizer;

public class NonUISynchronizer implements UISynchronizer, Startable, Disposable {

  private ExecutorService excecutor;

  private Thread fakedGUIThread;

  @Override
  public synchronized void start() {
    if (excecutor != null) return;

    excecutor =
        Executors.newSingleThreadExecutor(
            new ThreadFactory() {

              @Override
              public Thread newThread(Runnable runnable) {
                fakedGUIThread = new Thread(runnable);
                fakedGUIThread.setName("GUI-THREAD");
                return fakedGUIThread;
              }
            });

    Future<?> execution =
        excecutor.submit(
            new Runnable() {

              @Override
              public void run() {
                // NOP ensure executor is running and created the thread
              }
            });

    try {
      execution.get(10000, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized void stop() {
    dispose();
  }

  @Override
  public synchronized void dispose() {

    if (fakedGUIThread != null) fakedGUIThread.interrupt();

    if (excecutor != null) excecutor.shutdownNow();

    excecutor = null;
    fakedGUIThread = null;
  }

  @Override
  public synchronized void asyncExec(Runnable runnable) {
    if (excecutor == null) throw new IllegalStateException("synchronizer is not running");

    excecutor.submit(runnable);
  }

  @Override
  public void syncExec(Runnable runnable) {
    Future<?> execution = null;

    synchronized (this) {
      if (excecutor == null) throw new IllegalStateException("synchronizer is not running");

      if (Thread.currentThread() == fakedGUIThread) {
        runnable.run();
        return;
      }
      execution = excecutor.submit(runnable);
    }

    try {
      execution.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      dispose();
    } finally {
      super.finalize();
    }
  }

  @Override
  public synchronized boolean isUIThread() {
    return Thread.currentThread() == fakedGUIThread;
  }
}
