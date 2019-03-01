package saros.misc.xstream;

/** Wraps an object to make it replaceable. */
abstract class Replaceable<T> {
  protected T delegate;
  private boolean isReset;

  protected Replaceable(T delegate) {
    if (delegate == null) throw new IllegalArgumentException("delegate must not be null");

    this.isReset = false;
    this.delegate = delegate;
  }

  /**
   * Deactivates this replaceable.
   *
   * <p>Implementation can call {@link #isReset()} to ensure that no calls reach the delegate.
   */
  public synchronized void reset() {
    this.isReset = true;
  }

  protected synchronized boolean isReset() {
    return isReset;
  }

  /**
   * Replaces the current {@link #delegate} with the given one. Does nothing if the given delegate
   * is <code>null</code>.
   *
   * @see #isReset()
   */
  public synchronized void replace(T with) {
    if (with == null) return;

    this.delegate = with;
    this.isReset = false;
  }
}
