package saros.intellij.filesystem;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IProject;
import saros.filesystem.IResource;

/**
 * Provides static methods to convert VirtualFiles to Saros resource objects or Saros resources
 * objects to VirtualFiles.
 */
public class VirtualFileConverter {

  private VirtualFileConverter() {
    // NOP
  }

  /**
   * Returns an <code>IResource</code> representing the given <code>VirtualFile</code>. Uses the
   * given reference points to try to obtain a resource for the virtual file.
   *
   * <p>If the given virtual file is represented by one of the given reference points, the reference
   * point object is returned.
   *
   * @param referencePoints the list of shared reference points
   * @param virtualFile the virtual file get an <code>IResource</code> object for
   * @return an <code>IResource</code> representing the given <code>VirtualFile</code> or <code>null
   *     </code> if no such representation could be obtained from the given reference points
   */
  @Nullable
  public static IResource convertToResource(
      @NotNull Collection<IProject> referencePoints, @NotNull VirtualFile virtualFile) {

    for (IProject referencePoint : referencePoints) {
      IResource resource = convertToResource(virtualFile, referencePoint);

      if (resource != null) {
        return resource;
      }
    }

    return null;
  }

  /**
   * Returns an <code>IResource</code> representing the given <code>VirtualFile</code>.
   *
   * <p>If the given virtual file is represented by the given reference point, the reference point
   * object is returned.
   *
   * @param virtualFile file to get the <code>IResource</code> for
   * @param referencePoint reference point the file belongs to
   * @return an <code>IResource</code> for the given file or <code>null</code> if the given file
   *     does not exist or the relative path between the reference point and the file could not be
   *     constructed
   */
  @Nullable
  public static IResource convertToResource(
      @NotNull VirtualFile virtualFile, @NotNull IProject referencePoint) {

    IntellijReferencePointImpl intellijReferencePoint = (IntellijReferencePointImpl) referencePoint;

    if (virtualFile.equals(intellijReferencePoint.getVirtualFile())) {
      return referencePoint;
    }

    return intellijReferencePoint.getResource(virtualFile);
  }

  /**
   * Returns a <code>VirtualFile</code> for the given resource.
   *
   * @param resource the resource to get a VirtualFile for
   * @return a VirtualFile for the given resource or <code>null</code> if the given resource could
   *     not be found in the current VFS snapshot
   */
  @Nullable
  public static VirtualFile convertToVirtualFile(@NotNull IResource resource) {
    if (resource instanceof IProject) {
      throw new IllegalArgumentException(
          "The given resource must be a file or a folder. resource: " + resource);
    }

    return ((IntellijResourceImplV2) resource).getVirtualFile();
  }
}
