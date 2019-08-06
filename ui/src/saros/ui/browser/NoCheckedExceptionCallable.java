package saros.ui.browser;

import java.util.concurrent.Callable;

// Adopted from https://github.com/ag-se/swt-browser-improved
public interface NoCheckedExceptionCallable<V> extends Callable<V> {
  @Override
  V call();
}
