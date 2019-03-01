package saros.stf.test.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.shared.Constants.ACTIVATE_ACCOUNT_DIALOG_TITLE;
import static saros.stf.shared.Constants.BUTTON_ACTIVATE_ACCOUNT;
import static saros.stf.shared.Constants.BUTTON_ADD_ACCOUNT;
import static saros.stf.shared.Constants.BUTTON_EDIT_ACCOUNT;
import static saros.stf.shared.Constants.BUTTON_REMOVE_ACCOUNT;
import static saros.stf.shared.Constants.GROUP_TITLE_XMPP_JABBER_ACCOUNTS;
import static saros.stf.shared.Constants.MENU_PREFERENCES;
import static saros.stf.shared.Constants.MENU_SAROS;
import static saros.stf.shared.Constants.NO;
import static saros.stf.shared.Constants.NODE_SAROS;
import static saros.stf.shared.Constants.OK;
import static saros.stf.shared.Constants.REMOVE_ACCOUNT_DIALOG_TITLE;
import static saros.stf.shared.Constants.SHELL_PREFERNCES;
import static saros.stf.shared.Constants.YES;

import java.util.Arrays;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.net.xmpp.JID;
import saros.stf.client.StfTestCase;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotShell;

public class AccountPreferenceTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  @Before
  public void runBeforeEveryTest() throws Exception {
    closeAllShells();
    resetDefaultAccount();
  }

  // @Test(expected = TimeoutException.class)
  // public void testCreateDuplicateAccount() throws Exception {
  // ALICE.superBot().menuBar().saros().preferences()
  // .addAccount(new JID("foo@bar.com"), "foobar");
  //
  // ALICE.superBot().menuBar().saros().preferences()
  // .addAccount(new JID("foo@bar.com"), "foobar1");
  // }

  @Test
  public void testActivateAccountButton() throws Exception {
    ALICE.superBot().menuBar().saros().preferences().addAccount(new JID("a@bar.com"), "a");

    ALICE.superBot().menuBar().saros().preferences().addAccount(new JID("b@bar.com"), "b");

    ALICE.remoteBot().activateWorkbench();

    openPreferencePage();

    IRemoteBotShell shell = getPreferencePageShell();

    // UNIX SELECTS THE FIRST ENTRY BY DEFAULT

    // assertDefaultStates(shell);

    shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).select(ALICE.getBaseJid());

    assertFalse(
        "activate account button must no be enabled when the active account is already selected",
        shell
            .bot()
            .buttonInGroup(BUTTON_ACTIVATE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .isEnabled());

    shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).select("a@bar.com");

    assertTrue(
        "activate account button must be enabled when a non active account is selected",
        shell
            .bot()
            .buttonInGroup(BUTTON_ACTIVATE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .isEnabled());

    shell.bot().buttonInGroup(BUTTON_ACTIVATE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS).click();

    shell = shell.bot().shell(ACTIVATE_ACCOUNT_DIALOG_TITLE);

    shell.activate();
    shell.bot().button(OK).click();
    shell.waitShortUntilIsClosed();

    shell = getPreferencePageShell();

    String[] selection = shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).selection();

    assertTrue(
        "list item must not be deselected after activation",
        selection != null && selection.length == 1 && selection[0].equals("a@bar.com"));

    assertFalse(
        "activate account button must no be enabled when the active account is already selected",
        shell
            .bot()
            .buttonInGroup(BUTTON_ACTIVATE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .isEnabled());
  }

  @Test
  public void testRemoveAccountButton() throws Exception {
    ALICE.superBot().menuBar().saros().preferences().addAccount(new JID("a@bar.com"), "a");

    ALICE.superBot().menuBar().saros().preferences().addAccount(new JID("b@bar.com"), "b");

    ALICE.remoteBot().activateWorkbench();

    openPreferencePage();

    IRemoteBotShell shell = getPreferencePageShell();

    // UNIX SELECTS THE FIRST ENTRY BY DEFAULT

    // assertDefaultStates(shell);

    shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).select(ALICE.getBaseJid());

    assertFalse(
        "remove account button must no be enabled when the active account is already selected",
        shell
            .bot()
            .buttonInGroup(BUTTON_REMOVE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .isEnabled());

    shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).select("a@bar.com");

    assertTrue(
        "remove account button must be enabled when a non active account is selected",
        shell
            .bot()
            .buttonInGroup(BUTTON_REMOVE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .isEnabled());

    shell.bot().buttonInGroup(BUTTON_REMOVE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS).click();

    shell = shell.bot().shell(REMOVE_ACCOUNT_DIALOG_TITLE);

    shell.activate();
    shell.bot().button(NO).click();
    shell.waitShortUntilIsClosed();

    shell = getPreferencePageShell();

    String[] selection = shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).selection();

    assertTrue(
        "list item must not be deselected after cancel the removal of the account",
        selection != null && selection.length == 1 && selection[0].equals("a@bar.com"));

    shell.bot().buttonInGroup(BUTTON_REMOVE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS).click();

    shell = shell.bot().shell(REMOVE_ACCOUNT_DIALOG_TITLE);

    shell.activate();
    shell.bot().button(YES).click();
    shell.waitShortUntilIsClosed();

    shell = getPreferencePageShell();

    assertDefaultStates(shell);

    assertFalse(
        "account 'a@bar.com' is still in list after removal",
        Arrays.asList(shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems())
            .contains("a@bar.com"));
  }

  private void assertDefaultStates(IRemoteBotShell shell) throws Exception {

    assertEquals(
        "item(s) were selected in the default state",
        0,
        shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).selectionCount());

    assertFalse(
        "activate account button must no be enabled without any item selected",
        shell
            .bot()
            .buttonInGroup(BUTTON_ACTIVATE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .isEnabled());

    assertFalse(
        "edit account must no be enabled without any item selected",
        shell
            .bot()
            .buttonInGroup(BUTTON_EDIT_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .isEnabled());

    assertFalse(
        "activate account button must no be enabled without any item selected",
        shell
            .bot()
            .buttonInGroup(REMOVE_ACCOUNT_DIALOG_TITLE, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .isEnabled());

    assertTrue(
        "add account button must be enabled all the time",
        shell
            .bot()
            .buttonInGroup(BUTTON_ADD_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .isEnabled());
  }

  private void openPreferencePage() throws Exception {
    ALICE.remoteBot().activateWorkbench();
    ALICE.remoteBot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();
  }

  private IRemoteBotShell getPreferencePageShell() throws Exception {
    IRemoteBotShell shell = ALICE.remoteBot().shell(SHELL_PREFERNCES);
    shell.activate();
    shell.bot().tree().expandNode(NODE_SAROS).select();
    return shell;
  }
}
