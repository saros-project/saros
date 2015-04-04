package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import de.fu_berlin.inf.dpp.ui.ide_embedding.IWebResourceLocator;

/**
 * This is the IntelliJ implementation of the resource locator for webpages.
 * Those resources are located in the classes subfolder of the deployed plug-in.
 */
public class IntelliJWebResourceLocator implements IWebResourceLocator {

    @Override
    public String getResourceLocation(String resourceName) {
        return IntelliJWebResourceLocator.class.getClassLoader().getResource(resourceName).toString();
    }
}
