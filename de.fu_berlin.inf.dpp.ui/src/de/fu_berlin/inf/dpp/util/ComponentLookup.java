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

package de.fu_berlin.inf.dpp.util;

import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;

import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.xmpp.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import org.picocontainer.annotations.Inject;

/**
 * This class is used to get pico container components for classes which are not
 * created by the pico container.
 * TODO I think over time a better solution has to be implemented but for now this
 * is sufficient.
 * In the IntelliJ package there is a similar solution in
 * de.fu_berlin.inf.dpp.core.context.SarosPluginContext which could be adapated
 * for this.
 */
public class ComponentLookup {

    @Inject
    private static IDialogManager dialogManager;

    @Inject
    private static ConnectionHandler connectionHandler;

    @Inject
    private static XMPPConnectionService connectionService;

    @Inject
    private static UISynchronizer uiSynchronizer;

    @Inject
    private static XMPPAccountStore accountStore;

    @Inject
    private static SubscriptionHandler subscriptionHandler;

    public static ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public static XMPPConnectionService getConnectionService() {
        return connectionService;
    }

    public static UISynchronizer getUISynchronizer() {
        return uiSynchronizer;
    }

    public static IDialogManager getDialogManager() {
        return dialogManager;
    }

    public static XMPPAccountStore getAccountStore() {
        return accountStore;
    }

    public static SubscriptionHandler getSubscriptionHandler() {
        return subscriptionHandler;
    }
}
