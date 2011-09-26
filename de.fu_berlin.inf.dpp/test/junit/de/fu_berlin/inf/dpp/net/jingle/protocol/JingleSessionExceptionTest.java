/**
 * 
 */
package de.fu_berlin.inf.dpp.net.jingle.protocol;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.jingle.JingleSessionException;

/**
 * 
 */
public class JingleSessionExceptionTest {

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.jingle.JingleSessionException#JingleSessionException(java.lang.String)}
     * .
     */
    @Test
    public void testJingleSessionExceptionString() {
        String jingleString = "Hello";
        JingleSessionException jingle = new JingleSessionException(jingleString);
        assertTrue("The jingle wasn't created correctly- Message not equal",
            jingle.getMessage().equals(jingleString));
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.jingle.JingleSessionException#JingleSessionException(java.lang.String, de.fu_berlin.inf.dpp.net.JID)}
     * .
     */
    @Test
    public void testJingleSessionExceptionStringJID() {
        String jingleString = "Hello";
        String jidString = "userXYZ@jabber.org";
        JID jid = new JID(jidString);
        JingleSessionException jingle = new JingleSessionException(
            jingleString, jid);
        assertTrue("The jingle wasn't created correctly- Message not equal",
            jingle.getMessage().equals(jingleString));
        assertTrue("The jingle wasn't created correctly- JID not equal", jingle
            .getJID().equals(jid));
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.jingle.JingleSessionException#JingleSessionException(java.lang.String, java.lang.Throwable)}
     * .
     */
    @Test
    public void testJingleSessionExceptionStringThrowable() {
        String jingleString = "Hello";
        Throwable throwable = new Throwable("unexpected exception occured");
        JingleSessionException jingle = new JingleSessionException(
            jingleString, throwable);
        assertTrue("The jingle wasn't created correctly- Message not equal",
            jingle.getMessage().equals(jingleString));
        jingle.initCause(throwable);
        assertTrue("The jingle wasn't created correctly- Cause not equal",
            jingle.getCause().equals(throwable));
    }

}
