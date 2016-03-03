package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import de.fu_berlin.inf.dpp.ui.ide_embedding.IUiResourceLocator;

/**
 * This is the IntelliJ implementation of the resource locator for webpages.
 * Those resources are located in the classes subfolder of the deployed plug-in.
 */
public class IntelliJWebResourceLocator implements IUiResourceLocator {

    @Override
    public String getResourceLocation(String resourceName) {
        return IntelliJWebResourceLocator.class.getClassLoader().getResource(resourceName).toString();
    }
}
