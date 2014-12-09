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

package de.fu_berlin.inf.dpp.ui.manager;

import de.fu_berlin.inf.dpp.ui.browser_functions.AccountBrowserFunctions;
import org.eclipse.swt.browser.Browser;

/**
 * Encloses the different managers for the UI.
 * Up till now there only exists the {@link de.fu_berlin.inf.dpp.ui.manager.ContactListManager}.
 * Other managers for managing the chat, various user activities etc. will follow.
 */
public class HTMLUIManager {

    private HTMLUIManager() {
    }

    /**
     * Factory method
     * Up till now the HTMLUiManager is specific for each browser instance.
     * That means that is has to be re-created when the browser changes.
     *
     * @param browser the SWT browser
     * @return the created HTML UI manager object
     */
    public static HTMLUIManager create(Browser browser) {
        ContactListManager.createManager(browser);
        new AccountBrowserFunctions(browser).createJavascriptFunctions();
        return new HTMLUIManager();
    }
}
