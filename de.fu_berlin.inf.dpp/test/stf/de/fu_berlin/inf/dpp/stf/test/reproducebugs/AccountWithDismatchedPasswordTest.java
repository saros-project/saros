package de.fu_berlin.inf.dpp.stf.test.reproducebugs;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.CANCEL;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.ERROR_MESSAGE_PASSWORDS_NOT_MATCH;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.FINISH;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.LABEL_PASSWORD;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.LABEL_REPEAT_PASSWORD;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.LABEL_USER_NAME;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.LABEL_XMPP_JABBER_SERVER;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.MENU_CREATE_ACCOUNT;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.MENU_SAROS;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.SHELL_CREATE_XMPP_JABBER_ACCOUNT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Constants;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;

public class AccountWithDismatchedPasswordTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE);
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
        ALICE.remoteBot().menu(MENU_SAROS).menu(MENU_CREATE_ACCOUNT).click();

        ALICE.remoteBot()
            .waitUntilShellIsOpen(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        IRemoteBotShell shell_alice = ALICE.remoteBot().shell(
            SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        shell_alice.activate();
        shell_alice.bot().comboBoxWithLabel(LABEL_XMPP_JABBER_SERVER)
            .setText(Constants.SERVER);
        shell_alice.bot().textWithLabel(LABEL_USER_NAME)
            .setText(Constants.NEW_XMPP_JABBER_ID);
        shell_alice.bot().textWithLabel(LABEL_PASSWORD)
            .setText(Constants.PASSWORD);
        shell_alice.bot().textWithLabel(LABEL_REPEAT_PASSWORD)
            .setText(Constants.NO_MATCHED_REPEAT_PASSWORD);

        assertFalse(shell_alice.bot().button(FINISH).isEnabled());
        String errorMessage = shell_alice.getErrorMessage();
        assertTrue(errorMessage.equals(ERROR_MESSAGE_PASSWORDS_NOT_MATCH));
        shell_alice.confirm(CANCEL);
        assertFalse(ALICE.remoteBot().isShellOpen(
            SHELL_CREATE_XMPP_JABBER_ACCOUNT));
    }
}
