package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;

/**
 * This class encapsulates the logic to start a SWT related runnable on Mac OS. As Mac OS requires
 * that all UI related actions happen on the main thread, this class uses reflection to get the
 * corresponding executor and dispatches the runnable there.
 *
 * <p>TODO test if method be called more than once or if it blocks after the start of the SWT event
 * dispatch loop
 */
class MacExecutor {

  static void run(final Runnable runnable) {
    Executor mainQueueExecutor;
    try {
      // TODO should be cached if this methods is called more than once in the future
      Object dispatch =
          Class.forName("com.apple.concurrent.Dispatch").getMethod("getInstance").invoke(null);
      mainQueueExecutor =
          (Executor)
              dispatch.getClass().getMethod("getNonBlockingMainQueueExecutor").invoke(dispatch);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    mainQueueExecutor.execute(runnable);
  }
}
