package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestEditorComponent2 extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestEditorComponent2.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
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
