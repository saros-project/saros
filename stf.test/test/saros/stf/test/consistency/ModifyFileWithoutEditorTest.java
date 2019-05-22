package saros.stf.test.consistency;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.annotation.TestLink;
import saros.stf.client.StfTestCase;

@TestLink(id = "Saros-84_synchronize_file_modifications_outside_of_editors")
public class ModifyFileWithoutEditorTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
    restoreSessionIfNecessary("Foo1_Saros", ALICE, BOB);
  }

  @Before
  public void setUp() throws Exception {
    closeAllShells();
    closeAllEditors();
  }

  @After
  public void cleanUpSaros() throws Exception {
    if (checkIfTestRunInTestSuite()) {
      ALICE.superBot().internal().deleteFolder("Foo1_Saros", "src");
      tearDownSaros();
    } else {
      tearDownSarosLast();
    }
  }

  @Test
  public void testCreateSameFileAtOnce() throws Exception {

    ALICE.superBot().internal().createFile("Foo1_Saros", "src/readme.txt", "Chuck Norris");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared("Foo1_Saros/src/readme.txt");

    ALICE.superBot().internal().append("Foo1_Saros", "src/readme.txt", " finished");

    // do not make inconsistencies
    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 10000);

    BOB.superBot().internal().append("Foo1_Saros", "src/readme.txt", " World of Warcraft");

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 10000);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFile("Foo1_Saros", "src", "readme.txt")
        .open();

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectFile("Foo1_Saros", "src", "readme.txt")
        .open();

    ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();
    BOB.remoteBot().editor("readme.txt").waitUntilIsActive();

    String aliceText = ALICE.remoteBot().editor("readme.txt").getText();
    String bobText = BOB.remoteBot().editor("readme.txt").getText();
    assertEquals("Chuck Norris finished World of Warcraft", aliceText);
    assertEquals(aliceText, bobText);
  }
}
