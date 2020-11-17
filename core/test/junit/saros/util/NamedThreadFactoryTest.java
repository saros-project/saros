package saros.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NamedThreadFactoryTest {

  private final Runnable dummy =
      new Runnable() {
        @Override
        public void run() {
          // NOP
        }
      };

  @Test(expected = NullPointerException.class)
  public void testNullName() {
    new NamedThreadFactory(null);
  }

  @Test
  public void testEmptyName() {
    NamedThreadFactory factory = new NamedThreadFactory("");

    Thread t1 = factory.newThread(dummy);
    Thread t2 = factory.newThread(dummy);

    assertEquals(ThreadUtils.THREAD_PREFIX + "0", t1.getName());
    assertEquals(ThreadUtils.THREAD_PREFIX + "1", t2.getName());
  }

  @Test
  public void testName() {
    String name = "anyname";
    NamedThreadFactory factory = new NamedThreadFactory(name);

    Thread t1 = factory.newThread(dummy);
    Thread t2 = factory.newThread(dummy);

    assertEquals(ThreadUtils.THREAD_PREFIX + name + "0", t1.getName());
    assertEquals(ThreadUtils.THREAD_PREFIX + name + "1", t2.getName());
  }
}
