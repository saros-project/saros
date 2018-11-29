package de.fu_berlin.inf.dpp.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class SubProgressMonitorTest {

  private static class TestMonitor extends NullProgressMonitor {

    private int worked;

    private String subTaskName;
    private String mainTaskName;

    @Override
    public void setTaskName(String name) {
      mainTaskName = name;
    }

    public String getTaskName() {
      return mainTaskName;
    }

    public String getSubTaskName() {
      return subTaskName;
    }

    @Override
    public void subTask(String name) {
      subTaskName = name;
    }

    @Override
    public void worked(int amount) {
      worked += amount;
    }

    public int getWorked() {
      return worked;
    }

    public void reset() {
      worked = 0;
      mainTaskName = null;
      subTaskName = null;
    }
  }

  private TestMonitor consumer;

  @Before
  public void setup() {
    consumer = new TestMonitor();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValueAsParentMonitor() {
    new SubProgressMonitor(null, 0);
  }

  @Test
  public void testConsumeOnDoneWithoutCallingWorked() {

    SubProgressMonitor monitor = new SubProgressMonitor(consumer, 100);

    monitor.beginTask("foo", 300);
    monitor.done();

    assertEquals(100, consumer.getWorked());
  }

  @Test
  public void testWorkedScalingLargeInterval() {

    SubProgressMonitor monitor = new SubProgressMonitor(consumer, 100);

    monitor.beginTask("foo", 400);
    monitor.worked(100);
    assertEquals(25, consumer.getWorked());

    monitor.worked(100);
    assertEquals(50, consumer.getWorked());

    monitor.worked(100);
    assertEquals(75, consumer.getWorked());

    monitor.worked(100);
    assertEquals(100, consumer.getWorked());

    /* ------------------------------------ */

    consumer.reset();

    monitor = new SubProgressMonitor(consumer, 100);

    monitor.beginTask("foo", 400);

    for (int i = 0; i < 400; i++) {
      monitor.worked(1);

      if (i == 100) assertEquals(25, consumer.getWorked());
      else if (i == 200) assertEquals(50, consumer.getWorked());
      if (i == 300) assertEquals(75, consumer.getWorked());
    }

    assertEquals(100, consumer.getWorked());
  }

  @Test
  public void testWorkedScalingSmallInterval() {

    SubProgressMonitor monitor = new SubProgressMonitor(consumer, 100);

    monitor.beginTask("foo", 400);

    for (int i = 0; i < 400; i++) {
      monitor.worked(1);

      if (i == 100) assertEquals(25, consumer.getWorked());
      else if (i == 200) assertEquals(50, consumer.getWorked());
      if (i == 300) assertEquals(75, consumer.getWorked());
    }

    assertEquals(100, consumer.getWorked());

    /* ------------------------------------ */

    consumer.reset();

    monitor = new SubProgressMonitor(consumer, 100);

    monitor.beginTask("foo", 4);
    monitor.worked(1);
    assertEquals(25, consumer.getWorked());

    monitor.worked(1);
    assertEquals(50, consumer.getWorked());

    monitor.worked(1);
    assertEquals(75, consumer.getWorked());

    monitor.worked(1);
    assertEquals(100, consumer.getWorked());
  }

  @Test
  public void testSuppressFlags() {

    final int[] supFlags = {
      SubProgressMonitor.SUPPRESS_NONE,
      SubProgressMonitor.SUPPRESS_BEGINTASK,
      SubProgressMonitor.SUPPRESS_SETTASKNAME,
      SubProgressMonitor.SUPPRESS_SUBTASK,
      SubProgressMonitor.SUPPRESS_BEGINTASK | SubProgressMonitor.SUPPRESS_SETTASKNAME,
      SubProgressMonitor.SUPPRESS_BEGINTASK | SubProgressMonitor.SUPPRESS_SUBTASK,
      SubProgressMonitor.SUPPRESS_SETTASKNAME | SubProgressMonitor.SUPPRESS_SUBTASK,
      SubProgressMonitor.SUPPRESS_BEGINTASK
          | SubProgressMonitor.SUPPRESS_SETTASKNAME
          | SubProgressMonitor.SUPPRESS_SUBTASK
    };

    for (int flags : supFlags) {
      SubProgressMonitor monitor = new SubProgressMonitor(consumer, 100, flags);

      monitor.beginTask("foo", 4711);

      if ((flags & SubProgressMonitor.SUPPRESS_BEGINTASK) == 0)
        assertEquals("foo", consumer.getTaskName());
      else assertNull(consumer.getTaskName());

      consumer.reset();

      monitor.setTaskName("bar");

      if ((flags & SubProgressMonitor.SUPPRESS_SETTASKNAME) == 0)
        assertEquals("bar", consumer.getTaskName());
      else assertNull(consumer.getTaskName());

      consumer.reset();

      monitor.subTask("foobar");

      if ((flags & SubProgressMonitor.SUPPRESS_SUBTASK) == 0)
        assertEquals("foobar", consumer.getSubTaskName());
      else assertNull(consumer.getSubTaskName());

      consumer.reset();
    }
  }
}
