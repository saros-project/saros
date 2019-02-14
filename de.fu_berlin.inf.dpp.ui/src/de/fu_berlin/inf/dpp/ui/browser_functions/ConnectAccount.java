package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunction.Policy;
import de.fu_berlin.inf.dpp.ui.core_facades.ConnectionFacade;

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
