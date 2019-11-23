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
// System.out.println("2 - testSessionUserReadOnlyPrivilegeDefault()" + HOST.hasReadOnlyAccessPrivilege());
	  assertEquals(HOST.hasReadOnlyAccessPrivilege(), new Boolean(false));
  }
  @Test
  public void testSessionUserWritePrivilegeDefault() {
	  assertEquals(HOST.hasWriteAccessPrivilege(), new Boolean(false));
  }
  @Test
  public void testSessionUserShareDocumentPrivilegeDefault() {
	  assertEquals(HOST.hasShareDocumentPrivilege(), new Boolean(false));
  }
  @Test
  public void testSessionUserInvitePrivilegeDefault() {
	  assertEquals(HOST.hasInvitePrivilege(), new Boolean(false));
  }
  @Test
  public void testSessionUserGrantPermissionPrivilegeDefault() {
	  assertEquals(HOST.hasGrantPermissionPrivilege(), new Boolean(false));
  }
  @Test
  public void testSessionUserJoinSessionPrivilegeDefault() {
	  assertEquals(HOST.hasJoinSessionPrivilege(), new Boolean(false));
  }
  @Test
  public void testSessionUserStartSessionPrivilegeDefault() {
	  assertEquals(HOST.hasStartSessionServerPrivilege(), new Boolean(false));
  }
  @Test
  public void testSessionUserStopSessionPrivilegeDefault() {
	  assertEquals(HOST.hasStopSessionServerPrivilege(), new Boolean(false));
  }
  @Test
  public void testSessionUserDeleteSessionDataPrivilegeDefault() {
	  assertEquals(HOST.hasDeleteSessionDataPrivilege(), new Boolean(false));
  }
  @Test
  public void testSessionUserConfigureServerPrivilegeDefault() {
	  assertEquals(HOST.hasConfigureServerPrivilege(), new Boolean(false));
  }

  // test add privilege
  @Test
  public void testSessionUserReadOnlyPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_READONLY_ACCESS, new Boolean(true));
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasReadOnlyAccessPrivilege(), new Boolean(true));
  }
  @Test
  public void testSessionUserWritePrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_WRITE_ACCESS, new Boolean(true));
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasWriteAccessPrivilege(), new Boolean(true));
  }
  @Test
  public void testSessionUserShareDocumentPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_SHARE_DOCUMENT, new Boolean(true));
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasShareDocumentPrivilege(), new Boolean(true));
  }
  @Test
  public void testSessionUserInvitePrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_INVITE_USER, new Boolean(true));
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasInvitePrivilege(), new Boolean(true));
  }
  @Test
  public void testSessionUserGrantPermissionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_GRANT_PERMISSION, new Boolean(true));
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasGrantPermissionPrivilege(), new Boolean(true));
  }
  @Test
  public void testSessionUserJoinSessionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_JOIN, new Boolean(true));
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasJoinSessionPrivilege(), new Boolean(true));
  }
  @Test
  public void testSessionUserStartSessionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_START_SERVER, new Boolean(true));
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasStartSessionServerPrivilege(), new Boolean(true));
  }
  @Test
  public void testSessionUserStopSessionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_STOP_SERVER, new Boolean(true));
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasStopSessionServerPrivilege(), new Boolean(true));
  }
  @Test
  public void testSessionUserDeleteSessionDataPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_DELETE_DATA, new Boolean(true));
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasDeleteSessionDataPrivilege(), new Boolean(true));
  }
  @Test
  public void testSessionUserConfigureServerPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.CONFIGURE_SERVER, new Boolean(true));
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasConfigureServerPrivilege(), new Boolean(true));
  }
}
