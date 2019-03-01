package saros.ui.browser_functions;

import saros.HTMLUIContextFactory;
import saros.ui.browser_functions.BrowserFunction.Policy;
import saros.ui.core_facades.ConnectionFacade;

/** Disconnect the active account. */
public class DisconnectAccount extends TypedJavascriptFunction {

  public static final String JS_NAME = "disconnect";

  private final ConnectionFacade connectionFacade;

  /**
   * Created by PicoContainer
   *
   * @param connectionFacade
   * @see HTMLUIContextFactory
   */
  public DisconnectAccount(ConnectionFacade connectionFacade) {
    super(JS_NAME);
    this.connectionFacade = connectionFacade;
  }

  /** Disconnect the active account. */
  @BrowserFunction(Policy.ASYNC)
  public void disconnect() {
    connectionFacade.disconnect();
  }
}
