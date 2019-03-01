package saros.test.fakes.synchonize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NonUISynchronizerTest {

  private NonUISynchronizer synchronizer;

  private volatile int sync = 0;

  @Before
  public void setUp() {
    synchronizer = new NonUISynchronizer();
    synchronizer.start();
  }

  @After
  public void tearDown() {
    synchronizer.stop();
  }

  @Test
  public void testIsDispatchThread() {

    final AtomicReference<Boolean> isDispatchThread = new AtomicReference<Boolean>();
    isDispatchThread.set(false);

    synchronizer.syncExec(
        new Runnable() {

          @Override
          public void run() {
            isDispatchThread.set(synchronizer.isUIThread());
          }
        });

    assertTrue("isDispatchThread method should return true", isDispatchThread.get());

    assertFalse("isDispatchThread method should return false", synchronizer.isUIThread());
  }

  @Test(timeout = 30000)
  public void testAsyncDispatching() {

    final CountDownLatch sync0 = new CountDownLatch(1);
    final CountDownLatch sync1 = new CountDownLatch(1);

    synchronizer.asyncExec(
        new Runnable() {

          @Override
          public void run() {
            try {
              sync0.await();
              sync1.countDown();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
        });

    sync0.countDown();

    try {
      sync1.await(20000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertEquals("runnables were not executed sync", 0, sync1.getCount());
  }

  /*
   * this test is a little bit sloppy, as it may return wrong results but if
   * this test fails it indicates that the implementation is broken
   */
  @Test(timeout = 60000)
  public void testSyncDispatching() {

    final CountDownLatch sync0 = new CountDownLatch(1);
    final CountDownLatch sync1 = new CountDownLatch(1);

    synchronizer.syncExec(
        new Runnable() {

          @Override
          public void run() {
            sync |= 1;
            sync0.countDown();
          }
        });

    synchronizer.syncExec(
        new Runnable() {

          @Override
          public void run() {
            sync |= 2;
            sync1.countDown();
          }
        });

    sync = 0;

    try {
      sync0.await(20000, TimeUnit.MILLISECONDS);
      sync1.await(20000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertTrue("runnables were not executed sync", 0 == sync);
  }
}
