package saros.intellij.filesystem;

import static saros.filesystem.IResource.Type.FOLDER;

import com.intellij.ide.highlighter.ProjectFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IResource;
import saros.intellij.editor.ProjectAPI;

public abstract class IntelliJResourceImpl implements IResource {

  @Override
  public boolean isIgnored() {
    IntelliJProjectImpl sarosProject = getProject().adaptTo(IntelliJProjectImpl.class);

    VirtualFile virtualFile = sarosProject.findVirtualFile(getProjectRelativePath());

    Module module = sarosProject.getModule();
    Project project = module.getProject();

    if (virtualFile == null) {
      return true;
    }

    return isGitConfig()
        || isExcluded(project, virtualFile)
        || isModuleFile(module, virtualFile)
        || isProjectConfig(project, virtualFile);
  }

  /**
   * Returns whether this resource is part of the git configuration directory.
   *
   * @return whether this resource is part of the git configuration directory
   */
  private boolean isGitConfig() {
    String path = getProjectRelativePath().toPortableString();

    return (path.startsWith(".git/")
        || path.contains("/.git/")
        || getType() == FOLDER && (path.endsWith("/.git") || path.equals(".git")));
  }

  /**
   * Returns whether the given virtual file is located under an excluded root in the given project.
   *
   * @param project the project to check for
   * @param virtualFile the virtual file to check
   * @return whether the given virtual file is located under an excluded root in the given project
   */
  private boolean isExcluded(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    return ProjectAPI.isExcluded(project, virtualFile);
  }

  /**
   * Returns whether the given virtual file is the module file for the shared module.
   *
   * @param module the shared module
   * @param virtualFile the virtual file to check
   * @return whether the given virtual file is the module file for the shared module
   */
  // TODO consider whether to check all module files for the project to ensure that it is always
  //  ignored, independently of where it is currently located
  private boolean isModuleFile(@NotNull Module module, @NotNull VirtualFile virtualFile) {
    if (virtualFile.isDirectory()) {
      return false;
    }

    return virtualFile.equals(module.getModuleFile());
  }

  /**
   * Returns whether the given virtual file is part of the project configuration.
   *
   * <p>On file-based projects, the configuration is held in a file with the extension {@link
   * ProjectFileType#DOT_DEFAULT_EXTENSION}. On directory-based projects, the configuration is held
   * in the <code>.idea</code> folder.
   *
   * @param project the project to check for
   * @param virtualFile the virtual file to check
   * @return whether the given virtual file is part of the project configuration
   */
  private boolean isProjectConfig(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    VirtualFile projectFile = project.getProjectFile();
    if (projectFile == null) {
      return false;
    }

    if (projectFile.getName().endsWith(ProjectFileType.DOT_DEFAULT_EXTENSION)) {
      if (virtualFile.isDirectory()) {
        return false;
      }

      return projectFile.equals(virtualFile);
    }

    VirtualFile projectConfigDir = projectFile.getParent();
    if (projectConfigDir == null) {
      return false;
    }

    Path projectConfigDirPath = Paths.get(projectConfigDir.getPath()).normalize();
    Path filePath = Paths.get(virtualFile.getPath()).normalize();

    return filePath.startsWith(projectConfigDirPath);
  }

  @Nullable
  @Override
  public <T extends IResource> T adaptTo(@NotNull Class<T> clazz) {
    return clazz.isInstance(this) ? clazz.cast(this) : null;
  }
}
