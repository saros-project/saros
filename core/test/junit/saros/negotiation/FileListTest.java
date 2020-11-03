package saros.negotiation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static saros.filesystem.IResource.Type.FILE;
import static saros.filesystem.IResource.Type.FOLDER;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.misc.xstream.XStreamFactory;

/**
 * Reference point Layout for test
 *
 * <pre>
 *  foo (Reference Point)
 *      bar (Empty folder)
 *      info.txt (File, random content, UTF-8 encoding)
 *      foobar (Folder)
 *         foo (Empty Folder)
 *         info.txt (File, random content, ISO-8859-1 encoding)
 * </pre>
 */
public class FileListTest {

  private static final XStream xstream = XStreamFactory.getSecureXStream();

  static {
    xstream.registerConverter(BooleanConverter.BINARY);
    xstream.processAnnotations(FileList.class);
  }

  private IReferencePoint referencePoint;

  @Before
  public void setUp() throws Exception {
    referencePoint = createReferencePointLayout();
  }

  @Test
  public void testCreateFileListForReferencePoint() throws IOException {

    final FileList fileList = FileListFactory.createFileList(referencePoint, null, null);

    final List<String> paths = fileList.getPaths();

    assertTrue("file list does not contain file: info.txt", paths.contains("info.txt"));

    assertTrue(
        "file list does not contain file: foobar/info.txt", paths.contains("foobar/info.txt"));

    assertTrue("file list does not contain folder: bar", paths.contains("bar/"));

    assertTrue("file list does not contain folder: foobar/foo", paths.contains("foobar/foo/"));

    final Set<String> expectedEncodings = new HashSet<String>(Arrays.asList("ISO-8859-1", "UTF-8"));

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

  private static IReferencePoint createReferencePointLayout() {

    final IReferencePoint referencePoint = EasyMock.createMock(IReferencePoint.class);

    final IFolder barFolder = createFolderMock(referencePoint, "bar", Collections.emptyList());

    final IFolder foobarfooFolder =
        createFolderMock(referencePoint, "foobar/foo", Collections.emptyList());

    final IFile infoTxtFile = createFileMock(referencePoint, "info.txt", "1234", "UTF-8");

    final IFile foobarInfoTxtFile =
        createFileMock(referencePoint, "foobar/info.txt", "12345", "ISO-8859-1");

    List<IResource> fooBarFolderMembers = new ArrayList<>();
    fooBarFolderMembers.add(foobarfooFolder);
    fooBarFolderMembers.add(foobarInfoTxtFile);

    final IFolder foobarFolder = createFolderMock(referencePoint, "foobar", fooBarFolderMembers);

    EasyMock.expect(referencePoint.getName()).andStubReturn("foo");

    List<IResource> projectMembers = new ArrayList<>();
    projectMembers.add(barFolder);
    projectMembers.add(infoTxtFile);
    projectMembers.add(foobarFolder);

    try {
      EasyMock.expect(referencePoint.members()).andStubReturn(projectMembers);
    } catch (IOException e) {
      // cannot happen
    }

    EasyMock.replay(referencePoint);

    return referencePoint;
  }

  private static IFile createFileMock(
      final IReferencePoint referencePoint,
      final String path,
      final String content,
      final String encoding) {

    final Path relativePath = Paths.get(path);

    final IFile fileMock = EasyMock.createMock(IFile.class);

    EasyMock.expect(fileMock.getReferencePoint()).andStubReturn(referencePoint);
    EasyMock.expect(fileMock.getReferencePointRelativePath()).andStubReturn(relativePath);

    EasyMock.expect(fileMock.isIgnored()).andStubReturn(false);
    EasyMock.expect(fileMock.exists()).andStubReturn(true);
    EasyMock.expect(fileMock.getType()).andStubReturn(FILE);

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

  private static IFolder createFolderMock(
      final IReferencePoint referencePoint, final String path, final List<IResource> members) {

    final Path relativePath = Paths.get(path);

    final IFolder folderMock = EasyMock.createMock(IFolder.class);

    EasyMock.expect(folderMock.getReferencePoint()).andStubReturn(referencePoint);
    EasyMock.expect(folderMock.getReferencePointRelativePath()).andStubReturn(relativePath);

    EasyMock.expect(folderMock.isIgnored()).andStubReturn(false);
    EasyMock.expect(folderMock.exists()).andStubReturn(true);
    EasyMock.expect(folderMock.getType()).andStubReturn(FOLDER);

    try {
      EasyMock.expect(folderMock.members()).andStubReturn(members);
    } catch (IOException e) {
      // cannot happen as the mock is in recording mode
    }

    EasyMock.replay(folderMock);
    return folderMock;
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
