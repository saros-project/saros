package saros.test.mocks;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Optional;
import org.easymock.EasyMock;
import org.powermock.api.easymock.PowerMock;
import saros.activities.SPath;
import saros.filesystem.IFile;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.session.ISarosSession;
import saros.session.User;

/**
 * This class provides several mock-creating methods for various purposes. It makes use of EasyMock
 * and PowerMock.
 */
public class SarosMocks {

  /**
   * Creates a mocked User object with the given JID
   *
   * @return a replayed EasyMock
   */
  public static User mockUser(JID jid) {
    User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.getJID()).andStubReturn(jid);
    EasyMock.replay(user);
    return user;
  }

  /**
   * Add the information of a user to the given mocked session.
   *
   * @param mockedSession The session to that the user will be known after this call. The session
   *     mock is <i>not</i> replayed.
   * @param user May be a (replayed) mock
   */
  public static void addUserToSession(ISarosSession mockedSession, User user) {
    EasyMock.expect(mockedSession.getUser(user.getJID())).andStubReturn(user);
  }

  /**
   * Creates a mocked SPath
   *
   * <p>The method {@link Object#toString()} is <i>not</i> mocked and may be called (e.g. for
   * logging/messages).
   *
   * @return a non-replayed PowerMock (must be replayed with {@link PowerMock#replay(Object...)}).
   */
  public static SPath prepareMockSPath() {
    return PowerMock.createPartialMockForAllMethodsExcept(SPath.class, "toString");
  }

  /**
   * Creates a mocked SPath
   *
   * @return a replayed PowerMock
   */
  public static SPath mockSPath() {
    SPath path = prepareMockSPath();
    PowerMock.replay(path);

    return path;
  }

  /**
   * Creates a mock <code>IFile</code> object that is backed by a mocked project and path object.
   *
   * <p>Allows to request the file from the project using the project relative path requested from
   * the file. This behavior is necessary in order to be able use the mock file in the constructor
   * {@link SPath#SPath(IResource)}.
   *
   * @return a mock <code>IFile</code> object that is backed by a mocked project and path object
   */
  public static IFile mockFile() {
    IProject projectMock = createNiceMock(IProject.class);
    IPath pathMock = createNiceMock(IPath.class);
    IFile fileMock = createNiceMock(IFile.class);

    expect(projectMock.getFile(pathMock)).andStubReturn(fileMock);

    expect(pathMock.isAbsolute()).andStubReturn(false);

    expect(fileMock.getProject()).andStubReturn(projectMock);
    expect(fileMock.getProjectRelativePath()).andStubReturn(pathMock);

    replay(projectMock, pathMock, fileMock);

    return fileMock;
  }

  /**
   * Create a mocked XMPPContactsService.
   *
   * @param jid
   * @return a mocked XMPPContactsService
   */
  public static XMPPContactsService contactsServiceMockFor(JID jid) {
    XMPPContact contactMock = createMock(XMPPContact.class);
    expect(contactMock.getSarosJid()).andStubReturn(Optional.of(jid));
    replay(contactMock);

    XMPPContactsService contactsServiceMock = createMock(XMPPContactsService.class);

    contactsServiceMock.addListener(anyObject());
    expect(contactsServiceMock.getContact(anyObject(String.class)))
        .andStubReturn(Optional.of(contactMock));
    replay(contactsServiceMock);

    return contactsServiceMock;
  }
}
