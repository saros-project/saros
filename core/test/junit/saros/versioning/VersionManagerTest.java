package saros.versioning;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import org.jivesoftware.smack.packet.IQ;
import org.junit.Before;
import org.junit.Test;
import saros.communication.InfoManager;
import saros.communication.extensions.InfoExchangeExtension;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.test.fakes.net.FakeConnectionFactory;
import saros.test.fakes.net.FakeConnectionFactory.FakeConnectionFactoryResult;
import saros.test.mocks.SarosMocks;

public class VersionManagerTest {

  private ITransmitter aliceTransmitter;
  private IReceiver aliceReceiver;
  private VersionManager versionManagerLocal;

  private final JID aliceJID = new JID("alice@alice.com/Saros");
  private final JID bobJID = new JID("bob@bob.com/Saros");

  private XMPPContact bobContact;

  @Before
  public void setUp() {
    FakeConnectionFactoryResult result =
        FakeConnectionFactory.createConnections(aliceJID, bobJID).get();

    aliceReceiver = result.getReceiver(aliceJID);
    aliceTransmitter = result.getTransmitter(aliceJID);
  }

  private void init(Version local, Version remote) {
    XMPPContactsService aliceContactsService = SarosMocks.contactsServiceMockFor(bobJID);
    bobContact = aliceContactsService.getContact(bobJID.getRAW()).get();

    InfoManager infoManager =
        new InfoManager(aliceReceiver, aliceTransmitter, aliceContactsService);
    versionManagerLocal = new VersionManager(local.toString(), infoManager);

    HashMap<String, String> info = new HashMap<>();
    info.put(VersionManager.VERSION_KEY, remote.toString());
    InfoExchangeExtension versionExchangeResponse = new InfoExchangeExtension(info);

    IQ reply = InfoExchangeExtension.PROVIDER.createIQ(versionExchangeResponse);
    reply.setType(IQ.Type.SET);
    reply.setTo(aliceJID.getRAW());
    aliceReceiver.processPacket(reply);
  }

  @Test
  public void testVersionsSame() {

    Version local = Version.parseVersion("1.1.1");
    Version remote = Version.parseVersion("1.1.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OK, result.getCompatibility());
  }

  @Test
  public void testLocalMajorVersionOlder() {
    Version local = Version.parseVersion("1.1.1");
    Version remote = Version.parseVersion("2.1.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OLDER, result.getCompatibility());
  }

  @Test
  public void testLocalMinorVersionOlder() {
    Version local = Version.parseVersion("1.1.1");
    Version remote = Version.parseVersion("1.2.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OLDER, result.getCompatibility());
  }

  @Test
  public void testLocalMajorVersionNewer() {
    Version local = Version.parseVersion("2.1.1");
    Version remote = Version.parseVersion("1.1.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.NEWER, result.getCompatibility());
  }

  @Test
  public void testLocalMinorVersionNewer() {
    Version local = Version.parseVersion("1.2.1");
    Version remote = Version.parseVersion("1.1.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.NEWER, result.getCompatibility());
  }

  @Test
  public void testLocalMicroVersionNewer() {
    Version local = Version.parseVersion("1.1.2");
    Version remote = Version.parseVersion("1.1.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OK, result.getCompatibility());
  }

  @Test
  public void testLocalMicroVersionOlder() {
    Version local = Version.parseVersion("1.1.1");
    Version remote = Version.parseVersion("1.1.2");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OK, result.getCompatibility());
  }

  @Test
  public void testVersionsSameIncludingQualifier() {

    Version local = Version.parseVersion("1.1.1.r1");
    Version remote = Version.parseVersion("1.1.1.r1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OK, result.getCompatibility());
  }

  @Test
  public void testVersionsSameOnlyQualifierDiffers() {
    Version local = Version.parseVersion("1.1.1.r1");
    Version remote = Version.parseVersion("1.1.1.r2");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.QUALIFIER_MISMATCH, result.getCompatibility());
  }

  @Test
  public void testVersionsDifferentMicroWithSameQualifier() {
    Version local = Version.parseVersion("1.1.1.r1");
    Version remote = Version.parseVersion("1.1.2.r1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.QUALIFIER_MISMATCH, result.getCompatibility());
  }
}
