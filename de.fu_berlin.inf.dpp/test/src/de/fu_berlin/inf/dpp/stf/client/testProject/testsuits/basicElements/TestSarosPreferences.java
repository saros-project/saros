package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestSarosPreferences extends STFTest {

    private static JID JID_TO_CREATE = new JID(
        ("test3@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE));
    private static String PASSWORD = "dddfffggg";
    private static String NO_MATCHED_REPEAT_PASSWORD = "dd";
    /* infos about the added account */
    private final static String SERVER = "saros-con.imp.fu-berlin.de";
    private final static String USERNAME = "lin";

    private final static String REGISTERED_USERNAME = "bob_stf";

    private static final String INVALID_SERVER_NAME = "saros-con";

    private final static JID JID_TO_ADD = new JID(
        ("bob_stf@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE));

    private static final Logger log = Logger
        .getLogger(TestSarosPreferences.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbenchs();
        setUpSaros();
        // setUpSessionByDefault(alice, bob);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        // reBuildSession(alice, bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        if (!alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password))
            alice.sarosM.createAccountNoGUI(alice.jid.getDomain(),
                alice.jid.getName(), alice.password);
        if (!alice.sarosM.isAccountActiveNoGUI(alice.jid))
            alice.sarosM.activateAccountNoGUI(alice.jid);
        if (alice.sarosM.isAccountExistNoGUI(JID_TO_ADD, PASSWORD))
            alice.sarosM.deleteAccount(JID_TO_ADD);
    }

    @Test
    public void createAccountWithMenuSarosCreateAccount()
        throws RemoteException {
        alice.sarosM.creatAccount(JID_TO_CREATE, PASSWORD);

    }

    @Test
    public void createAccountWithButtonAddAccountInShellSarosPeferences()
        throws RemoteException {
        alice.sarosM.createAccountWithButtonAddAccountInShellSarosPeferences(
            JID_TO_CREATE, PASSWORD);
    }

    @Test
    public void createAccountWhichAlreadyExisted() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        alice.shell.activateShellAndWait(SHELL_CREATE_NEW_XMPP_ACCOUNT);

        Map<String, String> labelsAndTexts = new HashMap<String, String>();
        labelsAndTexts.put(LABEL_XMPP_JABBER_SERVER, SERVER);
        labelsAndTexts.put(LABEL_USER_NAME, REGISTERED_USERNAME);
        labelsAndTexts.put(LABEL_PASSWORD, PASSWORD);
        labelsAndTexts.put(LABEL_REPEAT_PASSWORD, PASSWORD);

        alice.shell.confirmShellWithTextFieldAndWait(
            SHELL_CREATE_NEW_XMPP_ACCOUNT, labelsAndTexts, FINISH);

        // wait a minute,so that bot can get the error message.
        alice.button.waitUntilButtonEnabled(FINISH);
        assertTrue(alice.shell.isShellActive(SHELL_CREATE_NEW_XMPP_ACCOUNT));
        String errorMessage = alice.shell
            .getErrorMessageInShell(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        assertTrue(errorMessage.matches(ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS
            + STRING_REGEX_WITH_LINE_BREAK));
        alice.shell.confirmShell(SHELL_CREATE_NEW_XMPP_ACCOUNT, CANCEL);
        assertFalse(alice.shell.isShellOpen(SHELL_CREATE_NEW_XMPP_ACCOUNT));
    }

    @Test
    public void createAccountWithDismatchedPassword() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        alice.shell.activateShellAndWait(SHELL_CREATE_NEW_XMPP_ACCOUNT);

        alice.text.setTextInTextWithLabel(SERVER, LABEL_XMPP_JABBER_SERVER);
        alice.text.setTextInTextWithLabel(USERNAME, LABEL_USER_NAME);
        alice.text.setTextInTextWithLabel(PASSWORD, LABEL_PASSWORD);
        alice.text.setTextInTextWithLabel(NO_MATCHED_REPEAT_PASSWORD,
            LABEL_REPEAT_PASSWORD);
        assertFalse(alice.button.isButtonEnabled(FINISH));
        String errorMessage = alice.shell
            .getErrorMessageInShell(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        assertTrue(errorMessage.equals(ERROR_MESSAGE_PASSWORDS_NOT_MATCH));
        alice.shell.confirmShell(SHELL_CREATE_NEW_XMPP_ACCOUNT, CANCEL);
        assertFalse(alice.shell.isShellOpen(SHELL_CREATE_NEW_XMPP_ACCOUNT));
    }

    @Test
    public void createAccountWithInvalidServer() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        alice.shell.activateShellAndWait(SHELL_CREATE_NEW_XMPP_ACCOUNT);

        Map<String, String> labelsAndTexts = new HashMap<String, String>();
        labelsAndTexts.put(LABEL_XMPP_JABBER_SERVER, INVALID_SERVER_NAME);
        labelsAndTexts.put(LABEL_USER_NAME, USERNAME);
        labelsAndTexts.put(LABEL_PASSWORD, PASSWORD);
        labelsAndTexts.put(LABEL_REPEAT_PASSWORD, PASSWORD);

        alice.shell.confirmShellWithTextFieldAndWait(
            SHELL_CREATE_NEW_XMPP_ACCOUNT, labelsAndTexts, FINISH);

        String errorMessage = alice.shell
            .getErrorMessageInShell(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        assertTrue(errorMessage.matches(ERROR_MESSAGE_COULD_NOT_CONNECT));
        alice.shell.confirmShell(SHELL_CREATE_NEW_XMPP_ACCOUNT, CANCEL);
        assertFalse(alice.shell.isShellOpen(SHELL_CREATE_NEW_XMPP_ACCOUNT));
    }

    @Test
    public void addAndActivateAccountNoGUI() throws RemoteException {
        assertFalse(alice.sarosM.isAccountExistNoGUI(JID_TO_ADD, PASSWORD));
        alice.sarosM.addAccount(JID_TO_CREATE, PASSWORD);
        assertTrue(alice.sarosM.isAccountExistNoGUI(JID_TO_ADD, PASSWORD));
        assertTrue(alice.sarosM.isAccountActiveNoGUI(alice.jid));
        assertFalse(alice.sarosM.isAccountActiveNoGUI(JID_TO_ADD));
        alice.sarosM.activateAccountNoGUI(JID_TO_ADD);
        assertTrue(alice.sarosM.isAccountActiveNoGUI(JID_TO_ADD));
        assertFalse(alice.sarosM.isAccountActiveNoGUI(alice.jid));
    }

    @Test
    public void addAndActivateAcount() throws RemoteException {
        assertFalse(alice.sarosM.isAccountExist(JID_TO_ADD, PASSWORD));
        alice.sarosM.addAccount(JID_TO_ADD, PASSWORD);
        assertTrue(alice.sarosM.isAccountExistNoGUI(JID_TO_ADD, PASSWORD));
        assertTrue(alice.sarosM.isAccountActiveNoGUI(alice.jid));
        assertFalse(alice.sarosM.isAccountActiveNoGUI(JID_TO_ADD));
        alice.sarosM.activateAccount(JID_TO_ADD, PASSWORD);
        assertTrue(alice.sarosM.isAccountActiveNoGUI(JID_TO_ADD));
        assertFalse(alice.sarosM.isAccountActiveNoGUI(alice.jid));
    }

    @Test
    public void changeAccount() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
        assertTrue(alice.sarosM.isAccountActiveNoGUI(alice.jid));
        alice.sarosM.changeAccount(alice.jid, USERNAME, PASSWORD, SERVER);
        assertFalse(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
        assertFalse(alice.sarosM.isAccountActiveNoGUI(alice.jid));
        assertTrue(alice.sarosM.isAccountExistNoGUI(JID_TO_ADD, PASSWORD));
        assertTrue(alice.sarosM.isAccountActiveNoGUI(JID_TO_ADD));

    }

    @Test
    public void changeAccountGUI() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
        assertTrue(alice.sarosM.isAccountActiveNoGUI(alice.jid));
        alice.sarosM.changeAccountGUI(alice.jid, USERNAME, PASSWORD, SERVER);
        assertFalse(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
        assertFalse(alice.sarosM.isAccountActiveNoGUI(alice.jid));
        assertTrue(alice.sarosM.isAccountExistNoGUI(JID_TO_ADD, PASSWORD));
        assertTrue(alice.sarosM.isAccountActiveNoGUI(JID_TO_ADD));
    }

    @Test
    public void deleteActiveAccount() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
        alice.sarosM.deleteAccount(alice.jid);
        assertFalse(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
    }

    @Test
    public void deleteActiveAccountGUI() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
        alice.sarosM.deleteAccountGUI(alice.jid, alice.password);
        assertTrue(alice.sarosM.isAccountActiveNoGUI(alice.jid));
        assertTrue(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
    }

    @Test
    public void deleteInactiveAccountGUI() throws RemoteException {
        assertFalse(alice.sarosM.isAccountExist(JID_TO_ADD, PASSWORD));
        alice.sarosM.addAccount(JID_TO_ADD, PASSWORD);
        assertTrue(alice.sarosM.isAccountExistNoGUI(JID_TO_ADD, PASSWORD));
        alice.sarosM.deleteAccountGUI(JID_TO_ADD, PASSWORD);
        assertFalse(alice.sarosM.isAccountExistNoGUI(JID_TO_ADD, PASSWORD));
    }
}
