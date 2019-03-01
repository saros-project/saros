package de.fu_berlin.inf.dpp.context;

import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatService;
import de.fu_berlin.inf.dpp.communication.chat.single.SingleUserChatService;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.editor.colorstorage.ColorIDSetStorage;
import de.fu_berlin.inf.dpp.monitoring.remote.RemoteProgressManager;
import de.fu_berlin.inf.dpp.negotiation.NegotiationFactory;
import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.TCPServer;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.mdns.MDNSService;
import de.fu_berlin.inf.dpp.net.stream.IBBStreamService;
import de.fu_berlin.inf.dpp.net.stream.IStreamService;
import de.fu_berlin.inf.dpp.net.stream.Socks5StreamService;
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
import de.fu_berlin.inf.dpp.session.ColorNegotiationHook;
import de.fu_berlin.inf.dpp.session.ProjectNegotiationTypeHook;
import de.fu_berlin.inf.dpp.session.SarosSessionManager;
import de.fu_berlin.inf.dpp.versioning.VersionManager;
import java.util.Arrays;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

/**
 * This is the basic core factory for Saros. All components that are created by this factory
 * <b>must</b> be working on any platform the application is running on.
 *
 * @author srossbach
 */
public class CoreContextFactory extends AbstractContextFactory {

  /**
   * Must not be static in order to avoid heavy work during class initialization
   *
   * @see <a href="https://github.com/saros-project/saros/commit/237daca">commit&nbsp;237daca</a>
   */
  private final Component[] getContextComponents() {
    return new Component[] {

      // Facades
      Component.create(ConnectionHandler.class),

      // Version support
      Component.create(VersionManager.class),
      Component.create(MultiUserChatService.class),
      Component.create(SingleUserChatService.class),
      Component.create(SarosSessionManager.class),
      Component.create(XMPPAccountStore.class),
      Component.create(ColorIDSetStorage.class),

      // Negotiation
      Component.create(NegotiationFactory.class),

      // Negotiation hooks
      Component.create(SessionNegotiationHookManager.class),
      Component.create(ColorNegotiationHook.class),
      Component.create(ProjectNegotiationTypeHook.class),

      // Network
      Component.create(DispatchThreadContext.class),
      Component.create(IConnectionManager.class, DataTransferManager.class),
      Component.create(DiscoveryManager.class),
      Component.create(
          BindKey.bindKey(IStreamService.class, IContextKeyBindings.IBBStreamService.class),
          IBBStreamService.class),
      Component.create(
          BindKey.bindKey(IStreamService.class, IContextKeyBindings.Socks5StreamService.class),
          Socks5StreamService.class),
      Component.create(RosterTracker.class),
      Component.create(XMPPConnectionService.class),
      Component.create(MDNSService.class),
      Component.create(TCPServer.class),
      Component.create(IStunService.class, StunServiceImpl.class),
      Component.create(SubscriptionHandler.class),
      Component.create(IUPnPService.class, UPnPServiceImpl.class),
      Component.create(IUPnPAccess.class, UPnPAccessImpl.class),
      Component.create(IReceiver.class, XMPPReceiver.class),
      Component.create(ITransmitter.class, XMPPTransmitter.class),
      Component.create(RemoteProgressManager.class),

      // Observables
      Component.create(FileReplacementInProgressObservable.class),
      Component.create(IsInconsistentObservable.class)
    };
  }

  @Override
  public void createComponents(MutablePicoContainer container) {
    for (Component component : Arrays.asList(getContextComponents()))
      container.addComponent(component.getBindKey(), component.getImplementation());
  }
}
