package saros.stf.test.consistency;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;
import static saros.stf.shared.Constants.ACCEPT;
import static saros.stf.shared.Constants.SHELL_SESSION_INVITATION;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.EclipseTestThread;
import saros.stf.client.util.Util;
import saros.stf.test.stf.Constants;

public class EditDuringNonHostInvitationTest extends StfTestCase {

  private EclipseTestThread aliceIsWriting;

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  @Ignore("Non-Host Invitation is currently deactivated")
  public void testEditDuringInvitationNonHostInvites() throws Exception {

    Util.setUpSessionWithJavaProjectAndClass(
        Constants.PROJECT1, Constants.PKG1, Constants.CLS1, ALICE, BOB);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    BOB.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    aliceIsWriting =
        createTestThread(
            new EclipseTestThread.Runnable() {

              @Override
              public void run() throws Exception {
                for (int i = 0; i < 20; i++)
                  ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).typeText("FooBar");
              }
            });

    aliceIsWriting.start();

    CARL.superBot().confirmShellAddProjectWithNewProject(Constants.PROJECT1);

    aliceIsWriting.join();
    aliceIsWriting.verify();

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();

    String textByBob = BOB.remoteBot().editor(Constants.CLS1_SUFFIX).getText();

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsTextSame(textByBob);

    String textByAlice = ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getText();

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
