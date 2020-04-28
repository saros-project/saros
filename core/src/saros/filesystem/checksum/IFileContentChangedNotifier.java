package saros.filesystem.checksum;

public interface IFileContentChangedNotifier {

  public void addFileContentChangedListener(IFileContentChangedListener listener);

  public void removeFileContentChangedListener(IFileContentChangedListener listener);
}
