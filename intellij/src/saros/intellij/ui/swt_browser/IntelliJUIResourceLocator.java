package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import de.fu_berlin.inf.dpp.ui.ide_embedding.IUIResourceLocator;

/**
 * This is the IntelliJ implementation of the resource locator for pages. Those resources are
 * located in the classes subfolder of the deployed plug-in.
 */
public class IntelliJUIResourceLocator implements IUIResourceLocator {

  @Override
  public String getResourceLocation(String resourceName) {
    return IntelliJUIResourceLocator.class.getClassLoader().getResource(resourceName).toString();
  }
}
