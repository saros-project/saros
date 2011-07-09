package de.fu_berlin.inf.dpp.test.xmpp;

import static com.google.common.collect.Lists.newArrayList;
import static de.fu_berlin.inf.dpp.test.util.SarosTestUtils.submonitor;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.net.util.XMPPAccountUtils;

/**
 * Facade for creating and deleting user-accounts on a XMPP-Server.
 * 
 * You only need to call
 * {@link de.fu_berlin.inf.dpp.xmpp.XMPPServerFacadeForTests#getNextUser()} to
 * get a {@link de.fu_berlin.inf.dpp.pcordes.xmpp.XmppUser}.
 * 
 * @author cordes
 */
public class XMPPServerFacadeForTests {

    private static Logger LOG = Logger
        .getLogger(XMPPServerFacadeForTests.class);

    public static final String SERVER_ADDRESS = "localhost";
    public static final String DEFAULT_PASSWORD = "test1234";

    public static final String[] TEST_NAMES = { "alice", "bob", "carol",
        "dave", "ted", "mallory", "marvin", "mallet" };
    private List<String> userNamesToUse = newArrayList(TEST_NAMES);

    private List<XmppUser> createdUsers = newArrayList();
    private static XMPPServer server;

    /**
     * creates an user-account on the local XMPP-Server. The credential-data
     * will be automatically created.
     * 
     * The first 8 user-accounts will get readable names and the following an
     * index-based name.
     * 
     * If there is already a user-account with the generated
     * credential-information this one will be deleted before an account will be
     * created.
     * 
     * @return
     */
    public XmppUser getNextUser() {
        XmppUser result = new XmppUser();
        result.setUsername(getNextUserName());
        result.setPassword(DEFAULT_PASSWORD);
        result.setServerAdress(SERVER_ADDRESS);

        deleteUserAccount(result);
        createAccount(result);

        createdUsers.add(result);
        return result;
    }

    public void deleteAllCreatedAccounts() {
        for (XmppUser user : createdUsers) {
            deleteUserAccount(user);
        }
    }

    private String getNextUserName() {
        String userName;

        if (userNamesToUse.size() > 0) {
            userName = userNamesToUse.remove(0);
        } else {
            userName = "testuser-" + createdUsers.size();
        }
        return userName;
    }

    private void createAccount(XmppUser user) {
        try {
            XMPPAccountUtils.createAccount(user.getServerAdress(),
                user.getUsername(), user.getPassword(), submonitor());
            LOG.debug("\n\n **** XMPP-FACADE: Created User-Account: "
                + user.getUsername());
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    private static void deleteUserAccount(XmppUser user) {
        try {
            XMPPAccountUtils.deleteUserAccoountOnServer(user.getServerAdress(),
                user.getUsername(), user.getPassword());
            LOG.debug("\n\n **** XMPP-FACADE: Deleted User-Account: "
                + user.getUsername() + " ****");
        } catch (Exception e) {
            e.printStackTrace();
            // do nothing we only tried it
        }

    }

    public static void startServer() {
        if (server != null)
            return;

        File openfireLibs = new File("test/lib/openfire");
        File openfireHome = new File("test/resources/openfirehome");

        System.setProperty("openfire.lib.dir", openfireLibs.getAbsolutePath());
        System.setProperty("openfireHome", openfireHome.getAbsolutePath());

        server = new XMPPServer();
    }

    public static void stopServer() {
        try {
            server.stop();
        } catch (Exception e) {

        }
        server = null;
    }

}
