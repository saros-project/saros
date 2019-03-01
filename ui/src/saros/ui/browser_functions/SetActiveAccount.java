package saros.ui.browser_functions;

import org.apache.log4j.Logger;
import saros.HTMLUIContextFactory;
import saros.HTMLUIStrings;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.ui.JavaScriptAPI;

/** Set an existing account as active. The currently active account can't be deleted. */
public class SetActiveAccount extends TypedJavascriptFunction {

  private static final Logger LOG = Logger.getLogger(SetActiveAccount.class);

  public static final String JS_NAME = "setActiveAccount";

  private XMPPAccountStore accountStore;

  /**
   * Created by PicoContainer
   *
   * @param accountStore to redirect the set action.
   * @see HTMLUIContextFactory
   */
  public SetActiveAccount(XMPPAccountStore accountStore) {
    super(JS_NAME);
    this.accountStore = accountStore;
  }

  /**
   * Activate the given {@link XMPPAccount}
   *
   * <p>Note that, on success, this action will trigger a re-rendering of the account data to
   * reflect the changes immediately. If this operation fails, an error is shown to the user.
   *
   * @param account the account to be activated
   */
  @BrowserFunction
  public void setActiveAccount(XMPPAccount account) {
    try {
      accountStore.setAccountActive(account);
    } catch (IllegalArgumentException e) {
      LOG.error("Couldn't activate account " + account.toString() + ". Error:" + e.getMessage(), e);
      JavaScriptAPI.showError(browser, HTMLUIStrings.ERR_ACCOUNT_SET_ACTIVE_FAILED);
    }
  }
}
