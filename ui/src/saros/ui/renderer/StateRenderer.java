package saros.ui.renderer;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import saros.HTMLUIContextFactory;
import saros.account.IAccountStoreListener;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.XMPPConnectionService;
import saros.ui.JavaScriptAPI;
import saros.ui.core_facades.RosterFacade;
import saros.ui.core_facades.RosterFacade.RosterChangeListener;
import saros.ui.model.Contact;
import saros.ui.model.State;

/**
 * This class is responsible for transferring information about the state of Saros to the browser so
 * they can be displayed. This information are encapsulated in {@link saros.ui.model.State}.
 *
 * <p>This class also manages the {@link saros.ui.model.State} via listeners for the {@link
 * saros.net.ConnectionState} and the {@link org.jivesoftware.smack.Roster}, from which the list of
 * {@link saros.ui.model.Contact}s is created.
 */
public class StateRenderer extends Renderer {

  private State state;

  /**
   * Created by PicoContainer
   *
   * @see HTMLUIContextFactory
   */
  public StateRenderer(
      XMPPConnectionService connectionService,
      RosterFacade rosterFacade,
      XMPPAccountStore accountStore) {

    state = new State();

    connectionService.addListener(
        new IConnectionListener() {
          @Override
          public void connectionStateChanged(Connection connection, ConnectionState newState) {

            ConnectionState sanitizedState = newState;
            if (sanitizedState == ConnectionState.ERROR) {
              sanitizedState = ConnectionState.NOT_CONNECTED;
            }

            state.setConnectionState(sanitizedState);
            render();
          }
        });

    rosterFacade.addListener(
        new RosterChangeListener() {
          @Override
          public void setValue(List<Pair<RosterEntry, Presence>> rosterEntries) {
            List<Contact> contacts = new ArrayList<Contact>();

            for (Pair<RosterEntry, Presence> entry : rosterEntries) {
              contacts.add(ContactRenderer.convert(entry.getLeft(), entry.getRight()));
            }

            state.setContactList(contacts);
            render();
          }
        });

    accountStore.addListener(
        new IAccountStoreListener() {
          @Override
          public void activeAccountChanged(XMPPAccount activeAccount) {
            state.setAccount(activeAccount);
            render();
          }
        });
  }

  @Override
  public synchronized void render(IJQueryBrowser browser) {
    JavaScriptAPI.updateState(browser, this.state);
  }
}
