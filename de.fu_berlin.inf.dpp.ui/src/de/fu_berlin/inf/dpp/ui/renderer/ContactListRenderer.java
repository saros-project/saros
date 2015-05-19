package de.fu_berlin.inf.dpp.ui.renderer;

import org.jivesoftware.smack.Roster;

import com.google.gson.Gson;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.ui.model.ContactList;

/**
 * This class is responsible for transferring the contact list and connection
 * state to the browser so that they can be displayed. It holds the connection
 * and the contact list state so that the current state can be re-rendered when
 * the browser instance changes.
 */
public class ContactListRenderer extends Renderer {

    private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

    private ContactList contactList = ContactList.EMPTY_CONTACT_LIST;

    /**
     * Displays the given connection state and contact list in the browser.
     * 
     * @param state
     *            the connection state
     * @param contactList
     *            the contact list
     */
    public synchronized void render(ConnectionState state,
        ContactList contactList) {
        connectionState = state;
        this.contactList = contactList;
        render();
    }

    @Override
    public synchronized void render(IJQueryBrowser browser) {
        renderConnectionState(browser);
        renderContactList(browser);
    }

    /**
     * Renders the given connection state in the browser.
     * 
     * @param state
     *            the connection state to be displayed
     */
    public synchronized void renderConnectionState(ConnectionState state) {
        connectionState = state;
        render();
    }

    /**
     * Renders the contact list represented by the given roster in the browser.
     * 
     * @param roster
     *            the roster containing the contact list
     */
    public synchronized void renderContactList(Roster roster) {
        contactList = contactList.rebuild(roster);
        render();
    }

    // TODO: Change the JS calls to the coming JS API
    private synchronized void renderConnectionState(IJQueryBrowser browser) {
        switch (connectionState) {
        case CONNECTED:
            browser.run("__angular_setIsConnected(" + true + ");");
            break;
        case NOT_CONNECTED:
            browser.run("__angular_setIsConnected(" + false + ");");
            break;
        case CONNECTING:
            browser.run("__angular_setIsConnecting();");
            break;
        case DISCONNECTING:
            browser.run("__angular_setIsDisconnecting();");
            break;
        default:
            break;
        }
    }

    /**
     * Renders the currently saved contact list in the HTML UI. For that, the
     * {@link de.fu_berlin.inf.dpp.ui.model.ContactList} object is transformed
     * into a JSON string and then transmitted to Javascript.
     */
    private synchronized void renderContactList(IJQueryBrowser browser) {
        Gson gson = new Gson();
        final String jsonString = gson.toJson(contactList);
        browser.run("__angular_displayContactList(" + jsonString + ");");
    }

}
