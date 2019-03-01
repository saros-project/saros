package de.fu_berlin.inf.dpp.ui.renderer;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.account.IAccountStoreListener;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import java.util.ArrayList;
import java.util.List;

/** This class is responsible for sending the account list to the HTML UI. */
public class AccountRenderer extends Renderer {

  private List<XMPPAccount> accounts;

  /**
   * Created by PicoContainer
   *
   * @param accountStore
   * @see HTMLUIContextFactory
   */
  public AccountRenderer(XMPPAccountStore accountStore) {
    this.accounts = new ArrayList<XMPPAccount>();

    accountStore.addListener(
        new IAccountStoreListener() {
          @Override
          public void accountsChanged(List<XMPPAccount> currentAccounts) {
            update(currentAccounts);
            render();
          }
        });
  }

  private synchronized void update(List<XMPPAccount> allAccounts) {
    accounts = new ArrayList<XMPPAccount>(allAccounts);
  }

  @Override
  public synchronized void render(IJQueryBrowser browser) {
    JavaScriptAPI.updateAccounts(browser, accounts);
  }
}
