package de.fu_berlin.inf.dpp.negotiation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IFolder_V2;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

/*
 *Project Layout for test
 *
 *  foo (Project, UTF-16 encoding)
 *      bar (Empty folder)
 *      info.txt (File, random content, UTF-8 encoding)
 *      foobar (Folder)
 *         foo (Empty Folder)
 *         info.txt (File, random content, ISO-8859-1 encoding)
 *
 */
public class FileListTest {

  private static final XStream xstream = new XStream();

  static {
    xstream.registerConverter(BooleanConverter.BINARY);
    xstream.processAnnotations(FileList.class);
  }

  private IFolder_V2 project;
  private IReferencePoint referencePoint;
  private IReferencePointManager referencePointManager;

  @Before
  public void setUp() throws Exception {
    referencePoint = createReferencePointMock();
    project = createProjectLayout(referencePoint);
    referencePointManager = createReferencePointManagerMock(referencePoint, project);
  }

  @Test
  public void testCreateFileListForProject() throws IOException {

    final FileList fileList =
        FileListFactory.createFileList(referencePointManager, referencePoint, null, null, null);

    final List<String> paths = fileList.getPaths();

    assertTrue("file list does not contain file: info.txt", paths.contains("info.txt"));

    assertTrue(
        "file list does not contain file: foobar/info.txt", paths.contains("foobar/info.txt"));

    assertTrue("file list does not contain folder: bar", paths.contains("bar/"));

    assertTrue("file list does not contain folder: foobar/foo", paths.contains("foobar/foo/"));

    final Set<String> expectedEncodings =
        new HashSet<String>(Arrays.asList("ISO-8859-1", "UTF-8", "UTF-16"));

    assertNotNull("no checksum found for file: info.txt", fileList.getMetaData("info.txt"));

    assertNotNull(
        "no checksum found for file: foobar/info.txt", fileList.getMetaData("foobar/info.txt"));

    assertEquals("not all encodings were fetched", expectedEncodings, fileList.getEncodings());
  }

  @Test
  public void testToXmlAndBack() throws Exception {
    List<String> files = new ArrayList<String>();
    StringBuilder builder = new StringBuilder();
    Random random = new Random();
    for (int a = 0; a < 2; a++) {
      for (int i = 0; i < 2; i++) {
        for (int k = 0; k < 2; k++) {
          builder.setLength(0);
          builder.append("string12345");
          for (int j = 0; j < 5; j++) {
            builder.append((char) (random.nextInt(26) + 65));
          }
          files.add("foo1234567890" + i + "/bar1234567890" + a + "/" + builder.toString());
        }
      }
    }

    FileList list = FileListFactory.createFileList(files);
    String xml = toXML(list);
    FileList listFromXml = fromXML(xml);
    assertEquals(list, listFromXml);
  }

  private static IFolder_V2 createProjectLayout(IReferencePoint referencePoint) {

    final IFolder_V2 project = EasyMock.createMock(IFolder_V2.class);

    final IFolder barFolder = createFolderMock(project, "bar", new IResource[0]);

    final IFolder foobarfooFolder = createFolderMock(project, "foobar/foo", new IResource[0]);

    final IFile infoTxtFile = createFileMock(project, "info.txt", "1234", "UTF-8");

    final IFile foobarInfoTxtFile =
        createFileMock(project, "foobar/info.txt", "12345", "ISO-8859-1");

    final IFolder foobarFolder =
        createFolderMock(project, "foobar", new IResource[] {foobarfooFolder, foobarInfoTxtFile});

    EasyMock.expect(project.getName()).andStubReturn("foo");
    EasyMock.expect(project.getReferencePoint()).andStubReturn(referencePoint);

    try {
      EasyMock.expect(project.getDefaultCharset()).andStubReturn("UTF-16");

      EasyMock.expect(project.members())
          .andStubReturn(new IResource[] {barFolder, infoTxtFile, foobarFolder});
    } catch (IOException e) {
      // cannot happen
    }

    EasyMock.replay(project);

    return project;
  }

  private static IFile createFileMock(
      final IFolder_V2 project, final String path, final String content, final String encoding) {

    final IPath relativePath = createPathMock(path);

    final IFile fileMock = EasyMock.createMock(IFile.class);

    EasyMock.expect(fileMock.getReferenceFolder()).andStubReturn(project);
    EasyMock.expect(fileMock.getProjectRelativePath()).andStubReturn(relativePath);

    EasyMock.expect(fileMock.isDerived()).andStubReturn(false);
    EasyMock.expect(fileMock.exists()).andStubReturn(true);
    EasyMock.expect(fileMock.getType()).andStubReturn(IResource.FILE);

    // only used for UI feedback
    EasyMock.expect(fileMock.getName()).andStubReturn("");

    try {
      EasyMock.expect(fileMock.getContents())
          .andStubAnswer(
              new IAnswer<InputStream>() {
                @Override
                public InputStream answer() throws Throwable {
                  return new ByteArrayInputStream(content.getBytes());
                }
              });
      EasyMock.expect(fileMock.getCharset()).andStubReturn(encoding);
    } catch (IOException e) {
      // cannot happen as the mock is in recording mode
    }

    EasyMock.replay(fileMock);
    return fileMock;
  }

  private static IPath createPathMock(final String path) {

    assertFalse(path.charAt(path.length() - 1) == '/');

    final IPath pathMock = EasyMock.createMock(IPath.class);

    EasyMock.expect(pathMock.toPortableString()).andStubReturn(path);

    EasyMock.replay(pathMock);
    return pathMock;
  }

  private static IFolder createFolderMock(
      final IFolder_V2 project, final String path, final IResource[] members) {

    final IPath relativePath = createPathMock(path);

    final IFolder folderMock = EasyMock.createMock(IFolder.class);

    EasyMock.expect(folderMock.getReferenceFolder()).andStubReturn(project);
    EasyMock.expect(folderMock.getProjectRelativePath()).andStubReturn(relativePath);

    EasyMock.expect(folderMock.isDerived()).andStubReturn(false);
    EasyMock.expect(folderMock.exists()).andStubReturn(true);
    EasyMock.expect(folderMock.getType()).andStubReturn(IResource.FOLDER);

    try {
      EasyMock.expect(folderMock.members()).andStubReturn(members);
    } catch (IOException e) {
      // cannot happen as the mock is in recording mode
    }

    EasyMock.replay(folderMock);
    return folderMock;
  }

  private static IReferencePoint createReferencePointMock() {
    IReferencePoint referencePoint = EasyMock.createMock(IReferencePoint.class);
    EasyMock.replay(referencePoint);
    return referencePoint;
  }

  private static IReferencePointManager createReferencePointManagerMock(
      IReferencePoint referencePoint, IFolder_V2 project) {
    IReferencePointManager referencePointManager =
        EasyMock.createMock(IReferencePointManager.class);
    EasyMock.expect(referencePointManager.get(referencePoint)).andStubReturn(project);
    EasyMock.replay(referencePointManager);
    return referencePointManager;
  }

  private static String toXML(FileList list) {
    StringWriter writer = new StringWriter(512 * 1024);
    xstream.marshal(list, new CompactWriter(writer));
    return writer.toString();
  }

  private static FileList fromXML(String xml) {
    return (FileList) xstream.fromXML(xml);
  }
}
