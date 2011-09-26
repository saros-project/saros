/**
 * 
 */
package de.fu_berlin.inf.dpp.net.util;

import static org.junit.Assert.fail;

import java.util.Random;

import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.XMPPException;
import org.junit.Test;

import de.fu_berlin.inf.dpp.test.util.SarosTestUtils;

/**
 * 
 */
public class XMPPAccountUtilsTest {

    private final String server = "saros-con.imp.fu-berlin.de";
    private final String testPw = "1234";

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.util.XMPPAccountUtils#createAccount(java.lang.String, java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)}
     * .
     * 
     * @throws XMPPException
     * @throws InterruptedException
     * @throws SpamException
     */
    @Test()
    public void testCreateAccount() throws XMPPException, InterruptedException {
        int rand = new Random().nextInt(20000);
        String userName = "testaccount" + rand;
        try {
            SubMonitor monitor = SarosTestUtils.submonitor();
            // create account once
            // once should be successful
            XMPPAccountUtils.createAccount(server, userName, testPw, monitor);

            Thread.sleep(2000);
            //
            // XMPPConnection connection = new XMPPConnection(server);
            // connection.connect();
            //
            // AccountManager manager = connection.getAccountManager();
            // System.out.println(manager.getAccountAttribute(userName));
            // should fail,
            // already
            // exists

            try {
                XMPPAccountUtils.createAccount(server, userName, testPw,
                    monitor);
            } catch (XMPPException e) {
                System.out.println(e.getMessage());
                // YESSSS account already existed ^^
                if (!e.getMessage().contains("exist"))
                    fail("Account should already exist!");
            }

        } catch (XMPPException e) {
            if (e.getMessage().contains("resource-constraint(500")) {
                // accounts created too quickly!!
                // skip the test
                return;
            }
        } finally {
            // clean up and delete the account
            XMPPAccountUtils.deleteUserAccoountOnServer(server, userName,
                testPw);
        }

    }
}
