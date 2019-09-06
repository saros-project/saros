package saros.util;

import org.apache.log4j.Logger;

public class ThreadUtils {

  static final String THREAD_PREFIX = "saros-";

  private static final Logger defaultLog = Logger.getLogger(ThreadUtils.class);

  private ThreadUtils() {
    // NOP do not allow object creation
  }

  /**
   * Return a new Runnable which runs the given runnable but catches all RuntimeExceptions and logs
   * them to the given log.
   *
   * <p>Errors are logged and re-thrown.
   *
   * <p>This method does NOT actually run the given runnable, but only wraps it.
   *
   * @param log The log to print any exception messages thrown which occur when running the given
   *     runnable or <code>null</code>
   */
  public static Runnable wrapSafe(Logger log, final Runnable runnable) {
    Logger logToUse = log == null ? defaultLog : log;
    StackTrace stackTrace = new StackTrace();

    return new Runnable() {
      @Override
      public void run() {
        try {
          runnable.run();
        } catch (Exception e) {
          logToUse.error("Internal Error:", e);
          logToUse.error("Original caller:", stackTrace);
        } catch (Error e) {
          logToUse.error("Internal Fatal Error:", e);
          logToUse.error("Original caller:", stackTrace);
          // Re-throw errors (such as an OutOfMemoryError)
          throw e;
        }
      }
    };
  }

  /**
   * Run the given runnable in a new thread (with the given name) and log any RuntimeExceptions to
   * the given log.
   *
   * @return The Thread which has been created and started to run the given runnable
   * @nonBlocking
   */
  public static Thread runSafeAsync(String name, final Logger log, final Runnable runnable) {
    Thread t = new Thread(wrapSafe(log, runnable));

    String threadName = name == null ? t.getName() : name;
    t.setName(THREAD_PREFIX + threadName);

    t.start();
    return t;
  }

  /**
   * Run the given runnable in a new thread and log any RuntimeExceptions to the given log.
   *
   * @return The Thread which has been created and started to run the given runnable.
   * @nonBlocking
   */
  public static Thread runSafeAsync(final Logger log, final Runnable runnable) {
    return runSafeAsync(null, log, runnable);
  }

  /**
   * Run the given runnable (in the current thread!) and log any RuntimeExceptions to the given log
   * and block until the runnable returns.
   *
   * @blocking
   */
  public static void runSafeSync(Logger log, Runnable runnable) {
    wrapSafe(log, runnable).run();
  }
}
