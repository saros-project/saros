package saros.ui.browser_functions;

import org.apache.log4j.Logger;
import saros.HTMLUIContextFactory;
import saros.HTMLUIStrings;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.ui.JavaScriptAPI;

/** Edit an existing account in the account store. */
public class EditAccount extends TypedJavascriptFunction {

  private static final Logger LOG = Logger.getLogger(EditAccount.class);

  public static final String JS_NAME = "editAccount";

  private XMPPAccountStore accountStore;

  /**
   * Created by PicoContainer
   *
   * @param accountStore to manage the XMPPAccounts
   * @see HTMLUIContextFactory
   */
  public EditAccount(XMPPAccountStore accountStore) {
    super(JS_NAME);
    this.accountStore = accountStore;
  }

  /**
   * Edit a given {@link XMPPAccount} in the {@link XMPPAccountStore}.
   *
   * <p>Note that, on success, this action will trigger a re-rendering of the account data to
   * reflect the changes immediately. If this operation fails, an error is shown to the user.
   *
   * @param oldAccount the account to be changed
   * @param newAccount the new data for the account
   */
  @BrowserFunction
  public void editAccount(XMPPAccount oldAccount, XMPPAccount newAccount) {
    try {
      accountStore.changeAccountData(
          oldAccount,
          newAccount.getUsername(),
          newAccount.getPassword(),
          newAccount.getDomain(),
          newAccount.getServer(),
          newAccount.getPort(),
          newAccount.useTLS(),
          newAccount.useSASL());
    } catch (IllegalArgumentException e) {
      LOG.error("Couldn't edit account " + e.getMessage(), e);
      JavaScriptAPI.showError(browser, HTMLUIStrings.ERR_ACCOUNT_EDIT_FAILED);
    }
  }
}
