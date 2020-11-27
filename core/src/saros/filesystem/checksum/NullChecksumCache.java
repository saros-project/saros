package saros.filesystem.checksum;

import saros.filesystem.IFile;

/**
 * A checksum cache that always makes callers recalculate checksums.
 *
 * <p>Useful if your implementation has difficulties using the {@link FileSystemChecksumCache}.
 */
public class NullChecksumCache implements IChecksumCache {
  @Override
  public Long getChecksum(IFile file) {
    return null;
  }

  @Override
  public boolean addChecksum(IFile file, long checksum) {
    return false;
  }
}
