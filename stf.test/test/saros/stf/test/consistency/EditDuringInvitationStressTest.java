package saros.stf.test.consistency;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;
import static saros.stf.shared.Constants.ACCEPT;
import static saros.stf.shared.Constants.SHELL_SESSION_INVITATION;

import java.rmi.RemoteException;
import java.util.Random;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.tester.SarosTester;
import saros.stf.client.util.Util;
import saros.stf.shared.Constants.TypeOfCreateProject;
import saros.stf.test.Constants;
import saros.test.util.EclipseTestThread;

public class EditDuringInvitationStressTest extends StfTestCase {

  private EclipseTestThread bobIsWriting;
  private final String[] CLASS_NAMES = {"ClassA", "ClassB", "ClassC", "ClassD", "ClassE"};

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  public void testEditMultipleClassesDuringInvitation() throws Exception {

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, CLASS_NAMES);

    Util.buildSessionSequentially(Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, "ClassA");

    openTestClasses(BOB);

    ALICE.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

    bobIsWriting =
        createTestThread(
            new EclipseTestThread.Runnable() {

              private final Random random = new Random();

              @Override
              public void run() throws Exception {
                for (int i = 0; i < 100; i++) {
                  String nextClass = CLASS_NAMES[i % 5] + ".java";
                  BOB.remoteBot().editor(nextClass).typeText(String.valueOf(generateCharacter()));
                }
              }

              // ([A-Z] union [a-z])
              private char generateCharacter() {
                return (char)
                    (((random.nextInt() & 0x7FFFFFFF) % 26) | 0x41 | ((random.nextInt() & 1) << 5));
              }
            });

    bobIsWriting.start();

    CARL.superBot().confirmShellAddProjectWithNewProject(Constants.PROJECT1);

    bobIsWriting.join();
    bobIsWriting.verify();

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, "ClassA");

    openTestClasses(ALICE);
    openTestClasses(CARL);

    compareFilesOfTesters(BOB, ALICE);
    compareFilesOfTesters(BOB, CARL);
  }

  private void openTestClasses(SarosTester tester) throws RemoteException {
    for (String className : CLASS_NAMES) {
      tester
          .superBot()
          .views()
          .packageExplorerView()
          .selectClass(Constants.PROJECT1, Constants.PKG1, className)
          .open();
    }
  }

  private void compareFilesOfTesters(SarosTester testerA, SarosTester testerB)
      throws RemoteException {
    for (String className : CLASS_NAMES) {
      String textByA = testerA.remoteBot().editor(className + ".java").getText();
      String textByB = testerB.remoteBot().editor(className + ".java").getText();
      assertEquals(textByA, textByB);
    }
  }
}
