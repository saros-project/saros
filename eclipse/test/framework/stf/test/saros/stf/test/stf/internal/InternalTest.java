package saros.stf.test.stf.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;

import java.rmi.RemoteException;
import java.util.zip.Deflater;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;

public class InternalTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  @After
  public void deleteWorkspace() throws Exception {
    ALICE.superBot().internal().clearWorkspace();
  }

  @Test
  public void testCreateJavaProject() throws Exception {
    ALICE.superBot().internal().createJavaProject("Hello");
    assertTrue("resource does not exist", ALICE.superBot().internal().existsResource("Hello"));
  }

  @Test
  public void testCreateProject() throws Exception {
    ALICE.superBot().internal().createJavaProject("Hello");
    assertTrue("resource does not exist", ALICE.superBot().internal().existsResource("Hello"));
  }

  @Test
  public void testCreateFolder() throws Exception {
    ALICE.superBot().internal().createProject("foo");
    ALICE.superBot().internal().createFolder("foo", "bar/a/b/c/d");
    assertTrue(
        "resource does not exist", ALICE.superBot().internal().existsResource("foo/bar/a/b/c/d"));
  }

  @Test(expected = RemoteException.class)
  public void testCreateFolderWithoutProject() throws Exception {
    ALICE.superBot().internal().createFolder("foo", "bar");
  }

  @Test
  public void testChangeSarosVersion() throws RemoteException {
    ALICE.superBot().internal().changeSarosVersion("47.11.0");
  }

  @Test
  public void testcreateFile() throws Exception {
    ALICE.superBot().internal().createJavaProject("Hello");
    ALICE.superBot().internal().createFile("Hello", "test.bar", "bla");
    assertTrue(
        "resource does not exist", ALICE.superBot().internal().existsResource("Hello/test.bar"));

    ALICE.superBot().internal().createFile("Hello", "a/b/c/d/e/f/g/abc.def", "bla");
    ALICE.superBot().internal().createFile("Hello", "x/y/z/foo.bar", 100, false);
    ALICE.superBot().internal().createFile("Hello", "x/y/z/foo.bar.comp", 100, true);

    assertTrue(
        "resource does not exist",
        ALICE.superBot().internal().existsResource("Hello/a/b/c/d/e/f/g/abc.def"));

    assertTrue(
        "resource does not exist",
        ALICE.superBot().internal().existsResource("Hello/x/y/z/foo.bar"));

    assertTrue(
        "resource does not exist",
        ALICE.superBot().internal().existsResource("Hello/x/y/z/foo.bar.comp"));
  }

  @Test
  public void testGetFileContentAndCompressIt() throws Exception {
    ALICE.superBot().internal().createProject("Hello");
    ALICE.superBot().internal().createFile("Hello", "x/y/z/foo.bar.comp", 1024 * 1024, true);

    byte[] content = ALICE.superBot().internal().getFileContent("Hello", "x/y/z/foo.bar.comp");

    assertEquals(1024 * 1024, content.length);

    // make it bigger for the case the content can not be compressed
    byte[] contentContent = new byte[2048 * 1024];
    Deflater compresser = new Deflater();
    compresser.setInput(content);
    compresser.finish();
    int compressedDataLength = compresser.deflate(contentContent);
    assertTrue(
        "compressed file content is to large: " + compressedDataLength,
        compressedDataLength <= 5 * 1024);
  }

  @Test
  public void testGetFileContent() throws Exception {
    ALICE.superBot().internal().createProject("Hello");
    ALICE.superBot().internal().createFile("Hello", "bar", "Kitzmann Bier");

    assertEquals(
        "Kitzmann Bier", new String(ALICE.superBot().internal().getFileContent("Hello", "bar")));
  }

  @Test
  public void testGetFileSize() throws Exception {
    ALICE.superBot().internal().createJavaProject("Hello");
    ALICE.superBot().internal().createFile("Hello", "test.bar", "bla");
    assertEquals(3L, ALICE.superBot().internal().getFileSize("Hello", "test.bar"));
  }

  @AfterClass
  public static void resetSarosVersion() throws RemoteException {
    ALICE.superBot().internal().resetSarosVersion();
  }
}
