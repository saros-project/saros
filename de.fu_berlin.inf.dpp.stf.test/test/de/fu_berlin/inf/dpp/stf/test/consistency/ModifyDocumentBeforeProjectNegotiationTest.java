package de.fu_berlin.inf.dpp.stf.test.consistency;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_ADD_PROJECTS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.stf.Constants;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModifyDocumentBeforeProjectNegotiationTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  public void testModifyDocumentBeforeProjectNegotiation() throws Exception {

    Util.setUpSessionWithJavaProjectAndClass(
        Constants.PROJECT1, Constants.PKG1, Constants.CLS1, ALICE, BOB);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    ALICE.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    CARL.remoteBot().waitLongUntilShellIsOpen(SHELL_ADD_PROJECTS);

    // this test will fail if a jupiter proxy is added when bob is typing
    // text now
    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).typeText("The mighty Foobar");

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 10000);

    CARL.superBot().confirmShellAddProjectWithNewProject(Constants.PROJECT1);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    BOB.remoteBot().editor(Constants.CLS1_SUFFIX).typeText(" bars everyfoo");

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(CARL.getJID(), 10000);

    CARL.remoteBot().editor(Constants.CLS1_SUFFIX).typeText("\n\n\nFoo yourself ");

    CARL.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 10000);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).navigateTo(0, 0);
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).typeText("blablablublub");

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 10000);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(CARL.getJID(), 10000);

    String aliceText = ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getText();
    String bobText = BOB.remoteBot().editor(Constants.CLS1_SUFFIX).getText();
    String carlText = CARL.remoteBot().editor(Constants.CLS1_SUFFIX).getText();

    assertEquals(aliceText, bobText);
    assertEquals(aliceText, carlText);
  }
}
