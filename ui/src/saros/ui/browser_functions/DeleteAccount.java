package saros.ui.browser_functions;

import org.apache.log4j.Logger;
import saros.HTMLUIContextFactory;
import saros.HTMLUIStrings;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.ui.JavaScriptAPI;

/**
 * Delete an existing account from the account store. The currently active account cannot be
 * deleted.
 */
public class DeleteAccount extends TypedJavascriptFunction {

  private static final Logger LOG = Logger.getLogger(DeleteAccount.class);

  public static final String JS_NAME = "deleteAccount";

  private XMPPAccountStore accountStore;

  /**
   * Created by PicoContainer
   *
   * @param accountStore to redirect the delete action
   * @see HTMLUIContextFactory
   */
  public DeleteAccount(XMPPAccountStore accountStore) {
    super(JS_NAME);
    this.accountStore = accountStore;
  }

  /**
   * Delete a given {@link XMPPAccount} from the {@link XMPPAccountStore}. The active account cannot
   * be deleted.
   *
   * <p>If this operation fails, an error is shown to the user.
   *
   * @param account to be deleted
   */
  @BrowserFunction
  public void deleteAccount(XMPPAccount account) {
    try {
      accountStore.deleteAccount(account);
    } catch (IllegalStateException e) {
      LOG.warn("Couldn't delete active account: " + e.getMessage(), e);
      JavaScriptAPI.showError(browser, HTMLUIStrings.ERR_ACCOUNT_DELETE_ACTIVE);
    } catch (IllegalArgumentException e) {
      LOG.error("Couldn't delete account: " + e.getMessage(), e);
      // FIXME Misleading error message
      JavaScriptAPI.showError(browser, HTMLUIStrings.ERR_ACCOUNT_DELETE_ACTIVE);
    }
  }
}
