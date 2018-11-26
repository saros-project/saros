package de.fu_berlin.inf.dpp.stf.test.stf.menubar;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.test.Constants;
import java.rmi.RemoteException;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class MenuFileTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  @After
  public void afterEveryTest() throws Exception {
    clearWorkspaces();
  }

  /**
   * ********************************************
   *
   * <p>test all related actions with the sub menus of the context menu "New"
   *
   * <p>********************************************
   */
  @Test
  public void testNewProject() throws RemoteException {

    ALICE.superBot().views().packageExplorerView().tree().newC().project(Constants.PROJECT1);
    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .existsWithRegex(Pattern.quote(Constants.PROJECT1) + ".*"));
  }

  @Test
  public void testNewJavaProject() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);
    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .existsWithRegex(Pattern.quote(Constants.PROJECT1) + ".*"));
  }

  @Test
  public void testNewFolder() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectJavaProject(Constants.PROJECT1)
        .newC()
        .folder(Constants.FOLDER1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
        .newC()
        .folder(Constants.FOLDER2);
    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Pattern.quote(Constants.FOLDER1) + ".*"));
    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
            .existsWithRegex(Pattern.quote(Constants.FOLDER2) + ".*"));
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFolder(Constants.PROJECT1, Constants.FOLDER1, Constants.FOLDER2)
        .delete();
    assertFalse(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
            .existsWithRegex(Pattern.quote(Constants.FOLDER2) + ".*"));
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
        .delete();
    assertFalse(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Pattern.quote(Constants.FOLDER1) + ".*"));
  }

  @Test
  public void testNewPackage1() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .pkg(Constants.PROJECT1, Constants.PKG1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .pkg(Constants.PROJECT1, Constants.PKG1 + ".subpkg");
    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectSrc(Constants.PROJECT1)
            .existsWithRegex(Pattern.quote(Constants.PKG1) + ".*"));
    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectSrc(Constants.PROJECT1)
            .existsWithRegex(Pattern.quote(Constants.PKG1 + ".subpkg") + ".*"));
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectPkg(Constants.PROJECT1, Constants.PKG1 + ".subpkg")
        .delete();
    assertFalse(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Pattern.quote(Constants.PKG1 + ".subpkg") + ".*"));
  }

  @Test
  public void testNewPackage2() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().project(Constants.PROJECT1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectProject(Constants.PROJECT1)
        .newC()
        .folder(Constants.FOLDER1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
        .newC()
        .file(Constants.FILE1);
    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
            .existsWithRegex(Pattern.quote(Constants.FILE1) + ".*"));
  }

  @Test
  public void testNewClass() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS1_SUFFIX) + ".*"));
  }

  @Test
  public void testNewProjectWithClass() throws RemoteException {
    assertFalse(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .existsWithRegex(Pattern.quote(Constants.PROJECT1) + ".*"));
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, "pkg", "Cls");
    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .existsWithRegex(Pattern.quote(Constants.PROJECT1) + ".*"));
    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, "pkg")
            .existsWithRegex(Pattern.quote("Cls" + SUFFIX_JAVA) + ".*"));
  }
}
