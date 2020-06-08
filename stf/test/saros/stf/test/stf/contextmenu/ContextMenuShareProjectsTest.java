package saros.stf.test.stf.contextmenu;

import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.shared.Constants.SHELL_SHARE_PROJECT;

import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import saros.stf.test.stf.Constants;

public class ContextMenuShareProjectsTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  /*
   * Just tests that the dialog is opened. The functionality of the dialog is
   * tested in MenuSarosByAliceBobTest
   */
  @Test
  public void testShareProjectsOpens() throws Exception {
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
        .selectJavaProject(Constants.PROJECT1)
        .openShareProjects();

    IRemoteBotShell shell = ALICE.remoteBot().shell(SHELL_SHARE_PROJECT);
    assertTrue(shell.activate());
  }
}
