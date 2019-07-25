package saros.intellij.ui.wizards.pages.moduleselection;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Data holder class representing the input of a {@link ModuleTab}. */
public class ModuleSelectionResult {
  private final LocalRepresentationOption localRepresentationOption;
  private final Project project;
  private final String newModuleName;
  private final Path newModuleBasePath;
  private final Module existingModule;

  /**
   * Initializes a ModuleSelectionResult object.
   *
   * <p>Depending of the chose sharing mode, different values can be null.
   *
   * @param localRepresentationOption the mode that was chosen
   * @param project the project to use
   * @param newModuleName the chose name for the new module to create
   * @param newModuleBasePath the chose base path of the new module to create
   * @param existingModule the existing module to use
   */
  ModuleSelectionResult(
      @NotNull LocalRepresentationOption localRepresentationOption,
      @NotNull Project project,
      @Nullable String newModuleName,
      @Nullable Path newModuleBasePath,
      @Nullable Module existingModule) {

    this.localRepresentationOption = localRepresentationOption;
    this.project = project;
    this.newModuleName = newModuleName;
    this.newModuleBasePath = newModuleBasePath;
    this.existingModule = existingModule;
  }

  /**
   * Returns the chosen mode of how to locally represent the shared module.
   *
   * @return the chosen mode of how to locally represent the shared module
   */
  @NotNull
  public LocalRepresentationOption getLocalRepresentationOption() {
    return localRepresentationOption;
  }

  /**
   * Returns the chosen project to use for the shared module.
   *
   * @return the chosen project to use for the shared module
   */
  @NotNull
  public Project getProject() {
    return project;
  }

  /**
   * Returns the chosen name to use when creating the new module for the session. This value can be
   * <code>null
   * </code> if the mode {@link LocalRepresentationOption#USE_EXISTING_MODULE} is chosen.
   *
   * @return the chosen name to use when creating the new module for the session
   */
  @Nullable
  public String getNewModuleName() {
    return newModuleName;
  }

  /**
   * Returns the chosen base path to use when creating the new module for the session. This value
   * can be <code>null
   * </code> if the mode {@link LocalRepresentationOption#USE_EXISTING_MODULE} is chosen.
   *
   * @return the chosen base path to use when creating the new module for the session
   */
  @Nullable
  public Path getNewModuleBasePath() {
    return newModuleBasePath;
  }

  /**
   * Returns the chosen existing module to use for the session. This value can be <code>null</code>
   * if the mode {@link LocalRepresentationOption#CREATE_NEW_MODULE} is chosen.
   *
   * @return the chosen existing module to use for the session
   */
  @Nullable
  public Module getExistingModule() {
    return existingModule;
  }
}
