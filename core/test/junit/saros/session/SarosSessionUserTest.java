package saros.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import org.junit.After;
import org.junit.Before;

import org.junit.Test;
import saros.net.xmpp.JID;
import saros.session.User;
import saros.session.UserPrivilege;
import saros.session.UserPrivilege.Keys;

public class SarosSessionUserTest {

  private JID ALICE = new JID("alice@test/Saros");
  private JID BOB = new JID("bob@test/Saros");

  private User HOST = new User(ALICE, true, true, 0, 0);
  private User INVITEE = new User(BOB, true, true, 0, 0);

  protected void setUp() {
	  // System.out.println("1 - SarosSessionUserTest setUp()");
  }
  
  // testing default privileges, all should return false
  @Test
  public void testSessionUserReadOnlyPrivilegeDefault() {
	  setUp();
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_READONLY_ACCESS), false);
  }
  @Test
  public void testSessionUserWritePrivilegeDefault() {
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_WRITE_ACCESS), false);
  }
  @Test
  public void testSessionUserShareDocumentPrivilegeDefault() {
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_SHARE_DOCUMENT), false);
  }
  @Test
  public void testSessionUserInvitePrivilegeDefault() {
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_INVITE_USER), false);
  }
  @Test
  public void testSessionUserGrantPermissionPrivilegeDefault() {
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_GRANT_PERMISSION), false);
  }
  @Test
  public void testSessionUserJoinSessionPrivilegeDefault() {
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_JOIN), false);
  }
  @Test
  public void testSessionUserStartSessionPrivilegeDefault() {
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_START_SERVER), false);
  }
  @Test
  public void testSessionUserStopSessionPrivilegeDefault() {
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_STOP_SERVER), false);
  }
  @Test
  public void testSessionUserDeleteSessionDataPrivilegeDefault() {
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_DELETE_DATA), false);
  }
  @Test
  public void testSessionUserConfigureServerPrivilegeDefault() {
	  assertEquals(HOST.hasPrivilege(Keys.CONFIGURE_SERVER), false);
  }

  // testing to create every single privilege and to add it to the users privileges
  @Test
  public void testSessionUserReadOnlyPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(Keys.SESSION_READONLY_ACCESS, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_READONLY_ACCESS), true);
  }
  @Test
  public void testSessionUserWritePrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(Keys.SESSION_WRITE_ACCESS, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_WRITE_ACCESS), true);
  }
  @Test
  public void testSessionUserShareDocumentPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(Keys.SESSION_SHARE_DOCUMENT, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_SHARE_DOCUMENT), true);
  }
  @Test
  public void testSessionUserInvitePrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(Keys.SESSION_INVITE_USER, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_INVITE_USER), true);
  }
  @Test
  public void testSessionUserGrantPermissionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(Keys.SESSION_GRANT_PERMISSION, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_GRANT_PERMISSION), true);
  }
  @Test
  public void testSessionUserJoinSessionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(Keys.SESSION_JOIN, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_JOIN), true);
  }
  @Test
  public void testSessionUserStartSessionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(Keys.SESSION_START_SERVER, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_START_SERVER), true);
  }
  @Test
  public void testSessionUserStopSessionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(Keys.SESSION_STOP_SERVER, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_STOP_SERVER), true);
  }
  @Test
  public void testSessionUserDeleteSessionDataPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(Keys.SESSION_DELETE_DATA, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasPrivilege(Keys.SESSION_DELETE_DATA), true);
  }
  @Test
  public void testSessionUserConfigureServerPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(Keys.CONFIGURE_SERVER, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasPrivilege(Keys.CONFIGURE_SERVER), true);
  }
}