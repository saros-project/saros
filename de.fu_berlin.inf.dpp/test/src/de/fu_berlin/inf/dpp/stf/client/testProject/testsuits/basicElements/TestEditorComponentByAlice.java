package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertContains;
import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertDoesNotContain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.swt.SWT;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

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
        deleteAllProjectsByAllActiveTesters();
    }

    @Test
    public void testTypeInEditor() throws RemoteException {
        alice.fileM.newProject(PROJECT1);
        String fileName = "test.txt";
        String[] path = { PROJECT1, fileName };
        alice.fileM.newFile(path);
        alice.editor.waitUntilEditorActive(fileName);

        String expected = "Hello World";
        alice.editor.typeTextInEditor(expected, path);
        assertEquals(expected, alice.editor.getTextOfEditor(path));
    }

    @Test
    public void testDeleteInEditor() throws RemoteException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        alice.editor.waitUntilJavaEditorActive(CLS1);
        String fileName = CLS1 + ".java";
        alice.editor.navigateInEditor(fileName, 3, 0);
        alice.editor.typeTextInJavaEditor("testtext", PROJECT1, PKG1, CLS1);
        alice.editor.navigateInEditor(fileName, 3, 3);
        alice.editor.pressShortcut(fileName, IKeyLookup.DELETE_NAME,
            IKeyLookup.DELETE_NAME);
        assertEquals("tesext",
            alice.editor.getJavaTextOnLine(PROJECT1, PKG1, CLS1, 3));
    }

    @Test
    public void testEnterInEditor() throws RemoteException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        alice.editor.waitUntilJavaEditorActive(CLS1);
        String fileName = CLS1 + ".java";
        alice.editor.navigateInEditor(fileName, 3, 0);
        alice.editor.typeTextInJavaEditor("testtext", PROJECT1, PKG1, CLS1);

        alice.editor.pressShortCutEnter(fileName);
        assertEquals(4, alice.editor.getJavaCursorLinePosition(CLS1));
    }

    @Test
    public void autoComplateProposal() throws RemoteException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        alice.editor.waitUntilJavaEditorActive(CLS1);
        alice.editor.navigateInEditor(CLS1 + ".java", 3, 0);
        assertDoesNotContain("public static void main",
            alice.editor.getJavaTextOnLine(PROJECT1, PKG1, CLS1, 3));
        alice.editor.autoCompleteProposal(CLS1 + ".java", "main",
            "main - main method");
        assertContains("public static void main",
            alice.editor.getJavaTextOnLine(PROJECT1, PKG1, CLS1, 3));
    }

    @Test
    public void getAutoComplateProposal() throws RemoteException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        alice.editor.waitUntilJavaEditorActive(CLS1);
        alice.editor.navigateInEditor(CLS1 + ".java", 3, 0);
        List<String> autoCompleteProposals = alice.editor
            .getAutoCompleteProposals(CLS1 + ".java", "JFr");
        assertEquals(autoCompleteProposals.toString(), 2,
            autoCompleteProposals.size());
        assertEquals("JFrame - javax.swing", autoCompleteProposals.get(0));
        String string = autoCompleteProposals.get(1);
        assertTrue(string.equals("JFr()  void - Method stub")
            || string.equals("JFr() : void - Method stub"));
    }

    @Test
    public void pressShortCutSave() throws RemoteException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.openClass(PROJECT1, PKG1, CLS1);
        alice.editor.waitUntilJavaEditorActive(CLS1);
        alice.editor.navigateInEditor(CLS1 + ".java", 3, 0);
        alice.editor.autoCompleteProposal(CLS1 + ".java", "main",
            "main - main method");
        assertTrue(alice.editor.isClassDirty(PROJECT1, PKG1, CLS1,
            ID_JAVA_EDITOR));
        alice.editor.pressShortCutSave(CLS1 + ".java");
        assertFalse(alice.editor.isClassDirty(PROJECT1, PKG1, CLS1,
            ID_JAVA_EDITOR));
    }

    @Test
    public void quickFix() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newClassImplementsRunnable(PROJECT1, PKG1, CLS1);
        alice.editor.pressShortCutNextAnnotation(CLS1 + ".java");
        alice.editor.quickfix(CLS1 + ".java", "Add unimplemented methods");
        assertContains("public void run()",
            alice.editor.getJavaTextOnLine(PROJECT1, PKG1, CLS1, 5));

        alice.editor.navigateInEditor(CLS1 + ".java", 7, 0);
        alice.editor.autoCompleteProposal(CLS1 + ".java", "sys",
            "sysout - print to standard out");
    }

    @Test
    public void getSelection() throws RemoteException {
        alice.fileM.newProject(PROJECT1);
        alice.fileM.newFile(PROJECT1, FILE1);
        alice.editor.navigateInEditor(FILE1, 0, 0);
        alice.editor.typeTextInEditor("pleese", PROJECT1, FILE1);
        alice.editor.selectRange(FILE1, 0, 0, 6);
        System.out.println(alice.editor.getSelection(FILE1));

    }

    @Test
    public void quickFixWithSpellChecker() throws RemoteException {
        alice.fileM.newProject(PROJECT1);
        alice.fileM.newFile(PROJECT1, FILE1);
        alice.editor
            .typeTextInEditor("pleese open the window", PROJECT1, FILE1);
        alice.editor.selectLine(FILE1, 0);
        alice.editor.quickfix(FILE1, 0);
        System.out.println(alice.editor.getTextOnLine(FILE1, 0));
        assertContains("please", alice.editor.getTextOnLine(FILE1, 0));
    }

    @Test
    public void allTogether() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newClassImplementsRunnable(PROJECT1, PKG1, CLS1);
        alice.editor.pressShortCutNextAnnotation(CLS1 + ".java");
        alice.editor.quickfix(CLS1 + ".java", "Add unimplemented methods");
        assertContains("public void run()",
            alice.editor.getJavaTextOnLine(PROJECT1, PKG1, CLS1, 5));
        alice.editor.navigateInEditor(CLS1 + ".java", 7, 0);
        alice.editor.autoCompleteProposal(CLS1 + ".java", "sys",
            "sysout - print to standard out");
        assertContains("System.out.println()",
            alice.editor.getJavaTextOnLine(PROJECT1, PKG1, CLS1, 7));
        alice.editor.typeTextInJavaEditor("\"Hello World\"", PROJECT1, PKG1,
            CLS1);
        assertContains("System.out.println(\"Hello World\")",
            alice.editor.getJavaTextOnLine(PROJECT1, PKG1, CLS1, 7));

        alice.editor.navigateInEditor(CLS1 + ".java", 3, 0);
        alice.editor.autoCompleteProposal(CLS1 + ".java", "main",
            "main - main method");
        alice.editor.typeTextInJavaEditor("new Thread (new " + CLS1 + " ()",
            PROJECT1, PKG1, CLS1);
        alice.editor.pressShortCutQuickAssignToLocalVariable(CLS1 + ".java");
        assertContains("Thread thread = new Thread (new " + CLS1 + " ());",
            alice.editor.getTextOnCurrentLine(CLS1 + ".java"));
        alice.editor.pressShortCut(CLS1 + ".java", SWT.NONE, '\n');
        alice.editor.typeTextInJavaEditor("\n", PROJECT1, PKG1, CLS1);
        assertEquals(5, alice.editor.getCursorLine(CLS1 + ".java"));
        alice.editor
            .typeTextInJavaEditor("thread.start(", PROJECT1, PKG1, CLS1);
        alice.editor.typeTextInJavaEditor("\n", PROJECT1, PKG1, CLS1);
        alice.editor.typeTextInJavaEditor(";\n", PROJECT1, PKG1, CLS1);
        alice.editor.typeTextInJavaEditor("thread.join(", PROJECT1, PKG1, CLS1);
        alice.editor.typeTextInJavaEditor("\n", PROJECT1, PKG1, CLS1);
        alice.editor.typeTextInJavaEditor(";", PROJECT1, PKG1, CLS1);
        alice.editor.quickfix(CLS1 + ".java", "Add throws declaration");
        assertContains("InterruptedException",
            alice.editor.getTextOnLine(CLS1 + ".java", 3));
        alice.editor.pressShortCut(CLS1 + ".java", SWT.NONE, '\n');
        alice.editor.pressShortCutSave(CLS1 + ".java");
        assertFalse(alice.editor.isClassDirty(PROJECT1, PKG1, CLS1,
            ID_JAVA_EDITOR));
        alice.workbench.sleep(100);
        alice.editor.pressShortRunAsJavaApplication(CLS1 + ".java");
        alice.consoleV.waitsUntilTextInConsoleExisted();
        assertContains("Hello World", alice.consoleV.getTextInConsole());

    }
}
