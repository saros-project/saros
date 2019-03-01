package saros.whiteboard.net;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;
import saros.whiteboard.sxe.constants.SXEMessageType;
import saros.whiteboard.sxe.net.SXEMessage;
import saros.whiteboard.sxe.net.SXEMessageReader;
import saros.whiteboard.sxe.net.SXESession;

/**
 * The Smack extension provider that will parse the SXE messages if initialized.
 *
 * @author jurke
 */
public class SXEExtensionProvider implements PacketExtensionProvider {

  private static final SXEExtensionProvider instance = new SXEExtensionProvider();

  public static SXEExtensionProvider getInstance() {
    return instance;
  }

  private final SXEMessageReader reader = new SXEMessageReader();

  /** The constructor will register the provider in the Smack API */
  private SXEExtensionProvider() {
    ProviderManager providerManager = ProviderManager.getInstance();
    providerManager.addExtensionProvider(SXEMessage.SXE_TAG, SXEMessage.SXE_XMLNS, this);
  }

  @Override
  public PacketExtension parseExtension(XmlPullParser xpp) throws Exception {

    SXEExtension pe = new SXEExtension();

    SXEMessage message = reader.parseMessage(xpp);

    pe.setMessage(message);

    return pe;
  }

  public SXEPacketFilter getInvitationPacketFilter() {
    return new SXEPacketFilter(SXEMessageType.STATE_OFFER);
  }

  public SXEPacketFilter getRecordsPacketFilter(SXESession session) {
    return new SXEPacketFilter(session, SXEMessageType.RECORDS);
  }
}
