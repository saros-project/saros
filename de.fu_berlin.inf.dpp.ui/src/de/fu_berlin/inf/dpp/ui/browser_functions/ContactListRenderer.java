package de.fu_berlin.inf.dpp.ui.browser_functions;

import com.google.gson.Gson;
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.model.ContactList;

/**
 * This class is responsible for rendering the contact list by calling
 * a Javascript function.
 * TODO this class leaves much room for improvement. Render methods should be merged
 * when the HTML part and the Java/Javascript interface is rewritten
 * so that there is only one call to render the complete current state.
 * Further get rid of constant null checks in ContactListManager
 */
public class ContactListRenderer {

    private IJQueryBrowser browser;

    /**
     * @param browser the SWT browser in which the contact list should be rendered
     */
    public ContactListRenderer(IJQueryBrowser browser) {
        this.browser = browser;
    }

    /**
     * Renders the contact list in the HTML UI.
     * The given {@link de.fu_berlin.inf.dpp.ui.model.ContactList} object
     * is transformed into a JSON string and then transmitted to Javascript.
     *
     * @param contactList the contact list to be rendered
     */
    public void renderContactList(ContactList contactList) {
        Gson gson = new Gson();
        final String jsonString = gson.toJson(contactList);
        executeInBrowser("__angular_displayContactList(" + jsonString + ");");
    }

    /**
     * Reflects the current state in the HTML and Set the text of the connect button
     * accordingly. This methods just distinguishes between connected and disconnected.
     *
     * @param connected true if connected, false if disconnected
     */
    public void renderIsConnected(boolean connected) {
        executeInBrowser("__angular_setIsConnected(" + connected + ");");
    }

    /**
     * Connecting in process. Disable the connect button
     * and set its text accordingly.
     */
    public void renderIsConnecting() {
        executeInBrowser("__angular_setIsConnecting();");
    }

    /**
     * Disconnecting in process. Disable the connect button
     * and set its text accordingly.
     */
    public void renderIsDisconnecting() {
        executeInBrowser("__angular_setIsDisconnecting();");
    }

    private void executeInBrowser(final String script) {
        //TODO evaluate results
        browser.run(script);
    }
}
