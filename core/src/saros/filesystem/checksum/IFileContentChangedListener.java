package saros.filesystem.checksum;

import saros.filesystem.IFile;

public interface IFileContentChangedListener {
  public void fileContentChanged(IFile file);
}
