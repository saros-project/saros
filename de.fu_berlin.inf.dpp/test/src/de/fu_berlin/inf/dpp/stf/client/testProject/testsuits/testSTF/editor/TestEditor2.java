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
        alice.editor.typeTextInEditor(aliceText, path);

        String bobText = "b";
        bob.editor.typeTextInEditor(bobText, path);

        assertEquals(aliceText, alice.editor.getTextOfEditor(path));
        assertEquals(bobText, bob.editor.getTextOfEditor(path));
    }
}
