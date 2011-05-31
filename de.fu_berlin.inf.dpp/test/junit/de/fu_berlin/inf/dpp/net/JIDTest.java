/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * (c) Björn Kahlert - 2010
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
package de.fu_berlin.inf.dpp.net;

import static org.junit.Assert.assertEquals;

import org.jivesoftware.smack.RosterEntry;
import org.junit.Before;
import org.junit.Test;

public class JIDTest {

    private JID jid;
    private JID jidWithResource;
    private JID servicePerspectiveJID;
    private JID servicePerspectiveJIDWithResource;

    @Before
    public void setUp() throws Exception {
        jid = new JID("userXYZ@jabber.org");
        jidWithResource = new JID("userXYZ@jabber.org/Saros");
        servicePerspectiveJID = JID
            .createFromServicePerspective("saros128280129@conference.jabber.ccc.de/userXYZ@jabber.org");
        servicePerspectiveJIDWithResource = JID
            .createFromServicePerspective("saros128280129@conference.jabber.ccc.de/userXYZ@jabber.org/Saros");
    }

    /**
     * TODO Test also other malformatted JID formats.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMalformatedJID() {
        new JID((String) null);
    }

    /**
     * TODO Test also other malformatted JID formats.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMalformatedJID2() {
        new JID((RosterEntry) null);
    }

    @Test
    public void testGetUser() {
        assertEquals("userXYZ", jid.getName());
        assertEquals("userXYZ", jidWithResource.getName());
        assertEquals("userXYZ", servicePerspectiveJID.getName());
        assertEquals("userXYZ", servicePerspectiveJIDWithResource.getName());
    }

    @Test
    public void testGetHost() {
        assertEquals("jabber.org", jid.getDomain());
        assertEquals("jabber.org", jidWithResource.getDomain());
        assertEquals("jabber.org", servicePerspectiveJID.getDomain());
        assertEquals("jabber.org",
            servicePerspectiveJIDWithResource.getDomain());
    }

    @Test
    public void testGetResource() {
        assertEquals("", jid.getResource());
        assertEquals("Saros", jidWithResource.getResource());
        assertEquals("", servicePerspectiveJID.getResource());
        assertEquals("Saros", servicePerspectiveJIDWithResource.getResource());
    }

    @Test
    public void testGetBase() {
        assertEquals("userXYZ@jabber.org", jid.getBase());
        assertEquals("userXYZ@jabber.org", jidWithResource.getBase());
        assertEquals("userXYZ@jabber.org", servicePerspectiveJID.getBase());
        assertEquals("userXYZ@jabber.org",
            servicePerspectiveJIDWithResource.getBase());
    }

    @Test
    public void testEquals() {
        assertEquals(jid, jid);
        assertEquals(jid, jidWithResource);
        assertEquals(jid, servicePerspectiveJID);
        assertEquals(jid, servicePerspectiveJIDWithResource);

        assertEquals(jidWithResource, jidWithResource);
        assertEquals(jidWithResource, servicePerspectiveJID);
        assertEquals(jidWithResource, servicePerspectiveJIDWithResource);

        assertEquals(servicePerspectiveJID, servicePerspectiveJID);
        assertEquals(servicePerspectiveJID, servicePerspectiveJIDWithResource);

        assertEquals(servicePerspectiveJIDWithResource,
            servicePerspectiveJIDWithResource);

        assert !jid.equals(new JID("bob@jabber.org"));
        assert !jidWithResource.equals(new JID("bob@jabber.org"));
        assert !servicePerspectiveJID.equals(new JID("bob@jabber.org"));
        assert !servicePerspectiveJIDWithResource.equals(new JID(
            "bob@jabber.org"));
    }
}
