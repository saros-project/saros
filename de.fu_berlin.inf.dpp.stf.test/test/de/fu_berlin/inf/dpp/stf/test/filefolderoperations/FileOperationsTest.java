package de.fu_berlin.inf.dpp.stf.test.filefolderoperations;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.Constants;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

import java.io.IOException;
import java.rmi.RemoteException;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileOperationsTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  /**
   * Preconditions:
   *
   * <ol>
   *   <li>ALICE (Host, Write Access) shares a Java project with BOB and CARL.
   *   <li>BOB (Read-Only Access)
   *   <li>CARL (Read-Only Access)
   * </ol>
   */
  @Before
  public void beforeEveryTest() throws Exception {
    closeAllShells();
    closeAllEditors();
    clearWorkspaces();

    Util.setUpSessionWithJavaProjectAndClass("Foo1_Saros", "my.pkg", "MyClass", ALICE, BOB, CARL);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared("Foo1_Saros/src/my/pkg/MyClass.java");

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared("Foo1_Saros/src/my/pkg/MyClass.java");

    Util.activateFollowMode(ALICE, CARL);
  }

  @After
  public void afterEveryTest() throws Exception {
    leaveSessionHostFirst(ALICE);
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>ALICE rename the class "CLS1" to "CLS2"
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>the class'name are renamed by BOB and CARL too
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void testRenameFile() throws RemoteException {

    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .exists(Constants.CLS1_SUFFIX));
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .refactor()
        .rename(Constants.CLS2);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);
    assertFalse(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .exists(Constants.CLS1_SUFFIX));
    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .exists(Constants.CLS2_SUFFIX));

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);
    assertFalse(
        CARL.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .exists(Constants.CLS1_SUFFIX));
    assertTrue(
        CARL.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .exists(Constants.CLS2_SUFFIX));
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>ALICE delete the class "CLS1"
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>the class are deleted by BOB and CARL too
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void testDeleteFile() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .delete();
    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassNotExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    assertFalse(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .exists(Constants.CLS1_SUFFIX));
    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassNotExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    assertFalse(
        CARL.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .exists(Constants.CLS1_SUFFIX));
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>ALICE create a new package "PKG2"
   *   <li>ALICE create a new class "CLS1" under package "PKG2"
   *   <li>ALICE set text in the class "CLS1"
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>the package "PKG2" are created by BOB and CARL too
   *   <li>the new class "CLS1" are created by BOB and CARL too
   *   <li>CARL and BOB should see the change by ALICE
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void testNewPkgAndClass() throws CoreException, IOException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .pkg(Constants.PROJECT1, Constants.PKG2);
    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilPkgExists(Constants.PROJECT1, Constants.PKG2);
    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilPkgExists(Constants.PROJECT1, Constants.PKG2);
    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectSrc(Constants.PROJECT1)
            .exists(Constants.PKG2));
    assertTrue(
        CARL.superBot()
            .views()
            .packageExplorerView()
            .selectSrc(Constants.PROJECT1)
            .exists(Constants.PKG2));

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .cls(Constants.PROJECT1, Constants.PKG2, Constants.CLS1);
    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG2, Constants.CLS1);
    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG2, Constants.CLS1);
    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG2)
            .exists(Constants.CLS1_SUFFIX));
    assertTrue(
        CARL.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG2)
            .exists(Constants.CLS1_SUFFIX));

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).setTextFromFile(Constants.CP1);

    String clsContentOfAlice =
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.classPathToFilePath(Constants.PROJECT1, Constants.PKG2, Constants.CLS1));

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilFileContentSame(
            clsContentOfAlice,
            Util.classPathToFilePath(Constants.PROJECT1, Constants.PKG2, Constants.CLS1));

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilFileContentSame(
            clsContentOfAlice,
            Util.classPathToFilePath(Constants.PROJECT1, Constants.PKG2, Constants.CLS1));

    String clsContentOfBob =
        BOB.superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.classPathToFilePath(Constants.PROJECT1, Constants.PKG2, Constants.CLS1));

    String clsContentOfCarl =
        CARL.superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.classPathToFilePath(Constants.PROJECT1, Constants.PKG2, Constants.CLS1));

    assertTrue(clsContentOfBob.equals(clsContentOfAlice));
    assertTrue(clsContentOfCarl.equals(clsContentOfAlice));
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>ALICE create a new package "PKG2" and under it create a new class "CLS2"
   *   <li>ALICE move the class "CLS2" to the package "PKG1"
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>
   *   <li>the class "CLS2" should be moved into the package "PKG1" by CARL and BOB
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void testMoveClass() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .pkg(Constants.PROJECT1, Constants.PKG2);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .cls(Constants.PROJECT1, Constants.PKG2, Constants.CLS2);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG2, Constants.CLS2)
        .refactor()
        .moveClassTo(Constants.PROJECT1, Constants.PKG1);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);

    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .exists(Constants.CLS2_SUFFIX));
    assertFalse(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG2)
            .exists(Constants.CLS2_SUFFIX));
    assertTrue(
        CARL.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .exists(Constants.CLS2_SUFFIX));
    assertFalse(
        CARL.superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG2)
            .exists(Constants.CLS2_SUFFIX));
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>ALICE rename the package "PKG1" to "PKG2"
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>the package should be renamed by CARL and BOB
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void testRenamePkg() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectPkg(Constants.PROJECT1, Constants.PKG1)
        .refactor()
        .rename(Constants.PKG2);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilPkgExists(Constants.PROJECT1, Constants.PKG2);
    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilPkgNotExists(Constants.PROJECT1, Constants.PKG1);
    assertFalse(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectJavaProject(Constants.PROJECT1)
            .exists(Constants.PKG1));
    assertTrue(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectSrc(Constants.PROJECT1)
            .exists(Constants.PKG2));

    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilPkgExists(Constants.PROJECT1, Constants.PKG2);
    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilPkgNotExists(Constants.PROJECT1, Constants.PKG1);
    assertFalse(
        CARL.superBot()
            .views()
            .packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .exists(Constants.PKG1));
    assertTrue(
        CARL.superBot()
            .views()
            .packageExplorerView()
            .selectSrc(Constants.PROJECT1)
            .exists(Constants.PKG2));
  }

  /**
   * Steps:
   *
   * <ol>
   *   <li>ALICE delete the package "PKG1"
   * </ol>
   *
   * Result:
   *
   * <ol>
   *   <li>the package should be deleted by CARL and BOB
   * </ol>
   *
   * @throws RemoteException
   */
  @Test
  public void testDeletePkg() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectPkg(Constants.PROJECT1, Constants.PKG1)
        .delete();
    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilPkgNotExists(Constants.PROJECT1, Constants.PKG1);
    CARL.superBot()
        .views()
        .packageExplorerView()
        .waitUntilPkgNotExists(Constants.PROJECT1, Constants.PKG1);
    assertFalse(
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectJavaProject(Constants.PROJECT1)
            .exists(Constants.PKG1));
    assertFalse(
        CARL.superBot()
            .views()
            .packageExplorerView()
            .selectJavaProject(Constants.PROJECT1)
            .exists(Constants.PKG1));
  }
}
