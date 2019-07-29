package saros.ui.browser;

import java.util.concurrent.Callable;

public interface NoCheckedExceptionCallable<V> extends Callable<V> {
  @Override
  V call();
}
