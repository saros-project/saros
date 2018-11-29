package de.fu_berlin.inf.dpp.monitoring;

/**
 * A progress monitor that uses a given amount of work ticks from a parent monitor. It can be used
 * as follows:
 *
 * <pre>
 * try {
 *     pm.beginTask(&quot;Main Task&quot;, 100);
 *     doSomeWork(pm, 30);
 *     SubProgressMonitor subMonitor = new SubProgressMonitor(pm, 40);
 *     try {
 *         subMonitor.beginTask(&quot;&quot;, 300);
 *         doSomeWork(subMonitor, 300);
 *     } finally {
 *         subMonitor.done();
 *     }
 *     doSomeWork(pm, 30);
 * } finally {
 *     pm.done();
 * }
 * </pre>
 */
public final class SubProgressMonitor implements IProgressMonitor {

  /**
   * Indicates that strings passed to {@linkplain #setTaskName}, {@linkplain #subTask}, and
   * {@linkplain #beginTask} should all be propagated to the parent.
   */
  public static final int SUPPRESS_NONE = 0;

  /** Indicates that strings passed into {@linkplain #beginTask} should be ignored. */
  public static final int SUPPRESS_BEGINTASK = 1;

  /** Indicates that strings passed into {@linkplain #setTaskName} should be ignored. */
  public static final int SUPPRESS_SETTASKNAME = 2;

  /** Indicates that strings passed into {@linkplain #subTask} should be ignored. */
  public static final int SUPPRESS_SUBTASK = 4;

  private final IProgressMonitor delegate;

  private boolean beginTaskCalled;
  private boolean subTaskCalled;

  private final int style;

  private final int totalTicksToConsume;
  private int ticksToConsume;

  private long currentWork;
  private long totalWork;

  private long lastProgress;

  /**
   * Creates a new sub-progress monitor for the given monitor. The sub progress monitor uses the
   * given number of work ticks from its parent monitor.
   *
   * @param monitor the parent progress monitor
   * @param ticks the number of work
   * @throws IllegalArgumentException if monitor is <code>null</code>
   */
  public SubProgressMonitor(final IProgressMonitor monitor, final int ticks) {
    this(monitor, ticks, SUPPRESS_NONE);
  }

  /**
   * Creates a new sub-progress monitor for the given monitor. The sub progress monitor uses the
   * given number of work ticks from its parent monitor.
   *
   * @param monitor the parent progress monitor
   * @param ticks the number of work
   * @param style the style to use when changing labels which can be a combination of {@linkplain
   *     #SUPPRESS_BEGINTASK} and {@linkplain #SUPPRESS_SETTASKNAME} and {@linkplain
   *     #SUPPRESS_SUBTASK}, or only {@linkplain #SUPPRESS_NONE} if nothing should be suppressed
   * @throws IllegalArgumentException if monitor is <code>null</code>
   */
  public SubProgressMonitor(final IProgressMonitor monitor, final int ticks, final int style) {
    if (monitor == null) throw new IllegalArgumentException("monitor is null");

    delegate = monitor;
    totalTicksToConsume = ticks < 0 ? 0 : ticks;
    ticksToConsume = totalTicksToConsume;
    this.style = style;
  }

  @Override
  public void done() {
    if (!beginTaskCalled) return;

    delegate.worked(ticksToConsume);

    if (subTaskCalled) delegate.subTask("");

    beginTaskCalled = false;
    subTaskCalled = false;
  }

  @Override
  public void subTask(final String name) {
    if ((style & SUPPRESS_SUBTASK) != 0) return;

    delegate.subTask(name);
    subTaskCalled = true;
  }

  @Override
  public void setTaskName(final String name) {
    if ((style & SUPPRESS_SETTASKNAME) != 0) return;

    delegate.setTaskName(name);
  }

  @Override
  public void worked(final int amount) {
    if (amount <= 0 || totalWork == 0 || totalTicksToConsume == 0 || !beginTaskCalled) return;

    currentWork += amount;

    if (currentWork > totalWork) currentWork = totalWork;

    final long newProgress = (currentWork * totalTicksToConsume) / totalWork;

    final int currentTicks = (int) (newProgress - lastProgress);

    if (currentTicks > 0) {
      ticksToConsume -= currentTicks;
      lastProgress = newProgress;
      delegate.worked(currentTicks);
    }
  }

  @Override
  public void setCanceled(final boolean canceled) {
    delegate.setCanceled(canceled);
  }

  @Override
  public boolean isCanceled() {
    return delegate.isCanceled();
  }

  @Override
  public void beginTask(final String name, final int size) {
    if (beginTaskCalled) return;

    if ((style & SUPPRESS_BEGINTASK) == 0) delegate.setTaskName(name);

    totalWork = size < 0 ? 0 : size;
    beginTaskCalled = true;
  }

  /**
   * Returns the total ticks to consume from the parent monitor.
   *
   * @return the total ticks to consume from the parent monitor
   */
  public int getTotalTicks() {
    return totalTicksToConsume;
  }

  /**
   * Returns the parent monitor that this monitor is consuming ticks from.
   *
   * @return the parent monitor of this monitor
   */
  public IProgressMonitor getParent() {
    return delegate;
  }
}
