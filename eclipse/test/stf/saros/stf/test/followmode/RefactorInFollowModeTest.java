package saros.stf.test.followmode;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.annotation.TestLink;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.shared.Constants.TypeOfCreateProject;

@TestLink(id = "Saros-40_followmode_and_refactoring")
public class RefactorInFollowModeTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testRefactorInFollowMode() throws Exception {

    ALICE.superBot().internal().createJavaProject("foo");
    ALICE.superBot().internal().createJavaClass("foo", "bar", "HelloWorld");

    ALICE.superBot().views().packageExplorerView().selectClass("foo", "bar", "HelloWorld").open();

    StringBuilder builder = new StringBuilder();
    builder.append(ALICE.remoteBot().editor("HelloWorld.java").getText());
    builder.setLength(builder.length() - 1);
    builder.append("\n\nint myfoobar = 0;\n");
    for (int i = 0; i < 10; i++)
      builder
          .append("public void test")
          .append(i)
          .append("()\n{myfoobar = ")
          .append(i)
          .append(
              ";\n}\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

    builder.append("}");

    ALICE.remoteBot().editor("HelloWorld.java").closeWithSave();

    Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared("foo/src/bar/HelloWorld.java");

    ALICE.superBot().views().packageExplorerView().selectClass("foo", "bar", "HelloWorld").open();

    BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).followParticipant();

    List<Integer> viewPortBeforeRefactor = BOB.remoteBot().editor("HelloWorld.java").getViewport();

    ALICE.remoteBot().editor("HelloWorld.java").setText(builder.toString());

    ALICE.remoteBot().editor("HelloWorld.java").navigateTo(4, 8);

    // wait for the text to be selected
    Thread.sleep(10000);

    ALICE.remoteBot().activateWorkbench();
    ALICE.remoteBot().menu("Refactor").menu("Rename...").click();
    ALICE.remoteBot().editor("HelloWorld.java").typeText("bar\n");

    // refactor might take a while
    Thread.sleep(10000);

    List<Integer> viewPortAfterRefactor = BOB.remoteBot().editor("HelloWorld.java").getViewport();

    assertEquals("viewport changed", viewPortBeforeRefactor.get(0), viewPortAfterRefactor.get(0));

    assertEquals(
        "file content is not the same",
        ALICE.remoteBot().editor("HelloWorld.java").getText(),
        BOB.remoteBot().editor("HelloWorld.java").getText());
  }
}
