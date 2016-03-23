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

package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import org.picocontainer.annotations.Inject;

/**
 * Adds a contact to the roster of the current user.
 */
public class AddContactAction extends AbstractSarosAction {

    private static Logger LOGGER = Logger.getLogger(AddContactAction.class);

    @Inject
    private XMPPConnectionService connectionService;

    @Override
    public String getActionName() {
        return "addContact";
    }

    @Override
    public void execute() {
        String userID = SafeDialogUtils
            .showInputDialog("Their User-ID", "", "Add a contact");

        if (!userID.isEmpty()) {
            tryAddContact(userID);
        }
    }

    private void tryAddContact(String userID) {
        JID jid = new JID(userID);

        if (XMPPUtils.validateJID(jid)) {
            try {

                if (XMPPUtils
                    .isJIDonServer(connectionService.getConnection(), jid,
                        null)) {
                    sendSubscriptionRequest(jid);
                } else {
                    confirmAddOnError(jid, new XMPPException(
                        new XMPPError(XMPPError.Condition.item_not_found)));
                }
            } catch (XMPPException e) {
                confirmAddOnError(jid, e);
            }
        } else {
            SafeDialogUtils.showError("Invalid User-ID", "Error");
        }
    }

    private void confirmAddOnError(JID jid, XMPPException exception) {
        String dialogMessage;
        String dialogTitle;
        XMPPError error = exception.getXMPPError();

        if (error.getCondition().equals("item-not-found")) {
            dialogMessage = "You entered a valid XMPP server.\n\nUnfortunately your entered JID is unknown to the server.\nPlease make sure you spelled the JID correctly.";
            dialogTitle = "Contact unknown to XMPP server";
        } else if (error.getCondition().equals("remote-server-not-found")) {
            dialogMessage = "The responsible XMPP server could not be found.";
            dialogTitle = "Remote XMPP server not found";
        } else if (error.getCode() == 501) {
            dialogMessage = "The responsible XMPP server does not support status requests.\n\nIf the contact exists you can still successfully add him.";
            dialogTitle = "Contact status check unsupported by XMPP server";
        } else if (error.getCode() == 503) {
            dialogMessage = "For privacy reasons the XMPP server does not reply to status requests.\n\nIf the contact exists you can still successfully add him.";
            dialogTitle = "Unable to check the contact status";
        } else if (exception.getMessage()
            .contains("No response from the server")) {
            dialogMessage = "The responsible XMPP server is not connectable.\nThe server is either inexistent or offline right now.";
            dialogTitle = "The XMPP server did not respond";
        } else {
            dialogMessage = "An unknown error has occurred.";
            dialogTitle = "Unknown error";
        }

        boolean userConfirmation = SafeDialogUtils.showYesNoDialog(
            dialogMessage + "\n\nDo you want to add the contact anyway?",
            dialogTitle);

        if (userConfirmation) {
            try {
                sendSubscriptionRequest(jid);
            } catch (XMPPException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void sendSubscriptionRequest(JID jid) throws XMPPException {
        connectionService.getRoster().createEntry(jid.getBase(), null, null);
    }
}
