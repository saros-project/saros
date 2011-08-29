package de.fu_berlin.inf.dpp.stf.test.filefolderoperations;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

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
                                                                       */
        1024 * 1024 * 100, false);

        Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT,
            ALICE, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/test/foo.txt");

        ALICE.superBot().internal().createFolder("foo", "a/b/c");
        ALICE.superBot().internal().createFolder("foo", "a/c/a");

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/a/b/c");
        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/a/c/a");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "test", "foo.txt").refactor().moveTo("foo", "a");
        BOB.superBot().views().packageExplorerView()
            .waitUntilFileExists("foo", "a", "foo.txt");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "a", "foo.txt").refactor().moveTo("foo", "a/b");
        BOB.superBot().views().packageExplorerView()
            .waitUntilFileExists("foo", "a/b", "foo.txt");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "a", "b", "foo.txt").refactor()
            .moveTo("foo", "a/b/c");
        BOB.superBot().views().packageExplorerView()
            .waitUntilFileExists("foo", "a/b/c", "foo.txt");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "a", "b", "c", "foo.txt").refactor()
            .moveTo("foo", "a/c");
        BOB.superBot().views().packageExplorerView()
            .waitUntilFileExists("foo", "a/c", "foo.txt");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "a", "c", "foo.txt").refactor()
            .moveTo("foo", "a/c/a");
        BOB.superBot().views().packageExplorerView()
            .waitUntilFileExists("foo", "a/c/a", "foo.txt");

    }
}
