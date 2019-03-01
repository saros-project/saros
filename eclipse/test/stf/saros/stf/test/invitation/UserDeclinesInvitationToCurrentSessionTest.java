package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.CANCEL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import org.junit.BeforeClass;
import org.junit.Test;

@TestLink(id = "Saros-133_user_declines_invitation_to_an_already_established_session")
public class UserDeclinesInvitationToCurrentSessionTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  public void testUserDeclinesInvitationToCurrentSession() throws Exception {
    Util.setUpSessionWithProjectAndFile("foo", "readme.txt", "1234/1234=1", ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo");

    ALICE.superBot().views().sarosView().selectContact(CARL.getJID()).addToSarosSession();

    IRemoteBotShell shell = CARL.remoteBot().shell(SHELL_SESSION_INVITATION);

    shell.bot().button(CANCEL).click();

    Thread.sleep(5000);

    assertTrue(
        "Alice closed the session because Carl cancels the invitation but Bob was in the session",
        ALICE.superBot().views().sarosView().isInSession());
  }
}
