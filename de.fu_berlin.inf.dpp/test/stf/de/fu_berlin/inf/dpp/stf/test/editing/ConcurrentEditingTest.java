package de.fu_berlin.inf.dpp.stf.test.editing;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;
import java.rmi.RemoteException;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConcurrentEditingTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  static final String FILE = "file.txt";

  /**
   * Test to reproduce bug "Inconsistency when concurrently writing at same position"
   *
   * @throws RemoteException
   * @throws InterruptedException
   * @see <a href="https://sourceforge.net/p/dpp/bugs/419/">Bug tracker entry 419</a>
   */
  @Test
  public void testBugInconsistencyConcurrentEditing() throws Exception, InterruptedException {
    ALICE.superBot().views().packageExplorerView().tree().newC().project(Constants.PROJECT1);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectProject(Constants.PROJECT1)
        .newC()
        .file(FILE);
    ALICE.remoteBot().waitUntilEditorOpen(FILE);
    ALICE.remoteBot().editor(FILE).setTextFromFile("test/resources/stf/lorem.txt");
    ALICE.remoteBot().editor(FILE).navigateTo(0, 6);

    Util.buildSessionSequentially(Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);
    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilFileExists(Constants.PROJECT1 + "/" + FILE);
    BOB.superBot().views().packageExplorerView().selectFile(Constants.PROJECT1, FILE).open();

    BOB.remoteBot().waitUntilEditorOpen(FILE);
    BOB.remoteBot().editor(FILE).navigateTo(0, 30);

    Thread.sleep(1000);

    // Alice goes to 0,6 and hits Delete
    ALICE.remoteBot().activateWorkbench();
    int waitActivate = 100;
    ALICE.remoteBot().editor(FILE).show();

    ALICE.remoteBot().editor(FILE).waitUntilIsActive();
    // at the same time, Bob enters L at 0,30
    BOB.remoteBot().activateWorkbench();
    Thread.sleep(waitActivate);
    BOB.remoteBot().editor(FILE).show();
    BOB.remoteBot().editor(FILE).waitUntilIsActive();

    Thread.sleep(waitActivate);
    ALICE.remoteBot().editor(FILE).pressShortcut(new String[] {IKeyLookup.BACKSPACE_NAME});

    BOB.remoteBot().editor(FILE).typeText("L");
    // both sleep for less than 1000ms

    // Alice hits Delete again
    ALICE.remoteBot().editor(FILE).pressShortcut(new String[] {IKeyLookup.BACKSPACE_NAME});
    // Bob enters o
    BOB.remoteBot().editor(FILE).typeText("o");

    Thread.sleep(1000);
    String ALICEText = ALICE.remoteBot().editor(FILE).getText();
    String BOBText = BOB.remoteBot().editor(FILE).getText();

    ALICE.remoteBot().editor(FILE).closeWithoutSave();
    BOB.remoteBot().editor(FILE).closeWithoutSave();

    assertEquals(ALICEText, BOBText);
  }
}
