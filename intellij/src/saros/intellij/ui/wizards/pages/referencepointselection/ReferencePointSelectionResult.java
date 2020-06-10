package saros.intellij.ui.wizards.pages.referencepointselection;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Data holder class representing the input of a {@link ReferencePointTab}. */
public class ReferencePointSelectionResult {
  private final LocalRepresentationOption localRepresentationOption;
  private final Project project;
  private final String newDirectoryName;
  private final VirtualFile newDirectoryBaseDirectory;
  private final VirtualFile existingDirectory;

  /**
   * Initializes a ReferencePointSelectionResult object.
   *
   * <p>Depending on the chosen sharing mode, different values can be null.
   *
   * @param localRepresentationOption the mode that was chosen
   * @param project the project to use
   * @param newDirectoryName the chose name for the new directory to create
   * @param newDirectoryBaseDirectory the chose base directory of the new directory to create
   * @param existingDirectory the existing directory to use
   */
  ReferencePointSelectionResult(
      @NotNull LocalRepresentationOption localRepresentationOption,
      @NotNull Project project,
      @Nullable String newDirectoryName,
      @Nullable VirtualFile newDirectoryBaseDirectory,
      @Nullable VirtualFile existingDirectory) {

    this.localRepresentationOption = localRepresentationOption;
    this.project = project;
    this.newDirectoryName = newDirectoryName;
    this.newDirectoryBaseDirectory = newDirectoryBaseDirectory;
    this.existingDirectory = existingDirectory;
  }

  /**
   * Returns the chosen mode of how to locally represent the shared reference point.
   *
   * @return the chosen mode of how to locally represent the shared reference point
   */
  @NotNull
  public LocalRepresentationOption getLocalRepresentationOption() {
    return localRepresentationOption;
  }

  /**
   * Returns the chosen project to use for the shared reference point.
   *
   * @return the chosen project to use for the shared reference point
   */
  @NotNull
  public Project getProject() {
    return project;
  }

  /**
   * Returns the chosen name to use when creating the new directory for the session. This value can
   * be <code>null</code> if the mode {@link LocalRepresentationOption#USE_EXISTING_DIRECTORY} is
   * chosen.
   *
   * @return the chosen name to use when creating the new directory for the session
   */
  @Nullable
  public String getNewDirectoryName() {
    return newDirectoryName;
  }

  /**
   * Returns the chosen virtual file representing the base directory to use when creating the new
   * directory for the session. This value can be <code>null</code> if the mode {@link
   * LocalRepresentationOption#USE_EXISTING_DIRECTORY} is chosen.
   *
   * @return the chosen virtual file representing the base directory to use when creating the new
   *     directory for the session
   */
  @Nullable
  public VirtualFile getNewDirectoryBaseDirectory() {
    return newDirectoryBaseDirectory;
  }

  /**
   * Returns the chosen existing virtual file representing the directory to use for the session.
   * This value can be <code>null</code> if the mode {@link
   * LocalRepresentationOption#CREATE_NEW_DIRECTORY} is chosen.
   *
   * @return the chosen existing virtual file to use for the session
   */
  @Nullable
  public VirtualFile getExistingDirectory() {
    return existingDirectory;
  }
}
