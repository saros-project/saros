package saros.stf.test.consistency;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;
import static saros.stf.shared.Constants.ACCEPT;
import static saros.stf.shared.Constants.SHELL_ADD_PROJECTS;
import static saros.stf.shared.Constants.SHELL_SESSION_INVITATION;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.test.stf.Constants;

public class ModifyDocumentBeforeProjectNegotiationTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        selectFirst(ALICE, BOB, CARL);
        restoreSessionIfNecessary("Foo1_Saros", ALICE, BOB);
    }

    @After
    public void restoreNetwork() throws Exception {

        tearDownSarosLast();
    }

    @Test
    public void testModifyDocumentBeforeProjectNegotiation() throws Exception {

        ALICE.superBot().internal().createJavaClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1);
        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("Foo1_Saros/src/my/pkg/MyClass.java");

        ALICE.superBot().views().sarosView().selectContact(CARL.getJID())
            .addToSarosSession();

        CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

        CARL.remoteBot().waitLongUntilShellIsOpen(SHELL_ADD_PROJECTS);

        // this test will fail if a jupiter proxy is added when bob is typing
        // text now
        BOB.superBot().views().packageExplorerView()
            .selectFile("Foo1_Saros", "src", "my", "pkg", "MyClass.java")
            .open();

        BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .typeText("The mighty Foobar");

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        CARL.superBot()
            .confirmShellAddProjectWithNewProject(Constants.PROJECT1);

        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("Foo1_Saros/src/my/pkg/MyClass.java");

        CARL.superBot().views().packageExplorerView()
            .selectFile("Foo1_Saros", "src", "my", "pkg", "MyClass.java")
            .open();

        BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .typeText(" bars everyfoo");

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(CARL.getJID(), 10000);

        CARL.remoteBot().editor(Constants.CLS1_SUFFIX)
            .typeText("\n\n\nFoo yourself ");

        CARL.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        ALICE.superBot().views().packageExplorerView()
            .selectFile("Foo1_Saros", "src", "my", "pkg", "MyClass.java")
            .open();

        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).navigateTo(0, 0);
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .typeText("blablablublub");

        ALICE.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(BOB.getJID(), 10000);

        ALICE.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(CARL.getJID(), 10000);

        String aliceText = ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .getText();
        String bobText = BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .getText();
        String carlText = CARL.remoteBot().editor(Constants.CLS1_SUFFIX)
            .getText();

        assertEquals(aliceText, bobText);
        assertEquals(aliceText, carlText);
    }
}
