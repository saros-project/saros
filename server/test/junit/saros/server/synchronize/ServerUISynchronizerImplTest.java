package saros.server.synchronize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import saros.synchronize.UISynchronizer;

public class ServerUISynchronizerImplTest {

  private UISynchronizer synchronizer;

  @Before
  public void setUp() {
    synchronizer = new ServerUISynchronizerImpl();
  }

  // This test can return false positive under heavy load if the
  // implementation is incorrect
  @Test
  public void syncExec() {
    final AtomicBoolean taskHasRun = new AtomicBoolean(false);

    synchronizer.syncExec(
        new Runnable() {
          @Override
          public void run() {
            sleep(100);
            taskHasRun.set(true);
          }
        });

    assertTrue(taskHasRun.get());
  }

  @Test
  public void asyncExec() throws Exception {
    final AtomicBoolean taskHasRun = new AtomicBoolean(false);
    final Semaphore startLock = new Semaphore(0);
    final Semaphore doneLock = new Semaphore(0);

    synchronizer.asyncExec(
        new Runnable() {
          @Override
          public void run() {
            try {
              startLock.acquire();
            } catch (InterruptedException e) {
              // Ignore
            }
            taskHasRun.set(true);
            doneLock.release();
          }
        });

    assertFalse(taskHasRun.get());
    startLock.release();
    doneLock.acquire();
    assertTrue(taskHasRun.get());
  }

  @Test
  public void isUIThread() throws Exception {
    assertFalse(synchronizer.isUIThread());

    final AtomicBoolean task1InUIThread = new AtomicBoolean();
    final AtomicBoolean task2InUIThread = new AtomicBoolean();
    final CountDownLatch doneSignal = new CountDownLatch(2);

    synchronizer.syncExec(
        new Runnable() {
          @Override
          public void run() {
            task1InUIThread.set(synchronizer.isUIThread());
            doneSignal.countDown();
          }
        });
    synchronizer.asyncExec(
        new Runnable() {
          @Override
          public void run() {
            task2InUIThread.set(synchronizer.isUIThread());
            doneSignal.countDown();
          }
        });

    doneSignal.await();
    assertFalse(synchronizer.isUIThread());
    assertTrue(task1InUIThread.get());
    assertTrue(task2InUIThread.get());
  }

  // This test can fail under heavy load
  @Test
  public void executionOrderEqualsCallOrder() throws Exception {
    final BlockingQueue<String> queue = new ArrayBlockingQueue<String>(16);

    synchronizer.asyncExec(
        new Runnable() {
          @Override
          public void run() {

            queue.add("first");

            sleep(100);

            synchronizer.asyncExec(
                new Runnable() {
                  @Override
                  public void run() {
                    queue.add("first-A");
                  }
                });

            synchronizer.syncExec(
                new Runnable() {
                  @Override
                  public void run() {
                    queue.add("first-S");
                  }
                });
          }
        });

    synchronizer.syncExec(
        new Runnable() {
          @Override
          public void run() {
            queue.add("second");
          }
        });

    sleep(200);

    synchronizer.asyncExec(
        new Runnable() {
          @Override
          public void run() {
            queue.add("third");
          }
        });

    assertEquals("first", queue.take());
    assertEquals("first-S", queue.take());
    assertEquals("second", queue.take());
    assertEquals("first-A", queue.take());
    assertEquals("third", queue.take());
  }

  private static void sleep(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      // Don't care
    }
  }
}
