package de.fu_berlin.inf.dpp.intellij.filesystem;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJPathImpl;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class IntelliJFileImpl extends IntelliJResourceImpl implements IFile {

  private static final int BUFFER_SIZE = 32 * 1024;

  /** Relative path from the given project */
  private final IPath path;

  private VirtualFile srcRoot;

  public IntelliJFileImpl(VirtualFile srcRoot, IPath relPath) {
    this.srcRoot = srcRoot;
    this.path = relPath;
  }

  /**
   * Returns whether this file exists.
   *
   * <p><b>Note:</b> A derived file is treated as being nonexistent.
   *
   * @return <code>true</code> if the resource exists, is a file, and is not derived, <code>false
   *     </code> otherwise
   */
  @Override
  public boolean exists() {
    final VirtualFile file = FilesystemUtils.findVirtualFile(srcRoot, path);

    return file != null && file.exists() && !file.isDirectory();
  }

  @NotNull
  @Override
  public IPath getFullPath() {
    IPath rootPath =
        IntelliJPathImpl.fromString(FilesystemUtils.getModuleOfFile(srcRoot).getName());
    return rootPath.append(path);
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
   * @return an <code>IFolder</code> object for the parent of this file
   */
  @NotNull
  @Override
  public IFolder getParent() {
    if (path.segmentCount() == 1) return new IntelliJProjectImpl(srcRoot);

    return new IntelliJFolderImpl(srcRoot, path.removeLastSegments(1));
  }

  @NotNull
  @Override
  public IFolder getReferenceFolder() {
    return new IntelliJProjectImpl(srcRoot);
  }

  @NotNull
  @Override
  public IPath getProjectRelativePath() {
    return path;
  }

  @Override
  public int getType() {
    return IResource.FILE;
  }

  @Override
  public boolean isDerived(final boolean checkAncestors) {
    return isDerived();
  }

  @Override
  public boolean isDerived() {
    return !exists();
  }

  @Override
  public void delete(final int updateFlags) throws IOException {

    Filesystem.runWriteAction(
        new ThrowableComputable<Void, IOException>() {

          @Override
          public Void compute() throws IOException {

            final VirtualFile file = FilesystemUtils.findVirtualFile(srcRoot, path);

            if (file == null)
              throw new FileNotFoundException(
                  IntelliJFileImpl.this + " does not exist or is derived");

            if (file.isDirectory()) throw new IOException(this + " is not a file");

            file.delete(IntelliJFileImpl.this);

            return null;
          }
        },
        ModalityState.defaultModalityState());
  }

  @Override
  public void move(@NotNull final IPath destination, final boolean force) throws IOException {
    throw new IOException("NYI");
  }

  @NotNull
  @Override
  public IPath getLocation() {
    // TODO might return a wrong location
    return IntelliJPathImpl.fromString(srcRoot.getPath()).append(path);
  }

  @Nullable
  @Override
  public String getCharset() throws IOException {
    final VirtualFile file = FilesystemUtils.findVirtualFile(srcRoot, path);

    return file == null ? null : file.getCharset().name();
  }

  @NotNull
  @Override
  public InputStream getContents() throws IOException {
    /*
     * TODO maybe this needs to be wrapped, the core logic assumes that it
     * can read from any thread
     */

    final VirtualFile file = FilesystemUtils.findVirtualFile(srcRoot, path);

    if (file == null) throw new FileNotFoundException(this + " does not exist or is " + "derived");

    return file.getInputStream();
  }

  /**
   * Sets the content of this file in the local filesystem.
   *
   * <p><b>Note:</b> The force flag is not supported.
   *
   * @param input new contents of the file
   * @param force not supported
   * @param keepHistory not supported
   * @throws IOException if the file does not exist
   */
  @Override
  public void setContents(final InputStream input, final boolean force, final boolean keepHistory)
      throws IOException {

    Filesystem.runWriteAction(
        new ThrowableComputable<Void, IOException>() {

          @Override
          public Void compute() throws IOException {

            final VirtualFile file = FilesystemUtils.findVirtualFile(srcRoot, path);

            if (file == null) {
              String exceptionText = IntelliJFileImpl.this + " does not exist or is derived";

              if (force) exceptionText += ", force option is not supported";

              throw new FileNotFoundException(exceptionText);
            }

            final OutputStream out = file.getOutputStream(IntelliJFileImpl.this);

            final InputStream in = input == null ? new ByteArrayInputStream(new byte[0]) : input;

            final byte[] buffer = new byte[BUFFER_SIZE];

            int read = 0;

            try {

              while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);

            } finally {
              IOUtils.closeQuietly(out);
              IOUtils.closeQuietly(in);
            }

            return null;
          }
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
   * @param force not supported
   * @throws FileAlreadyExistsException if the file already exists
   * @throws FileNotFoundException if the parent directory of this file does not exist
   */
  @Override
  public void create(@Nullable final InputStream input, final boolean force) throws IOException {

    Filesystem.runWriteAction(
        new ThrowableComputable<Void, IOException>() {

          @Override
          public Void compute() throws IOException {

            final IResource parent = getParent();

            final VirtualFile parentFile =
                FilesystemUtils.findVirtualFile(srcRoot, parent.getProjectRelativePath());

            if (parentFile == null)
              throw new FileNotFoundException(
                  parent
                      + " does not exist or is derived, cannot create file "
                      + IntelliJFileImpl.this);

            final VirtualFile file = parentFile.findChild(getName());

            if (file != null) {
              String exceptionText = IntelliJFileImpl.this + " already exists";

              if (force) exceptionText += ", force option is not supported";

              throw new FileAlreadyExistsException(exceptionText);
            }

            parentFile.createChildData(IntelliJFileImpl.this, getName());

            if (input != null) setContents(input, force, true);

            return null;
          }
        },
        ModalityState.defaultModalityState());
  }

  @Override
  public long getSize() throws IOException {
    final VirtualFile file = FilesystemUtils.findVirtualFile(srcRoot, path);

    return file == null ? 0L : file.getLength();
  }

  @Override
  public int hashCode() {
    return FilesystemUtils.getModuleOfFile(srcRoot).getName().hashCode() + 31 * path.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {

    if (this == obj) return true;

    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;

    IntelliJFileImpl other = (IntelliJFileImpl) obj;

    return srcRoot.equals(other.srcRoot) && path.equals(other.path);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " : " + path + " - " + srcRoot;
  }

  @Override
  public IReferencePoint getReferencePoint() {
    return IntelliJReferencePointManager.create(FilesystemUtils.getModuleOfFile(srcRoot));
  }
}
