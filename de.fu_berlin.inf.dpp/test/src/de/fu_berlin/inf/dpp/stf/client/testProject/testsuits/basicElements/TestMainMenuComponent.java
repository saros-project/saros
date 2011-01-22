package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestMainMenuComponent extends STFTest {
    private final static Logger log = Logger
        .getLogger(TestMainMenuComponent.class);

    /* name of all the main menus */
    private static final String MENU_SAROS = "Saros";
    private static final String MENU_CREATE_ACCOUNT = "Create Account";
    private static final String MENU_PREFERENCES = "Preferences";

    /* title of shells which are pop up by clicking the main menus */
    private static final String SHELL_PREFERNCES = "Preferences";

    /* All infos about the shell "Create New User Account" */
    private final static String SHELL_CREATE_NEW_USER_ACCOUNT = "Create New User Account";
    private final static String LABEL_JABBER_SERVER = "Jabber Server";
    private final static String LABEL_USER_NAME = "Username";
    private final static String LABEL_PASSWORD = "Password";
    private final static String LABEL_REPEAT_PASSWORD = "Repeat Password";

    /* Error massage */
    private final static String ERROR_MESSAGE_PASSWORDS_NOT_MATCH = "Passwords don't match.";
    private final static String ERROR_MESSAGE_COULD_NOT_CONNECT = "Could not connect.*";
    private final static String ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS = "conflict(409): Account already exists";

    /* infos about the added account */
    private final static String SERVER = "saros-con.imp.fu-berlin.de";
    private final static String USERNAME = "lin";
    private final static String PASSWORD = "lin";
    private final static String JID_ANOTHER_ACCOUNT = ("lin@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    private final static JID ANOTHER_JID = new JID(JID_ANOTHER_ACCOUNT);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
    }

    @AfterClass
    public static void runAfterClass() {
        //
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        //
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        if (!alice.sarosM.isAccountExist(alice.jid, alice.password))
            alice.sarosM.createAccountNoGUI(alice.jid.getDomain(),
                alice.jid.getName(), alice.password);
        if (!alice.sarosM.isAccountActive(alice.jid))
            alice.sarosM.activateAccountNoGUI(alice.jid);
        if (alice.sarosM.isAccountExist(ANOTHER_JID, PASSWORD))
            alice.sarosM.deleteAccount(ANOTHER_JID);
    }

    /**********************************************
     * 
     * create new account with invalid inputs.
     * 
     **********************************************/
    @Test
    public void createAlreadyExistedAccount() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        if (!alice.shell.activateShellWithText(SHELL_CREATE_NEW_USER_ACCOUNT))
            alice.shell.waitUntilShellActive(SHELL_CREATE_NEW_USER_ACCOUNT);
        alice.text.setTextInTextWithLabel(bob.getXmppServer(),
            LABEL_JABBER_SERVER);
        alice.text.setTextInTextWithLabel(bob.getName(), LABEL_USER_NAME);
        alice.text.setTextInTextWithLabel(bob.password, LABEL_PASSWORD);
        alice.text.setTextInTextWithLabel(bob.password, LABEL_REPEAT_PASSWORD);
        assertTrue(alice.button.isButtonEnabled(FINISH));
        alice.button.clickButton(FINISH);
        // wait a minute,so that bot can get the error message.
        alice.button.waitUntilButtonEnabled(FINISH);
        assertTrue(alice.shell.isShellActive(SHELL_CREATE_NEW_USER_ACCOUNT));
        String errorMessage = alice.shell
            .getErrorMessageInShell(SHELL_CREATE_NEW_USER_ACCOUNT);
        String[] ms = errorMessage.split("\n");
        assertTrue(ms[0].trim().equals(ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS));

        alice.shell.confirmShell(SHELL_CREATE_NEW_USER_ACCOUNT, CANCEL);
        assertFalse(alice.shell.isShellOpen(SHELL_CREATE_NEW_USER_ACCOUNT));
    }

    @Test
    public void createAccountWithDismatchedPassword() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        if (!alice.shell.activateShellWithText(SHELL_CREATE_NEW_USER_ACCOUNT))
            alice.shell.waitUntilShellActive(SHELL_CREATE_NEW_USER_ACCOUNT);
        alice.text.setTextInTextWithLabel(bob.getXmppServer(),
            LABEL_JABBER_SERVER);
        alice.text.setTextInTextWithLabel(bob.getName(), LABEL_USER_NAME);
        alice.text.setTextInTextWithLabel(bob.password, LABEL_PASSWORD);
        alice.text.setTextInTextWithLabel(bob.password + "d",
            LABEL_REPEAT_PASSWORD);
        assertFalse(alice.button.isButtonEnabled(FINISH));
        String errorMessage = alice.shell
            .getErrorMessageInShell(SHELL_CREATE_NEW_USER_ACCOUNT);
        assertTrue(errorMessage.equals(ERROR_MESSAGE_PASSWORDS_NOT_MATCH));
        alice.shell.confirmShell(SHELL_CREATE_NEW_USER_ACCOUNT, CANCEL);
        assertFalse(alice.shell.isShellOpen(SHELL_CREATE_NEW_USER_ACCOUNT));
    }

    @Test
    public void createAccountWithInvalidServer() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        if (!alice.shell.activateShellWithText(SHELL_CREATE_NEW_USER_ACCOUNT))
            alice.shell.waitUntilShellActive(SHELL_CREATE_NEW_USER_ACCOUNT);
        alice.text
            .setTextInTextWithLabel("invalid server", LABEL_JABBER_SERVER);
        alice.text.setTextInTextWithLabel("invalid name", LABEL_USER_NAME);
        alice.text.setTextInTextWithLabel(bob.password, LABEL_PASSWORD);
        alice.text.setTextInTextWithLabel(bob.password, LABEL_REPEAT_PASSWORD);
        assertTrue(alice.button.isButtonEnabled(FINISH));
        alice.button.clickButton(FINISH);
        alice.button.waitUntilButtonEnabled(FINISH);
        String errorMessage = alice.shell
            .getErrorMessageInShell(SHELL_CREATE_NEW_USER_ACCOUNT);
        assertTrue(errorMessage.matches(ERROR_MESSAGE_COULD_NOT_CONNECT));
        alice.shell.confirmShell(SHELL_CREATE_NEW_USER_ACCOUNT, CANCEL);
        assertFalse(alice.shell.isShellOpen(SHELL_CREATE_NEW_USER_ACCOUNT));
    }

    @Test
    public void addAndActivateAccount() throws RemoteException {
        assertFalse(alice.sarosM.isAccountExist(ANOTHER_JID, PASSWORD));
        alice.sarosM.createAccountNoGUI(SERVER, USERNAME, PASSWORD);
        assertTrue(alice.sarosM.isAccountExist(ANOTHER_JID, PASSWORD));
        assertTrue(alice.sarosM.isAccountActive(alice.jid));
        assertFalse(alice.sarosM.isAccountActive(ANOTHER_JID));
        alice.sarosM.activateAccountNoGUI(ANOTHER_JID);
        assertTrue(alice.sarosM.isAccountActive(ANOTHER_JID));
        assertFalse(alice.sarosM.isAccountActive(alice.jid));
    }

    @Test
    public void addAndActivateAcountGUI() throws RemoteException {
        assertFalse(alice.sarosM.isAccountExistGUI(ANOTHER_JID, PASSWORD));
        alice.sarosM.addAccount(ANOTHER_JID, PASSWORD);
        assertTrue(alice.sarosM.isAccountExist(ANOTHER_JID, PASSWORD));
        assertTrue(alice.sarosM.isAccountActive(alice.jid));
        assertFalse(alice.sarosM.isAccountActive(ANOTHER_JID));
        alice.sarosM.activateAccount(ANOTHER_JID, PASSWORD);
        assertTrue(alice.sarosM.isAccountActive(ANOTHER_JID));
        assertFalse(alice.sarosM.isAccountActive(alice.jid));
    }

    @Test
    public void changeAccount() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExist(alice.jid, alice.password));
        assertTrue(alice.sarosM.isAccountActive(alice.jid));
        alice.sarosM.changeAccount(alice.jid, USERNAME, PASSWORD, SERVER);
        assertFalse(alice.sarosM.isAccountExist(alice.jid, alice.password));
        assertFalse(alice.sarosM.isAccountActive(alice.jid));
        assertTrue(alice.sarosM.isAccountExist(ANOTHER_JID, PASSWORD));
        assertTrue(alice.sarosM.isAccountActive(ANOTHER_JID));

    }

    @Test
    public void changeAccountGUI() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExist(alice.jid, alice.password));
        assertTrue(alice.sarosM.isAccountActive(alice.jid));
        alice.sarosM.changeAccountGUI(alice.jid, USERNAME, PASSWORD, SERVER);
        assertFalse(alice.sarosM.isAccountExist(alice.jid, alice.password));
        assertFalse(alice.sarosM.isAccountActive(alice.jid));
        assertTrue(alice.sarosM.isAccountExist(ANOTHER_JID, PASSWORD));
        assertTrue(alice.sarosM.isAccountActive(ANOTHER_JID));
    }

    @Test
    public void deleteActiveAccount() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExist(alice.jid, alice.password));
        alice.sarosM.deleteAccount(alice.jid);
        assertFalse(alice.sarosM.isAccountExist(alice.jid, alice.password));
    }

    @Test
    public void deleteActiveAccountGUI() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExist(alice.jid, alice.password));
        alice.sarosM.deleteAccountGUI(alice.jid, alice.password);
        assertTrue(alice.sarosM.isAccountActive(alice.jid));
        assertTrue(alice.sarosM.isAccountExist(alice.jid, alice.password));
    }

    @Test
    public void deleteInactiveAccountGUI() throws RemoteException {
        assertFalse(alice.sarosM.isAccountExistGUI(ANOTHER_JID, PASSWORD));
        alice.sarosM.addAccount(ANOTHER_JID, PASSWORD);
        assertTrue(alice.sarosM.isAccountExist(ANOTHER_JID, PASSWORD));
        alice.sarosM.deleteAccountGUI(ANOTHER_JID, PASSWORD);
        assertFalse(alice.sarosM.isAccountExist(ANOTHER_JID, PASSWORD));
    }
}
