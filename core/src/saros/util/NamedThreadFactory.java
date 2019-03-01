package saros.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/** Thread Factory which assigns a given name + consecutive number to created threads if desired. */
public final class NamedThreadFactory implements ThreadFactory {

  private ThreadFactory defaultFactory = Executors.defaultThreadFactory();

  private int count = 0;

  private final String name;
  private final boolean suffix;

  /**
   * Creates a new {@link ThreadFactory} that will assign created threads the given name including a
   * number suffix for each newly created thread.
   *
   * @param name the name to assign to new threads
   * @throws NullPointerException if name is <code>null</code>
   */
  public NamedThreadFactory(String name) {
    this(name, true);
  }

  /**
   * Creates a new {@link ThreadFactory} that will assign created threads the given name including a
   * number suffix for each newly created thread.
   *
   * @param name the name to assign to new threads
   * @param suffix if <code>true</code> the name will be suffixed by a number
   * @throws NullPointerException if name is <code>null</code>
   */
  public NamedThreadFactory(String name, boolean suffix) {
    if (name == null) throw new NullPointerException("name is null");

    this.name = name;
    this.suffix = suffix;
  }

  @Override
  public Thread newThread(Runnable r) {

    Thread result = defaultFactory.newThread(r);

    String threadName = name;

    synchronized (this) {
      if (suffix) threadName = threadName.concat(String.valueOf(count++));
    }

    result.setName(threadName);

    return result;
  }
}
