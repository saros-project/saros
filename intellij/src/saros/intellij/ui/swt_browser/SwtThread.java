package saros.intellij.ui.swt_browser;

import java.util.concurrent.CountDownLatch;
import org.eclipse.swt.widgets.Display;

/**
 * Implementation of a thread that creates the event dispatch loop for SWT. This thread has to be
 * started in order to able to use SWT components inside IntelliJ.
 */
class SwtThread extends Thread {

  private final CountDownLatch displayCreatedLatch;

  SwtThread(CountDownLatch displayCreatedLatch) {
    this.displayCreatedLatch = displayCreatedLatch;
  }

  @Override
  public void run() {
    // Execute the SWT event dispatch loop...
    Display display = Display.getDefault(); // creates new one if none present
    displayCreatedLatch.countDown();
    while (!display.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
  }
}
