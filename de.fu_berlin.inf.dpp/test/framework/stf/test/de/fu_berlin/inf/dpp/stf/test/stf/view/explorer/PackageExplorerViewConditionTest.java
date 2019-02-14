package de.fu_berlin.inf.dpp.stf.test.stf.view.explorer;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import java.rmi.RemoteException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PackageExplorerViewConditionTest extends StfTestCase {
  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  @Before
  public void beforeEveryTest() throws Exception {
    closeAllEditors();
    clearWorkspaces();
  }

  @Test
  public void testWaitUntilFolderExists() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().project("foo");
    ALICE.superBot().views().packageExplorerView().tree().newC().folder("bar");

    ALICE.superBot().views().packageExplorerView().waitUntilFolderExists("foo", "bar");
  }

  @Test
  public void waitUntilFolderNotExists() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().project("foo");
    ALICE.superBot().views().packageExplorerView().selectProject("foo");

    ALICE.superBot().views().packageExplorerView().tree().newC().folder("bar");

    ALICE.superBot().views().packageExplorerView().waitUntilFolderExists("foo", "bar");
    ALICE.superBot().views().packageExplorerView().selectFolder("foo", "bar").delete();
    ALICE.superBot().views().packageExplorerView().waitUntilFolderNotExists("foo", "bar");
  }

  @Test
  public void waitUntilPkgExists() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject("foo");

    ALICE.superBot().views().packageExplorerView().selectJavaProject("foo");
    ALICE.superBot().views().packageExplorerView().tree().newC().pkg("foo", "bar");

    ALICE.superBot().views().packageExplorerView().waitUntilPkgExists("foo", "bar");
  }

  @Test
  public void waitUntilPkgNotExists() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject("foo");

    ALICE.superBot().views().packageExplorerView().selectJavaProject("foo");
    ALICE.superBot().views().packageExplorerView().tree().newC().pkg("foo", "bar");

    ALICE.superBot().views().packageExplorerView().waitUntilPkgExists("foo", "bar");

    ALICE.superBot().views().packageExplorerView().selectPkg("foo", "bar").delete();
    ALICE.superBot().views().packageExplorerView().waitUntilPkgNotExists("foo", "bar");
  }

  @Test
  public void waitUntilFileExists() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().project("foo");
    ALICE.superBot().views().packageExplorerView().tree().newC().file("bar");

    ALICE.superBot().views().packageExplorerView().waitUntilFileExists("foo", "bar");
  }

  @Test
  public void waitUntilFileNotExists() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().project("foo");
    ALICE.superBot().views().packageExplorerView().selectProject("foo");

    ALICE.superBot().views().packageExplorerView().tree().newC().file("bar");

    ALICE.superBot().views().packageExplorerView().waitUntilFileExists("foo", "bar");
    ALICE.superBot().views().packageExplorerView().selectFolder("foo", "bar").delete();
    ALICE.superBot().views().packageExplorerView().waitUntilFileNotExists("foo", "bar");
  }

  @Test
  public void waitUntilClassExists() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses("foo", "bar", "foo");
    ALICE.superBot().views().packageExplorerView().waitUntilClassExists("foo", "bar", "foo");
  }

  @Test
  public void waitUntilClassNotExists() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses("foo", "bar", "foo");
    ALICE.superBot().views().packageExplorerView().selectClass("foo", "bar", "foo").delete();
    ALICE.superBot().views().packageExplorerView().waitUntilClassNotExists("foo", "bar", "foo");
  }
}
