package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import org.junit.BeforeClass;
import org.junit.Test;

public class InviteAndLeaveStressTest extends StfTestCase {

  private static final int MAX_PROJECTS = 10;

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  public void testInviteAndLeaveStress() throws Exception {

    for (int i = 0; i < MAX_PROJECTS; i++) {
      ALICE.superBot().internal().createProject("foo" + i);
      ALICE.superBot().internal().createFile("foo" + i, "foo.txt", "foo");
    }

    for (int i = 0; i < MAX_PROJECTS; i++) {
      Util.buildSessionConcurrently("foo" + i, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

      leaveSessionHostFirst(ALICE);
    }
  }
}
