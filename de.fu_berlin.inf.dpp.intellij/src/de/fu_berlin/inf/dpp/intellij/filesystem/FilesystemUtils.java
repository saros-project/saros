package de.fu_berlin.inf.dpp.intellij.filesystem;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.annotations.Inject;

public class FilesystemUtils {

  @Inject private static Project project;

  static {
    SarosPluginContext.initComponent(new FilesystemUtils());
  }

  /**
   * Returns the virtual file for the given path belonging to this module.
   *
   * <p><b>Note:</b> This method can not return files for derived resources or resources belonging
   * to a sub-module.
   *
   * @param path relative path to the file
   * @return the virtual file or <code>null</code> if it does not exists in the VFS snapshot, is
   *     derived, belongs to a sub-module, or the given path is absolute.
   */
  @Nullable
  public static VirtualFile findVirtualFile(VirtualFile contentRoot, @NotNull IPath path) {

    Module module = ModuleUtil.findModuleForFile(contentRoot, project);

    if (path.isAbsolute()) return null;

    if (path.segmentCount() == 0) return contentRoot;

    VirtualFile virtualFile = contentRoot.findFileByRelativePath(path.toString());

    if (virtualFile != null
        && ModuleRootManager.getInstance(module).getFileIndex().isInContent(virtualFile)) {
      return virtualFile;
    }

    return null;
  }

  public static Module getModuleOfFile(@NotNull VirtualFile resource) {
    return ModuleUtil.findModuleForFile(resource, project);
  }

  /**
   * Returns the content root of the given module.
   *
   * <p>This method is used to enforce the current restriction that shared modules must contain
   * exactly one content root.
   *
   * @param module the module to get the content root for
   * @return the content root of the given module
   * @throws IllegalArgumentException if the given module does not have exactly one content root
   */
  @NotNull
  public static VirtualFile getModuleContentRoot(@NotNull Module module) {

    ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

    VirtualFile[] contentRoots = moduleRootManager.getContentRoots();

    int numberOfContentRoots = contentRoots.length;

    if (numberOfContentRoots != 1) {
      throw new IllegalArgumentException(
          "Modules shared with Saros currently must contain exactly one "
              + "content root. The given module "
              + module
              + " has "
              + numberOfContentRoots
              + " content roots: "
              + Arrays.toString(contentRoots));
    }

    return contentRoots[0];
  }
}
