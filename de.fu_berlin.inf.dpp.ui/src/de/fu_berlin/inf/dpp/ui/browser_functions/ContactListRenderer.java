package de.fu_berlin.inf.dpp.ui.browser_functions;

import com.google.gson.Gson;
import de.fu_berlin.inf.dpp.ui.model.ContactList;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;

/**
 * This class is responsible for rendering the contact list by calling
 * a Javascript function.
 */
public class ContactListRenderer {

    private Browser browser;

    /**
     * @param browser the SWT browser in which the contact list should be rendered
     */
    public ContactListRenderer(Browser browser) {
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
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                browser.execute("__angular_displayContactList(" + jsonString
                    + ");"); //TODO evaluate result
            }
        });
    }
}
