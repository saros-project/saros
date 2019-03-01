package saros.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import saros.net.xmpp.JID;

public class JIDTest {

  private JID jid;
  private JID jidWithResource;
  private JID servicePerspectiveJID;
  private JID servicePerspectiveJIDWithResource;

  @Before
  public void setUp() throws Exception {
    jid = new JID("userXYZ@jabber.org");
    jidWithResource = new JID("userXYZ@jabber.org/Saros");
    servicePerspectiveJID =
        JID.createFromServicePerspective(
            "saros128280129@conference.jabber.ccc.de/userXYZ@jabber.org");
    servicePerspectiveJIDWithResource =
        JID.createFromServicePerspective(
            "saros128280129@conference.jabber.ccc.de/userXYZ@jabber.org/Saros");
  }

  @Test
  public void testWellformed() {

    assertTrue(
        "nasty!#$%()*+,-.;=?[\\]^_`{|}~node@example.com is a valid JID",
        new JID("nasty!#$%()*+,-.;=?[\\]^_`{|}~node@example.com").isValid());

    assertTrue("foo.com is a valid domain for a JID", new JID("foo.com").isValid());

    assertTrue("foo is a valid domain for a JID", new JID("foo").isValid());

    assertTrue("IP6 addr. is a valid domain for a JID", new JID("::1").isValid());

    assertTrue(
        "IP6 addr. is a valid domain for a JID",
        new JID("user@2001:cdba:0000:0000:0000:0000:3257:9652").isValid());

    assertTrue("IP4 addr. is a valid domain for a JID", new JID("127.0.0.1").isValid());

    assertTrue(
        "unicode support missing: cannot encode: الجزيرة (aljazeera)",
        new JID("news@االجزيرة").isValid());
  }

  @Test
  public void testMalformed() {
    assertFalse("'foo@bar@foo.bar' is not a valid JID", new JID("foo@bar@foo.bar").isValid());

    assertFalse("space characters are not allowed in a JID", new JID("foo bar@example").isValid());

    assertFalse("' is not allowed in a JID", new JID("foo'sbar@example").isValid());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor() {
    new JID((String) null);
  }

  @Test
  public void testGetName() {
    assertEquals("userXYZ", jid.getName());
    assertEquals("userXYZ", jidWithResource.getName());
    assertEquals("userXYZ", servicePerspectiveJID.getName());
    assertEquals("userXYZ", servicePerspectiveJIDWithResource.getName());
  }

  @Test
  public void testGetDomain() {
    assertEquals("jabber.org", jid.getDomain());
    assertEquals("jabber.org", jidWithResource.getDomain());
    assertEquals("jabber.org", servicePerspectiveJID.getDomain());
    assertEquals("jabber.org", servicePerspectiveJIDWithResource.getDomain());
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
    assertEquals("userXYZ@jabber.org", servicePerspectiveJIDWithResource.getBase());
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

    assertEquals(servicePerspectiveJIDWithResource, servicePerspectiveJIDWithResource);

    /*
     * requires JUNIT 4.12 which is not shipped with Eclipse 3.7 and cannot
     * be updated as well
     */
    // assertNotEquals(jid, new JID("bob@jabber.org"));
    // assertNotEquals(jidWithResource, new JID("bob@jabber.org"));
    // assertNotEquals(servicePerspectiveJID, new JID("bob@jabber.org"));
    // assertNotEquals(servicePerspectiveJIDWithResource, new
    // JID("bob@jabber.org"));
  }
}
