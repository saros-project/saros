package saros.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Thread Factory which assigns a given name + consecutive number to created threads if desired. */
public final class NamedThreadFactory implements ThreadFactory {

  private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

  private final String name;
  private final AtomicInteger suffixCounter;

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

    this.name = ThreadUtils.THREAD_PREFIX + name;
    suffixCounter = suffix ? new AtomicInteger() : null;
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread result = defaultFactory.newThread(r);

    if (suffixCounter == null) {
      result.setName(name);
    } else {
      result.setName(name + suffixCounter.getAndIncrement());
    }

    return result;
  }
}
