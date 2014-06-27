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

package de.fu_berlin.inf.dpp.core.context;

import de.fu_berlin.inf.dpp.AbstractSarosContextFactory;
import de.fu_berlin.inf.dpp.ISarosContextBindings;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatService;
import de.fu_berlin.inf.dpp.communication.chat.single.SingleUserChatService;
import de.fu_berlin.inf.dpp.invitation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.IBBTransport;
import de.fu_berlin.inf.dpp.net.internal.ITransport;
import de.fu_berlin.inf.dpp.net.internal.Socks5Transport;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.stun.IStunService;
import de.fu_berlin.inf.dpp.net.stun.internal.StunServiceImpl;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPAccess;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.net.upnp.internal.UPnPAccessImpl;
import de.fu_berlin.inf.dpp.net.upnp.internal.UPnPServiceImpl;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.xmpp.discovery.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.xmpp.roster.RosterTracker;
import de.fu_berlin.inf.dpp.net.xmpp.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.SessionNegotiationObservable;
import de.fu_berlin.inf.dpp.versioning.VersionManager;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

import java.util.Arrays;

/**
 * This is the basic core factory for Saros. All components that are created by
 * this factory <b>must</b> be working on any platform the application is
 * running on.
 *
 * @author srossbach
 */
//todo: adopted from eclipse
public class SarosCoreContextFactory extends AbstractSarosContextFactory {

    // TODO: uncomment components when submitted to repository

    private final Component[] components = new Component[] {

        // Version support
        Component.create(VersionManager.class),

        Component.create(MultiUserChatService.class),
        Component.create(SingleUserChatService.class),

        Component.create(XMPPAccountStore.class),

        // Invitation hooks
        Component.create(SessionNegotiationHookManager.class),

        // Network
        Component.create(DispatchThreadContext.class),

        Component.create(DataTransferManager.class),

        Component.create(DiscoveryManager.class),

        Component.create(BindKey.bindKey(ITransport.class,
            ISarosContextBindings.IBBTransport.class), IBBTransport.class),

        Component.create(BindKey.bindKey(ITransport.class,
                ISarosContextBindings.Socks5Transport.class),
            Socks5Transport.class
        ),

        Component.create(RosterTracker.class),
        Component.create(XMPPConnectionService.class),

        Component.create(IStunService.class, StunServiceImpl.class),

        Component.create(SubscriptionHandler.class),

        Component.create(IUPnPService.class, UPnPServiceImpl.class),
        Component.create(IUPnPAccess.class, UPnPAccessImpl.class),
        Component.create(IReceiver.class, XMPPReceiver.class),
        Component.create(ITransmitter.class, XMPPTransmitter.class),

        // Observables
        Component.create(FileReplacementInProgressObservable.class),
        Component.create(SessionNegotiationObservable.class),
        Component.create(ProjectNegotiationObservable.class),
        //Component.create(IsInconsistentObservable.class), //todo
        Component.create(SessionIDObservable.class),
        Component.create(SarosSessionObservable.class),
        // Component.create(AwarenessInformationCollector.class),//todo
        // Component.create(FollowingActivitiesManager.class),  //todo

        // Handlers
        // Component.create(CancelInviteHandler.class), //todo
        // Component.create(CancelProjectSharingHandler.class), //todo
        // Component.create(InvitationHandler.class), //todo
        // Component.create(LeaveAndKickHandler.class), //todo

        // Component.create(RemoteProgressManager.class), //todo

    };

    @Override
    public void createComponents(MutablePicoContainer container) {
        for (Component component : Arrays.asList(components)) {

            container.addComponent(component.getBindKey(),
                component.getImplementation());
        }
    }
}
