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

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.test.fakes.net.FakeConnectionFactory;
import saros.test.fakes.net.FakeConnectionFactory.FakeConnectionFactoryResult;

public class VersionManagerTest {

  private ITransmitter aliceTransmitter;
  private ITransmitter bobTransmitter;

  private IReceiver aliceReceiver;
  private IReceiver bobReceiver;

  private VersionManager versionManagerRemote;
  private VersionManager versionManagerLocal;

  private final JID aliceJID = new JID("alice@alice.com/Saros");
  private final JID bobJID = new JID("bob@bob.com/Saros");

  @Before
  public void setUp() {
    FakeConnectionFactoryResult result =
        FakeConnectionFactory.createConnections(aliceJID, bobJID).get();

    aliceReceiver = result.getReceiver(aliceJID);
    bobReceiver = result.getReceiver(bobJID);

    aliceTransmitter = result.getTransmitter(aliceJID);
    bobTransmitter = result.getTransmitter(bobJID);
  }

  private void init(Version local, Version remote) {

    versionManagerLocal = new VersionManager(local.toString(), aliceReceiver, aliceTransmitter);

    versionManagerRemote = new VersionManager(remote.toString(), bobReceiver, bobTransmitter);
  }

  @Test
  public void testVersionsSame() {

    Version local = Version.parseVersion("1.1.1.r1");
    Version remote = Version.parseVersion("1.1.1.r1");

    init(local, remote);

    VersionCompatibilityResult result = versionManagerLocal.determineVersionCompatibility(bobJID);

    assertEquals(Compatibility.OK, result.getCompatibility());
  }

  @Test
  public void testVersionsSameOnlyQualifierDiffers() {

    Version local = Version.parseVersion("1.1.1.r1");
    Version remote = Version.parseVersion("1.1.1.r2");

    init(local, remote);

    VersionCompatibilityResult result = versionManagerLocal.determineVersionCompatibility(bobJID);

    assertEquals(Compatibility.OK, result.getCompatibility());
  }

  @Test
  public void testlocalVersionsTooOld() {

    Version local = Version.parseVersion("1.1.1.r1");
    Version remote = Version.parseVersion("1.1.2.r1");

    init(local, remote);

    VersionCompatibilityResult result = versionManagerLocal.determineVersionCompatibility(bobJID);

    assertEquals(Compatibility.TOO_OLD, result.getCompatibility());
  }

  @Test
  public void testlocalVersionsTooNew() {

    Version local = Version.parseVersion("1.1.2.r1");
    Version remote = Version.parseVersion("1.1.1.r1");

    init(local, remote);

    VersionCompatibilityResult result = versionManagerLocal.determineVersionCompatibility(bobJID);

    assertEquals(Compatibility.TOO_NEW, result.getCompatibility());
  }

  @Test
  public void testLocalVersionTooNewButCompatible() throws Exception {

    Version local = Version.parseVersion("999999.1.2.r1");
    Version remote = Version.parseVersion("999999.1.1.r1");

    init(local, remote);

    Properties chart = new Properties();

    chart.put(local.toString(), remote.toString());
    versionManagerLocal.setCompatibilityChart(chart);

    assertEquals(Compatibility.TOO_OLD, versionManagerRemote.determineCompatibility(remote, local));

    assertEquals(Compatibility.OK, versionManagerLocal.determineCompatibility(local, remote));

    // do the "real" network check:

    VersionCompatibilityResult resultRemote =
        versionManagerRemote.determineVersionCompatibility(aliceJID);

    VersionCompatibilityResult resultLocal =
        versionManagerLocal.determineVersionCompatibility(bobJID);

    assertEquals(resultLocal.getCompatibility(), resultRemote.getCompatibility());
  }

  @Test
  public void testLocalVersionTooOldButCompatible() throws Exception {
    Version local = Version.parseVersion("1999999.1.1.r1");
    Version remote = Version.parseVersion("1999999.1.2.r1");

    init(local, remote);

    Properties chart = new Properties();

    chart.put(remote.toString(), local.toString());
    versionManagerRemote.setCompatibilityChart(chart);

    assertEquals(Compatibility.TOO_OLD, versionManagerLocal.determineCompatibility(local, remote));

    assertEquals(Compatibility.OK, versionManagerRemote.determineCompatibility(remote, local));

    // do the "real" network check:

    VersionCompatibilityResult resultRemote =
        versionManagerRemote.determineVersionCompatibility(aliceJID);

    VersionCompatibilityResult resultLocal =
        versionManagerLocal.determineVersionCompatibility(bobJID);

    assertEquals(resultLocal.getCompatibility(), resultRemote.getCompatibility());
  }
}
