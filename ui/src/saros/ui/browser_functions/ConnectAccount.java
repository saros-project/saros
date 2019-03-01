package saros.ui.browser_functions;

import saros.HTMLUIContextFactory;
import saros.account.XMPPAccount;
import saros.ui.browser_functions.BrowserFunction.Policy;
import saros.ui.core_facades.ConnectionFacade;

/** Connect with given Account. */
public class ConnectAccount extends TypedJavascriptFunction {

  public static final String JS_NAME = "connect";

  private final ConnectionFacade connectionFacade;

  /**
   * Created by PicoContainer
   *
   * @param connectionFacade
   * @see HTMLUIContextFactory
   */
  public ConnectAccount(ConnectionFacade connectionFacade) {
    super(JS_NAME);
    this.connectionFacade = connectionFacade;
  }

  /**
   * Connect with given Account.
   *
   * @param account
   */
  @BrowserFunction(Policy.ASYNC)
  public void connect(final XMPPAccount account) {
    connectionFacade.connect(account);
  }
}
