/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * (c) Karl Beecher - 2011
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

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

    assertEquals(Compatibility.OK, result.getCompatibility());
  }

  @Test
  public void testlocalVersionOlder() {
    Version local = Version.parseVersion("1.1.1.r1");
    Version remote = Version.parseVersion("1.1.2.r1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.OLDER, result.getCompatibility());
  }

  @Test
  public void testlocalVersionNewer() {
    Version local = Version.parseVersion("1.1.2.r1");
    Version remote = Version.parseVersion("1.1.1.r1");

    init(local, remote);

    VersionCompatibilityResult result =
        versionManagerLocal.determineVersionCompatibility(bobContact);

    assertEquals(Compatibility.NEWER, result.getCompatibility());
  }
}
