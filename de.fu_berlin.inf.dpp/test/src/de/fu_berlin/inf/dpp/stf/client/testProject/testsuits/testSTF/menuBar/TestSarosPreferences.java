package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.Shell;

public class TestSarosPreferences extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
        setUpSaros();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetDefaultAccount();
    }

    @Test(expected = RuntimeException.class)
    public void createExistedAccountWithMenuSarosCreateAccount()
        throws RemoteException {
        alice.sarosM.creatAccount(alice.jid, PASSWORD);
    }

    /*
     * NOTE: createAccount can not be repeated tested. The reasons are:
     * 
     * 1.registered user can not be automatically deleted.
     * 
     * 2. It's not allowed to register account so fast.
     */
    @Test
    @Ignore
    public void createAccountWithButtonAddAccountInShellSarosPeferences()
        throws RemoteException {
        alice.sarosM.createAccountInShellSarosPeferences(JID_TO_CREATE,
            PASSWORD);
    }

    @Test
    public void createAccountWhichAlreadyExisted() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        alice.bot().shell(SHELL_CREATE_NEW_XMPP_ACCOUNT).activateAndWait();

        Map<String, String> labelsAndTexts = new HashMap<String, String>();
        labelsAndTexts.put(LABEL_XMPP_JABBER_SERVER, SERVER);
        labelsAndTexts.put(LABEL_USER_NAME, REGISTERED_USER_NAME);
        labelsAndTexts.put(LABEL_PASSWORD, PASSWORD);
        labelsAndTexts.put(LABEL_REPEAT_PASSWORD, PASSWORD);

        Shell shell_alice = alice.bot().shell(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        shell_alice.confirmWithTextFieldAndWait(labelsAndTexts, FINISH);
        // wait a minute,so that bot can get the error message.
        shell_alice.bot_().button(FINISH).waitUntilIsEnabled();
        assertTrue(shell_alice.isActive());
        String errorMessage = shell_alice.getErrorMessageInShell();
        assertTrue(errorMessage.matches(ERROR_MESSAGE_ACCOUNT_ALREADY_EXISTS
            + STRING_REGEX_WITH_LINE_BREAK));
        shell_alice.confirm(CANCEL);
        assertFalse(alice.bot().isShellOpen(SHELL_CREATE_NEW_XMPP_ACCOUNT));
    }

    @Test
    public void createAccountWithDismatchedPassword() throws RemoteException {

        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        Shell shell_alice = alice.bot().shell(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        shell_alice.activateAndWait();

        alice.text.setTextInTextWithLabel(SERVER, LABEL_XMPP_JABBER_SERVER);
        alice.text.setTextInTextWithLabel(NEW_USER_NAME, LABEL_USER_NAME);
        alice.text.setTextInTextWithLabel(PASSWORD, LABEL_PASSWORD);
        alice.text.setTextInTextWithLabel(NO_MATCHED_REPEAT_PASSWORD,
            LABEL_REPEAT_PASSWORD);
        assertFalse(shell_alice.bot_().button(FINISH).isEnabled());
        String errorMessage = shell_alice.getErrorMessageInShell();
        assertTrue(errorMessage.equals(ERROR_MESSAGE_PASSWORDS_NOT_MATCH));
        shell_alice.confirm(CANCEL);
        assertFalse(alice.bot().isShellOpen(SHELL_CREATE_NEW_XMPP_ACCOUNT));
    }

    @Test
    public void createAccountWithInvalidServer() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        Shell shell_alice = alice.bot().shell(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        shell_alice.activateAndWait();

        Map<String, String> labelsAndTexts = new HashMap<String, String>();
        labelsAndTexts.put(LABEL_XMPP_JABBER_SERVER, INVALID_SERVER_NAME);
        labelsAndTexts.put(LABEL_USER_NAME, NEW_USER_NAME);
        labelsAndTexts.put(LABEL_PASSWORD, PASSWORD);
        labelsAndTexts.put(LABEL_REPEAT_PASSWORD, PASSWORD);

        shell_alice.confirmWithTextFieldAndWait(labelsAndTexts, FINISH);

        shell_alice.bot_().button(FINISH).waitUntilIsEnabled();
        String errorMessage = shell_alice.getErrorMessageInShell();
        assertTrue(errorMessage.matches(ERROR_MESSAGE_COULD_NOT_CONNECT));
        shell_alice.confirm(CANCEL);
        assertFalse(alice.bot().isShellOpen(SHELL_CREATE_NEW_XMPP_ACCOUNT));
    }

    @Test
    public void addAndActivateAccountNoGUI() throws RemoteException {
        assertFalse(alice.sarosM.isAccountExistNoGUI(JID_TO_ADD, PASSWORD));
        alice.sarosM.addAccount(JID_TO_ADD, PASSWORD);
        assertTrue(alice.sarosM.isAccountExistNoGUI(JID_TO_ADD, PASSWORD));
        assertTrue(alice.sarosM.isAccountActiveNoGUI(alice.jid));
        assertFalse(alice.sarosM.isAccountActiveNoGUI(JID_TO_ADD));
        alice.sarosM.activateAccountNoGUI(JID_TO_ADD);
        assertTrue(alice.sarosM.isAccountActiveNoGUI(JID_TO_ADD));
        assertFalse(alice.sarosM.isAccountActiveNoGUI(alice.jid));
    }

    @Test
    public void addAndActivateAcount() throws RemoteException {
        assertFalse(alice.sarosM.isAccountExist(JID_TO_ADD));
        alice.sarosM.addAccount(JID_TO_ADD, PASSWORD);
        assertTrue(alice.sarosM.isAccountExist(JID_TO_ADD));
        assertTrue(alice.sarosM.isAccountActive(alice.jid));
        assertFalse(alice.sarosM.isAccountActive(JID_TO_ADD));
        alice.sarosM.activateAccount(JID_TO_ADD);
        assertTrue(alice.sarosM.isAccountActive(JID_TO_ADD));
        assertFalse(alice.sarosM.isAccountActive(alice.jid));
    }

    @Test
    public void changeAccountNoGUI() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
        assertTrue(alice.sarosM.isAccountActiveNoGUI(alice.jid));
        alice.sarosM.changeAccountNoGUI(alice.jid, NEW_USER_NAME, PASSWORD,
            SERVER);
        assertFalse(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
        assertFalse(alice.sarosM.isAccountActiveNoGUI(alice.jid));
        assertTrue(alice.sarosM.isAccountExistNoGUI(JID_TO_CHANGE, PASSWORD));
        assertTrue(alice.sarosM.isAccountActiveNoGUI(JID_TO_CHANGE));

    }

    @Test
    public void changeAccount() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExist(alice.jid));
        assertTrue(alice.sarosM.isAccountActive(alice.jid));
        alice.sarosM.changeAccount(alice.jid, NEW_USER_NAME, PASSWORD, SERVER);
        assertFalse(alice.sarosM.isAccountExist(alice.jid));
        assertFalse(alice.sarosM.isAccountActive(alice.jid));
        assertTrue(alice.sarosM.isAccountExist(JID_TO_CHANGE));
        assertTrue(alice.sarosM.isAccountActive(JID_TO_CHANGE));
    }

    /*
     * FIXME After running the test the single active account would be deleted.
     * But you can still connect with the deleted account by clicking the
     * toolbar buton "connect".
     */
    @Test
    @Ignore("There may be some bugs existed.")
    public void deleteActiveAccountNoGUI() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
        alice.sarosM.deleteAccountNoGUI(alice.jid);
        assertFalse(alice.sarosM.isAccountExistNoGUI(alice.jid, alice.password));
    }

    @Test(expected = RuntimeException.class)
    public void deleteActiveAccount() throws RemoteException {
        assertTrue(alice.sarosM.isAccountExist(alice.jid));
        alice.sarosM.deleteAccount(alice.jid, alice.password);
        assertTrue(alice.sarosM.isAccountActive(alice.jid));
        assertTrue(alice.sarosM.isAccountExist(alice.jid));
    }

    @Test
    public void deleteInactiveAccount() throws RemoteException {
        assertFalse(alice.sarosM.isAccountExist(JID_TO_ADD));
        alice.sarosM.addAccount(JID_TO_ADD, PASSWORD);
        assertTrue(alice.sarosM.isAccountExist(JID_TO_ADD));
        alice.sarosM.deleteAccount(JID_TO_ADD, PASSWORD);
        assertFalse(alice.sarosM.isAccountExist(JID_TO_ADD));
    }
}
