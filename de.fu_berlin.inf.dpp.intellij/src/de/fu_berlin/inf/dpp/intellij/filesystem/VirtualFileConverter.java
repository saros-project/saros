package de.fu_berlin.inf.dpp.intellij.filesystem;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.annotations.Inject;

/**
 * Provides static methods to convert VirtualFiles to Saros resource objects or Saros resources
 * objects to VirtualFiles.
 */
public class VirtualFileConverter {

  private static final Logger log = Logger.getLogger(VirtualFileConverter.class);

  @Inject private static Project project;

  static {
    SarosPluginContext.initComponent(new VirtualFileConverter());
  }

  private VirtualFileConverter() {
    // NOP
  }

  /**
   * Returns an <code>SPath</code> representing the given file.
   *
   * @param virtualFile file to get the <code>SPath</code> for
   * @return an <code>SPath</code> representing the given file or <code>null</code> if given file
   *     does not exist, no module could be found for the file or the found module can not be shared
   *     through saros, or the relative path between the module root and the file could not be
   *     constructed
   */
  @Nullable
  public static SPath convertToSPath(@NotNull VirtualFile virtualFile) {

    IResource resource = convertToResource(virtualFile);

    return resource == null ? null : new SPath(resource);
  }

  /**
   * Returns an <code>IResource</code> representing the given <code>VirtualFile</code>.
   *
   * @param virtualFile file to get the <code>IResource</code> for
   * @return an <code>IResource</code> representing the given file or <code>null</code> if given
   *     file does not exist, no module could be found for the file or the found module can not be
   *     shared through saros, or the relative path between the module root and the file could not
   *     be constructed
   */
  @Nullable
  public static IResource convertToResource(@NotNull VirtualFile virtualFile) {

    Module module = ModuleUtil.findModuleForFile(virtualFile, project);

    if (module == null) {
      log.debug(
          "Could not convert VirtualFile "
              + virtualFile
              + " as no module could be found for the file.");

      return null;
    }

    try {
      IntelliJProjectImpl wrappedModule = new IntelliJProjectImpl(module);

      return wrappedModule.getResource(virtualFile);

    } catch (IllegalArgumentException e) {
      if (log.isTraceEnabled()) {
        log.trace(
            "Could not convert VirtualFile "
                + virtualFile
                + " as the module for the resource does not comply with the current restrictions.");
      }

      return null;

    } catch (IllegalStateException e) {
      log.warn(
          "Could not convert VirtualFile "
              + virtualFile
              + " as the creation of an IProject object for its module "
              + module
              + " failed.",
          e);

      return null;
    }
  }

  /**
   * Returns an <code>IResource</code> representing the given <code>VirtualFile</code>.
   *
   * @param virtualFile file to get the <code>IResource</code> for
   * @param project module the file belongs to
   * @return an <code>IResource</code> for the given file or <code>null</code> if the given file
   *     does not exist, does not belong to the passed module, or the relative path path between the
   *     module root and the file could not be constructed
   */
  @Nullable
  public static IResource convertToResource(
      @NotNull VirtualFile virtualFile, @NotNull IProject project) {

    IntelliJProjectImpl wrappedModule = project.getAdapter(IntelliJProjectImpl.class);

    return wrappedModule.getResource(virtualFile);
  }

  /**
   * Returns a <code>VirtualFile</code> for the given resource.
   *
   * @param path the SPath representing the resource to get a VirtualFile for
   * @return a VirtualFile for the given resource or <code>null</code> if the given resource does
   *     not exists in the VFS snapshot, is derived, or belongs to a sub-module
   */
  @Nullable
  public static VirtualFile convertToVirtualFile(@NotNull SPath path) {

    IResource resource = path.getResource();

    if (resource == null) {
      return null;
    }

    return convertToVirtualFile(resource);
  }

  /**
   * Returns a <code>VirtualFile</code> for the given resource.
   *
   * @param resource the resource to get a VirtualFile for
   * @return a VirtualFile for the given resource or <code>null</code> if the given resource does
   *     not exists in the VFS snapshot, is derived, or belongs to a sub-module
   */
  @Nullable
  public static VirtualFile convertToVirtualFile(@NotNull IResource resource) {

    if (resource instanceof IProject) {
      throw new IllegalArgumentException(
          "The given resource must be a file or a folder. resource: " + resource);
    }

    IntelliJProjectImpl wrappedModule = resource.getProject().getAdapter(IntelliJProjectImpl.class);

    return wrappedModule.findVirtualFile(resource.getProjectRelativePath());
  }
}
