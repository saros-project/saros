package saros.stf.server.util;

import java.util.Arrays;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

public class WidgetUtil {

  public enum OperatingSystem {
    MAC,
    WINDOW,
    UNIX
  }

  private static final OperatingSystem OS_TYPE;

  static {
    String osName = System.getProperty("os.name");
    if (osName.matches("Windows.*")) OS_TYPE = OperatingSystem.WINDOW;
    else if (osName.matches("Mac OS X.*")) {
      OS_TYPE = OperatingSystem.MAC;
    } else OS_TYPE = OperatingSystem.UNIX;
  }

  public static OperatingSystem getOperatingSystem() {
    return OS_TYPE;
  }

  public static SWTBotToolbarButton getToolbarButtonWithRegex(
      final SWTBotView view, final String regex) {

    try {
      new SWTBot()
          .waitUntil(
              new DefaultCondition() {
                @Override
                public String getFailureMessage() {
                  return "Could not find toolbar button matching " + regex;
                }

                @Override
                public boolean test() throws Exception {
                  return getToolbarButton(view, regex) != null;
                }
              });
      return getToolbarButton(view, regex);

    } catch (TimeoutException e) {
      throw new WidgetNotFoundException(
          "Timed out waiting for toolbar button matching " + regex, e); // $NON-NLS-1$
    }
  }

  private static SWTBotToolbarButton getToolbarButton(SWTBotView view, String regex) {

    for (SWTBotToolbarButton button : view.getToolbarButtons())
      if (button.getToolTipText().matches(regex)) return button;

    return null;
  }

  public static SWTBotTreeItem getTreeItemWithRegex(
      final SWTBotTree tree, final String... regexNodes) {

    try {
      new SWTBot()
          .waitUntil(
              new DefaultCondition() {
                @Override
                public String getFailureMessage() {
                  return "Could not find node matching " + Arrays.asList(regexNodes);
                }

                @Override
                public boolean test() throws Exception {
                  return getTreeItem(tree.getAllItems(), regexNodes) != null;
                }
              });
      return getTreeItem(tree.getAllItems(), regexNodes);

    } catch (TimeoutException e) {
      throw new WidgetNotFoundException(
          "Timed out waiting for tree item matching " + Arrays.asList(regexNodes),
          e); //$NON-NLS-1$
    }
  }

  private static SWTBotTreeItem getTreeItem(SWTBotTreeItem items[], final String... regexNodes) {

    SWTBotTreeItem currentItem = null;

    int l = regexNodes.length;
    int i = 0;

    regex:
    for (; i < l; i++) {

      if (items.length == 0) break regex;

      currentItem = null;

      String regex = regexNodes[i];
      int il = items.length;

      items:
      for (int j = 0; j < il; j++) {
        SWTBotTreeItem item = items[j];
        if (item.getText().matches(regex)) {
          currentItem = item;
          if (!currentItem.isExpanded()) currentItem.expand();
          items = currentItem.getItems();
          break items;
        }
      }

      if (currentItem == null) return null;
    }

    return i == l ? currentItem : null;
  }
}
