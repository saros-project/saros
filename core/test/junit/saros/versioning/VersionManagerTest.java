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
  private static final String DUMMY_IMPLEMENTATION_IDENTIFIER = "DUMMY";

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

    String[] localVersion = local.toString().split(Version.IMPLEMENTATION_SEPARATOR);
    assert localVersion.length == 2;

    InfoManager infoManager =
        new InfoManager(aliceReceiver, aliceTransmitter, aliceContactsService);
    versionManagerLocal = new VersionManager(localVersion[0], localVersion[1], infoManager);

    HashMap<String, String> info = new HashMap<>();
    info.put(VersionManager.VERSION_KEY, remote.toString());
    InfoExchangeExtension versionExchangeResponse = new InfoExchangeExtension(info);

    IQ reply = InfoExchangeExtension.PROVIDER.createIQ(versionExchangeResponse);
    reply.setType(IQ.Type.SET);
    reply.setTo(aliceJID.getRAW());
    aliceReceiver.processPacket(reply);
  }

  /**
   * Returns a version object for the given version numbers. The object is created using {@link
   * #DUMMY_IMPLEMENTATION_IDENTIFIER} as the implementation identifier.
   *
   * <p>This helper method can be used when the implementation identifier is not of interest for the
   * test.
   *
   * @param versionNumbers the version numbers to parse
   * @return a version object for the given version numbers
   */
  private static Version getVersion(String versionNumbers) {
    return Version.parseVersion(DUMMY_IMPLEMENTATION_IDENTIFIER, versionNumbers);
  }

  @Test
  public void testVersionsSame() {
    Version local = Version.parseVersion("A", "1.1.1");
    Version remote = Version.parseVersion("A", "1.1.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OK, result.getCompatibility());
  }

  @Test
  public void testVersionsDifferentImplementationIdentifiers() {
    Version local = Version.parseVersion("A", "1.1.1");
    Version remote = Version.parseVersion("B", "1.1.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.INCOMPATIBLE_IMPLEMENTATIONS, result.getCompatibility());
  }

  @Test
  public void testVersionsDifferentImplementationIdentifiersWithQualifier() {
    Version local = Version.parseVersion("A", "1.1.1.r1");
    Version remote = Version.parseVersion("B", "1.1.1.r1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.INCOMPATIBLE_IMPLEMENTATIONS, result.getCompatibility());
  }

  @Test
  public void testLocalMajorVersionOlder() {
    Version local = getVersion("1.1.1");
    Version remote = getVersion("2.1.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OLDER, result.getCompatibility());
  }

  @Test
  public void testLocalMinorVersionOlder() {
    Version local = getVersion("1.1.1");
    Version remote = getVersion("1.2.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OLDER, result.getCompatibility());
  }

  @Test
  public void testLocalMajorVersionNewer() {
    Version local = getVersion("2.1.1");
    Version remote = getVersion("1.1.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.NEWER, result.getCompatibility());
  }

  @Test
  public void testLocalMinorVersionNewer() {
    Version local = getVersion("1.2.1");
    Version remote = getVersion("1.1.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.NEWER, result.getCompatibility());
  }

  @Test
  public void testLocalMicroVersionNewer() {
    Version local = getVersion("1.1.2");
    Version remote = getVersion("1.1.1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OK, result.getCompatibility());
  }

  @Test
  public void testLocalMicroVersionOlder() {
    Version local = getVersion("1.1.1");
    Version remote = getVersion("1.1.2");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OK, result.getCompatibility());
  }

  @Test
  public void testVersionsSameIncludingQualifier() {

    Version local = getVersion("1.1.1.r1");
    Version remote = getVersion("1.1.1.r1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OK, result.getCompatibility());
  }

  @Test
  public void testVersionsSameOnlyQualifierDiffers() {
    Version local = getVersion("1.1.1.r1");
    Version remote = getVersion("1.1.1.r2");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.QUALIFIER_MISMATCH, result.getCompatibility());
  }

  @Test
  public void testVersionsDifferentMicroWithSameQualifier() {
    Version local = getVersion("1.1.1.r1");
    Version remote = getVersion("1.1.2.r1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.QUALIFIER_MISMATCH, result.getCompatibility());
  }
}
