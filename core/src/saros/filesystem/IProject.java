package saros.filesystem;

import java.io.IOException;

/**
 * Represents a project in the local (virtual) file system.
 *
 * <p>Defines the project specific default behavior of methods defined by {@link IContainer}.
 *
 * <p>This interface is under development. It is currently a placeholder and will be reworked once
 * the migration to reference points is completed.
 */
public interface IProject extends IContainer {
  /**
   * Returns an empty path.
   *
   * @return an empty path
   */
  IPath getProjectRelativePath();

  /**
   * Always throws an IO exception.
   *
   * <p>Deleting a project resource is not supported.
   *
   * @throws IOException always
   */
  default void delete() throws IOException {
    throw new IOException("Deleting project resources is not supported - tried to delete " + this);
  }

  /**
   * Always returns <code>null</code>.
   *
   * <p>Resources above the shared project can not be described through (conventional) relative
   * paths. Additionally, such resources are not part of the shared scope and should therefore not
   * be of interest for Saros.
   *
   * @return <code>null</code>
   */
  default IContainer getParent() {
    return null;
  }

  /**
   * Returns a reference to itself.
   *
   * @return a reference to itself
   */
  default IProject getProject() {
    return this;
  }

  default Type getType() {
    return Type.PROJECT;
  }

  /**
   * Returns <code>false</code>.
   *
   * <p>Projects are the base for all shared resources and can therefore not be ignored.
   *
   * @return <code>false</code>
   */
  default boolean isIgnored() {
    return false;
  }
}
