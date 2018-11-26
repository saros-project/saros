package de.fu_berlin.inf.dpp.account;

import java.util.List;

/**
 * An {@link IAccountStoreListener} which does nothing by default. Extend this class if you only
 * want to react to specific change events.
 */
public class NullAccountStoreListener implements IAccountStoreListener {
  @Override
  public void accountsChanged(List<XMPPAccount> currentAccounts) {
    // do nothing
  }

  @Override
  public void activeAccountChanged(XMPPAccount activeAccount) {
    // do nothing
  }
}
