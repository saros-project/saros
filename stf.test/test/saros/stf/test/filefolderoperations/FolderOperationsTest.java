package saros.stf.test.filefolderoperations;

import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.annotation.TestLink;
import saros.stf.client.StfTestCase;

@TestLink(id = "Saros-43_folder_operations")
public class FolderOperationsTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
    restoreSessionIfNecessary("Foo1_Saros", ALICE, BOB);
  }

  @Before
  public void setUp() throws Exception {
    closeAllShells();
    closeAllEditors();
  }

  @After
  public void cleanUpSaros() throws Exception {
    if (checkIfTestRunInTestSuite()) {
      ALICE.superBot().internal().deleteFolder("Foo1_Saros", "src");
      tearDownSaros();
    } else {
      tearDownSarosLast();
    }
  }

  @Test
  public void testRenameFolder() throws Exception {

    ALICE.superBot().internal().createFile("Foo1_Saros", "src/test/foo.txt", "");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared("Foo1_Saros/src/test/foo.txt");

    ALICE.superBot().internal().createFolder("Foo1_Saros", "src/a/b/c");
    ALICE.superBot().internal().createFolder("Foo1_Saros", "src/a/c/a");

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("Foo1_Saros/src/a/b/c");

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("Foo1_Saros/src/a/c/a");

    /*
     * 5 * 100 = 0.5 GB (that should not be transfered)
     */

    long duration = 0;

    duration += moveAndMeasure("src/test", "src/a");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilFileExists("Foo1_Saros", "src", "a", "foo.txt");

    duration += moveAndMeasure("src/a", "src/a/b");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilFileExists("Foo1_Saros", "src", "a/b", "foo.txt");

    duration += moveAndMeasure("src/a/b", "src/a/b/c");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilFileExists("Foo1_Saros", "src", "a/b/c", "foo.txt");

    duration += moveAndMeasure("src/a/b/c", "src/a/c");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilFileExists("Foo1_Saros", "src", "a/c", "foo.txt");

    duration += moveAndMeasure("src/a/c", "src/a/c/a");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilFileExists("Foo1_Saros", "src", "a/c/a", "foo.txt");

    assertTrue("file was transmitted on every move", duration < 10000);
  }

  private long moveAndMeasure(String from, String to) throws Exception {
    long start = System.currentTimeMillis();

    List<String> source = new ArrayList<String>(Arrays.asList(from.split("/")));

    source.add("foo.txt");

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFile("Foo1_Saros", source.toArray(new String[0]))
        .refactor()
        .moveTo("Foo1_Saros", to);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    return System.currentTimeMillis() - start;
  }
}
