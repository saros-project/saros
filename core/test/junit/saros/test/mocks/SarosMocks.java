package saros.test.mocks;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Optional;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;

/**
 * This class provides several mock-creating methods for various purposes. It makes use of EasyMock
 * and PowerMock.
 */
public class SarosMocks {

  /**
   * Create a mocked XMPPContactsService.
   *
   * @param jid
   * @return a mocked XMPPContactsService
   */
  public static XMPPContactsService contactsServiceMockFor(JID jid) {
    XMPPContact contactMock = createMock(XMPPContact.class);
    expect(contactMock.getSarosJid()).andStubReturn(Optional.of(jid));
    replay(contactMock);

    XMPPContactsService contactsServiceMock = createMock(XMPPContactsService.class);

    contactsServiceMock.addListener(anyObject());
    expect(contactsServiceMock.getContact(anyObject(String.class)))
        .andStubReturn(Optional.of(contactMock));
    replay(contactsServiceMock);

    return contactsServiceMock;
  }
}
