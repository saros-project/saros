package saros.stf.test.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.annotation.TestLink;
import saros.stf.client.StfTestCase;

@TestLink(id = "Saros-18_creating_new_files")
public class CreatingNewFileTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB, CARL);
        restoreSessionIfNecessary("Foo1_Saros", ALICE, BOB, CARL);
    }

    @After
    public void cleanUpSaros() throws Exception {

        ALICE.superBot().internal().deleteFolder("Foo1_Saros", "src");
        CARL.superBot().internal().deleteFolder("Foo1_Saros", "bar");
        tearDownSarosLast();

    }

    @Test
    public void testCreatingNewFileTest() throws Exception {

        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("Foo1_Saros");
        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("Foo1_Saros");

        ALICE.superBot().internal().createFile("Foo1_Saros", "src/readme.txt",
            "this is a test case");

        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("Foo1_Saros/src/readme.txt");
        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("Foo1_Saros/src/readme.txt");

        ALICE.superBot().views().sarosView().selectUser(CARL.getJID())
            .restrictToReadOnlyAccess();

        ALICE.superBot().views().sarosView().selectUser(CARL.getJID())
            .waitUntilHasReadOnlyAccess();

        // Lin's fault not mine !

        // BOB.superBot().views().sarosView().selectParticipant(ALICE.getJID()).waitUntilHasReadOnlyAccess();

        // CARL.superBot().views().sarosView().selectParticipant(CARL.getJID()).waitUntilHasReadOnlyAccess();

        ALICE.superBot().views().sarosView().selectUser(CARL.getJID())
            .followParticipant();
        BOB.superBot().views().sarosView().selectUser(ALICE.getJID())
            .followParticipant();

        assertTrue(ALICE.superBot().views().sarosView().isFollowing());
        assertTrue(BOB.superBot().views().sarosView().isFollowing());

        CARL.superBot().internal().createFile("Foo1_Saros", "bar/readme.txt",
            "not visible");

        CARL.superBot().views().packageExplorerView()
            .selectFile("Foo1_Saros", "bar", "readme.txt").open();

        CARL.remoteBot().editor("readme.txt").waitUntilIsActive();
        CARL.remoteBot().editor("readme.txt").typeText(
            "eene meene miste es rappelt in der kiste, eene meene meck und du bist weck ! weck bist du noch lange nicht ...");

        assertFalse("Carls editor must not be opened",
            ALICE.remoteBot().isEditorOpen("readme.txt"));
        assertFalse("Bobs editor must not be opened",
            BOB.remoteBot().isEditorOpen("readme.txt"));

        assertFalse("Alices created file must not be marked as shared (CARL)",
            ALICE.superBot().views().packageExplorerView()
                .isResourceShared("Foo1_Saros/bar/readme.txt"));

        assertFalse("Alices created file must not be marked as shared (BOB)",
            BOB.superBot().views().packageExplorerView()
                .isResourceShared("Foo1_Saros/bar/readme.txt"));

        assertEquals(
            "Alice had changed a file during read only access while typing",
            CARL.remoteBot().editor("readme.txt").getText(), "not visible");

        try {
            CARL.superBot().views().sarosView()
                .waitUntilIsInconsistencyDetected();
        } catch (TimeoutException e) {
            fail("ALICE should have received an inconsistency warning, "
                + e.getMessage());
        }
    }
}
