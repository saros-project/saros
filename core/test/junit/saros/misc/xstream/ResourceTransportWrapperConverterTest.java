package saros.misc.xstream;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.activities.ResourceTransportWrapper;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IPath;
import saros.filesystem.IPathFactory;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.filesystem.IResource.Type;
import saros.session.ISarosSession;

public class ResourceTransportWrapperConverterTest {

  private static class Dummy {
    private ResourceTransportWrapper<IResource> resource;

    public Dummy(IResource resource) {
      this.resource = new ResourceTransportWrapper<>(resource);
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof Dummy)) return false;

      Dummy dummy = (Dummy) other;
      return (dummy.resource.equals(this.resource));
    }
  }

  private static IFile file;
  private static IFolder folder;

  private static IReferencePoint project;
  private static IPathFactory pathFactory;

  @BeforeClass
  public static void prepare() {
    /* Mocks */

    IPath filePath = EasyMock.createMock(IPath.class);
    IPath folderPath = EasyMock.createMock(IPath.class);
    pathFactory = EasyMock.createMock(IPathFactory.class);
    project = EasyMock.createNiceMock(IReferencePoint.class);
    file = EasyMock.createNiceMock(IFile.class);
    folder = EasyMock.createNiceMock(IFolder.class);

    String actualFilePath = "/foo/src/Main.java";

    expect(filePath.isAbsolute()).andStubReturn(false);
    expect(filePath.toPortableString()).andStubReturn(actualFilePath);

    String actualFolderPath = "/foo/bar/";

    expect(folderPath.isAbsolute()).andStubReturn(false);
    expect(folderPath.toPortableString()).andStubReturn(actualFolderPath);

    expect(pathFactory.fromPath(filePath)).andStubReturn(actualFilePath);
    expect(pathFactory.fromString(actualFilePath)).andStubReturn(filePath);

    expect(pathFactory.fromPath(folderPath)).andStubReturn(actualFolderPath);
    expect(pathFactory.fromString(actualFolderPath)).andStubReturn(folderPath);

    expect(project.getFile(filePath)).andStubReturn(file);
    expect(project.getFolder(folderPath)).andStubReturn(folder);

    expect(file.getReferencePoint()).andStubReturn(project);
    expect(file.getReferencePointRelativePath()).andStubReturn(filePath);
    expect(file.getType()).andStubReturn(Type.FILE);

    expect(folder.getReferencePoint()).andStubReturn(project);
    expect(folder.getReferencePointRelativePath()).andStubReturn(folderPath);
    expect(folder.getType()).andStubReturn(Type.FOLDER);

    EasyMock.replay(filePath, folderPath, pathFactory, project, file, folder);
  }

  @Test
  public void conversionRunningSession() {
    /* Mocks */
    ISarosSession session = EasyMock.createMock(ISarosSession.class);
    expect(session.getReferencePointId(project)).andStubReturn("ABC");
    expect(session.getReferencePoint("ABC")).andStubReturn(project);

    EasyMock.replay(session);

    /* XStream */
    XStream sender = XStreamFactory.getSecureXStream(new DomDriver());
    sender.registerConverter(new ResourceTransportWrapperConverter(session, pathFactory));

    XStream receiver = XStreamFactory.getSecureXStream(new DomDriver());
    receiver.registerConverter(new ResourceTransportWrapperConverter(session, pathFactory));

    /* Test */
    ResourceTransportWrapper<IFile> wrappedFile = new ResourceTransportWrapper<>(file);

    ResourceTransportWrapper<?> fileCopy1 =
        (ResourceTransportWrapper<?>) receiver.fromXML(sender.toXML(wrappedFile));
    assertEquals(wrappedFile, fileCopy1);

    ResourceTransportWrapper<?> fileCopy2 =
        (ResourceTransportWrapper<?>) receiver.fromXML(sender.toXML(wrappedFile));
    assertEquals(wrappedFile, fileCopy2);

    ResourceTransportWrapper<IFolder> wrappedFolder = new ResourceTransportWrapper<>(folder);

    ResourceTransportWrapper<?> folderCopy1 =
        (ResourceTransportWrapper<?>) receiver.fromXML(sender.toXML(wrappedFolder));
    assertEquals(wrappedFolder, folderCopy1);

    ResourceTransportWrapper<?> folderCopy2 =
        (ResourceTransportWrapper<?>) receiver.fromXML(sender.toXML(wrappedFolder));
    assertEquals(wrappedFolder, folderCopy2);
  }

  @Test
  public void conversionLeavingReceiver() {
    /* Mocks */
    ISarosSession senderSession = EasyMock.createMock(ISarosSession.class);
    expect(senderSession.getReferencePointId(project)).andStubReturn("ABC");
    expect(senderSession.getReferencePoint("ABC")).andStubReturn(project);

    ISarosSession receiverSession = EasyMock.createMock(ISarosSession.class);
    expect(receiverSession.getReferencePointId(project)).andReturn("ABC");
    expect(receiverSession.getReferencePoint("ABC")).andReturn(project);
    expect(receiverSession.getReferencePointId(project)).andReturn(null);
    expect(receiverSession.getReferencePoint("ABC")).andReturn(null);

    EasyMock.replay(senderSession, receiverSession);

    /* XStream */
    XStream sender = XStreamFactory.getSecureXStream(new DomDriver());
    sender.registerConverter(new ResourceTransportWrapperConverter(senderSession, pathFactory));

    XStream receiver = XStreamFactory.getSecureXStream(new DomDriver());
    receiver.registerConverter(new ResourceTransportWrapperConverter(receiverSession, pathFactory));

    /* Test */
    ResourceTransportWrapper<IFile> wrappedFile = new ResourceTransportWrapper<>(file);

    // first call on running session on receiver side
    receiver.fromXML(sender.toXML(wrappedFile));

    // second call on non-functional session on receiver side
    ResourceTransportWrapper<?> copy2 =
        (ResourceTransportWrapper<?>) receiver.fromXML(sender.toXML(wrappedFile));
    assertNull(copy2);
  }

  @Test
  public void conversionLeavingSender() {
    /* Mocks */
    ISarosSession senderSession = EasyMock.createMock(ISarosSession.class);
    expect(senderSession.getReferencePointId(project)).andReturn("ABC");
    expect(senderSession.getReferencePoint("ABC")).andReturn(project);
    expect(senderSession.getReferencePointId(project)).andReturn(null);
    expect(senderSession.getReferencePoint("ABC")).andReturn(null);

    ISarosSession receiverSession = EasyMock.createMock(ISarosSession.class);
    expect(receiverSession.getReferencePointId(project)).andStubReturn("ABC");
    expect(receiverSession.getReferencePoint("ABC")).andStubReturn(project);
    expect(receiverSession.getReferencePoint(EasyMock.isNull(String.class))).andStubReturn(null);

    EasyMock.replay(senderSession, receiverSession);

    /* XStream */
    XStream sender = XStreamFactory.getSecureXStream(new DomDriver());
    sender.registerConverter(new ResourceTransportWrapperConverter(senderSession, pathFactory));

    XStream receiver = XStreamFactory.getSecureXStream(new DomDriver());
    receiver.registerConverter(new ResourceTransportWrapperConverter(receiverSession, pathFactory));

    /* Test */
    Dummy dummy = new Dummy(file);
    Dummy copy = (Dummy) receiver.fromXML(sender.toXML(dummy));
    assertEquals(dummy, copy);

    Dummy copy2 = (Dummy) receiver.fromXML(sender.toXML(dummy));
    assertNull(copy2.resource);
  }
}
