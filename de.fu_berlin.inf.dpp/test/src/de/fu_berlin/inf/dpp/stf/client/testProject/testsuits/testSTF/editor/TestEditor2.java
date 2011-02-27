package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.editor;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestEditor2 extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
        createProjectWithFileBy(alice, bob);
    }

    @Test
    public void testConcurrentEditing() throws RemoteException {
        // no session!
        String aliceText = "a";
        alice.sarosBot().packageExplorerView().selectFile(path).open();

        alice.bot().editor(FILE3).typeText(aliceText);

        String bobText = "b";
        bob.sarosBot().packageExplorerView().selectFile(path).open();

        bob.bot().editor(FILE3).typeText(bobText);

        assertEquals(aliceText, alice.bot().editor(FILE3).getText());
        assertEquals(bobText, bob.bot().editor(FILE3).getText());
    }
}
