package saros.filesystem;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Default implementation of the checksum cache {@link IChecksumCache interface} .
 *
 * <p>The implementation is optimized in regards to memory consumption. Affected files are
 * identified by using two different hash functions based on the path it points to rather than
 * storing the concrete path.
 *
 * <p>In order to use this implementation a concrete file change {@link IFileContentChangedNotifier
 * notifier} has to be provided that tracks file changes in the currently used file system.
 *
 * <p><b>Note:</b> This implementation is <b>NOT</b> capable of handling hash collisions.
 *
 * @author Stefan Rossbach
 */
// TODO add probability of hash collisions, lower bound should be 1 / (2^32 *
// 2^128)
public final class FileSystemChecksumCache implements IChecksumCache {

  private static final Logger LOG = Logger.getLogger(FileSystemChecksumCache.class);

  private static final int SEED = 0xDEADBEEF;

  private static class Murmur3Hash<T> {

    long h1;
    long h2;
    T object;

    public Murmur3Hash(long h1, long h2) {
      this.h1 = h1;
      this.h2 = h2;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object o) {

      if (o == null || !(o instanceof Murmur3Hash)) return false;

      return h1 == ((Murmur3Hash) o).h1 && h2 == ((Murmur3Hash) o).h2;
    }

    @Override
    public String toString() {
      return "0x" + Long.toHexString(h2).toUpperCase() + Long.toHexString(h1).toUpperCase();
    }

    @Override
    public int hashCode() {
      return (int) (h1 ^ h2);
    }

    public void setObject(T object) {
      this.object = object;
    }

    public T getObject() {
      return object;
    }
  }

  private final IFileContentChangedListener fileContentChangedListener =
      new IFileContentChangedListener() {

        @Override
        public void fileContentChanged(IFile file) {
          synchronized (FileSystemChecksumCache.this) {
            final String path = file.getFullPath().toOSString();

            Murmur3Hash<Long> hash = create128BitMurmur3Hash(path);
            Murmur3Hash<Long> currentHash = getHash(path, hash);

            if (currentHash != null) {
              if (LOG.isTraceEnabled())
                LOG.trace(
                    "invalidating checksum for existing file: " + path + " [" + currentHash + "]");

              currentHash.setObject(null);
            } else {
              if (LOG.isTraceEnabled())
                LOG.trace("invalidating checksum for new file: " + path + " [" + hash + "]");

              addChecksum(file, 0);
              getHash(path, hash).setObject(null);
            }
          }
        }
      };

  private Map<Integer, Object> cache = new HashMap<Integer, Object>();

  public FileSystemChecksumCache(IFileContentChangedNotifier fileContentChangedNotifier) {
    fileContentChangedNotifier.addFileContentChangedListener(fileContentChangedListener);
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public synchronized Long getChecksum(IFile file) {

    final String path = file.getFullPath().toOSString();

    Object object = cache.get(path.hashCode());

    if (object == null) {
      logNoValidChecksum(path);
      return null;
    }

    Murmur3Hash<Long> hash = create128BitMurmur3Hash(path);

    if (object instanceof Murmur3Hash) {
      if (hash.equals(object)) {
        Murmur3Hash<Long> currentHash = (Murmur3Hash<Long>) object;
        logValidChecksum(path, currentHash);
        return currentHash.getObject();
      } else {
        logNoValidChecksum(path);
        return null;
      }
    }

    List<Murmur3Hash<Long>> hashes = (List<Murmur3Hash<Long>>) object;
    int index = hashes.indexOf(hash);

    if (index == -1) {
      logNoValidChecksum(path);
      return null;
    }

    Murmur3Hash<Long> currentHash = hashes.get(index);
    logValidChecksum(path, currentHash);
    return currentHash.getObject();
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized boolean addChecksum(IFile file, long checksum) {

    final String path = file.getFullPath().toOSString();

    Murmur3Hash<Long> hash = create128BitMurmur3Hash(path);
    hash.setObject(checksum);

    Object object = cache.get(path.hashCode());

    if (object == null) {
      cache.put(path.hashCode(), hash);
      return false;
    }

    Murmur3Hash<Long> currentHash = getHash(path, hash);

    if (currentHash != null) {
      if (currentHash.getObject() == null) {
        currentHash.setObject(checksum);
        return true;
      }

      currentHash.setObject(checksum);
      return false;
    }

    List<Murmur3Hash<Long>> list;

    if (object instanceof Murmur3Hash) {
      currentHash = (Murmur3Hash<Long>) object;
      list = new ArrayList<Murmur3Hash<Long>>(2);
      list.add(currentHash);
      cache.put(path.hashCode(), list);
    } else {
      list = (List<Murmur3Hash<Long>>) object;
    }

    list.add(hash);
    return false;
  }

  @SuppressWarnings("unchecked")
  private Murmur3Hash<Long> getHash(String path, Murmur3Hash<Long> hash) {
    Object object = cache.get(path.hashCode());

    if (object == null) return null;

    if (hash.equals(object)) return (Murmur3Hash<Long>) object;

    if (object instanceof Murmur3Hash) return null;

    List<Murmur3Hash<Long>> list = (List<Murmur3Hash<Long>>) object;

    int index = list.indexOf(hash);

    if (index == -1) return null;

    return list.get(index);
  }

  private Murmur3Hash<Long> create128BitMurmur3Hash(String path) {
    try {
      return create128BitMurmur3Hash(path.getBytes("UTF-8"), SEED);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(
          "invalid Java installation, UTF-8 charset must be included per specification", e);
    }
  }

  /*
   * Simplified version of
   * http://code.google.com/p/guava-libraries/source/browse
   * /guava/src/com/google/common/hash/Murmur3_128HashFunction.java
   *
   * Copyright (C) 2011 The Guava Authors
   *
   * License: http://www.apache.org/licenses/LICENSE-2.0
   */

  private Murmur3Hash<Long> create128BitMurmur3Hash(byte[] bytes, int seed) {
    long h1 = seed;
    long h2 = seed;
    long c1 = 0x87c37b91114253d5L;
    long c2 = 0x4cf5ad432745937fL;
    int len = 0;

    long k1 = 0;
    long k2 = 0;

    ByteBuffer buffer = ByteBuffer.wrap(bytes);

    while (buffer.remaining() >= 16) {
      k1 = buffer.getLong();
      k2 = buffer.getLong();
      len += 16;

      k1 = Long.rotateLeft(k1, 31);
      k1 *= c2;
      h1 ^= k1;

      h1 = Long.rotateLeft(h1, 27);
      h1 += h2;
      h1 = h1 * 5 + 0x52dce729;

      k2 *= c2;
      k2 = Long.rotateLeft(k2, 33);
      k2 *= c1;
      h2 ^= k2;

      h2 = Long.rotateLeft(h2, 31);
      h2 += h1;
      h2 = h2 * 5 + 0x38495ab5;
    }

    len += buffer.remaining();
    switch (buffer.remaining()) {
      case 15:
        k2 ^= (long) toInt(buffer.get(14)) << 48;
        // $FALL-THROUGH$
      case 14:
        k2 ^= (long) toInt(buffer.get(13)) << 40;
        // $FALL-THROUGH$
      case 13:
        k2 ^= (long) toInt(buffer.get(12)) << 32;
        // $FALL-THROUGH$
      case 12:
        k2 ^= (long) toInt(buffer.get(11)) << 24;
        // $FALL-THROUGH$
      case 11:
        k2 ^= (long) toInt(buffer.get(10)) << 16;
        // $FALL-THROUGH$
      case 10:
        k2 ^= (long) toInt(buffer.get(9)) << 8;
        // $FALL-THROUGH$
      case 9:
        k2 ^= (long) toInt(buffer.get(8)) << 0;
        k2 *= c2;
        k2 = Long.rotateLeft(k2, 33);
        k2 *= c1;
        h2 ^= k2;
        // $FALL-THROUGH$
      case 8:
        k1 ^= (long) toInt(buffer.get(7)) << 56;
        // $FALL-THROUGH$
      case 7:
        k1 ^= (long) toInt(buffer.get(6)) << 48;
        // $FALL-THROUGH$
      case 6:
        k1 ^= (long) toInt(buffer.get(5)) << 40;
        // $FALL-THROUGH$
      case 5:
        k1 ^= (long) toInt(buffer.get(4)) << 32;
        // $FALL-THROUGH$
      case 4:
        k1 ^= (long) toInt(buffer.get(3)) << 24;
        // $FALL-THROUGH$
      case 3:
        k1 ^= (long) toInt(buffer.get(2)) << 16;
        // $FALL-THROUGH$
      case 2:
        k1 ^= (long) toInt(buffer.get(1)) << 8;
        // $FALL-THROUGH$
      case 1:
        k1 ^= (long) toInt(buffer.get(0)) << 0;
        k1 *= c1;
        k1 = Long.rotateLeft(k1, 31);
        k1 *= c2;
        h1 ^= k1;
        // $FALL-THROUGH$
      default:
    }

    h1 ^= len;
    h2 ^= len;

    h1 += h2;
    h2 += h1;

    h1 ^= h1 >>> 33;
    h1 *= 0xff51afd7ed558ccdL;
    h1 ^= h1 >>> 33;
    h1 *= 0xc4ceb9fe1a85ec53L;
    h1 ^= h1 >>> 33;

    h2 ^= h2 >>> 33;
    h2 *= 0xff51afd7ed558ccdL;
    h2 ^= h2 >>> 33;
    h2 *= 0xc4ceb9fe1a85ec53L;
    h2 ^= h2 >>> 33;

    h1 += h2;
    h2 += h1;

    return new Murmur3Hash<Long>(h1, h2);
  }

  private int toInt(byte b) {
    return b & 0xFF;
  }

  private void logNoValidChecksum(String path) {
    if (LOG.isTraceEnabled()) LOG.trace("no valid checksum found for file: " + path);
  }

  private void logValidChecksum(String path, Murmur3Hash<Long> hash) {
    if (LOG.isTraceEnabled())
      LOG.trace(
          "found valid checksum found for file: "
              + path
              + " ["
              + hash.getObject()
              + ","
              + hash
              + "]");
  }
}
