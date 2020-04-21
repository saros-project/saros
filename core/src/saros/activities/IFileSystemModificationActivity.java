package saros.activities;

import saros.filesystem.IResource;

/** Marker interface for activities that directly change the content of the file system. */
public interface IFileSystemModificationActivity<T extends IResource> extends IResourceActivity<T> {
  // NOP
}
