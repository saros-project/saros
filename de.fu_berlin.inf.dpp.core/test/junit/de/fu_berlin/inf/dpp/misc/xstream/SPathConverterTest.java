package de.fu_berlin.inf.dpp.misc.xstream;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IFolder_V2;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

public class SPathConverterTest {

  private static IPath path;
  private static IFolder_V2 project;
  private static IReferencePoint referencePoint;
  private static IPathFactory pathFactory;
  private static IReferencePointManager referencePointManager;

  @BeforeClass
  public static void prepare() {
    /* Mocks */
    path = EasyMock.createMock(IPath.class);
    expect(path.isAbsolute()).andStubReturn(false);
    expect(path.toPortableString()).andStubReturn("/foo/src/Main.java");

    pathFactory = EasyMock.createMock(IPathFactory.class);
    expect(pathFactory.fromPath(path)).andStubReturn("/foo/src/Main.java");
    expect(pathFactory.fromString("/foo/src/Main.java")).andStubReturn(path);

    referencePoint = EasyMock.createNiceMock(IReferencePoint.class);
    project = EasyMock.createNiceMock(IFolder_V2.class);
    expect(project.getReferencePoint()).andStubReturn(referencePoint);
    referencePointManager = EasyMock.createNiceMock(IReferencePointManager.class);
    expect(referencePointManager.get(referencePoint)).andStubReturn(project);
    EasyMock.replay(pathFactory, referencePoint, path, project, referencePointManager);
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
    SPath spath = new SPath(referencePoint, path);
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

    ISarosSession receiverSession = EasyMock.createMock(ISarosSession.class);
    expect(receiverSession.getReferencePointID(referencePoint)).andReturn("ABC");
    expect(receiverSession.getReferencePoint("ABC")).andReturn(referencePoint);
    expect(receiverSession.getReferencePointID(referencePoint)).andReturn(null);
    expect(receiverSession.getReferencePoint("ABC")).andReturn(null);

    expect(senderSession.getComponent(IReferencePointManager.class))
        .andStubReturn(referencePointManager);

    expect(receiverSession.getComponent(IReferencePointManager.class))
        .andStubReturn(referencePointManager);

    EasyMock.replay(senderSession, receiverSession);

    /* XStream */
    XStream sender = new XStream(new DomDriver());
    sender.registerConverter(new SPathConverter(senderSession, pathFactory));

    XStream receiver = new XStream(new DomDriver());
    receiver.registerConverter(new SPathConverter(receiverSession, pathFactory));

    /* Test */
    SPath spath = new SPath(referencePoint, path);

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

    ISarosSession receiverSession = EasyMock.createMock(ISarosSession.class);
    expect(receiverSession.getReferencePointID(referencePoint)).andStubReturn("ABC");
    expect(receiverSession.getReferencePoint("ABC")).andStubReturn(referencePoint);
    expect(receiverSession.getReferencePoint(EasyMock.isNull(String.class))).andStubReturn(null);

    expect(senderSession.getComponent(IReferencePointManager.class))
        .andStubReturn(referencePointManager);

    expect(receiverSession.getComponent(IReferencePointManager.class))
        .andStubReturn(referencePointManager);

    EasyMock.replay(senderSession, receiverSession);

    /* XStream */
    XStream sender = new XStream(new DomDriver());
    sender.registerConverter(new SPathConverter(senderSession, pathFactory));

    XStream receiver = new XStream(new DomDriver());
    receiver.registerConverter(new SPathConverter(receiverSession, pathFactory));

    /* Test */
    Dummy dummy = new Dummy(new SPath(referencePoint, path));
    Dummy copy = (Dummy) receiver.fromXML(sender.toXML(dummy));
    assertEquals(dummy, copy);

    Dummy copy2 = (Dummy) receiver.fromXML(sender.toXML(dummy));
    assertEquals(null, copy2.path);
  }

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
}
