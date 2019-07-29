package saros.misc.xstream;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.activities.SPath;
import saros.filesystem.IPath;
import saros.filesystem.IPathFactory;
import saros.filesystem.IProject;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IReferencePointManager;
import saros.session.ISarosSession;

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
  private static IReferencePoint referencePoint;
  private static IReferencePointManager referencePointManager;
  private static IPathFactory pathFactory;

  @BeforeClass
  public static void prepare() {
    /* Mocks */
    referencePoint = EasyMock.createMock(IReferencePoint.class);

    path = EasyMock.createMock(IPath.class);
    expect(path.isAbsolute()).andStubReturn(false);
    expect(path.toPortableString()).andStubReturn("/foo/src/Main.java");

    pathFactory = EasyMock.createMock(IPathFactory.class);
    expect(pathFactory.fromPath(path)).andStubReturn("/foo/src/Main.java");
    expect(pathFactory.fromString("/foo/src/Main.java")).andStubReturn(path);

    project = EasyMock.createNiceMock(IProject.class);
    EasyMock.expect(project.getReferencePoint()).andStubReturn(referencePoint);
    EasyMock.replay(pathFactory, project, path);

    referencePointManager = EasyMock.createMock(IReferencePointManager.class);
    EasyMock.expect(referencePointManager.getProject(referencePoint)).andStubReturn(project);
    expect(referencePointManager.createSPath(referencePoint, path))
        .andStubReturn(new SPath(project, path));
    EasyMock.replay(referencePointManager);
  }

  @Test
  public void conversionRunningSession() {
    /* Mocks */
    ISarosSession session = EasyMock.createMock(ISarosSession.class);
    expect(session.getReferencePointID(referencePoint)).andStubReturn("ABC");
    expect(session.getReferencePoint("ABC")).andStubReturn(referencePoint);
    expect(session.getComponent(IReferencePointManager.class)).andStubReturn(referencePointManager);
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
    expect(senderSession.getReferencePointID(referencePoint)).andStubReturn("ABC");
    expect(senderSession.getReferencePoint("ABC")).andStubReturn(referencePoint);
    expect(senderSession.getComponent(IReferencePointManager.class))
        .andStubReturn(referencePointManager);

    ISarosSession receiverSession = EasyMock.createMock(ISarosSession.class);
    expect(receiverSession.getReferencePointID(referencePoint)).andReturn("ABC");
    expect(receiverSession.getReferencePoint("ABC")).andReturn(referencePoint);
    expect(receiverSession.getReferencePointID(referencePoint)).andReturn(null);
    expect(receiverSession.getReferencePoint("ABC")).andReturn(null);
    expect(receiverSession.getComponent(IReferencePointManager.class))
        .andStubReturn(referencePointManager);

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
    expect(senderSession.getReferencePointID(referencePoint)).andReturn("ABC");
    expect(senderSession.getReferencePoint("ABC")).andReturn(referencePoint);
    expect(senderSession.getReferencePointID(referencePoint)).andReturn(null);
    expect(senderSession.getReferencePoint("ABC")).andReturn(null);
    expect(senderSession.getComponent(IReferencePointManager.class))
        .andStubReturn(referencePointManager);

    ISarosSession receiverSession = EasyMock.createMock(ISarosSession.class);
    expect(receiverSession.getReferencePointID(referencePoint)).andStubReturn("ABC");
    expect(receiverSession.getReferencePoint("ABC")).andStubReturn(referencePoint);
    expect(receiverSession.getReferencePoint(EasyMock.isNull(String.class))).andStubReturn(null);
    expect(receiverSession.getComponent(IReferencePointManager.class))
        .andStubReturn(referencePointManager);

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
