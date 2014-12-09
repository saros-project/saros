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
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.manager.ContactListManager;
import de.fu_berlin.inf.dpp.ui.model.Account;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

/**
 * This class implements the functions to be called by Javascript code for
 * the contact list. These are the callback functions to invoke Java code from
 * Javascript.
 */
public class ContactListBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(ContactListBrowserFunctions.class);

    private ContactListManager contactListManager;

    private Browser browser;

    /**
     * @param browser the SWT browser in which the functions should be injected
     */
    public ContactListBrowserFunctions(Browser browser) {
        this.browser = browser;
    }

    public void setContactListManager(ContactListManager contactListManager) {
        this.contactListManager = contactListManager;
    }

    /**
     * Injects Javascript functions into the HTML page. These functions
     * call Java code below when invoked.
     */
    public void createJavascriptFunctions() {
        //TODO remember to disable button in HTML while connecting
        new BrowserFunction(browser, "__java_connect") {
            @Override
            public Object function(Object[] arguments) {
                if (arguments.length > 0 && arguments[0] != null) {
                    Gson gson = new Gson();
                    final Account account = gson
                        .fromJson((String) arguments[0], Account.class);
                    ThreadUtils.runSafeAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            contactListManager.connect(account);
                        }
                    });
                } else {
                    ThreadUtils.runSafeAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            //        String domain = "saros-con.imp.fu-berlin.de";
                            //        int port = 0;
                            //        String server = "";
                            //        boolean useTLS = false;
                            //        boolean useSASL = true
                            //        String username = "dev8_alice_stf";
                            //        String password = "dev";
                            contactListManager.connect(
                                new Account("dev8_alice_stf",
                                    "saros-con.imp.fu-berlin.de"));
                        }
                    });
                }
                return null;
            }
        };
        new BrowserFunction(browser, "__java_disconnect") {
            @Override
            public Object function(Object[] arguments) {
                ThreadUtils.runSafeAsync(LOG, new Runnable() {
                    @Override
                    public void run() {
                        contactListManager.disconnect();
                    }
                });
                return null;
            }
        };

        new BrowserFunction(browser, "__java_deleteContact") {
            @Override
            public Object function(final Object[] arguments) {
                ThreadUtils.runSafeAsync(LOG, new Runnable() {
                    @Override
                    public void run() {
                        contactListManager
                            .deleteContact(new JID((String) arguments[0]));
                    }
                });
                return null;
            }
        };

        new BrowserFunction(browser, "__java_addContact") {
            @Override
            public Object function(final Object[] arguments) {
                ThreadUtils.runSafeAsync(LOG, new Runnable() {
                    @Override
                    public void run() {
                        contactListManager
                            .addContact(new JID((String) arguments[0]));
                    }
                });
                return null;
            }
        };
    }
}
