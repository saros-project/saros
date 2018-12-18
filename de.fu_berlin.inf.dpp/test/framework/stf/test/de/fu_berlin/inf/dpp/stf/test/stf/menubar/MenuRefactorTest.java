package de.fu_berlin.inf.dpp.stf.test.stf.menubar;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.test.stf.Constants;
import java.rmi.RemoteException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MenuRefactorTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  @Before
  public void beforeEveryTest() throws Exception {
    clearWorkspaces();
  }

  /* *********************************************
   *
   * all related actions with the sub menus of the context menu "Refactor"
   *
   * ********************************************
   */
  @Test
  public void testMoveClassTo() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

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
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .refactor()
        .moveClassTo(Constants.PROJECT1, Constants.PKG2);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG2, Constants.CLS1);
  }

  @Test
  public void testRenameClass() throws RemoteException {

    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .refactor()
        .rename(Constants.CLS2);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);
  }

  @Test
  public void testRenameFile() throws RemoteException {
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

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFile(Constants.PROJECT1, Constants.FOLDER1, Constants.FILE1)
        .refactor()
        .rename(Constants.FILE2);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .waitUntilFileExists(Constants.PROJECT1, Constants.FOLDER1, Constants.FILE2);
  }

  @Test
  public void testRenameFolder() throws RemoteException {
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
        .refactor()
        .rename(Constants.FOLDER2);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .waitUntilFolderExists(Constants.PROJECT1, Constants.FOLDER2);
  }

  @Test
  public void testRenamePackage() throws RemoteException {
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
        .selectPkg(Constants.PROJECT1, Constants.PKG1)
        .refactor()
        .rename(Constants.PKG2);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .waitUntilPkgExists(Constants.PROJECT1, Constants.PKG2);
  }

  @Test
  public void testRenameProject() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectJavaProject(Constants.PROJECT1)
        .refactor()
        .rename(Constants.PROJECT2);

    ALICE.superBot().views().packageExplorerView().waitUntilFolderExists(Constants.PROJECT2);
  }
}
