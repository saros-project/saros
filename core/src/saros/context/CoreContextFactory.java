package saros.context;

import java.util.Arrays;
import saros.account.XMPPAccountStore;
import saros.communication.InfoManager;
import saros.communication.chat.muc.MultiUserChatService;
import saros.communication.chat.single.SingleUserChatService;
import saros.communication.connection.ConnectionHandler;
import saros.concurrent.watchdog.IsInconsistentObservable;
import saros.editor.colorstorage.ColorIDSetStorage;
import saros.monitoring.remote.RemoteProgressManager;
import saros.negotiation.AdditionalResourceDataFactory;
import saros.negotiation.SessionNegotiationFactory;
import saros.negotiation.hooks.SessionNegotiationHookManager;
import saros.net.DispatchThreadContext;
import saros.net.IConnectionManager;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.internal.DataTransferManager;
import saros.net.internal.XMPPReceiver;
import saros.net.internal.XMPPTransmitter;
import saros.net.stream.IBBStreamService;
import saros.net.stream.IStreamService;
import saros.net.stream.Socks5StreamService;
import saros.net.stun.IStunService;
import saros.net.stun.internal.StunServiceImpl;
import saros.net.upnp.IUPnPAccess;
import saros.net.upnp.IUPnPService;
import saros.net.upnp.internal.UPnPAccessImpl;
import saros.net.upnp.internal.UPnPServiceImpl;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.net.xmpp.filetransfer.XMPPFileTransferManager;
import saros.net.xmpp.subscription.SubscriptionHandler;
import saros.observables.FileReplacementInProgressObservable;
import saros.repackaged.picocontainer.BindKey;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.ColorNegotiationHook;
import saros.session.ResourceNegotiationTypeHook;
import saros.session.SarosSessionManager;
import saros.versioning.VersionManager;

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
      Component.create(InfoManager.class),
      Component.create(VersionManager.class),
      Component.create(MultiUserChatService.class),
      Component.create(SingleUserChatService.class),
      Component.create(SarosSessionManager.class),
      Component.create(XMPPAccountStore.class),
      Component.create(ColorIDSetStorage.class),

      // Negotiation
      Component.create(SessionNegotiationFactory.class),
      Component.create(AdditionalResourceDataFactory.class),

      // Negotiation hooks
      Component.create(SessionNegotiationHookManager.class),
      Component.create(ColorNegotiationHook.class),
      Component.create(ResourceNegotiationTypeHook.class),

      // Network
      Component.create(DispatchThreadContext.class),
      Component.create(IConnectionManager.class, DataTransferManager.class),
      Component.create(
          BindKey.bindKey(IStreamService.class, IContextKeyBindings.IBBStreamService.class),
          IBBStreamService.class),
      Component.create(
          BindKey.bindKey(IStreamService.class, IContextKeyBindings.Socks5StreamService.class),
          Socks5StreamService.class),
      Component.create(XMPPConnectionService.class),
      Component.create(IStunService.class, StunServiceImpl.class),
      Component.create(SubscriptionHandler.class),
      Component.create(XMPPContactsService.class),
      Component.create(XMPPFileTransferManager.class),
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
