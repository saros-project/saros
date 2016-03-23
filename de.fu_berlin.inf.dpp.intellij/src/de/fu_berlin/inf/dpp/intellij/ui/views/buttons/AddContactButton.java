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

package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.actions.AddContactAction;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.jivesoftware.smack.Connection;
import org.picocontainer.annotations.Inject;

public class AddContactButton extends SimpleButton {

    public static final String ADD_CONTACT_ICON_PATH = "/icons/famfamfam/contact_add_tsk.png";

    @Inject
    XMPPConnectionService connectionService;

    private IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState state) {
            setEnabledFromUIThread(connectionService.isConnected());
        }
    };

    public AddContactButton() {
        super(new AddContactAction(), "Add a contact", ADD_CONTACT_ICON_PATH, "addContact");

        SarosPluginContext.initComponent(this);
        setEnabledFromUIThread(connectionService.isConnected());
        connectionService.addListener(connectionListener);
    }
}
