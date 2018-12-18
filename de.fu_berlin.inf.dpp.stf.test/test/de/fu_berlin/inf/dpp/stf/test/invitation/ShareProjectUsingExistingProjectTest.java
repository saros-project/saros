package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.stf.Constants;
import java.rmi.RemoteException;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ShareProjectUsingExistingProjectTest extends StfTestCase {

  /**
   * Preconditions:
   *
   * <ol>
   *   <li>Alice (Host, Write Access)
   *   <li>Bob (Read-Only Access)
   * </ol>
   */
  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Before
  public void runBeforeEveryTest() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    BOB.superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);
  }

  @After
  public void runAfterEveryTest() throws Exception {
    leaveSessionHostFirst(ALICE);
    clearWorkspaces();
  }

  @Test
  public void shareProjectUsingExistingProject() throws Exception {
    assertFalse(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS1_SUFFIX) + ".*"));
    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS2) + ".*"));
    Util.buildSessionSequentially(
        Constants.PROJECT1, TypeOfCreateProject.EXIST_PROJECT, ALICE, BOB);
    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS1) + ".*"));
    assertFalse(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS2) + ".*"));
  }

  @Test
  @Ignore("The feature is currently disabled because it is not working as expected")
  public void testShareProjectUsingExistingProjectWithCopy() throws Exception {
    Util.buildSessionSequentially(
        Constants.PROJECT1, TypeOfCreateProject.EXIST_PROJECT_WITH_COPY, ALICE, BOB);
    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .tree()
            .existsWithRegex(Pattern.quote(Constants.PROJECT1) + ".*"));
    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS2) + ".*"));
    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .tree()
            .existsWithRegex(Pattern.quote(Constants.PROJECT1_COPY) + ".*"));
    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1_COPY, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS1) + ".*"));
  }
}
