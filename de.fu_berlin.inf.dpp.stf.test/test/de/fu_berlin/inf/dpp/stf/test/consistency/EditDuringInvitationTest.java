package de.fu_berlin.inf.dpp.stf.test.consistency;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.Constants;
import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.test.util.EclipseTestThread;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.junit.BeforeClass;
import org.junit.Test;

@TestLink(id = "Saros-36_edit_during_invitation")
public class EditDuringInvitationTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  public void testEditDuringInvitation() throws Exception {

    Util.setUpSessionWithJavaProjectAndClass(
        Constants.PROJECT1, Constants.PKG1, Constants.CLS1, ALICE, BOB);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    ALICE.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    EclipseTestThread bobIsWriting =
        createTestThread(
            new EclipseTestThread.Runnable() {

              @Override
              public void run() throws Exception {
                for (int i = 0; i < 20; i++)
                  BOB.remoteBot().editor(Constants.CLS1_SUFFIX).typeText("FooBar");
              }
            });

    bobIsWriting.start();

    CARL.superBot().confirmShellAddProjectWithNewProject(Constants.PROJECT1);

    bobIsWriting.join();
    bobIsWriting.verify();

    String textByBob = BOB.remoteBot().editor(Constants.CLS1_SUFFIX).getText();

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsTextSame(textByBob);

    String textByAlice = ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getText();

    // There are bugs here, CARL get completely different content as BOB.
    try {
      CARL.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsTextSame(textByBob);
    } catch (TimeoutException e) {
      //
    }
    String textByCarl = CARL.remoteBot().editor(Constants.CLS1_SUFFIX).getText();

    assertEquals(textByBob, textByAlice);
    assertEquals(textByBob, textByCarl);
  }
}
