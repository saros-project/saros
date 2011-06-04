package de.fu_berlin.inf.dpp.stf.test.stf.editor;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Constants;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

public class Editor2Test extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
        Util.createProjectWithFileBy(ALICE, BOB);
    }

    @Test
    public void testConcurrentEditing() throws RemoteException {

        ALICE.superBot().views().packageExplorerView()
            .selectFile(Constants.PATH).open();

        ALICE.remoteBot().editor(Constants.FILE3).typeText(ALICE.toString());

        BOB.superBot().views().packageExplorerView().selectFile(Constants.PATH)
            .open();

        BOB.remoteBot().editor(Constants.FILE3).typeText(BOB.toString());

        assertEquals(ALICE.toString(), ALICE.remoteBot()
            .editor(Constants.FILE3).getText());
        assertEquals(BOB.toString(), BOB.remoteBot().editor(Constants.FILE3)
            .getText());
    }
}
