/**
 * 
 */
package de.fu_berlin.inf.dpp.net;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Registration;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 */
public class XMPPUtilTest {
    private static final JID ALICE_JID = new JID(
        "alice_stf@saros-con.imp.fu-berlin.de");

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.XMPPUtil#getRegistrationInfo(java.lang.String, org.jivesoftware.smack.Connection)}
     * .
     * 
     * @throws XMPPException
     */
    @Test
    @Ignore("not testing anything")
    public void testGetRegistrationInfo() throws XMPPException {
        Connection connectionAlice = new XMPPConnection(ALICE_JID.getDomain());
        connectionAlice.connect();

        Registration reg = XMPPUtil.getRegistrationInfo(
            "alice_stf@saros-con.imp.fu-berlin.de", connectionAlice);

        System.out.println(reg.getFrom());
        System.out.println(reg.getTo());
        System.out.println(reg.toXML());
        System.out.println(reg.getType());
        System.out.println(reg.getAttributes());

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.XMPPUtil#getRegistrationInfo(java.lang.String, org.jivesoftware.smack.Connection)}
     * .
     * 
     * @throws XMPPException
     */
    @Test
    @Ignore("not testing anything")
    public void testGetRegistrationInfo2() throws XMPPException {
        Connection connectionAlice = new XMPPConnection(ALICE_JID.getDomain());
        connectionAlice.connect();

        Registration reg = XMPPUtil.getRegistrationInfo(
            "alice_stf@saros-con.imp.fu-berlin.de", connectionAlice);

        System.out.println(reg.getFrom());
        System.out.println(reg.getTo());
        System.out.println(reg.toXML());
        System.out.println(reg.getType());
        System.out.println(reg.getAttributes());

    }

}
