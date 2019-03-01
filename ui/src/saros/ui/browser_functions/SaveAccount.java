package saros.ui.browser_functions;

import org.apache.log4j.Logger;
import saros.HTMLUIContextFactory;
import saros.HTMLUIStrings;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.ui.JavaScriptAPI;
import saros.ui.browser_functions.BrowserFunction.Policy;

/** Save a given JID/Password combination. */
public class SaveAccount extends TypedJavascriptFunction {

  private static final Logger LOG = Logger.getLogger(SaveAccount.class);

  public static final String JS_NAME = "saveAccount";

  private XMPPAccountStore accountStore;

  /**
   * Created by PicoContainer
   *
   * @param accountStore the save action is delegated to
   * @see HTMLUIContextFactory
   */
  public SaveAccount(XMPPAccountStore accountStore) {
    super(JS_NAME);
    this.accountStore = accountStore;
  }

  /**
   * Saves a given {@link XMPPAccount}in the {@link XMPPAccountStore}.
   *
   * <p>No account is created and an error will be shown, if there is already an account with the
   * same values for all of <br>
   * {@link XMPPAccount#getUsername()}; {@link XMPPAccount#getDomain()}; {@link
   * XMPPAccount#getServer()}; {@link XMPPAccount#getPort()},
   *
   * @param acc the JID of the new account
   */
  @BrowserFunction(Policy.ASYNC)
  public void saveAccount(XMPPAccount acc) {
    try {
      accountStore.createAccount(
          acc.getUsername(),
          acc.getPassword(),
          acc.getDomain(),
          acc.getServer(),
          acc.getPort(),
          acc.useTLS(),
          acc.useSASL());
    } catch (IllegalArgumentException e) {
      // FIXME not all IllegalArgumentExceptions are due to already
      // present accounts
      LOG.debug("Account " + acc.getUsername() + " already present");
      JavaScriptAPI.showError(browser, HTMLUIStrings.ERR_ACCOUNT_ALREADY_PRESENT);
    }
  }
}
