package saros.stf.test.stf.view.explorer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import java.rmi.RemoteException;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.test.Constants;

public class PackageExplorerViewTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @After
  public void afterEveryTest() throws Exception {
    clearWorkspaces();
  }

  @Test
  public void testIsFileExist() throws RemoteException {
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
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .delete();
    assertFalse(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS1_SUFFIX) + ".*"));
  }

  @Test
  public void testIsFileExistWithGUI() throws RemoteException {
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

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .delete();
    assertFalse(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS1_SUFFIX) + ".*"));
  }

  @Test
  @Ignore("can't click the menu 'Multiple Contacts...'")
  public void testShareWith() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectProject(Constants.PROJECT1)
        .shareWith()
        .multipleContacts(Constants.PROJECT1, BOB.getJID());
  }
}
