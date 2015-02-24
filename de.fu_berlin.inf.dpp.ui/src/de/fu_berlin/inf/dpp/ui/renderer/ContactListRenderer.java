package de.fu_berlin.inf.dpp.ui.renderer;

import com.google.gson.Gson;
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.functions.CallbackFunction;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.ui.model.ContactList;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;
import org.jivesoftware.smack.Roster;

/**
 * This class is responsible for transferring the contact list and connection
 * state to the browser so that they can be displayed.
 * It holds the connection and the contact list state so that the current state
 * can be re-rendered when the browser instance changes.
 */
public class ContactListRenderer implements Renderer {

    private final BrowserManager browserManager;

    private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

    private ContactList contactList = ContactList.EMPTY_CONTACT_LIST;

    public ContactListRenderer(BrowserManager browserManager) {
        this.browserManager = browserManager;
    }

    /**
     * Displays the given connection state and contact list in the browser.
     *
     * @param state       the connection state
     * @param contactList the contact list
     */
    public synchronized void render(ConnectionState state,
        ContactList contactList) {
        connectionState = state;
        this.contactList = contactList;
        render();
    }

    @Override
    public synchronized void render() {
        renderConnectionState();
        renderContactList();
    }

    /**
     * Displays the given connection state in the browser.
     *
     * @param state the connection state to be displayed
     */
    public synchronized void renderConnectionState(ConnectionState state) {
        connectionState = state;
        render();
    }

    /**
     * Displays the contact list respresented by the given roster in the browser.
     *
     * @param roster the roster containing the contact list
     */
    public synchronized void renderContactList(Roster roster) {
        contactList = contactList.rebuild(roster);
        render();
    }

    private synchronized void renderConnectionState() {
        switch (connectionState) {
        case CONNECTED:
            executeInBrowser("Saros.setIsConnected(" + true + ");");
            break;
        case NOT_CONNECTED:
            executeInBrowser("Saros.setIsConnected(" + false + ");");
            break;
        case CONNECTING:
            executeInBrowser("Saros.setIsConnecting();");
            break;
        case DISCONNECTING:
            executeInBrowser("Saros.setIsDisconnecting();");
            break;
        default:
            break;
        }
    }

    /**
     * Displays the currently saved contact list in the HTML UI.
     * For that, the {@link de.fu_berlin.inf.dpp.ui.model.ContactList} object
     * is transformed into a JSON string and then transmitted to Javascript.
     */
    private synchronized void renderContactList() {
        Gson gson = new Gson();
        final String jsonString = gson.toJson(contactList);
        executeInBrowser("Saros.displayContactList(" + jsonString + ");");
    }

    private void executeInBrowser(final String script) {
        IJQueryBrowser browser = browserManager.getMainViewBrowser();
        if (browser != null) {
            browser.run(script, CallbackFunction.ERROR_LOGGING_CALLBACK);
        }
    }
}
