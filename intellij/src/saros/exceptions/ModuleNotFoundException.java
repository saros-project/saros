package saros.exceptions;

import saros.intellij.filesystem.IntelliJProjectImpl;

/**
 * Signals that a needed module object could not be found in the current project.
 *
 * <p>This exception will be thrown if no module with the same name could be found when trying to
 * reload a module by calling {@link IntelliJProjectImpl#refreshModule()}
 */
public class ModuleNotFoundException extends Exception {

  /**
   * Constructs a <code>ModuleNotFoundException</code> with the given message.
   *
   * @param message the exception message
   */
  public ModuleNotFoundException(String message) {
    super(message);
  }
}
