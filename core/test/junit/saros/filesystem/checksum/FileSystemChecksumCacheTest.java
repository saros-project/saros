package saros.filesystem.checksum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import saros.filesystem.IFile;

public class FileSystemChecksumCacheTest {

  private IAbsolutePathResolver absolutePathResolver;

  private IFile collidingA0;
  private IFile collidingA1;

  private IFile collidingB0;
  private IFile collidingB1;

  private IFile nonColliding;

  private IFileContentChangedListener listener;

  private IFileContentChangedNotifier notifier =
      new IFileContentChangedNotifier() {

        @Override
        public void addFileContentChangedListener(IFileContentChangedListener listener) {
          FileSystemChecksumCacheTest.this.listener = listener;
        }

        @Override
        public void removeFileContentChangedListener(IFileContentChangedListener listener) {
          // NOP
        }
      };

  @Before
  public void setup() {
    absolutePathResolver = EasyMock.createNiceMock(IAbsolutePathResolver.class);

    collidingA0 = createFileMock("righto");
    collidingA1 = createFileMock("buzzards");
    collidingB0 = createFileMock("wainages");
    collidingB1 = createFileMock("presentencing");
    nonColliding = createFileMock("01234567890123456789012345678901");

    EasyMock.replay(absolutePathResolver);
  }

  private IFile createFileMock(final String path) {
    IFile fileMock = EasyMock.createMock(IFile.class);

    EasyMock.expect(absolutePathResolver.getAbsolutePath(fileMock)).andStubReturn(path);

    return fileMock;
  }

  @Test
  public void testGetChecksumOfNonExistingEntry() {
    IChecksumCache cache = new FileSystemChecksumCache(notifier, absolutePathResolver);

    // add a colliding entry increase branch coverage
    // has someone 3 different strings that have the same hashcode ? :P
    cache.addChecksum(collidingA0, 5L);

    assertEquals(null, cache.getChecksum(collidingA1));
    assertEquals(null, cache.getChecksum(collidingB0));
  }

  @Test
  public void testSingleNewInsert() {
    IChecksumCache cache = new FileSystemChecksumCache(notifier, absolutePathResolver);
    assertFalse(cache.addChecksum(collidingA0, 5L));
    assertEquals(Long.valueOf(5), cache.getChecksum(collidingA0));
  }

  @Test
  public void testNonCollidingNewInserts() {
    IChecksumCache cache = new FileSystemChecksumCache(notifier, absolutePathResolver);
    assertFalse(cache.addChecksum(collidingA0, 5L));
    assertFalse(cache.addChecksum(collidingB0, 6L));

    assertEquals(Long.valueOf(5), cache.getChecksum(collidingA0));
    assertEquals(Long.valueOf(6), cache.getChecksum(collidingB0));
  }

  @Test
  public void testInsertCollidingPathes() {

    IChecksumCache cache = new FileSystemChecksumCache(notifier, absolutePathResolver);

    cache.addChecksum(collidingA0, 5L);
    cache.addChecksum(collidingA1, 6L);

    cache.addChecksum(collidingB0, 5L);
    cache.addChecksum(collidingB1, 6L);

    assertEquals(Long.valueOf(5), cache.getChecksum(collidingA0));
    assertEquals(Long.valueOf(6), cache.getChecksum(collidingA1));
    assertEquals(Long.valueOf(5), cache.getChecksum(collidingB0));
    assertEquals(Long.valueOf(6), cache.getChecksum(collidingB1));
  }

  @Test
  public void testChecksumInvalidationOnExistingChecksums() {
    IChecksumCache cache = new FileSystemChecksumCache(notifier, absolutePathResolver);

    cache.addChecksum(collidingB0, 5L);
    cache.addChecksum(collidingA0, 5L);
    cache.addChecksum(collidingA1, 6L);

    listener.fileContentChanged(collidingB0);
    listener.fileContentChanged(collidingA0);
    listener.fileContentChanged(collidingA1);

    assertEquals(null, cache.getChecksum(collidingB0));
    assertEquals(null, cache.getChecksum(collidingA0));
    assertEquals(null, cache.getChecksum(collidingA1));
  }

  @Test
  public void testaddChecksumsAfterFileContentChanged() {
    IChecksumCache cache = new FileSystemChecksumCache(notifier, absolutePathResolver);

    listener.fileContentChanged(collidingB0);
    listener.fileContentChanged(collidingA0);
    listener.fileContentChanged(collidingA1);

    assertEquals(null, cache.getChecksum(collidingB0));
    assertEquals(null, cache.getChecksum(collidingA0));
    assertEquals(null, cache.getChecksum(collidingA1));

    assertTrue(cache.addChecksum(collidingB0, 5L));
    assertTrue(cache.addChecksum(collidingA0, 5L));
    assertTrue(cache.addChecksum(collidingA1, 6L));
  }

  @Test
  public void testUpdateChecksum() {
    IChecksumCache cache = new FileSystemChecksumCache(notifier, absolutePathResolver);
    cache.addChecksum(nonColliding, 5L);
    cache.addChecksum(collidingA0, 5L);
    cache.addChecksum(collidingA1, 6L);

    cache.addChecksum(nonColliding, 1L);
    cache.addChecksum(collidingA0, 1L);
    cache.addChecksum(collidingA1, 1L);

    assertEquals(Long.valueOf(1), cache.getChecksum(nonColliding));
    assertEquals(Long.valueOf(1), cache.getChecksum(collidingA0));
    assertEquals(Long.valueOf(1), cache.getChecksum(collidingA1));
  }
}
