package saros.stf.test.consistency;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;
import static saros.stf.shared.Constants.ACCEPT;
import static saros.stf.shared.Constants.SHELL_SESSION_INVITATION;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.annotation.TestLink;
import saros.stf.client.StfTestCase;
import saros.stf.test.stf.Constants;
import saros.test.util.EclipseTestThread;

@TestLink(id = "Saros-36_edit_during_invitation")
public class EditDuringInvitationTest extends StfTestCase {

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
    public void testEditDuringInvitation() throws Exception {

        ALICE.superBot().internal().createJavaClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1);
        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("Foo1_Saros/src/my/pkg/MyClass.java");

        ALICE.superBot().views().sarosView().selectContact(CARL.getJID())
            .addToSarosSession();

        BOB.superBot().views().packageExplorerView()
            .selectFile("Foo1_Saros", "src", "my", "pkg", "MyClass.java")
            .open();
        CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

        EclipseTestThread bobIsWriting = createTestThread(
            new EclipseTestThread.Runnable() {

                @Override
                public void run() throws Exception {
                    for (int i = 0; i < 20; i++)
                        BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
                            .typeText("FooBar");
                }
            });

        bobIsWriting.start();

        CARL.superBot()
            .confirmShellAddProjectWithNewProject(Constants.PROJECT1);

        bobIsWriting.join();
        bobIsWriting.verify();

        String textByBob = BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .getText();
        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("Foo1_Saros/src/my/pkg/MyClass.java");

        CARL.superBot().views().packageExplorerView()
            .selectFile("Foo1_Saros", "src", "my", "pkg", "MyClass.java")
            .open();

        ALICE.superBot().views().packageExplorerView()
            .selectFile("Foo1_Saros", "src", "my", "pkg", "MyClass.java")
            .open();

        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(textByBob);

        String textByAlice = ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .getText();

        // There are bugs here, CARL get completely different content as BOB.
        try {
            CARL.remoteBot().editor(Constants.CLS1_SUFFIX)
                .waitUntilIsTextSame(textByBob);
        } catch (TimeoutException e) {
            //
        }
        String textByCarl = CARL.remoteBot().editor(Constants.CLS1_SUFFIX)
            .getText();

        assertEquals(textByBob, textByAlice);
        assertEquals(textByBob, textByCarl);
    }
}
