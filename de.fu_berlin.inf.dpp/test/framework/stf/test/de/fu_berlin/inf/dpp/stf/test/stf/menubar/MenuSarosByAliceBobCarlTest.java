package de.fu_berlin.inf.dpp.stf.test.stf.menubar;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;
import org.junit.BeforeClass;
import org.junit.Test;

public class MenuSarosByAliceBobCarlTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  public void inviteUsersInSession() throws Exception {
    Util.setUpSessionWithJavaProjectAndClass(
        Constants.PROJECT1, Constants.PKG1, Constants.CLS1, ALICE, BOB);
    assertFalse(CARL.superBot().views().sarosView().isInSession());
    Util.addTestersToSession(Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT, ALICE, CARL);

    CARL.superBot().views().packageExplorerView().waitUntilResourceIsShared(Constants.PROJECT1);
    assertTrue(CARL.superBot().views().sarosView().isInSession());
  }
}
