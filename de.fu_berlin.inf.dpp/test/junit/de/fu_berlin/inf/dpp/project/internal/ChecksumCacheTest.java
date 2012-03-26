package de.fu_berlin.inf.dpp.project.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.fu_berlin.inf.dpp.project.IChecksumCache;

public class ChecksumCacheTest {

    private String collidingA0 = "righto";
    private String collidingA1 = "buzzards";

    private String collidingB0 = "wainages";
    private String collidingB1 = "presentencing";

    private IFileContentChangedListener listener;

    private IFileContentChangedNotifier notifier = new IFileContentChangedNotifier() {

        @Override
        public void addFileContentChangedListener(
            IFileContentChangedListener listener) {
            ChecksumCacheTest.this.listener = listener;
        }

        @Override
        public void removeFileContentChangedListener(
            IFileContentChangedListener listener) {
            // NOP
        }

    };

    @Test
    public void testGetChecksumOfNonExistingEntry() {
        IChecksumCache cache = new ChecksumCacheImpl(notifier);

        // add a colliding entry increase branch coverage
        // has someone 3 different strings that have the same hashcode ? :P
        cache.addChecksum(collidingA0, 5L);

        assertEquals(null, cache.getChecksum(collidingA1));
        assertEquals(null, cache.getChecksum(collidingB0));
    }

    @Test
    public void testSingleNewInsert() {
        IChecksumCache cache = new ChecksumCacheImpl(notifier);
        assertFalse(cache.addChecksum(collidingA0, 5L));
        assertEquals(Long.valueOf(5), cache.getChecksum(collidingA0));
    }

    @Test
    public void testNonCollidingNewInserts() {
        IChecksumCache cache = new ChecksumCacheImpl(notifier);
        assertFalse(cache.addChecksum(collidingA0, 5L));
        assertFalse(cache.addChecksum(collidingB0, 6L));

        assertEquals(Long.valueOf(5), cache.getChecksum(collidingA0));
        assertEquals(Long.valueOf(6), cache.getChecksum(collidingB0));

    }

    @Test
    public void testInsertCollidingPathes() {

        IChecksumCache cache = new ChecksumCacheImpl(notifier);

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
        IChecksumCache cache = new ChecksumCacheImpl(notifier);

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
        IChecksumCache cache = new ChecksumCacheImpl(notifier);

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
        IChecksumCache cache = new ChecksumCacheImpl(notifier);
        cache.addChecksum("01234567890123456789012345678901", 5L);
        cache.addChecksum(collidingA0, 5L);
        cache.addChecksum(collidingA1, 6L);

        cache.addChecksum("01234567890123456789012345678901", 1L);
        cache.addChecksum(collidingA0, 1L);
        cache.addChecksum(collidingA1, 1L);

        assertEquals(Long.valueOf(1),
            cache.getChecksum("01234567890123456789012345678901"));
        assertEquals(Long.valueOf(1), cache.getChecksum(collidingA0));
        assertEquals(Long.valueOf(1), cache.getChecksum(collidingA1));

    }
}
