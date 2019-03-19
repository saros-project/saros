package saros.ui.browser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class SWTBrowser implements IBrowser {

  private enum ReadyState {
    LOADING,
    COMPLETE
  }

  private final Browser browser;
  private ReadyState state = ReadyState.LOADING;
  private List<Runnable> runOnDisposalList =
      Collections.synchronizedList(new ArrayList<Runnable>());

  private final ProgressListener readyStateListener =
      new ProgressListener() {
        @Override
        public void completed(ProgressEvent event) {
          state = ReadyState.COMPLETE;
        }

        @Override
        public void changed(ProgressEvent event) {
          state = ReadyState.LOADING;
        }
      };

  public SWTBrowser(Composite parent, int style) {
    browser = new Browser(parent, style);

    browser.addDisposeListener(
        (DisposeEvent e) -> {
          synchronized (runOnDisposalList) {
            for (Runnable runnable : runOnDisposalList) {
              runnable.run();
            }
          }
        });
  }

  @Override
  public boolean setFocus() {
    return this.syncExec(
        () -> {
          return browser.setFocus();
        });
  }

  @Override
  public void setSize(final int width, final int height) {
    this.syncExec(() -> browser.setSize(width, height));
  }

  @Override
  public String getUrl() {
    return this.syncExec(
        () -> {
          return browser.getUrl();
        });
  }

  @Override
  public boolean loadUrl(final String url, final int timeout) {
    browser.getShell().open();

    this.state = ReadyState.LOADING;
    browser.addProgressListener(readyStateListener);

    this.syncExec(
        () -> {
          browser.setUrl(url);
        });
    boolean successful = waitForPageLoaded(timeout);

    browser.removeProgressListener(readyStateListener);

    return successful;
  }

  private boolean waitForPageLoaded(final int timeout) {
    AtomicBoolean waiting = new AtomicBoolean(true);
    Display display = browser.getDisplay();

    // Wake up the ui thread after the timeout exceeded
    Runnable wakeUpTimeout =
        () -> {
          // avoid execution if url is already loaded
          if (state != ReadyState.COMPLETE) {
            waiting.set(false);
            display.wake();
          }
        };

    display.timerExec(timeout, wakeUpTimeout);

    this.syncExec(
        () -> {
          while (waiting.get() && state != ReadyState.COMPLETE) {
            if (!display.readAndDispatch()) display.sleep();
          }
        });

    return this.state == ReadyState.COMPLETE;
  }

  @Override
  public void loadHtml(final String html) {
    this.syncExec(() -> browser.setText(html));
  }

  @Override
  public Object evaluate(final String jsCode) {
    return this.syncExec(
        () -> {
          Object rv = browser.evaluate(jsCode);
          System.out.println(rv);
          return rv;
        });
  }

  @Override
  public boolean execute(String jsCode) {
    return this.syncExec(
        () -> {
          return browser.execute(jsCode);
        });
  }

  @Override
  public void addBrowserFunction(final AbstractJavascriptFunction function) {
    this.syncExec(
        () -> {
          BrowserFunction swtFunction =
              new BrowserFunction(browser, function.getName()) {
                @Override
                public Object function(Object[] arguments) {
                  return function.function(arguments);
                }
              };
          return swtFunction;
        });
  }

  @Override
  public void runOnDisposal(Runnable runnable) {
    runOnDisposalList.add(runnable);
  }

  private <V> V syncExec(final NoCheckedExceptionCallable<V> callable) {
    final AtomicReference<V> r = new AtomicReference<V>();
    final AtomicReference<RuntimeException> exception = new AtomicReference<RuntimeException>();
    browser
        .getDisplay()
        .syncExec(
            () -> {
              try {
                r.set(callable.call());
              } catch (RuntimeException e) {
                exception.set(e);
              }
            });

    if (exception.get() != null) {
      throw exception.get();
    }
    return r.get();
  }

  private void syncExec(final Runnable runnable) {
    final AtomicReference<RuntimeException> exception = new AtomicReference<RuntimeException>();
    browser
        .getDisplay()
        .syncExec(
            () -> {
              try {
                runnable.run();
              } catch (RuntimeException e) {
                exception.set(e);
              }
            });

    if (exception.get() != null) {
      throw exception.get();
    }
  }
}
