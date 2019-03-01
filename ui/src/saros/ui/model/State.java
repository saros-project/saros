package saros.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import saros.account.XMPPAccount;
import saros.net.ConnectionState;

/**
 * Represents the state of the browser application. It consists of an {@link XMPPAccount}, a list of
 * {@link saros.ui.model.Contact}s and the {@link saros.net.ConnectionState}.
 */
public class State {

  private XMPPAccount activeAccount;

  private List<Contact> contactList;

  private ConnectionState connectionState;

  /**
   * Initial state: no active account, an empty account list, and Saros being {@link
   * ConnectionState#NOT_CONNECTED}.
   */
  public State() {
    this(null, Collections.<Contact>emptyList(), saros.net.ConnectionState.NOT_CONNECTED);
  }

  /**
   * @param activeAccount the currently active account
   * @param contactList the list of contacts of the active account
   * @param connectionState the current connection state of the active account
   */
  private State(
      XMPPAccount activeAccount, List<Contact> contactList, ConnectionState connectionState) {
    this.activeAccount = activeAccount;
    this.contactList = new ArrayList<Contact>(contactList);
    this.connectionState = connectionState;
  }

  public void setContactList(List<Contact> contactList) {
    this.contactList = contactList;
  }

  /**
   * Set the currently active account. May be null if there is no account active.
   *
   * @param activeAccount the active account or null
   */
  public void setAccount(XMPPAccount activeAccount) {
    this.activeAccount = activeAccount;
  }

  public void setConnectionState(ConnectionState connectionState) {
    this.connectionState = connectionState;
  }

  /**
   * Returns the active account or null if there is no account active.
   *
   * @return the active account or null
   */
  public XMPPAccount getActiveAccount() {
    return activeAccount;
  }

  public List<Contact> getContactList() {
    return contactList;
  }

  public ConnectionState getConnectionState() {
    return this.connectionState;
  }
}
