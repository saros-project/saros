package saros.ui.widgets.wizard;

import java.util.Objects;
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
}
