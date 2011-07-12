package de.fu_berlin.inf.dpp.stf.test.account;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;

public class AccountPreferenceTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetDefaultAccount();
    }

    @Test(expected = TimeoutException.class)
    public void testDuplicateAccount() throws RemoteException {
        ALICE.superBot().menuBar().saros().preferences()
            .addAccount(new JID("foo@bar.com"), "foobar");

        ALICE.superBot().menuBar().saros().preferences()
            .addAccount(new JID("foo@bar.com"), "foobar1");
    }
}
