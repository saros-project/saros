package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

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
    private final static String JABBER_SERVER = "Jabber Server";
    private final static String USER_NAME = "Username";
    private final static String PASSWORD = "Password";
    private final static String REPEAT_PASSWORD = "Repeat Password";

    private final static String ERROR_MESSAGE_PASSWORDS_NOT_MATCH = "Passwords don't match.";
    private final static String ERROR_MESSAGE_COULD_NOT_CONNECT = "Could not connect.*";
    private final static String ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS = "conflict(409): Account already exists";

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
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() {
        //
    }

    /**********************************************
     * 
     * create new account with invalid inputs.
     * 
     **********************************************/
    @Test
    public void createAlreadyExistedAccount() throws RemoteException {
        alice.mainMenu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        if (!alice.shell.activateShellWithText(SHELL_CREATE_NEW_USER_ACCOUNT))
            alice.shell.waitUntilShellActive(SHELL_CREATE_NEW_USER_ACCOUNT);
        alice.basic.setTextInTextWithLabel(bob.getXmppServer(), JABBER_SERVER);
        alice.basic.setTextInTextWithLabel(bob.getName(), USER_NAME);
        alice.basic.setTextInTextWithLabel(bob.password, PASSWORD);
        alice.basic.setTextInTextWithLabel(bob.password, REPEAT_PASSWORD);
        assertTrue(alice.basic.isButtonEnabled(FINISH));
        alice.basic.clickButton(FINISH);
        // wait a minute,so that bot can get the error message.
        alice.basic.waitUntilButtonEnabled(FINISH);
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
        alice.mainMenu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        if (!alice.shell.activateShellWithText(SHELL_CREATE_NEW_USER_ACCOUNT))
            alice.shell.waitUntilShellActive(SHELL_CREATE_NEW_USER_ACCOUNT);
        alice.basic.setTextInTextWithLabel(bob.getXmppServer(), JABBER_SERVER);
        alice.basic.setTextInTextWithLabel(bob.getName(), USER_NAME);
        alice.basic.setTextInTextWithLabel(bob.password, PASSWORD);
        alice.basic.setTextInTextWithLabel(bob.password + "d", REPEAT_PASSWORD);
        assertFalse(alice.basic.isButtonEnabled(FINISH));
        String errorMessage = alice.shell
            .getErrorMessageInShell(SHELL_CREATE_NEW_USER_ACCOUNT);
        assertTrue(errorMessage.equals(ERROR_MESSAGE_PASSWORDS_NOT_MATCH));
        alice.shell.confirmShell(SHELL_CREATE_NEW_USER_ACCOUNT, CANCEL);
        assertFalse(alice.shell.isShellOpen(SHELL_CREATE_NEW_USER_ACCOUNT));
    }

    @Test
    public void createAccountWithInvalidServer() throws RemoteException {
        alice.mainMenu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        if (!alice.shell.activateShellWithText(SHELL_CREATE_NEW_USER_ACCOUNT))
            alice.shell.waitUntilShellActive(SHELL_CREATE_NEW_USER_ACCOUNT);
        alice.basic.setTextInTextWithLabel("invalid server", JABBER_SERVER);
        alice.basic.setTextInTextWithLabel("invalid name", USER_NAME);
        alice.basic.setTextInTextWithLabel(bob.password, PASSWORD);
        alice.basic.setTextInTextWithLabel(bob.password, REPEAT_PASSWORD);
        assertTrue(alice.basic.isButtonEnabled(FINISH));
        alice.basic.clickButton(FINISH);
        alice.basic.waitUntilButtonEnabled(FINISH);
        String errorMessage = alice.shell
            .getErrorMessageInShell(SHELL_CREATE_NEW_USER_ACCOUNT);
        assertTrue(errorMessage.matches(ERROR_MESSAGE_COULD_NOT_CONNECT));
        alice.shell.confirmShell(SHELL_CREATE_NEW_USER_ACCOUNT, CANCEL);
        assertFalse(alice.shell.isShellOpen(SHELL_CREATE_NEW_USER_ACCOUNT));
    }
}
