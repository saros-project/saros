package saros.filesystem;

public interface IFileContentChangedNotifier {

  public void addFileContentChangedListener(IFileContentChangedListener listener);

  public void removeFileContentChangedListener(IFileContentChangedListener listener);
}
