package saros.intellij.ui.swt_browser;

import com.intellij.openapi.util.Condition;
import saros.intellij.SarosComponent;

/** Tests if the HTML GUI of Saros is enabled. */
public class BrowserCondition implements Condition {
  @Override
  public boolean value(Object o) {
    return SarosComponent.isSwtBrowserEnabled();
  }
}
