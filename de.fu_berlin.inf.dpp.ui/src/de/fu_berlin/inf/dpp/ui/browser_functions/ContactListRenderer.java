/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

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
        // "{account: {username: 'dev8_alice_stf', domain: 'saros-con.imp.fu-berlin.de'}, contactList: [{displayName: 'dev8_bob_stf@saros-con.imp.fu-berlin.de'}]}"
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
