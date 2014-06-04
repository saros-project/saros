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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.xmpp.JID;

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

    @Test
    @Ignore
    public void testWellformedJID() {
        assertTrue("'foo.com' is a valid domain for a JID",
            new JID("foo.com").isValid());
        // new org.xmpp.packet.JID("foo.com"); OK

        assertTrue("'foo' is a valid domain for a JID",
            new JID("foo").isValid());
        // new org.xmpp.packet.JID("foo"); OK

        assertTrue("IP6 addr. is a valid domain for a JID", new JID(
            "[2001:0:5ef5:79fd:2887:225e:a7b4]").isValid());

        // new org.xmpp.packet.JID("[2001:0:5ef5:79fd:2887:225e:a7b4]"); NOK,
        // old RFC

        assertTrue("IP4 addr. is a valid domain for a JID",
            new JID("127.0.0.1").isValid());

        // new org.xmpp.packet.JID("[2001:0:5ef5:79fd:2887:225e:a7b4]"); OK

    }

    @Test
    public void testMalformedJID() {
        assertFalse("'foo@bar@foo.bar' is not a valid JID", new JID(
            "foo@bar@foo.bar").isValid());
    }

    /**
     * TODO Test also other malformatted JID formats.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testJIDContructorWithNullString() {
        new JID((String) null);
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
