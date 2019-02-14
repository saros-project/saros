package de.fu_berlin.inf.dpp.ui.renderer;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.account.IAccountStoreListener;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import de.fu_berlin.inf.dpp.ui.core_facades.RosterFacade;
import de.fu_berlin.inf.dpp.ui.core_facades.RosterFacade.RosterChangeListener;
import de.fu_berlin.inf.dpp.ui.model.Contact;
import de.fu_berlin.inf.dpp.ui.model.State;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;

/**
 * This class is responsible for transferring information about the state of Saros to the browser so
 * they can be displayed. This information are encapsulated in {@link
 * de.fu_berlin.inf.dpp.ui.model.State}.
 *
 * <p>This class also manages the {@link de.fu_berlin.inf.dpp.ui.model.State} via listeners for the
 * {@link de.fu_berlin.inf.dpp.net.ConnectionState} and the {@link org.jivesoftware.smack.Roster},
 * from which the list of {@link de.fu_berlin.inf.dpp.ui.model.Contact}s is created.
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
