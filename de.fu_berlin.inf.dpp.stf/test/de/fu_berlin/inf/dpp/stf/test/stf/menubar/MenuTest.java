package de.fu_berlin.inf.dpp.stf.test.stf.menubar;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.test.stf.Constants;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class MenuTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  @After
  public void afterEveryTest() throws Exception {
    clearWorkspaces();
  }

  @Test
  public void testDeleteProject() throws Exception {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);

    Thread.sleep(1000);

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .existsWithRegex(Pattern.quote(Constants.PROJECT1) + ".*"));

    ALICE.superBot().views().packageExplorerView().selectJavaProject(Constants.PROJECT1).delete();

    Thread.sleep(1000);

    assertFalse(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .existsWithRegex(Pattern.quote(Constants.PROJECT1) + ".*"));
  }

  @Test
  public void testDeleteFile() throws Exception {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);

    Thread.sleep(1000);

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS1 + SUFFIX_JAVA) + ".*"));

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .delete();

    Thread.sleep(1000);

    assertFalse(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Pattern.quote(Constants.CLS1 + SUFFIX_JAVA) + ".*"));
  }

  @Test
  public void testCopyProject() throws Exception {
    ALICE.superBot().views().packageExplorerView().tree().newC().project(Constants.PROJECT1);

    Thread.sleep(1000);

    assertFalse(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .existsWithRegex(Pattern.quote(Constants.PROJECT2) + ".*"));

    ALICE.superBot().views().packageExplorerView().selectProject(Constants.PROJECT1).copy();

    ALICE.superBot().views().packageExplorerView().tree().paste(Constants.PROJECT2);

    Thread.sleep(1000);

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .existsWithRegex(Pattern.quote(Constants.PROJECT2) + ".*"));
  }

  @Test
  public void testDeleteFolder() throws Exception {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectJavaProject(Constants.PROJECT1)
        .newC()
        .folder(Constants.FOLDER1);

    Thread.sleep(1000);

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Pattern.quote(Constants.FOLDER1) + ".*"));

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
        .delete();

    Thread.sleep(1000);

    assertFalse(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Pattern.quote(Constants.FOLDER1) + ".*"));
  }
}
