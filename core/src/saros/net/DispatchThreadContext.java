package saros.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.picocontainer.Disposable;
import saros.annotations.Component;
import saros.util.NamedThreadFactory;
import saros.util.ThreadUtils;

/** The ExecutorService under which all incoming activities should be executed. */
@Component(module = "core")
public class DispatchThreadContext implements Disposable {

  private static final Logger log = Logger.getLogger(DispatchThreadContext.class);

  protected ExecutorService dispatch =
      Executors.newSingleThreadExecutor(new NamedThreadFactory("DispatchContext", false));

  /**
   * Execute the given runnable as if it was received via the network component.
   *
   * <p>This is used by the ConcurrentDocumentManager to skip sending a JupiterActivity via the
   * network which originated on the host to the JupiterServer.
   */
  public void executeAsDispatch(Runnable runnable) {
    dispatch.submit(ThreadUtils.wrapSafe(log, runnable));
  }

  public ExecutorService getDispatchExecutor() {
    return dispatch;
  }

  @Override
  public void dispose() {
    dispatch.shutdownNow();
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      dispose();
    } finally {
      super.finalize();
    }
  }
}
