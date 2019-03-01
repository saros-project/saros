package de.fu_berlin.inf.dpp.filesystem;

public interface IFileContentChangedNotifier {

  public void addFileContentChangedListener(IFileContentChangedListener listener);

  public void removeFileContentChangedListener(IFileContentChangedListener listener);
}
