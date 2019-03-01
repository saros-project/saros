package saros.stf.test.stf.view.explorer;

import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.shared.Constants.TypeOfCreateProject;

public class PackageExplorerViewDecoratorTest extends StfTestCase {

  private static final String PROJECT = "foo";
  private static final String FOLDER = "bar";
  private static final String FILE_1 = "test_1.txt";
  private static final String FILE_2 = "test_2.txt";

  private static final String JAVA_PROJECT = "java";
  private static final String JAVA_PACKAGE = "de.alice";
  private static final String JAVA_CLASS = "HelloWorld";
  private static final String JAVA_CLASS_2 = "HelloHell";

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Before
  public void beforeEveryTest() throws Exception {
    closeAllEditors();
    clearWorkspaces();
  }

  @After
  public void afterEveryTest() throws Exception {
    if (ALICE.superBot().views().sarosView().isInSession()) leaveSessionPeersFirst(ALICE);
  }

  @Test
  public void testPackageExplorerViewMethodsWithoutARunningSession() throws Exception {
    ALICE.superBot().internal().createProject(PROJECT);
    ALICE.superBot().internal().createFile(PROJECT, FOLDER + "/" + FILE_1, "");

    ALICE.superBot().views().packageExplorerView().selectProject(PROJECT);

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectFolder(PROJECT, FOLDER)
            .exists(FILE_1));

    ALICE.superBot().views().packageExplorerView().selectFile(PROJECT, FOLDER, FILE_1).open();

    ALICE.remoteBot().editor(FILE_1).waitUntilIsActive();

    ALICE.superBot().internal().createJavaProject(JAVA_PROJECT);
    ALICE.superBot().internal().createJavaClass(JAVA_PROJECT, JAVA_PACKAGE, JAVA_CLASS);

    ALICE.superBot().views().packageExplorerView().selectJavaProject(JAVA_PROJECT);

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectSrc(JAVA_PROJECT)
            .exists(JAVA_PACKAGE));

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectPkg(JAVA_PROJECT, JAVA_PACKAGE)
            .exists(JAVA_CLASS + ".java"));

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(JAVA_PROJECT, JAVA_PACKAGE, JAVA_CLASS)
        .open();

    ALICE.remoteBot().editor(JAVA_CLASS + ".java").waitUntilIsActive();
  }

  @Test
  public void testPackageExplorerViewMethodsWithFullSharedProject() throws Exception {
    Util.setUpSessionWithProjectAndFile(PROJECT, FOLDER + "/" + FILE_1, "", ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared(PROJECT);

    ALICE.superBot().views().packageExplorerView().selectProject(PROJECT);

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectFolder(PROJECT, FOLDER)
            .exists(FILE_1));

    ALICE.superBot().views().packageExplorerView().selectFile(PROJECT, FOLDER, FILE_1).open();

    ALICE.remoteBot().editor(FILE_1).waitUntilIsActive();

    ALICE.superBot().internal().createJavaProject(JAVA_PROJECT);
    ALICE.superBot().internal().createJavaClass(JAVA_PROJECT, JAVA_PACKAGE, JAVA_CLASS);

    Util.addProjectToSessionSequentially(JAVA_PROJECT, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared(JAVA_PROJECT);

    ALICE.superBot().views().packageExplorerView().selectJavaProject(JAVA_PROJECT);

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectSrc(JAVA_PROJECT)
            .exists(JAVA_PACKAGE));

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectPkg(JAVA_PROJECT, JAVA_PACKAGE)
            .exists(JAVA_CLASS + ".java"));

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(JAVA_PROJECT, JAVA_PACKAGE, JAVA_CLASS)
        .open();

    ALICE.remoteBot().editor(JAVA_CLASS + ".java").waitUntilIsActive();
  }

  @Test
  public void testPackageExplorerViewMethodsWithPartialSharedProject() throws Exception {

    ALICE.superBot().internal().createProject(PROJECT);
    ALICE.superBot().internal().createFile(PROJECT, FOLDER + "/" + FILE_1, "");

    ALICE.superBot().internal().createFile(PROJECT, FOLDER + "/" + FILE_2, "");

    Util.buildFileSessionConcurrently(
        PROJECT, new String[] {FOLDER + "/" + FILE_1}, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared(PROJECT);

    ALICE.superBot().views().packageExplorerView().selectProject(PROJECT);

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectFolder(PROJECT, FOLDER)
            .exists(FILE_1));

    ALICE.superBot().views().packageExplorerView().selectFile(PROJECT, FOLDER, FILE_1).open();

    ALICE.remoteBot().editor(FILE_1).waitUntilIsActive();
  }

  @Test
  public void testPackageExplorerViewMethodsWithPartialSharedJavaProject() throws Exception {

    ALICE.superBot().internal().createJavaProject(JAVA_PROJECT);
    ALICE.superBot().internal().createJavaClass(JAVA_PROJECT, JAVA_PACKAGE, JAVA_CLASS);

    ALICE.superBot().internal().createJavaClass(JAVA_PROJECT, JAVA_PACKAGE, JAVA_CLASS_2);

    Util.buildFileSessionConcurrently(
        JAVA_PROJECT,
        new String[] {"src/" + JAVA_PACKAGE.replace('.', '/') + "/" + JAVA_CLASS + ".java"},
        TypeOfCreateProject.NEW_PROJECT,
        ALICE,
        BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared(JAVA_PROJECT);

    ALICE.superBot().views().packageExplorerView().selectJavaProject(JAVA_PROJECT);

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectSrc(JAVA_PROJECT)
            .exists(JAVA_PACKAGE));

    assertTrue(
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectPkg(JAVA_PROJECT, JAVA_PACKAGE)
            .exists(JAVA_CLASS + ".java"));

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(JAVA_PROJECT, JAVA_PACKAGE, JAVA_CLASS)
        .open();

    ALICE.remoteBot().editor(JAVA_CLASS + ".java").waitUntilIsActive();
  }
}
