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
	  assertEquals(HOST.hasReadOnlyAccessPrivilege(), false);
  }
  @Test
  public void testSessionUserWritePrivilegeDefault() {
	  assertEquals(HOST.hasWriteAccessPrivilege(), false);
  }
  @Test
  public void testSessionUserShareDocumentPrivilegeDefault() {
	  assertEquals(HOST.hasShareDocumentPrivilege(), false);
  }
  @Test
  public void testSessionUserInvitePrivilegeDefault() {
	  assertEquals(HOST.hasInvitePrivilege(), false);
  }
  @Test
  public void testSessionUserGrantPermissionPrivilegeDefault() {
	  assertEquals(HOST.hasGrantPermissionPrivilege(), false);
  }
  @Test
  public void testSessionUserJoinSessionPrivilegeDefault() {
	  assertEquals(HOST.hasJoinSessionPrivilege(), false);
  }
  @Test
  public void testSessionUserStartSessionPrivilegeDefault() {
	  assertEquals(HOST.hasStartSessionServerPrivilege(), false);
  }
  @Test
  public void testSessionUserStopSessionPrivilegeDefault() {
	  assertEquals(HOST.hasStopSessionServerPrivilege(), false);
  }
  @Test
  public void testSessionUserDeleteSessionDataPrivilegeDefault() {
	  assertEquals(HOST.hasDeleteSessionDataPrivilege(), false);
  }
  @Test
  public void testSessionUserConfigureServerPrivilegeDefault() {
	  assertEquals(HOST.hasConfigureServerPrivilege(), false);
  }
  
  // test add privilege
  @Test
  public void testSessionUserReadOnlyPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Privilege.READONLY_ACCESS, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasReadOnlyAccessPrivilege(), true);
  }
  @Test
  public void testSessionUserWritePrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Privilege.WRITE_ACCESS, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasWriteAccessPrivilege(), true);
  }
  @Test
  public void testSessionUserShareDocumentPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Privilege.SHARE_DOCUMENT, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasShareDocumentPrivilege(), true);
  }
  @Test
  public void testSessionUserInvitePrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Privilege.INVITE_USER, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasInvitePrivilege(), true);
  }
  @Test
  public void testSessionUserGrantPermissionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Privilege.GRANT_PERMISSION, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasGrantPermissionPrivilege(), true);
  }
  @Test
  public void testSessionUserJoinSessionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Privilege.JOIN_SESSION, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasJoinSessionPrivilege(), true);
  }
  @Test
  public void testSessionUserStartSessionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Privilege.START_SESSION_SERVER, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasStartSessionServerPrivilege(), true);
  }
  @Test
  public void testSessionUserStopSessionPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Privilege.STOP_SESSION_SERVER, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasStopSessionServerPrivilege(), true);
  }
  @Test
  public void testSessionUserDeleteSessionDataPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Privilege.DELETE_SESSION_DATA, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasDeleteSessionDataPrivilege(), true);
  }
  @Test
  public void testSessionUserConfigureServerPrivilegeAdd() {
	  UserPrivilege priv = new UserPrivilege(UserPrivilege.Privilege.CONFIGURE_SERVER, true);
	  HOST.addPrivilege(priv);
	  assertEquals(HOST.hasConfigureServerPrivilege(), true);
  }
}
