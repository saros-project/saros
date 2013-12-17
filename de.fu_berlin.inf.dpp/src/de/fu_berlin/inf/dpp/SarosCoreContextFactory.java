package de.fu_berlin.inf.dpp;

import java.util.Arrays;

import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.communication.SkypeManager;
import de.fu_berlin.inf.dpp.communication.audio.AudioService;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatService;
import de.fu_berlin.inf.dpp.communication.chat.muc.negotiation.MUCNegotiationManager;
import de.fu_berlin.inf.dpp.communication.chat.single.SingleUserChatService;
import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.editor.colorstorage.ColorIDSetStorage;
import de.fu_berlin.inf.dpp.invitation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.business.CancelInviteHandler;
import de.fu_berlin.inf.dpp.net.business.CancelProjectSharingHandler;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.business.InvitationHandler;
import de.fu_berlin.inf.dpp.net.business.LeaveAndKickHandler;
import de.fu_berlin.inf.dpp.net.discoverymanager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.IBBTransport;
import de.fu_berlin.inf.dpp.net.internal.ITransport;
import de.fu_berlin.inf.dpp.net.internal.Socks5Transport;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.stun.IStunService;
import de.fu_berlin.inf.dpp.net.stun.internal.StunServiceImpl;
import de.fu_berlin.inf.dpp.net.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPAccess;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.net.upnp.internal.UPnPAccessImpl;
import de.fu_berlin.inf.dpp.net.upnp.internal.UPnPServiceImpl;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.VideoSessionObservable;
import de.fu_berlin.inf.dpp.observables.VoIPSessionObservable;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.ColorNegotiationHook;
import de.fu_berlin.inf.dpp.project.internal.FollowingActivitiesManager;
import de.fu_berlin.inf.dpp.util.sendfile.FileStreamService;
import de.fu_berlin.inf.dpp.versioning.VersionManager;
import de.fu_berlin.inf.dpp.videosharing.VideoSharingService;

/**
 * This is the basic core factory for Saros. All components that are created by
 * this factory <b>must</b> be working on any platform the application is
 * running on.
 * 
 * @author srossbach
 */
public class SarosCoreContextFactory extends AbstractSarosContextFactory {

    // TODO we must abstract the IPrefenceStore stuff otherwise anything here is
    // broken

    private final Component[] components = new Component[] {

        // Version support ... broken uses Eclipse / OSGi STUFF
        Component.create(VersionManager.class),

        Component.create(MultiUserChatService.class),
        Component.create(SingleUserChatService.class),

        Component.create(SarosSessionManager.class),

        Component.create(XMPPAccountStore.class),
        Component.create(ColorIDSetStorage.class),

        // Invitation hooks
        Component.create(SessionNegotiationHookManager.class),
        Component.create(ColorNegotiationHook.class),
        Component.create(MUCNegotiationManager.class),

        // Network
        Component.create(DispatchThreadContext.class),

        Component.create(DataTransferManager.class),

        Component.create(DiscoveryManager.class),

        Component.create(BindKey.bindKey(ITransport.class,
            ISarosContextBindings.IBBTransport.class), IBBTransport.class),

        Component
            .create(BindKey.bindKey(ITransport.class,
                ISarosContextBindings.Socks5Transport.class),
                Socks5Transport.class),

        Component.create(RosterTracker.class),
        Component.create(SarosNet.class),
        Component.create(SkypeManager.class),

        // broken by any means ...
        Component.create(StreamServiceManager.class),

        // streaming services
        Component.create(FileStreamService.class),
        Component.create(AudioService.class),
        Component.create(VideoSharingService.class),

        Component.create(IStunService.class, StunServiceImpl.class),

        // broken - uses SWT GUI stuff
        Component.create(SubscriptionHandler.class),

        // broken - uses SWT GUI stuff
        Component.create(IUPnPService.class, UPnPServiceImpl.class),
        Component.create(IUPnPAccess.class, UPnPAccessImpl.class),
        Component.create(IReceiver.class, XMPPReceiver.class),
        Component.create(ITransmitter.class, XMPPTransmitter.class),

        // Observables
        Component.create(FileReplacementInProgressObservable.class),
        Component.create(InvitationProcessObservable.class),
        Component.create(ProjectNegotiationObservable.class),
        Component.create(IsInconsistentObservable.class),
        Component.create(SessionIDObservable.class),
        Component.create(SarosSessionObservable.class),
        Component.create(VoIPSessionObservable.class),
        Component.create(VideoSessionObservable.class),
        Component.create(AwarenessInformationCollector.class),
        Component.create(FollowingActivitiesManager.class),

        // Handlers
        Component.create(CancelInviteHandler.class),
        Component.create(CancelProjectSharingHandler.class),
        Component.create(InvitationHandler.class),
        Component.create(LeaveAndKickHandler.class),

        // FIXME: remove all extensions providers here !

        // Extension Providers
        Component
            .create(IncomingTransferObject.IncomingTransferObjectExtensionProvider.class), };

    @Override
    public void createComponents(MutablePicoContainer container) {
        for (Component component : Arrays.asList(components))
            container.addComponent(component.getBindKey(),
                component.getImplementation());
    }
}
