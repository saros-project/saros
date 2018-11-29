package de.fu_berlin.inf.dpp.stf.test.filefolderoperations;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

@TestLink(id = "Saros-43_folder_operations")
public class FolderOperationsTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testRenameFolder() throws Exception {

    ALICE.superBot().internal().createProject("foo");
    ALICE.superBot().internal().createFile("foo", "test/foo.txt", /*
                                                                       * 100
                                                                       * MByte
                                                                       */ 1024 * 1024 * 100, false);

    Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/test/foo.txt");

    ALICE.superBot().internal().createFolder("foo", "a/b/c");
    ALICE.superBot().internal().createFolder("foo", "a/c/a");

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/a/b/c");

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/a/c/a");

    /*
     * 5 * 100 = 0.5 GB (that should not be transfered)
     */

    long duration = 0;

    duration += moveAndMeasure("test", "a");

    BOB.superBot().views().packageExplorerView().waitUntilFileExists("foo", "a", "foo.txt");

    duration += moveAndMeasure("a", "a/b");

    BOB.superBot().views().packageExplorerView().waitUntilFileExists("foo", "a/b", "foo.txt");

    duration += moveAndMeasure("a/b", "a/b/c");

    BOB.superBot().views().packageExplorerView().waitUntilFileExists("foo", "a/b/c", "foo.txt");

    duration += moveAndMeasure("a/b/c", "a/c");

    BOB.superBot().views().packageExplorerView().waitUntilFileExists("foo", "a/c", "foo.txt");

    duration += moveAndMeasure("a/c", "a/c/a");

    BOB.superBot().views().packageExplorerView().waitUntilFileExists("foo", "a/c/a", "foo.txt");

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
        .selectFile("foo", source.toArray(new String[0]))
        .refactor()
        .moveTo("foo", to);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    return System.currentTimeMillis() - start;
  }
}
