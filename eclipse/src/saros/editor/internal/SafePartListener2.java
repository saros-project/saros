package saros.editor.internal;

import org.apache.log4j.Logger;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import saros.util.ThreadUtils;

/**
 * A listener which forwards calls to a another IPartListener2, but catches all exception which
 * might have occur in the forwarded to IPartListener2 and prints them to the log given in the
 * constructor.
 *
 * @pattern Proxy which adds the aspect of "safety"
 */
public class SafePartListener2 implements IPartListener2 {

  /**
   * The {@link IPartListener2} to forward all call to which are received by this {@link
   * IPartListener2}
   */
  protected IPartListener2 toForwardTo;

  /**
   * The {@link Logger} to use for printing an error message when a RuntimeException occurs when
   * calling the {@link #toForwardTo} {@link IPartListener2}.
   */
  protected Logger log;

  public SafePartListener2(Logger log, IPartListener2 toForwardTo) {
    this.toForwardTo = toForwardTo;
    this.log = log;
  }

  @Override
  public void partActivated(final IWorkbenchPartReference partRef) {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            toForwardTo.partActivated(partRef);
          }
        });
  }

  @Override
  public void partBroughtToTop(final IWorkbenchPartReference partRef) {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            toForwardTo.partBroughtToTop(partRef);
          }
        });
  }

  @Override
  public void partClosed(final IWorkbenchPartReference partRef) {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            toForwardTo.partClosed(partRef);
          }
        });
  }

  @Override
  public void partDeactivated(final IWorkbenchPartReference partRef) {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            toForwardTo.partDeactivated(partRef);
          }
        });
  }

  @Override
  public void partHidden(final IWorkbenchPartReference partRef) {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            toForwardTo.partHidden(partRef);
          }
        });
  }

  @Override
  public void partInputChanged(final IWorkbenchPartReference partRef) {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            toForwardTo.partInputChanged(partRef);
          }
        });
  }

  @Override
  public void partOpened(final IWorkbenchPartReference partRef) {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            toForwardTo.partOpened(partRef);
          }
        });
  }

  @Override
  public void partVisible(final IWorkbenchPartReference partRef) {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            toForwardTo.partVisible(partRef);
          }
        });
  }
}
