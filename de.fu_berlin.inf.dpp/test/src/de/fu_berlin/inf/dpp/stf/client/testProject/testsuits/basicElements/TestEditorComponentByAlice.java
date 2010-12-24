package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.STFTest;

public class TestEditorComponentByAlice extends STFTest {

    private static final Logger log = Logger
        .getLogger(TestBasicSarosElements.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbenchs();
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
    public void runAfterEveryTest() throws RemoteException {
        deleteProjectsByActiveTesters();
    }

    @Test
    public void testTypeInEditor() throws RemoteException {
        alice.pEV.newProject(PROJECT1);
        String fileName = "test.txt";
        String[] path = { PROJECT1, fileName };
        alice.pEV.newFile(path);
        alice.editor.waitUntilEditorActive(fileName);

        String expected = "Hello World";
        alice.editor.typeTextInEditor(expected, path);
        assertEquals(expected, alice.editor.getTextOfEditor(path));
    }

    @Test
    public void testDeleteInEditor() throws RemoteException {
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        alice.editor.waitUntilJavaEditorActive(CLS1);
        String fileName = CLS1 + ".java";
        alice.editor.navigateInEditor(fileName, 3, 0);
        alice.editor.typeTextInJavaEditor("testtext", PROJECT1, PKG1, CLS1);
        alice.editor.navigateInEditor(fileName, 3, 3);
        alice.editor.pressShortcutInEditor(fileName, IKeyLookup.DELETE_NAME,
            IKeyLookup.DELETE_NAME);
        assertEquals("tesext",
            alice.editor.getJavaTextOnLine(PROJECT1, PKG1, CLS1, 3));
    }

    @Test
    public void testEnterInEditor() throws RemoteException {
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        alice.editor.waitUntilJavaEditorActive(CLS1);
        String fileName = CLS1 + ".java";
        alice.editor.navigateInEditor(fileName, 3, 0);
        alice.editor.typeTextInJavaEditor("testtext", PROJECT1, PKG1, CLS1);

        alice.editor.pressShortcutInEditor(fileName, IKeyLookup.LF_NAME);
        assertEquals(4, alice.editor.getJavaCursorLinePosition(CLS1));
    }
}
