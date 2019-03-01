package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** @author Lindner */
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

    assertEquals("0", t1.getName());
    assertEquals("1", t2.getName());
  }

  @Test
  public void testName() {
    String name = "anyname";
    NamedThreadFactory factory = new NamedThreadFactory(name);

    Thread t1 = factory.newThread(dummy);
    Thread t2 = factory.newThread(dummy);

    assertEquals(name + "0", t1.getName());
    assertEquals(name + "1", t2.getName());
  }
}
