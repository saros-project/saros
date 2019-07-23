package saros.stf.test.html;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.ui.View.MAIN_VIEW;
import static saros.ui.View.SHARE_PROJECT_VIEW;

import java.rmi.RemoteException;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfHtmlTestCase;
import saros.stf.test.stf.Constants;

public class StartSessionWizardTest extends StfHtmlTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Before
  public void initProject() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
  }

  @Test
  public void shouldOpenDialog() throws Exception {
    assertTrue("Main view did not load", ALICE.htmlBot().view(MAIN_VIEW).isOpen());
    assertTrue(
        "No 'Start Session' button",
        ALICE.htmlBot().view(MAIN_VIEW).hasElementWithId("start-session"));

    ALICE.htmlBot().view(MAIN_VIEW).button("start-session").click();

    assertTrue(ALICE.htmlBot().view(SHARE_PROJECT_VIEW).isOpen());
    assertTrue(
        ALICE
            .htmlBot()
            .view(SHARE_PROJECT_VIEW)
            .textElement("header")
            .getText()
            .equals("Choose Files"));

    ALICE.htmlBot().view(SHARE_PROJECT_VIEW).button("next-button").click();
    assertTrue(
        ALICE
            .htmlBot()
            .view(SHARE_PROJECT_VIEW)
            .textElement("header")
            .getText()
            .equals("Choose Contacts"));
  }

  // TODO: fix the HTML StartSessionWizard to success this test
  @Test
  public void shouldStartSession() throws Exception {
    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS1) + ".*"));

    assertTrue("Main view did not load", ALICE.htmlBot().view(MAIN_VIEW).isOpen());

    ALICE.htmlBot().view(MAIN_VIEW).button("start-session").click();
    assertTrue(ALICE.htmlBot().view(SHARE_PROJECT_VIEW).isOpen());

    ALICE.htmlBot().view(SHARE_PROJECT_VIEW).tree("project-tree").check(Constants.PROJECT1);
    assertTrue(
        ALICE
            .htmlBot()
            .view(SHARE_PROJECT_VIEW)
            .tree("project-tree")
            .isChecked(Constants.PROJECT1));

    ALICE.htmlBot().view(SHARE_PROJECT_VIEW).tree("project-tree").uncheck(".classpath");
    assertFalse(
        ALICE
            .htmlBot()
            .view(SHARE_PROJECT_VIEW)
            .tree("project-tree")
            .isChecked(Constants.PROJECT1));

    ALICE.htmlBot().view(SHARE_PROJECT_VIEW).button("next-button").click();
    List<String> contactList = ALICE.htmlBot().getContactList(SHARE_PROJECT_VIEW);
    assertTrue(contactList.contains(BOB.getBaseJid()));

    ALICE.htmlBot().view(SHARE_PROJECT_VIEW).contactListItem(BOB.getBaseJid()).click();
    assertTrue(
        ALICE.htmlBot().view(SHARE_PROJECT_VIEW).button("next-button").text().equals("Finish"));
    ALICE.htmlBot().view(SHARE_PROJECT_VIEW).button("next-button").click();
    assertFalse(ALICE.htmlBot().view(SHARE_PROJECT_VIEW).isOpen());
  }
}
