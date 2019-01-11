package de.fu_berlin.inf.dpp.ui.ide_embedding;

/**
 * This interface couples the IDE-specific locating of resources. Concretely, the URL to the
 * resource is different in Eclipse and IntelliJ. Therefore, both platforms have an own
 * implementation of this interface.
 */
public interface IUIResourceLocator {

  /**
   * Gets a URL for a given resource.
   *
   * @param resourceName name of the resource, this is the relative path inside the resource folder
   * @return URL of the resource or <code>null</code> if the resource was not found
   */
  String getResourceLocation(String resourceName);
}
