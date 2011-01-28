package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.multiDriver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestConcurrentEditing extends STFTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
    }

    static final String FILE = "file.txt";

    /**
     * Test to reproduce bug
     * "Inconsistency when concurrently writing at same position"
     * 
     * @throws RemoteException
     * @throws InterruptedException
     * 
     * @see <a
     *      href="https://sourceforge.net/tracker/?func=detail&aid=3098992&group_id=167540&atid=843359">Bug
     *      tracker entry 3098992</a>
     */
    @Test
    public void testBugInconsistencyConcurrentEditing() throws RemoteException,
        InterruptedException {
        alice.fileM.newProject(PROJECT1);
        // cool trick, no need to always use PROJECT1, PKG1, CLS1 as arguments
        String[] path = { PROJECT1, FILE };
        alice.fileM.newFile(path);
        alice.editor.waitUntilEditorOpen(FILE);
        alice.editor.setTextInEditorWithSave("test/STF/lorem.txt", path);
        alice.editor.navigateInEditor(FILE, 0, 6);

        alice.buildSessionDoneSequentially(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            bob);
        bob.pEV.openFile(path);

        alice.sarosSessionV.grantWriteAccessGUI(bob.sarosSessionV);

        bob.editor.waitUntilEditorOpen(FILE);
        bob.editor.navigateInEditor(FILE, 0, 30);

        bob.workbench.sleep(1000);

        // Alice goes to 0,6 and hits Delete
        alice.workbench.activateWorkbench();
        int waitActivate = 100;
        alice.workbench.sleep(waitActivate);
        alice.editor.activateEditor(FILE);
        alice.editor.waitUntilEditorActive(FILE);
        alice.editor.pressShortcut(FILE, "DELETE");
        // at the same time, Bob enters L at 0,30
        bob.workbench.activateWorkbench();
        bob.workbench.sleep(waitActivate);
        bob.editor.activateEditor(FILE);
        bob.editor.waitUntilEditorActive(FILE);
        bob.editor.typeTextInEditor("L", path);
        // both sleep for less than 1000ms
        alice.workbench.sleep(300);

        // Alice hits Delete again
        alice.workbench.activateWorkbench();
        alice.workbench.sleep(waitActivate);
        alice.editor.activateEditor(FILE);
        alice.editor.waitUntilEditorActive(FILE);
        alice.editor.pressShortcut(FILE, "DELETE");
        // Bob enters o
        bob.workbench.activateWorkbench();
        bob.workbench.sleep(waitActivate);
        bob.editor.activateEditor(FILE);
        bob.editor.waitUntilEditorActive(FILE);
        bob.editor.typeTextInEditor("o", path);

        alice.workbench.sleep(5000);
        String aliceText = alice.editor.getTextOfEditor(path);
        String bobText = bob.editor.getTextOfEditor(path);
        assertEquals(aliceText, bobText);
    }

    @Test(expected = AssertionError.class)
    public void AliceAndBobeditInSameLine() throws RemoteException,
        InterruptedException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.buildSessionDoneConcurrently(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            bob);
        bob.pEV.openClass(PROJECT1, PKG1, CLS1);
        bob.editor.waitUntilJavaEditorActive(CLS1);
        alice.sarosSessionV.grantWriteAccessGUI(bob.sarosSessionV);

        String fileName = CLS1 + ".java";
        alice.editor.navigateInEditor(fileName, 3, 0);
        bob.editor.navigateInEditor(fileName, 3, 0);
        char[] content = "Merry Christmas and Happy New Year!".toCharArray();
        for (int i = 0; i < content.length; i++) {
            alice.editor.typeTextInJavaEditor(content[i] + "", PROJECT1, PKG1,
                CLS1);
            Thread.sleep(100);
            if (i != 0 && i % 2 == 0) {
                bob.editor.navigateInEditor(fileName, 3, i);
                bob.editor.pressShortcut(fileName, IKeyLookup.DELETE_NAME,
                    IKeyLookup.DELETE_NAME);
            }
        }

        String aliceText = alice.editor.getTextOfJavaEditor(PROJECT1, PKG1,
            CLS1);
        String bobText = bob.editor.getTextOfJavaEditor(PROJECT1, PKG1, CLS1);
        System.out.println(aliceText);
        System.out.println(bobText);
        assertEquals(aliceText, bobText);
        assertTrue(bob.sarosSessionV.isInconsistencyDetectedEnabled());
    }
}
