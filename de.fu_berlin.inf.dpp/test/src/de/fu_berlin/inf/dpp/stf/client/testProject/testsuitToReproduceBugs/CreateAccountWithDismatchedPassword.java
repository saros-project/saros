package de.fu_berlin.inf.dpp.stf.client.testProject.testsuitToReproduceBugs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotShell;

public class CreateAccountWithDismatchedPassword extends STFTest {

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

    /**
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore("there are bugs: can't correctly check if the given passwords are same or not")
    public void createAccountWithDismatchedPassword() throws RemoteException {
        alice.remoteBot().menu(MENU_SAROS).menu(MENU_CREATE_ACCOUNT).click();

        alice.remoteBot().waitUntilShellIsOpen(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        IRemoteBotShell shell_alice = alice.remoteBot().shell(
            SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        shell_alice.activate();
        shell_alice.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_SERVER)
            .setText(SERVER);
        shell_alice.bot().textWithLabel(LABEL_USER_NAME)
            .setText(NEW_XMPP_JABBER_ID);
        shell_alice.bot().textWithLabel(LABEL_PASSWORD).setText(PASSWORD);
        shell_alice.bot().textWithLabel(LABEL_REPEAT_PASSWORD)
            .setText(NO_MATCHED_REPEAT_PASSWORD);

        assertFalse(shell_alice.bot().button(FINISH).isEnabled());
        String errorMessage = shell_alice.getErrorMessage();
        assertTrue(errorMessage.equals(ERROR_MESSAGE_PASSWORDS_NOT_MATCH));
        shell_alice.confirm(CANCEL);
        assertFalse(alice.remoteBot().isShellOpen(SHELL_CREATE_XMPP_JABBER_ACCOUNT));
    }
}
