package de.fu_berlin.inf.dpp.intellij.ui.swt_browser;

import com.intellij.openapi.util.Condition;
import de.fu_berlin.inf.dpp.intellij.SarosComponent;

/** Tests if the HTML GUI of Saros is enabled. */
public class BrowserCondition implements Condition {
  @Override
  public boolean value(Object o) {
    return SarosComponent.isSwtBrowserEnabled();
  }
}
