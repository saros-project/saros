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

package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.fakes.net.FakeConnectionFactory;
import de.fu_berlin.inf.dpp.test.fakes.net.FakeConnectionFactory.FakeConnectionFactoryResult;
import de.fu_berlin.inf.dpp.util.VersionManager.Compatibility;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

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
        FakeConnectionFactoryResult result = FakeConnectionFactory
            .createConnections(aliceJID, bobJID).get();

        aliceReceiver = result.getReceiver(aliceJID);
        bobReceiver = result.getReceiver(bobJID);

        aliceTransmitter = result.getTransmitter(aliceJID);
        bobTransmitter = result.getTransmitter(bobJID);
    }

    private void init(Version local, Version remote) {

        versionManagerLocal = new VersionManager(local, aliceReceiver,
            aliceTransmitter);

        versionManagerRemote = new VersionManager(remote, bobReceiver,
            bobTransmitter);
    }

    @Test
    public void testVersionsSame() {

        Version local = new Version("1.1.1.r1");
        Version remote = new Version("1.1.1.r1");

        init(local, remote);

        VersionInfo info = versionManagerLocal.determineCompatibility(bobJID);

        assertEquals(Compatibility.OK, info.compatibility);
    }

    @Test
    public void testlocalVersionsTooOld() {

        Version local = new Version("1.1.1.r1");
        Version remote = new Version("1.1.2.r1");

        init(local, remote);

        VersionInfo info = versionManagerLocal.determineCompatibility(bobJID);

        assertEquals(Compatibility.TOO_OLD, info.compatibility);
    }

    @Test
    public void testlocalVersionsTooNew() {

        Version local = new Version("1.1.2.r1");
        Version remote = new Version("1.1.1.r1");

        init(local, remote);

        VersionInfo info = versionManagerLocal.determineCompatibility(bobJID);

        assertEquals(Compatibility.TOO_NEW, info.compatibility);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLocalVersionTooNewButCompatible() throws Exception {
        Version local = new Version("999999.1.2.r1");
        Version remote = new Version("999999.1.1.r1");

        init(local, remote);

        Field f = versionManagerLocal.getClass().getDeclaredField(
            "compatibilityChart");

        f.setAccessible(true);

        Map<Version, List<Version>> chart = (Map<Version, List<Version>>) f
            .get(versionManagerLocal);

        chart.put(local, Arrays.asList(remote));

        VersionInfo info = new VersionInfo();
        info.version = remote.toString();

        info.compatibility = versionManagerRemote.determineCompatibility(
            remote, local);

        assertEquals(Compatibility.TOO_OLD,
            versionManagerRemote.determineCompatibility(remote, local));

        assertEquals(Compatibility.OK,
            versionManagerLocal.determineCompatibility(info).compatibility);

        // do the "real" network check:

        VersionInfo remoteInfo = versionManagerRemote
            .determineCompatibility(aliceJID);

        VersionInfo localInfo = versionManagerLocal
            .determineCompatibility(bobJID);

        assertEquals(localInfo.compatibility, remoteInfo.compatibility);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLocalVersionTooOldButCompatible() throws Exception {
        Version local = new Version("1999999.1.1.r1");
        Version remote = new Version("1999999.1.2.r1");

        init(local, remote);

        Field f = versionManagerLocal.getClass().getDeclaredField(
            "compatibilityChart");

        f.setAccessible(true);

        Map<Version, List<Version>> chart = (Map<Version, List<Version>>) f
            .get(versionManagerRemote);

        chart.put(remote, Arrays.asList(local));

        VersionInfo info = new VersionInfo();
        info.version = remote.toString();

        info.compatibility = versionManagerRemote.determineCompatibility(
            remote, local);

        assertEquals(Compatibility.TOO_OLD,
            versionManagerLocal.determineCompatibility(local, remote));

        assertEquals(Compatibility.OK,
            versionManagerLocal.determineCompatibility(info).compatibility);

        // do the "real" network check:

        VersionInfo remoteInfo = versionManagerRemote
            .determineCompatibility(aliceJID);

        VersionInfo localInfo = versionManagerLocal
            .determineCompatibility(bobJID);

        assertEquals(localInfo.compatibility, remoteInfo.compatibility);
    }
}
