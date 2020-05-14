package saros.intellij.filesystem;

import static saros.filesystem.IResource.Type.FILE;

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
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IContainer;
import saros.filesystem.IFile;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.intellij.runtime.FilesystemRunner;

/** @deprecated use {@link IntellijFileImplV2} instead */
@Deprecated
public final class IntelliJFileImpl extends IntelliJResourceImpl implements IFile {
  private static final Logger log = Logger.getLogger(IntelliJFileImpl.class);

  private static final int BUFFER_SIZE = 32 * 1024;

  /** Relative path from the given project */
  private final IPath path;

  private final IntelliJProjectImpl project;

  public IntelliJFileImpl(@NotNull final IntelliJProjectImpl project, @NotNull final IPath path) {
    this.project = project;
    this.path = path;
  }

  /**
   * Returns whether this file exists.
   *
   * <p><b>Note:</b> An ignored file is treated as being nonexistent.
   *
   * @return <code>true</code> if the resource exists, is a file, and is not ignored, <code>false
   *     </code> otherwise
   * @see #isIgnored()
   */
  @Override
  public boolean exists() {
    final VirtualFile file = project.findVirtualFile(path);

    return file != null && file.exists() && !file.isDirectory();
  }

  @NotNull
  @Override
  public String getName() {
    return path.lastSegment();
  }

  /**
   * Returns the parent of this file.
   *
   * <p><b>Note:</b> The interface specification for this method does allow <code>null</code> as a
   * return value. This implementation, however, can not return null values, as suggested by its
   * NotNull tag.
   *
   * @return an <code>IContainer</code> object for the parent of this file
   */
  @NotNull
  @Override
  public IContainer getParent() {
    if (path.segmentCount() == 1) return project;

    return new IntelliJFolderImpl(project, path.removeLastSegments(1));
  }

  @NotNull
  @Override
  public IProject getProject() {
    return project;
  }

  @NotNull
  @Override
  public IPath getProjectRelativePath() {
    return path;
  }

  @Override
  public Type getType() {
    return FILE;
  }

  @Override
  public void delete() throws IOException {

    FilesystemRunner.runWriteAction(
        new ThrowableComputable<Void, IOException>() {

          @Override
          public Void compute() throws IOException {

            final VirtualFile file = project.findVirtualFile(path);

            if (file == null) {
              log.debug("Ignoring file deletion request for " + this + " as file does not exist");

              return null;
            }

            if (file.isDirectory()) throw new IOException(this + " is not a file");

            file.delete(IntelliJFileImpl.this);

            return null;
          }
        },
        ModalityState.defaultModalityState());
  }

  @Nullable
  @Override
  public String getCharset() {
    final VirtualFile file = project.findVirtualFile(path);

    return file == null ? null : file.getCharset().name();
  }

  @Override
  public void setCharset(String charset) throws IOException {
    if (charset == null) {
      return;
    }

    final VirtualFile file = project.findVirtualFile(path);

    if (file == null) {
      throw new FileNotFoundException("Could not obtain virtual file for " + this);
    }

    file.setCharset(Charset.forName(charset));
  }

  @NotNull
  @Override
  public InputStream getContents() throws IOException {
    /*
     * TODO maybe this needs to be wrapped, the core logic assumes that it
     * can read from any thread
     */

    final VirtualFile file = project.findVirtualFile(path);

    if (file == null) throw new FileNotFoundException(this + " does not exist or is ignored");

    return file.getInputStream();
  }

  /**
   * Sets the content of this file in the local filesystem.
   *
   * <p><b>Note:</b> The force flag is not supported.
   *
   * @param input new contents of the file
   * @throws IOException if the file does not exist
   */
  @Override
  public void setContents(final InputStream input) throws IOException {

    FilesystemRunner.runWriteAction(
        (ThrowableComputable<Void, IOException>)
            () -> {
              final VirtualFile file = project.findVirtualFile(path);

              if (file == null) {
                throw new FileNotFoundException(
                    IntelliJFileImpl.this + " does not exist or is ignored");
              }

              final OutputStream out = file.getOutputStream(IntelliJFileImpl.this);

              final InputStream in = input == null ? new ByteArrayInputStream(new byte[0]) : input;

              final byte[] buffer = new byte[BUFFER_SIZE];

              int read;

              try {

                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);

              } finally {
                IOUtils.closeQuietly(out);
                IOUtils.closeQuietly(in);
              }

              return null;
            },
        ModalityState.defaultModalityState());
  }

  /**
   * Creates this file in the local filesystem with the given content.
   *
   * <p><b>Note:</b> The force flag is not supported. It does not allow the re-creation of an
   * already existing file.
   *
   * @param input contents of the new file
   * @throws FileAlreadyExistsException if the file already exists
   * @throws FileNotFoundException if the parent directory of this file does not exist
   */
  @Override
  public void create(@Nullable final InputStream input) throws IOException {

    FilesystemRunner.runWriteAction(
        (ThrowableComputable<Void, IOException>)
            () -> {
              final IResource parent = getParent();

              final VirtualFile parentFile =
                  project.findVirtualFile(parent.getProjectRelativePath());

              if (parentFile == null)
                throw new FileNotFoundException(
                    parent
                        + " does not exist or is ignored, cannot create file "
                        + IntelliJFileImpl.this);

              final VirtualFile file = parentFile.findChild(getName());

              if (file != null) {
                throw new FileAlreadyExistsException(IntelliJFileImpl.this + " already exists");
              }

              parentFile.createChildData(IntelliJFileImpl.this, getName());

              if (input != null) setContents(input);

              return null;
            },
        ModalityState.defaultModalityState());
  }

  @Override
  public long getSize() {
    final VirtualFile file = project.findVirtualFile(path);

    return file == null ? 0L : file.getLength();
  }

  @Override
  public int hashCode() {
    return project.hashCode() + 31 * path.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {

    if (this == obj) return true;

    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;

    IntelliJFileImpl other = (IntelliJFileImpl) obj;

    return project.equals(other.project) && path.equals(other.path);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " : " + path + " - " + project;
  }
}
