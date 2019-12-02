package saros.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
  private User HOST = new User(ALICE, true, true, null);

 // testing default privileges, all should return false

  @Test
  public void testSessionUserReadOnlyPrivilegeDefault() {
    assertEquals(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_READONLY_ACCESS), false);
  }
  @Test
  public void testSessionUserWritePrivilegeDefault() {
    assertEquals(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_WRITE_ACCESS), false);
  }
  @Test
  public void testSessionUserShareDocumentPrivilegeDefault() {
    assertEquals(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_SHARE_DOCUMENT), false);
  }
  @Test
  public void testSessionUserInvitePrivilegeDefault() {
    assertEquals(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_INVITE_USER), false);
  }
  @Test
  public void testSessionUserGrantPermissionPrivilegeDefault() {
    assertEquals(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_GRANT_PERMISSION), false);
  }
  @Test
  public void testSessionUserJoinSessionPrivilegeDefault() {
    assertEquals(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_JOIN), false);
  }
  @Test
  public void testSessionUserStartSessionPrivilegeDefault() {
    assertEquals(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_START_SERVER), false);
  }
  @Test
  public void testSessionUserStopSessionPrivilegeDefault() {
    assertEquals(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_STOP_SERVER), false);
  }
  @Test
  public void testSessionUserDeleteSessionDataPrivilegeDefault() {
    assertEquals(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_DELETE_DATA), false);
  }
  @Test
  public void testSessionUserConfigureServerPrivilegeDefault() {
    assertEquals(HOST.hasPrivilege(UserPrivilege.Keys.CONFIGURE_SERVER), false);
  }

  // test add privilege
  @Test
  public void testSessionUserReadOnlyPrivilegeAdd() {
    UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_READONLY_ACCESS, true);
    HOST.addPrivilege(priv);
    assertTrue(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_READONLY_ACCESS));
  }
  @Test
  public void testSessionUserWritePrivilegeAdd() {
    UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_WRITE_ACCESS, true);
    HOST.addPrivilege(priv);
    assertTrue(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_WRITE_ACCESS));
  }
  @Test
  public void testSessionUserShareDocumentPrivilegeAdd() {
    UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_SHARE_DOCUMENT, true);
    HOST.addPrivilege(priv);
    assertTrue(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_SHARE_DOCUMENT));
  }
  @Test
  public void testSessionUserInvitePrivilegeAdd() {
    UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_INVITE_USER, true);
    HOST.addPrivilege(priv);
    assertTrue(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_INVITE_USER));
  }
  @Test
  public void testSessionUserGrantPermissionPrivilegeAdd() {
    UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_GRANT_PERMISSION, true);
    HOST.addPrivilege(priv);
    assertTrue(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_GRANT_PERMISSION));
  }
  @Test
  public void testSessionUserJoinSessionPrivilegeAdd() {
    UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_JOIN, true);
    HOST.addPrivilege(priv);
    assertTrue(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_JOIN));
  }
  @Test
  public void testSessionUserStartSessionPrivilegeAdd() {
    UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_START_SERVER, true);
    HOST.addPrivilege(priv);
    assertTrue(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_START_SERVER));
  }
  @Test
  public void testSessionUserStopSessionPrivilegeAdd() {
    UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_STOP_SERVER, true);
    HOST.addPrivilege(priv);
    assertTrue(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_STOP_SERVER));
  }
  @Test
  public void testSessionUserDeleteSessionDataPrivilegeAdd() {
    UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.SESSION_DELETE_DATA, true);
    HOST.addPrivilege(priv);
    assertTrue(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_DELETE_DATA));
  }
  @Test
  public void testSessionUserConfigureServerPrivilegeAdd() {
    UserPrivilege priv = new UserPrivilege(UserPrivilege.Keys.CONFIGURE_SERVER, true);
    HOST.addPrivilege(priv);
    assertTrue(HOST.hasPrivilege(UserPrivilege.Keys.CONFIGURE_SERVER));
  }

  // test set privilege
  @Test
  public void testSessionUserReadOnlyPrivilegeRemove() {
    HOST.setPrivilege(UserPrivilege.Keys.SESSION_READONLY_ACCESS, false);
    assertFalse(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_READONLY_ACCESS));
  }
@Test
public void testSessionUserWritePrivilegeRemove() {
  HOST.setPrivilege(UserPrivilege.Keys.SESSION_WRITE_ACCESS, false);
  assertFalse(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_WRITE_ACCESS));
}
@Test
public void testSessionUserShareDocumentPrivilegeRemove() {
  HOST.setPrivilege(UserPrivilege.Keys.SESSION_SHARE_DOCUMENT, false);
  assertFalse(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_SHARE_DOCUMENT));
}
@Test
public void testSessionUserInvitePrivilegeRemove() {
  HOST.setPrivilege(UserPrivilege.Keys.SESSION_INVITE_USER, false);
  assertFalse(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_INVITE_USER));
}
@Test
public void testSessionUserGrantPermissionPrivilegeRemove() {
  HOST.setPrivilege(UserPrivilege.Keys.SESSION_GRANT_PERMISSION, false);
  assertFalse(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_GRANT_PERMISSION));
}
@Test
public void testSessionUserJoinSessionPrivilegeRemove() {
  HOST.setPrivilege(UserPrivilege.Keys.SESSION_JOIN, false);
  assertFalse(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_JOIN));
}
@Test
public void testSessionUserStartSessionPrivilegeRemove() {
  HOST.setPrivilege(UserPrivilege.Keys.SESSION_START_SERVER, false);
  assertFalse(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_START_SERVER));
}
@Test
public void testSessionUserStopSessionPrivilegeRemove() {
  HOST.setPrivilege(UserPrivilege.Keys.SESSION_STOP_SERVER, false);
  assertFalse(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_STOP_SERVER));
}
@Test
public void testSessionUserDeleteSessionDataPrivilegeRemove() {
  HOST.setPrivilege(UserPrivilege.Keys.SESSION_DELETE_DATA, false);
  assertFalse(HOST.hasPrivilege(UserPrivilege.Keys.SESSION_DELETE_DATA));
}
@Test
public void testSessionUserConfigureServerPrivilegeRemove() {
  HOST.setPrivilege(UserPrivilege.Keys.CONFIGURE_SERVER, false);
  assertFalse(HOST.hasPrivilege(UserPrivilege.Keys.CONFIGURE_SERVER));
}
}
