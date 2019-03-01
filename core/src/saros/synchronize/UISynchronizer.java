package saros.synchronize;

/**
 * Abstraction layer to the underlying UI event loop in terms of the platform event model. Should
 * only be used in non-UI components.
 *
 * @author Stefan Rossbach
 */
public interface UISynchronizer {

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the user-interface thread
   * at the next reasonable opportunity. The caller of this method continues to run in parallel, and
   * is not notified when the runnable has completed.
   *
   * @param runnable code to run on the user-interface thread
   */
  public void asyncExec(Runnable runnable);

  /**
   * Causes the <code>run()</code> method of the runnable to be invoked by the user-interface thread
   * at the next reasonable opportunity. The thread which calls this method is suspended until the
   * runnable completes.
   *
   * @param runnable code to run on the user-interface thread
   */
  public void syncExec(Runnable runnable);

  /**
   * Checks if the current thread is the user-interface thread.
   *
   * @return <code>true</code> if the current thread is the user-interface thread, <code>false
   *     </code> otherwise
   */
  public boolean isUIThread();
}
