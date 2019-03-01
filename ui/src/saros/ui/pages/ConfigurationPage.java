package de.fu_berlin.inf.dpp.ui.pages;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;

/** Represents the Saros Configuration Wizard. This wizard is used to configure Saros */
public class ConfigurationPage extends AbstractBrowserPage {

  public static final String HTML_DOC_NAME = "configuration-page.html";

  /**
   * Created by PicoContainer
   *
   * @see HTMLUIContextFactory
   */
  public ConfigurationPage() {
    super(HTML_DOC_NAME, HTMLUIStrings.TITLE_CONFIGURATION_PAGE);
  }
}
