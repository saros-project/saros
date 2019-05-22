package saros.stf.test.editing;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.annotation.TestLink;
import saros.stf.client.StfTestCase;

@TestLink(id = "Saros-87_Observer_trying_to_type")
public class EditWithReadAccessOnlyTest extends StfTestCase {

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

    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).grantWriteAccess();
    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).waitUntilHasWriteAccess();
    if (checkIfTestRunInTestSuite()) {
      ALICE.superBot().internal().deleteFolder("Foo1_Saros", "src");
      tearDownSaros();
    } else {
      tearDownSarosLast();
    }
  }

  @Test
  public void testEditingWithReadOnlyAccess() throws Exception {

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("Foo1_Saros");
    ALICE.superBot().internal().createJavaClass("Foo1_Saros", "bar", "HelloWorld");
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass("Foo1_Saros", "bar", "HelloWorld")
        .open();

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass("Foo1_Saros", "bar", "HelloWorld")
        .open();

    ALICE.remoteBot().waitUntilEditorOpen("HelloWorld.java");
    BOB.remoteBot().waitUntilEditorOpen("HelloWorld.java");

    String aliceEditorText = ALICE.remoteBot().editor("HelloWorld.java").getText();

    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).restrictToReadOnlyAccess();

    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).waitUntilHasReadOnlyAccess();

    BOB.remoteBot().editor("HelloWorld.java").typeText("1234567890");
    String bobEditorText = BOB.remoteBot().editor("HelloWorld.java").getText();

    assertEquals(aliceEditorText, bobEditorText);
  }
}
