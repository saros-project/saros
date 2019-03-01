package saros.stf.test.stf.contextmenu;

import static saros.stf.client.tester.SarosTester.ALICE;

import java.rmi.RemoteException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.test.Constants;

public class ContextMenuDeleteTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  @After
  public void afterEveryTest() throws Exception {
    clearWorkspaces();
  }

  @Test
  public void testDeleteProject() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().project(Constants.PROJECT1);
    ALICE.superBot().views().packageExplorerView().selectJavaProject(Constants.PROJECT1).delete();
  }

  @Test
  public void testDeleteAllItems() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectPkg(Constants.PROJECT1, Constants.PKG1)
        .newC()
        .cls(Constants.CLS2);
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
        .selectPkg(Constants.PROJECT1, Constants.PKG1)
        .delete();
  }
}
