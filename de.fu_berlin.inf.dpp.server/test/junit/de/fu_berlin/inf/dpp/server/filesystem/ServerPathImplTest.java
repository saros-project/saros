package de.fu_berlin.inf.dpp.server.filesystem;

import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.absolutePath;
import static de.fu_berlin.inf.dpp.server.filesystem.FileSystemTestUtils.path;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.filesystem.IPath;
import java.io.File;
import org.junit.Ignore;
import org.junit.Test;

public class ServerPathImplTest {

  private IPath nativePath(String pathString) {
    return path(pathString.replace("/", File.separator));
  }

  @Test
  public void emptyPath() {
    assertSame(ServerPathImpl.EMPTY, path(""));
  }

  @Test
  public void equalsComparesSegments() {
    assertEquals(path("foo/bar"), path("foo/bar"));
    assertFalse(path("fooo/bar").equals(path("foo/bar")));
    assertFalse(path("foo/baz").equals(path("foo/bar")));
  }

  @Test
  public void equalsComparesTrailingSeparator() {
    assertEquals(path("foo/bar"), (path("foo/bar/")));
  }

  @Test
  public void equalsIgnoresSeparatorTypes() {
    assertEquals(path("foo/bar"), path("foo" + File.separator + "bar"));
  }

  @Test
  public void segments() {
    String[] segments = path("foo/bar").segments();
    assertArrayEquals(new String[] {"foo", "bar"}, segments);
  }

  @Test
  public void segmentsOfEmptyPath() {
    assertArrayEquals(new String[0], path("").segments());
  }

  @Test
  public void segmentsOfPathWithEmptySegments() {
    String[] segments = path("foo//bar").segments();
    assertArrayEquals(new String[] {"foo", "bar"}, segments);
  }

  @Test
  public void segmentsOfAbsolutePath() {
    String[] segments = absolutePath("foo/bar").segments();
    assertArrayEquals(new String[] {"foo", "bar"}, segments);
  }

  @Test
  public void segmentsOfTrailingSeparatorPath() {
    String[] segments = path("trailing/sep/").segments();
    assertArrayEquals(new String[] {"trailing", "sep"}, segments);
  }

  @Test
  public void segmentsOfNativePath() {
    String[] segments = nativePath("foo/bar").segments();
    assertArrayEquals(new String[] {"foo", "bar"}, segments);
  }

  @Test
  public void segmentsAreCopied() {
    IPath path = path("foo/bar");
    path.segments()[0] = "haha";
    assertArrayEquals(new String[] {"foo", "bar"}, path.segments());
  }

  @Test
  public void segmentCount() {
    assertEquals(2, path("foo/bar").segmentCount());
  }

  @Test
  public void segmentCountOfEmptyPath() {
    assertEquals(0, path("").segmentCount());
  }

  @Test
  public void segmentCountTrailingSeparatorPath() {
    assertEquals(2, path("trailing/sep/").segmentCount());
  }

  @Test
  public void segment() {
    IPath path = path("foo/bar");
    assertEquals("foo", path.segment(0));
    assertEquals("bar", path.segment(1));
  }

  @Test
  public void lastSegment() {
    assertEquals("bar", path("foo/bar").lastSegment());
  }

  @Test
  public void lastSegmentOfEmptyPath() {
    assertEquals(null, path("").lastSegment());
  }

  @Test
  public void removeZeroFirstSegments() {
    IPath path = path("foo/bar/baz");
    assertSame(path, path.removeFirstSegments(0));
  }

  @Test
  public void removeSomeFirstSegments() {
    IPath path = path("foo/bar/baz");
    assertEquals(path("bar/baz"), path.removeFirstSegments(1));
    assertEquals(path("baz"), path.removeFirstSegments(2));
  }

  @Test
  public void removeSomeFirstSegmentsFromAbsolutePath() {
    IPath path = absolutePath("foo/bar/baz");
    assertEquals(path("bar/baz"), path.removeFirstSegments(1));
    assertEquals(path("baz"), path.removeFirstSegments(2));
  }

  @Test
  public void removeAllFirstSegments() {
    IPath path = path("foo/bar/baz");
    assertSame(path(""), path.removeFirstSegments(3));
  }

  @Test
  public void removeMoreFirstSegmentsThanExist() {
    IPath path = path("foo/bar/baz");
    assertSame(path(""), path.removeFirstSegments(4));
  }

  @Test
  public void removeZeroLastSegments() {
    IPath path = path("foo/bar/baz");
    assertSame(path, path.removeLastSegments(0));
  }

  @Test
  public void removeSomeLastSegments() {
    IPath path = path("foo/bar/baz");
    assertEquals(path("foo/bar"), path.removeLastSegments(1));
    assertEquals(path("foo"), path.removeLastSegments(2));
  }

  @Test
  public void removeSomeLastSegmentsFromAbsolutePath() {
    IPath path = absolutePath("foo/bar/baz");
    assertEquals(absolutePath("foo/bar"), path.removeLastSegments(1));
    assertEquals(absolutePath("foo"), path.removeLastSegments(2));
  }

  @Test
  public void removeAllLastSegments() {
    IPath path = path("foo/bar/baz");
    assertSame(path(""), path.removeLastSegments(3));
  }

  @Test
  public void removeMoreLastSegmentsThanExist() {
    IPath path = path("foo/bar/baz");
    assertSame(path(""), path.removeLastSegments(4));
  }

  @Test
  public void isAbsolute() {
    assertTrue(absolutePath("foo/bar").isAbsolute());
    assertFalse(path("foo/bar").isAbsolute());
  }

  @Test
  public void makeAbsolute() {
    assertEquals(absolutePath("foo/bar"), path("foo/bar").makeAbsolute());
  }

  @Test
  @Ignore
  public void makeAbsoluteIsIdempotent() {
    assertSame(absolutePath("foo/bar"), absolutePath("foo/bar").makeAbsolute());
  }

  @Test
  public void isPrefixOf() {
    IPath path = path("foo/bar/baz");
    assertTrue(path("foo").isPrefixOf(path));
    assertTrue(path("foo/bar").isPrefixOf(path));
    assertFalse(path("fooo").isPrefixOf(path));
    assertFalse(path("foo/baz").isPrefixOf(path));
  }

  @Test
  public void isPrefixOfSelf() {
    assertTrue(path("foo/bar").isPrefixOf(path("foo/bar")));
  }

  @Test
  public void emptyPathIsPrefixOfEverything() {
    assertTrue(path("").isPrefixOf(path("")));
    assertTrue(path("").isPrefixOf(path("foo")));
    assertTrue(path("").isPrefixOf(path("foo/bar")));
  }

  @Test
  public void notPrefixIfLastSegmentIsSegmentPrefix() {
    assertFalse(path("foo/b").isPrefixOf(path("foo/bar")));
  }

  @Test
  public void appendPath() {
    IPath path1 = path("foo/bar");
    IPath path2 = path("baz");
    assertEquals(path("foo/bar/baz"), path1.append(path2));
  }

  @Test
  public void appendRelativeToAbsolutePath() {
    IPath path1 = absolutePath("foo");
    IPath path2 = path("bar");
    assertEquals(absolutePath("foo/bar"), path1.append(path2));
  }

  @Test
  public void appendAbsoluteToRelativePath() {
    IPath path1 = path("foo");
    IPath path2 = absolutePath("bar");
    assertEquals(path("foo/bar"), path1.append(path2));
  }

  @Test
  public void appendAbsoluteToAbsolutePath() {
    IPath path1 = absolutePath("foo");
    IPath path2 = absolutePath("bar");
    assertEquals(absolutePath("foo/bar"), path1.append(path2));
  }

  @Test
  public void appendPathString() {
    assertEquals(path("foo/bar/baz"), path("foo/bar").append("baz"));
  }

  @Test
  public void toPortableString() {
    assertEquals("foo/bar/baz", path("foo/bar/baz").toPortableString());
  }

  @Test
  public void toPortableStringWithTrailingSeparator() {
    assertEquals("foo/bar/baz", path("foo/bar/baz/").toPortableString());
  }

  @Test
  public void emptyPathToPortableString() {
    assertEquals("", path("").toPortableString());
  }

  @Test
  public void toOSSString() {
    IPath path = path("foo/bar");
    String expected = "foo" + File.separator + "bar";
    assertEquals(expected, path.toOSString());
  }

  @Test
  public void toOSStringWithTrailingSeparator() {
    IPath path = path("foo/bar/");
    String expected = "foo" + File.separator + "bar";
    assertEquals(expected, path.toOSString());
  }

  @Test
  public void emptyPathToOSString() {
    assertEquals("", path("").toOSString());
  }

  @Test
  public void toFile() {
    File file = path("foo/bar").toFile();
    assertEquals("foo" + File.separator + "bar", file.getPath());
  }
}
