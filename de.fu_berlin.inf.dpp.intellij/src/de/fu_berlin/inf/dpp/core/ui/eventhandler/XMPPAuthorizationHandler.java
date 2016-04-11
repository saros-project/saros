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

package de.fu_berlin.inf.dpp.core.ui.eventhandler;

import com.intellij.openapi.application.ApplicationManager;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.net.xmpp.subscription.SubscriptionListener;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * Handler for accepting or rejecting incoming XMPP subscription requests
 */
public class XMPPAuthorizationHandler {

    private static final Logger LOG = Logger
        .getLogger(XMPPAuthorizationHandler.class);
    private final SubscriptionListener subscriptionListener = new SubscriptionListener() {

        @Override
        public void subscriptionRequestReceived(final JID jid) {

            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    handleAuthorizationRequest(jid);
                }
            });
        }
    };
    private final SubscriptionHandler subscriptionHandler;

    public XMPPAuthorizationHandler(
        final SubscriptionHandler subscriptionHandler) {
        this.subscriptionHandler = subscriptionHandler;
        this.subscriptionHandler.addSubscriptionListener(subscriptionListener);
    }

    private void handleAuthorizationRequest(final JID jid) {

        boolean accept = DialogUtils.showConfirm(null,
            Messages.SubscriptionManager_incoming_subscription_request_title,
            MessageFormat.format(
                Messages.SubscriptionManager_incoming_subscription_request_message,
                jid.getBareJID()));
        if (accept) {
            subscriptionHandler.addSubscription(jid, true);
        } else {
            subscriptionHandler.removeSubscription(jid);
        }
    }
}
