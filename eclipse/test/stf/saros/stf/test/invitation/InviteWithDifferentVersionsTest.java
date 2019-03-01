package saros.stf.test.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.shared.Constants.OK;
import static saros.stf.shared.Constants.SHELL_SESSION_INVITATION;

import java.rmi.RemoteException;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotShell;

public class InviteWithDifferentVersionsTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testInvitationWithDifferentVersions() throws Exception {
    BOB.superBot().views().sarosView().disconnect();

    BOB.superBot().internal().changeSarosVersion("1.1.1");

    BOB.superBot().views().sarosView().connectWith(BOB.getJID(), BOB.getPassword(), true);

    BOB.superBot().views().sarosView().waitUntilIsConnected();

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses("foo", "bar", "VersionMismatch");

    ALICE.superBot().menuBar().saros().shareProjects("foo", BOB.getJID());

    Thread.sleep(5000);
    List<String> shellNamesAlice = ALICE.remoteBot().getOpenShellNames();
    List<String> shellNamesBob = BOB.remoteBot().getOpenShellNames();

    boolean foundAlice = false;

    String problemShellContentText = null;

    for (String shellName : shellNamesAlice) {
      if (shellName.matches(".*Problem Occurred.*")) {
        foundAlice = true;
        IRemoteBotShell shell = ALICE.remoteBot().shell(shellName);
        shell.activate();
        problemShellContentText = shell.bot().label(2).getText();
        shell.bot().button(OK).click();
        shell.waitShortUntilIsClosed();
        break;
      }
    }

    boolean foundBob = false;

    for (String shellName : shellNamesBob) {
      if (shellName.equals(SHELL_SESSION_INVITATION)) {
        foundBob = true;
        break;
      }
    }

    assertTrue("Alice session invitation continued", foundAlice);

    assertFalse("Bobs invitation window is open although he has an invalid version", foundBob);

    boolean isVersionMismatch =
        problemShellContentText != null
            && problemShellContentText
                .toLowerCase()
                .contains("is not compatible with your installed saros plugin");

    assertTrue("Expected version mismatch but got: " + problemShellContentText, isVersionMismatch);
  }

  @AfterClass
  public static void resetSarosVersion() throws RemoteException {
    BOB.superBot().internal().resetSarosVersion();
  }
}
