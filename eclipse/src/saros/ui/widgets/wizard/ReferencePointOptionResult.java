package saros.ui.widgets.wizard;

import static saros.ui.widgets.wizard.ReferencePointOptionComposite.LocalRepresentationOption.EXISTING_DIRECTORY;
import static saros.ui.widgets.wizard.ReferencePointOptionComposite.LocalRepresentationOption.NEW_DIRECTORY;
import static saros.ui.widgets.wizard.ReferencePointOptionComposite.LocalRepresentationOption.NEW_PROJECT;

import java.text.MessageFormat;
import java.util.Objects;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import saros.exception.IllegalInputException;
import saros.ui.Messages;
import saros.ui.widgets.wizard.ReferencePointOptionComposite.LocalRepresentationOption;

/** Data holder class for the result of a {@link ReferencePointOptionComposite}. */
public class ReferencePointOptionResult {
  private final LocalRepresentationOption localRepresentationOption;
  private final String newProjectName;
  private final String newDirectoryName;
  private final String newDirectoryBase;
  private final String existingDirectory;

  /**
   * Instantiates a new instance of the data holder class.
   *
   * <p>Depending on the local representation option, different given values are allowed to be
   * <code>null</code>
   *
   * @param localRepresentationOption the representation option that was chosen
   * @param newProjectName the name of the new project to create
   * @param newDirectoryName the name of the new directory to create
   * @param newDirectoryBase the bas path of the new directory to create
   * @param existingDirectory the existing directory to use
   */
  public ReferencePointOptionResult(
      LocalRepresentationOption localRepresentationOption,
      String newProjectName,
      String newDirectoryName,
      String newDirectoryBase,
      String existingDirectory) {

    Objects.requireNonNull(
        localRepresentationOption, "The given representation option must not be null");

    switch (localRepresentationOption) {
      case NEW_PROJECT:
        Objects.requireNonNull(
            newProjectName,
            "The new project name must not be null if the option to create a new project is chosen");

        break;

      case NEW_DIRECTORY:
        Objects.requireNonNull(
            newDirectoryName,
            "The new directory name must not be null if the option to create a new directory is chosen");

        Objects.requireNonNull(
            newDirectoryBase,
            "The new directory base path must not be null if the option to create a new directory is chosen");

        break;

      case EXISTING_DIRECTORY:
        Objects.requireNonNull(
            existingDirectory,
            "The existing directory must not be null if the option to use it is chosen");

        break;

      default:
        throw new IllegalStateException(
            "Encountered unknown local representation option " + localRepresentationOption);
    }

    this.localRepresentationOption = localRepresentationOption;
    this.newProjectName = newProjectName;
    this.newDirectoryName = newDirectoryName;
    this.newDirectoryBase = newDirectoryBase;
    this.existingDirectory = existingDirectory;
  }

  /**
   * Returns the chosen option on how to represent the reference point in the local workspace.
   *
   * @return the chosen option on how to represent the reference point in the local workspace
   */
  public LocalRepresentationOption getLocalRepresentationOption() {
    return localRepresentationOption;
  }

  /**
   * Returns the name of the new project to create as part of the resource negotiation to represent
   * the reference point.
   *
   * <p>The return value is undefined if the selected local representation option is not {@link
   * LocalRepresentationOption#NEW_PROJECT}.
   *
   * @return the name of the new project to create as part of the resource negotiation to represent
   *     the reference point
   */
  public String getNewProjectName() {
    return newProjectName;
  }

  /**
   * Returns the name of the new directory to create as part of the resource negotiation to
   * represent the reference point.
   *
   * <p>The return value is undefined if the selected local representation option is not {@link
   * LocalRepresentationOption#NEW_DIRECTORY}.
   *
   * @return the name of the new directory to create as part of the resource negotiation to
   *     represent the reference point
   */
  public String getNewDirectoryName() {
    return newDirectoryName;
  }

  /**
   * Returns the base path of the new directory to create as part of the resource negotiation to
   * represent the reference point.
   *
   * <p>The return value is undefined if the selected local representation option is not {@link
   * LocalRepresentationOption#NEW_DIRECTORY}.
   *
   * @return the base path of the new directory to create as part of the resource negotiation to
   *     represent the reference point
   */
  public String getNewDirectoryBase() {
    return newDirectoryBase;
  }

  /**
   * Returns the existing directory to use to represent the reference point.
   *
   * <p>The return value is undefined if the selected local representation option is not {@link
   * LocalRepresentationOption#EXISTING_DIRECTORY}.
   *
   * @return the existing directory to use to represent the reference point
   */
  public String getExistingDirectory() {
    return existingDirectory;
  }

  /**
   * Returns the container selected in this reference point option result.
   *
   * <p>Trims leading and trailing spaces from the input values before processing them.
   *
   * @param referencePointName the name of the reference point the result is belongs to
   * @return the container selected in this reference point option result
   * @throws IllegalInputException if the input contained in this result is not valid
   * @see #getNewProjectHandle(String, String)
   * @see #getNewDirectoryHandle(String, String, String)
   * @see #getExistingDirectoryHandle(String, String)
   */
  public IContainer getSelectedContainerHandle(String referencePointName)
      throws IllegalInputException {

    LocalRepresentationOption localRepresentationOption = getLocalRepresentationOption();

    if (localRepresentationOption == NEW_PROJECT) {
      String newProjectName = getNewProjectName().trim();

      return getNewProjectHandle(newProjectName, referencePointName);

    } else if (localRepresentationOption == NEW_DIRECTORY) {
      String newDirectoryName = getNewDirectoryName().trim();
      String newDirectoryBasePath = getNewDirectoryBase().trim();

      return getNewDirectoryHandle(newDirectoryName, newDirectoryBasePath, referencePointName);

    } else if (localRepresentationOption == EXISTING_DIRECTORY) {
      String existingDirectoryPath = getExistingDirectory().trim();

      return getExistingDirectoryHandle(existingDirectoryPath, referencePointName);

    } else {
      throw new IllegalStateException(
          "Encountered unknown local representation option for shared reference point '"
              + referencePointName
              + "': "
              + localRepresentationOption);
    }
  }

  /**
   * Returns a handle for the new project for the given name.
   *
   * @param newProjectName the name for the new project
   * @param referencePointName the name of the reference point the result is belongs to
   * @return a handle for the new project for the given name
   * @throws IllegalInputException if the given project name is not valid or a project with the
   *     given name already exists
   */
  private IContainer getNewProjectHandle(String newProjectName, String referencePointName)
      throws IllegalInputException {

    if (newProjectName == null || newProjectName.isEmpty()) {
      throw new IllegalInputException(
          MessageFormat.format(
              Messages.ReferencePointOptionResult_error_new_project_no_name, referencePointName));
    }

    IStatus status = ResourcesPlugin.getWorkspace().validateName(newProjectName, IResource.PROJECT);

    if (!status.isOK()) {
      throw new IllegalInputException(
          MessageFormat.format(
              Messages.ReferencePointOptionResult_error_new_project_invalid_name,
              referencePointName,
              status.getMessage()));
    }

    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(newProjectName);

    if (project.exists()) {
      throw new IllegalInputException(
          MessageFormat.format(
              Messages.ReferencePointOptionResult_error_new_project_already_exists,
              referencePointName,
              newProjectName));
    }

    return project;
  }

  /**
   * Returns a handle for the new directory for the given name and base path.
   *
   * @param newDirectoryName the name for the new directory
   * @param newDirectoryBasePath the base path for the new directory
   * @param referencePointName the name of the reference point the result is belongs to
   * @return a handle for the new directory for the given name and base path
   * @throws IllegalInputException if the given directory name or base path is not valid, the given
   *     base path does not point to an existing directory, or a file or directory with the given
   *     name and base path already exists
   */
  private IContainer getNewDirectoryHandle(
      String newDirectoryName, String newDirectoryBasePath, String referencePointName)
      throws IllegalInputException {

    if (newDirectoryName == null
        || newDirectoryName.isEmpty()
        || newDirectoryBasePath == null
        || newDirectoryBasePath.isEmpty()) {

      throw new IllegalInputException(
          MessageFormat.format(
              Messages.ReferencePointOptionResult_error_new_directory_no_name_or_path,
              referencePointName));
    }

    IContainer baseContainer = getContainerForPath(newDirectoryBasePath);

    if (baseContainer == null || !baseContainer.exists()) {
      throw new IllegalInputException(
          MessageFormat.format(
              Messages.ReferencePointOptionResult_error_new_directory_base_does_not_exist,
              referencePointName));
    }

    if (ResourcesPlugin.getWorkspace().validateName(newDirectoryName, IResource.FILE).isOK()
        && baseContainer.getFile(new Path(newDirectoryBasePath)).exists()) {

      throw new IllegalInputException(
          MessageFormat.format(
              Messages.ReferencePointOptionResult_error_new_directory_already_exists_as_file,
              referencePointName));
    }

    IStatus status =
        ResourcesPlugin.getWorkspace().validateName(newDirectoryName, IResource.FOLDER);

    if (!status.isOK()) {
      throw new IllegalInputException(
          MessageFormat.format(
              Messages.ReferencePointOptionResult_error_new_directory_invalid_name,
              referencePointName,
              status.getMessage()));
    }

    IContainer containerToCreate = baseContainer.getFolder(new Path(newDirectoryName));

    if (containerToCreate.exists()) {

      throw new IllegalInputException(
          MessageFormat.format(
              Messages.ReferencePointOptionResult_error_new_directory_already_exists,
              containerToCreate.getFullPath().toOSString()));
    }

    return containerToCreate;
  }

  /**
   * Returns a handle for the existing directory for the given path.
   *
   * @param existingDirectoryPath the path for the existing directory
   * @return a handle for the existing directory for the given path
   * @throws IllegalInputException if the given path is not valid or does not point to an existing
   *     directory
   */
  private IContainer getExistingDirectoryHandle(
      String existingDirectoryPath, String referencePointName) throws IllegalInputException {

    if (existingDirectoryPath == null || existingDirectoryPath.isEmpty()) {
      throw new IllegalInputException(
          MessageFormat.format(
              Messages.ReferencePointOptionResult_error_existing_directory_no_path,
              referencePointName));
    }

    IContainer existingContainer = getContainerForPath(existingDirectoryPath);

    if (existingContainer == null) {
      throw new IllegalInputException(
          MessageFormat.format(
              Messages.ReferencePointOptionResult_error_existing_directory_invalid_path,
              referencePointName));
    }

    if (!existingContainer.exists()) {
      throw new IllegalInputException(
          MessageFormat.format(
              Messages.ReferencePointOptionResult_error_existing_directory_does_not_exist,
              referencePointName));
    }

    return existingContainer;
  }

  /**
   * Returns a container handle for the given path.
   *
   * @param pathString the path to get a container handle for
   * @return a container handle for the given path or <code>null</code> if the given path is not
   *     valid
   */
  public static IContainer getContainerForPath(String pathString) {
    if (pathString == null || pathString.isEmpty()) {
      return null;
    }

    if (!isValidPath(pathString)) {
      return null;
    }

    IPath fullBasePath = new Path(pathString);

    if (fullBasePath.isEmpty()) {
      return null;
    }

    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(fullBasePath.segment(0));

    IPath projectRelativePath = fullBasePath.removeFirstSegments(1);

    if (projectRelativePath.isEmpty()) {
      return project;

    } else {

      return project.getFolder(projectRelativePath);
    }
  }

  /**
   * Returns whether the given string represents a valid path.
   *
   * @param pathString the path string to check
   * @return whether the given string represents a valid path
   */
  public static boolean isValidPath(String pathString) {
    // this handling is very weird; Eclipse only uses base object as access to internal constants
    return Path.EMPTY.isValidPath(pathString);
  }
}
