package de.fu_berlin.inf.dpp.misc.xstream;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

public class SPathConverterTest {

  private static class Dummy {
    private SPath path;

    public Dummy(SPath path) {
      this.path = path;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof Dummy)) return false;

      Dummy dummy = (Dummy) other;
      return (dummy.path.equals(this.path));
    }
  }

  private static IPath path;
  private static IProject project;
  private static IPathFactory pathFactory;

  @BeforeClass
  public static void prepare() {
    /* Mocks */
    path = EasyMock.createMock(IPath.class);
    expect(path.isAbsolute()).andStubReturn(false);
    expect(path.toPortableString()).andStubReturn("/foo/src/Main.java");

    pathFactory = EasyMock.createMock(IPathFactory.class);
    expect(pathFactory.fromPath(path)).andStubReturn("/foo/src/Main.java");
    expect(pathFactory.fromString("/foo/src/Main.java")).andStubReturn(path);

    project = EasyMock.createNiceMock(IProject.class);

    EasyMock.replay(pathFactory, project, path);
  }

  @Test
  public void conversionRunningSession() {
    /* Mocks */
    ISarosSession session = EasyMock.createMock(ISarosSession.class);
    expect(session.getProjectID(project)).andStubReturn("ABC");
    expect(session.getProject("ABC")).andStubReturn(project);

    EasyMock.replay(session);

    /* XStream */
    XStream sender = new XStream(new DomDriver());
    sender.registerConverter(new SPathConverter(session, pathFactory));

    XStream receiver = new XStream(new DomDriver());
    receiver.registerConverter(new SPathConverter(session, pathFactory));

    /* Test */
    SPath spath = new SPath(project, path);
    SPath copy = (SPath) receiver.fromXML(sender.toXML(spath));
    assertEquals(spath, copy);

    SPath copy2 = (SPath) receiver.fromXML(sender.toXML(spath));
    assertEquals(spath, copy2);
  }

  @Test
  public void conversionLeavingReceiver() {
    /* Mocks */
    ISarosSession senderSession = EasyMock.createMock(ISarosSession.class);
    expect(senderSession.getProjectID(project)).andStubReturn("ABC");
    expect(senderSession.getProject("ABC")).andStubReturn(project);

    ISarosSession receiverSession = EasyMock.createMock(ISarosSession.class);
    expect(receiverSession.getProjectID(project)).andReturn("ABC");
    expect(receiverSession.getProject("ABC")).andReturn(project);
    expect(receiverSession.getProjectID(project)).andReturn(null);
    expect(receiverSession.getProject("ABC")).andReturn(null);

    EasyMock.replay(senderSession, receiverSession);

    /* XStream */
    XStream sender = new XStream(new DomDriver());
    sender.registerConverter(new SPathConverter(senderSession, pathFactory));

    XStream receiver = new XStream(new DomDriver());
    receiver.registerConverter(new SPathConverter(receiverSession, pathFactory));

    /* Test */
    SPath spath = new SPath(project, path);

    // first call on running session on receiver side
    receiver.fromXML(sender.toXML(spath));

    // second call on non-functional session on receiver side
    SPath copy2 = (SPath) receiver.fromXML(sender.toXML(spath));
    assertEquals(null, copy2);
  }

  @Test
  public void conversionLeavingSender() {
    /* Mocks */
    ISarosSession senderSession = EasyMock.createMock(ISarosSession.class);
    expect(senderSession.getProjectID(project)).andReturn("ABC");
    expect(senderSession.getProject("ABC")).andReturn(project);
    expect(senderSession.getProjectID(project)).andReturn(null);
    expect(senderSession.getProject("ABC")).andReturn(null);

    ISarosSession receiverSession = EasyMock.createMock(ISarosSession.class);
    expect(receiverSession.getProjectID(project)).andStubReturn("ABC");
    expect(receiverSession.getProject("ABC")).andStubReturn(project);
    expect(receiverSession.getProject(EasyMock.isNull(String.class))).andStubReturn(null);

    EasyMock.replay(senderSession, receiverSession);

    /* XStream */
    XStream sender = new XStream(new DomDriver());
    sender.registerConverter(new SPathConverter(senderSession, pathFactory));

    XStream receiver = new XStream(new DomDriver());
    receiver.registerConverter(new SPathConverter(receiverSession, pathFactory));

    /* Test */
    Dummy dummy = new Dummy(new SPath(project, path));
    Dummy copy = (Dummy) receiver.fromXML(sender.toXML(dummy));
    assertEquals(dummy, copy);

    Dummy copy2 = (Dummy) receiver.fromXML(sender.toXML(dummy));
    assertEquals(null, copy2.path);
  }
}
