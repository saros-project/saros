package saros.intellij.project.filesystem;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

class VirtualFileMock extends VirtualFile {

  private File file;

  public VirtualFileMock(File file) {
    this.file = file;
  }

  @NotNull
  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public VirtualFileSystem getFileSystem() {
    return LocalFileSystem.getInstance(); // Must be mocked
  }

  @NotNull
  @Override
  public String getPath() {
    return file.getPath();
  }

  @Override
  public boolean isWritable() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isDirectory() {
    return file.isDirectory();
  }

  @Override
  public boolean isValid() {
    return file.exists();
  }

  @Override
  public VirtualFile getParent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public VirtualFile[] getChildren() {
    File[] childrenFiles = file.listFiles();

    if (childrenFiles == null) {
      return new VirtualFileMock[0];
    }

    List<VirtualFileMock> children = new ArrayList<VirtualFileMock>();

    for (File child : childrenFiles) {
      children.add(new VirtualFileMock(child));
    }

    return children.toArray(new VirtualFileMock[children.size()]);
  }

  @NotNull
  @Override
  public VirtualFile createChildDirectory(Object requestor, @NotNull String name)
      throws IOException {
    Path newPath = Files.createDirectory(file.toPath().resolve(name));

    return new VirtualFileMock(newPath.toFile());
  }

  @NotNull
  @Override
  public VirtualFile createChildData(Object requestor, @NotNull String name) throws IOException {
    Path newPath = Files.createFile(file.toPath().resolve(name));

    return new VirtualFileMock(newPath.toFile());
  }

  @Override
  public void delete(Object requestor) throws IOException {
    file.delete();
  }

  @Override
  public VirtualFile copy(
      Object requestor, @NotNull VirtualFile newParent, @NotNull String copyName)
      throws IOException {
    Path newPath = Files.copy(file.toPath(), Paths.get(newParent.getPath(), copyName));

    return new VirtualFileMock(newPath.toFile());
  }

  @NotNull
  @Override
  public OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
    return new FileOutputStream(file);
  }

  @NotNull
  @Override
  public byte[] contentsToByteArray() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getTimeStamp() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getLength() {
    return file.length();
  }

  @Override
  public void refresh(boolean b, boolean b1, Runnable runnable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(file);
  }
}
