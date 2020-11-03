package saros.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

// TODO replace "expected" with Assert.assertThrows(...) once jUnit 4.13 is used
public class PathUtilsTest {

  @Test
  public void testIsEmptyOnEmpty() {
    Path emptyPath = Paths.get("");

    assertTrue("Empty path is not seen as empty", PathUtils.isEmpty(emptyPath));
  }

  @Test
  public void testIsEmptyOnNotEmpty() {
    Path emptyPath = Paths.get("foo", "bar");

    assertFalse("Non-empty path is seen as empty", PathUtils.isEmpty(emptyPath));
  }

  @Test
  public void testIsEmptyOnLocalReference() {
    Path emptyPath = Paths.get(".");

    assertTrue("Local reference path is not seen as empty", PathUtils.isEmpty(emptyPath));
  }

  @Test
  public void testIsEmptyOnComplicatedLocalReference() {
    Path emptyPath = Paths.get(".", ".", ".");

    assertTrue(
        "Complicated local reference path is not seen as empty", PathUtils.isEmpty(emptyPath));
  }

  @Test
  public void testIsEmptyOnNormalizedEmptyPath() {
    Path emptyPath = Paths.get("foo", "bar", "..", "..");

    assertTrue("Normalized empty path is not seen as empty", PathUtils.isEmpty(emptyPath));
  }

  @Test
  public void testIsEmptyOnNormalizedNonEmptyPath() {
    Path emptyPath = Paths.get("foo", "bar", "..");

    assertFalse("Normalized non-empty path is seen as empty", PathUtils.isEmpty(emptyPath));
  }

  @Test
  public void testRemoveFirstSegmentsNone() {
    Path path = Paths.get("foo", "bar");
    Path shortenedPath = PathUtils.removeFirstSegments(path, 0);

    Path expectedResult = Paths.get("foo", "bar");

    assertEquals(
        "Dropping first segments did not result in expected outcome",
        expectedResult,
        shortenedPath);
  }

  @Test
  public void testRemoveFirstSegmentsSome() {
    Path path = Paths.get("foo", "bar");
    Path shortenedPath = PathUtils.removeFirstSegments(path, 1);

    Path expectedResult = Paths.get("bar");

    assertEquals(
        "Dropping first segments did not result in expected outcome",
        expectedResult,
        shortenedPath);
  }

  @Test
  public void testRemoveFirstSegmentsAll() {
    Path path = Paths.get("foo", "bar");
    Path shortenedPath = PathUtils.removeFirstSegments(path, 2);

    Path expectedResult = Paths.get("");

    assertEquals(
        "Dropping first segments did not result in expected outcome",
        expectedResult,
        shortenedPath);
  }

  @Test
  public void testRemoveFirstSegmentsMoreThanAll() {
    Path path = Paths.get("foo", "bar");
    Path shortenedPath = PathUtils.removeFirstSegments(path, 3);

    Path expectedResult = Paths.get("");

    assertEquals(
        "Dropping first segments did not result in expected outcome",
        expectedResult,
        shortenedPath);
  }

  @Test
  public void testRemoveLastSegmentsNone() {
    Path path = Paths.get("foo", "bar");
    Path shortenedPath = PathUtils.removeLastSegments(path, 0);

    Path expectedResult = Paths.get("foo", "bar");

    assertEquals(
        "Dropping first segments did not result in expected outcome",
        expectedResult,
        shortenedPath);
  }

  @Test
  public void testRemoveLastSegmentsSome() {
    Path path = Paths.get("foo", "bar");
    Path shortenedPath = PathUtils.removeLastSegments(path, 1);

    Path expectedResult = Paths.get("foo");

    assertEquals(
        "Dropping first segments did not result in expected outcome",
        expectedResult,
        shortenedPath);
  }

  @Test
  public void testRemoveLastSegmentsAll() {
    Path path = Paths.get("foo", "bar");
    Path shortenedPath = PathUtils.removeLastSegments(path, 2);

    Path expectedResult = Paths.get("");

    assertEquals(
        "Dropping first segments did not result in expected outcome",
        expectedResult,
        shortenedPath);
  }

  @Test
  public void testRemoveLastSegmentsMoreThanAll() {
    Path path = Paths.get("foo", "bar");
    Path shortenedPath = PathUtils.removeLastSegments(path, 3);

    Path expectedResult = Paths.get("");

    assertEquals(
        "Dropping first segments did not result in expected outcome",
        expectedResult,
        shortenedPath);
  }

  @Test
  public void testNormalizeResolvedParentReference() {
    Path path = Paths.get("foo", "stuff", "..", "bar");
    Path normalizedPath = PathUtils.normalize(path);

    Path expectedResult = Paths.get("foo", "bar");

    assertEquals(
        "Normalizing path did not result in expected outcome", expectedResult, normalizedPath);
  }

  @Test
  public void testNormalizeLocalReference() {
    Path path = Paths.get(".");
    Path normalizedPath = PathUtils.normalize(path);

    Path expectedResult = Paths.get("");

    assertEquals(
        "Normalizing path did not result in expected outcome", expectedResult, normalizedPath);
  }

  @Test
  public void testNormalizeMixed() {
    Path path = Paths.get(".", "foo", ".", "", "stuff", "..", ".", "bar", ".", ".");
    Path normalizedPath = PathUtils.normalize(path);

    Path expectedResult = Paths.get("foo", "bar");

    assertEquals(
        "Normalizing path did not result in expected outcome", expectedResult, normalizedPath);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNormalizeAbsolute() {
    Path path = Paths.get("foo", "bar").toAbsolutePath();
    PathUtils.normalize(path);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNormalizeDanglingParentReference() {
    Path path = Paths.get("..");
    System.out.println("Path: '" + PathUtils.normalize(path) + "'");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNormalizeDanglingParentReferenceAfterResolving() {
    Path path = Paths.get("foo", "bar", "..", "..", "..");
    System.out.println("Path: '" + PathUtils.normalize(path) + "'");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNormalizeUnresolvedParentReference() {
    Path path = Paths.get("foo", "..", "..", "bar");
    System.out.println("Path: '" + PathUtils.normalize(path) + "'");
  }
}
