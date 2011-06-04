package de.fu_berlin.inf.dpp.stf.test.stf.keyboard;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Constants;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

public class KeyboardLayoutTest extends StfTestCase {
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE);
        setUpWorkbench();
        setUpSaros();
    }

    @Test
    public void testKeyboardLayout() throws RemoteException {

        Util.createProjectWithFileBy(ALICE);
        //
        final String textToTest = "!\"§$%&/()={[]}\\+*~#'-_.:,;|<>^?";

        ALICE.superBot().views().packageExplorerView()
            .selectFile(Constants.PATH).open();

        ALICE.remoteBot().editor(Constants.FILE3)
            .typeText(String.valueOf((textToTest)));

        ALICE.remoteBot().sleep(500);
        assertEquals("keyboard layout is misconfigured", textToTest, ALICE
            .remoteBot().editor(Constants.FILE3).getText());
    }

}
