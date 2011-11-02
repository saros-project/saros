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

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import de.fu_berlin.inf.dpp.Constants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.internal.SarosTestNet;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.util.VersionManager.Compatibility;

public class VersionManagerTest {

    Version older;
    Version newer;
    VersionManager versionManager;

    Saros sarosObj;
    SarosTestNet net;
    XMPPTransmitter transmitter;

    @Before
    public void SetUp() {
        older = new Version("11.5.6.r3294");
        newer = new Version("11.7.1.r3426");

        // session = new SarosSessionStub().getSaros();
        sarosObj = createMock(Saros.class);
        net = new SarosTestNet(Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_SERVICE_NAME);

        versionManager = new VersionManager(sarosObj, net.xmppReceiver,
            net.xmppTransmitter);
    }

    @Test
    public void testVersionsSame() {
        Compatibility comp = versionManager
            .determineCompatibility(newer, newer);
        assertTrue(comp == Compatibility.OK);
    }

    @Test
    public void testLocalIsOlder() {
        Compatibility comp = versionManager
            .determineCompatibility(older, newer);
        assertTrue(comp == Compatibility.TOO_OLD);
    }

    @Test
    public void testUsingChart() {
        // By making the local version newer, we make the VersionManager consult
        // the compatibility chart. The versions we've chosen are not
        // compatible.
        Compatibility comp = versionManager
            .determineCompatibility(newer, older);
        assertTrue(comp == Compatibility.TOO_NEW);
    }
}
