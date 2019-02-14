package de.fu_berlin.inf.dpp.stf.test.editing;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import org.junit.BeforeClass;
import org.junit.Test;

@TestLink(id = "Saros-87_Observer_trying_to_type")
public class EditWithReadAccessOnlyTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testEditingWithReadOnlyAccess() throws Exception {
    Util.setUpSessionWithJavaProjectAndClass("foo", "bar", "HelloWorld", ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo");

    ALICE.superBot().views().packageExplorerView().selectClass("foo", "bar", "HelloWorld").open();

    BOB.superBot().views().packageExplorerView().selectClass("foo", "bar", "HelloWorld").open();

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
