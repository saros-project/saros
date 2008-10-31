/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
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
package de.fu_berlin.inf.dpp.test;

import junit.framework.TestCase;
import de.fu_berlin.inf.dpp.net.JID;

public class JIDTest extends TestCase {
    private JID id;
    private JID idWithResource;

    @Override
    protected void setUp() throws Exception {
	id = new JID("riad@jabber.org");
	idWithResource = new JID("riad@jabber.org/saros");
    }

    public void testMalformatedJID() {
	try {
	    new JID("riad");
	    fail();
	} catch (IllegalArgumentException e) {
	    // okay
	}
    }

    public void testGetUser() {
	assertEquals("riad", id.getName());
	assertEquals("riad", idWithResource.getName());
    }

    public void testGetHost() {
	assertEquals("jabber.org", id.getDomain());
	assertEquals("jabber.org", idWithResource.getDomain());
    }

    public void testGetResource() {
	assertEquals("", id.getResource());
	assertEquals("saros", idWithResource.getResource());
    }

    public void testGetBase() {
	assertEquals("riad@jabber.org", id.getBase());
	assertEquals("riad@jabber.org", idWithResource.getBase());
    }

    public void testEquals() {
	assertEquals(id, id);
	assertEquals(idWithResource, idWithResource);
	assertEquals(id, idWithResource);

	assert !id.equals(new JID("bob@jabber.org"));
    }
}
