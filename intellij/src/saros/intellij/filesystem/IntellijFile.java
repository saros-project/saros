package saros.intellij.filesystem;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;
import saros.filesystem.IResource;
import saros.intellij.runtime.FilesystemRunner;

/** Intellij implementation of the Saros file interface. */
public class IntellijFile extends AbstractIntellijResource implements IFile {
  private static final Logger log = Logger.getLogger(IntellijFile.class);

  public IntellijFile(@NotNull IntellijReferencePoint referencePoint, @NotNull Path relativePath) {
    super(referencePoint, relativePath);
  }

  @Override
  public boolean exists() {
    if (!referencePoint.exists()) {
      return false;
    }

    VirtualFile virtualFile = getVirtualFile();

    return existsInternal(virtualFile);
  }

  /**
   * Returns whether the given virtual file is not <code>null</code>, exists, and is a file.
   *
   * @param virtualFile the virtual file to check
   * @return whether the given virtual file is not <code>null</code>, exists, and is a file
   */
  private static boolean existsInternal(@Nullable VirtualFile virtualFile) {
    return virtualFile != null && virtualFile.exists() && !virtualFile.isDirectory();
  }

  @Override
  public void delete() throws IOException {
    FilesystemRunner.runWriteAction(
        (ThrowableComputable<Void, IOException>)
            () -> {
              deleteInternal();

              return null;
            },
        ModalityState.defaultModalityState());
  }

  /**
   * Deletes the file in the filesystem.
   *
   * @throws IOException if the resource is a directory or the deletion failed
   * @see VirtualFile#delete(Object)
   */
  private void deleteInternal() throws IOException {
    VirtualFile virtualFile = getVirtualFile();

    if (virtualFile == null || !virtualFile.exists()) {
      log.debug("Ignoring file deletion request for " + this + " as it does not exist.");

      return;
    }

    if (virtualFile.isDirectory()) {
      throw new IOException("Failed to delete " + this + " as it is not a file");
    }

    virtualFile.delete(this);
  }

  @Override
  @Nullable
  public String getCharset() throws IOException {
    VirtualFile virtualFile = getVirtualFile();

    if (!existsInternal(virtualFile)) {
      throw new FileNotFoundException(
          "Could not obtain charset for " + this + " as it does not exist or is not valid");
    }

    return virtualFile.getCharset().name();
  }

  @Override
  public void setCharset(@Nullable String charset) throws IOException {
    if (charset == null) {
      return;
    }

    VirtualFile virtualFile = getVirtualFile();

    if (!existsInternal(virtualFile)) {
      throw new FileNotFoundException(
          "Could not set charset for " + this + " as it does not exist or is not valid");
    }

    virtualFile.setCharset(Charset.forName(charset));
  }

  @Override
  @NotNull
  public InputStream getContents() throws IOException {
    VirtualFile virtualFile = getVirtualFile();

    if (!existsInternal(virtualFile)) {
      throw new FileNotFoundException(
          "Could not obtain contents for " + this + " as it does not exist or is not valid");
    }

    return virtualFile.getInputStream();
  }

  @Override
  public void setContents(@Nullable InputStream input) throws IOException {
    FilesystemRunner.runWriteAction(
        (ThrowableComputable<Void, IOException>)
            () -> {
              setContentsInternal(input);

              return null;
            },
        ModalityState.defaultModalityState());
  }

  /**
   * Sets the content of the file.
   *
   * @param input an input stream to write into the file
   * @throws IOException if the file does not exist or could not be written to
   * @see IOUtils#copy(InputStream, OutputStream)
   */
  private void setContentsInternal(@Nullable InputStream input) throws IOException {
    VirtualFile virtualFile = getVirtualFile();

    if (!existsInternal(virtualFile)) {
      throw new FileNotFoundException(
          "Could not set contents of " + this + " as it does not exist or is not valid.");
    }

    try (InputStream in = input == null ? new ByteArrayInputStream(new byte[0]) : input;
        OutputStream out = virtualFile.getOutputStream(this)) {

      IOUtils.copy(in, out);
    }
  }

  @Override
  public void create(@Nullable InputStream input) throws IOException {
    FilesystemRunner.runWriteAction(
        (ThrowableComputable<Void, IOException>)
            () -> {
              createInternal(input);

              return null;
            },
        ModalityState.defaultModalityState());
  }

  /**
   * Creates this file in the local filesystem with the given content.
   *
   * @param input an input stream to write into the file
   * @throws FileAlreadyExistsException if a resource with the same name already exists
   * @throws FileNotFoundException if the parent directory of this file does not exist
   */
  private void createInternal(@Nullable InputStream input) throws IOException {
    IResource parent = getParent();

    VirtualFile parentFile = referencePoint.findVirtualFile(parent.getReferencePointRelativePath());

    if (parentFile == null || !parentFile.exists()) {
      throw new FileNotFoundException(
          "Could not create "
              + this
              + " as its parent folder "
              + parent
              + " does not exist or is not valid");
    }

    VirtualFile virtualFile = parentFile.findChild(getName());

    if (virtualFile != null && virtualFile.exists()) {
      throw new FileAlreadyExistsException(
          "Could not create "
              + this
              + " as a resource with the same name already exists: "
              + virtualFile);
    }

    parentFile.createChildData(this, getName());

    if (input != null) {
      setContents(input);
    }
  }

  @Override
  public long getSize() throws IOException {
    VirtualFile virtualFile = getVirtualFile();

    if (!existsInternal(virtualFile)) {
      throw new FileNotFoundException(
          "Could not obtain the size for " + this + " as it does not exist or is not valid");
    }

    return virtualFile.getLength();
  }
}
