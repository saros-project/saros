package de.fu_berlin.inf.dpp.stf.server.bot.widget;

import static org.eclipse.swtbot.swt.finder.matchers.WithMnemonicRegex.withMnemonicRegex;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.hamcrest.Matcher;

public class ContextMenuHelper {

  private static final Logger log = Logger.getLogger(ContextMenuHelper.class);

  /**
   * Clicks the context menu item matching the text nodes.
   *
   * @param bot a SWTBot class that wraps a {@link Widget} which extends {@link Control}. E.g.
   *     {@link SWTBotTree}.
   * @param nodes the nodes of the context menu e.g New, Class
   * @throws WidgetNotFoundException if the widget is not found.
   */
  public static void clickContextMenu(
      final AbstractSWTBot<? extends Control> bot, final String... nodes) {
    clickContextMenuWithRegEx(bot, quote(nodes));
  }

  /**
   * Clicks the context menu item matching the text nodes with regular expression.
   *
   * @param bot a SWTBot class that wraps a {@link Widget} which extends {@link Control}. E.g.
   *     {@link SWTBotTree}.
   * @param nodes the nodes of the context menu e.g New, Class
   * @throws WidgetNotFoundException if the widget is not found.
   */
  public static void clickContextMenuWithRegEx(
      final AbstractSWTBot<? extends Control> bot, final String... nodes) {

    final MenuItem menuItem = getMenuItemWithRegEx(bot, nodes);
    // show
    if (menuItem == null) {
      throw new WidgetNotFoundException("Could not find menu with regex: " + Arrays.asList(nodes));
    }

    // click
    click(menuItem);

    // hide
    UIThreadRunnable.syncExec(
        new VoidResult() {
          @Override
          public void run() {
            hide(menuItem.getParent());
          }
        });
  }

  /**
   * Checks if the context menu item matching the text nodes exists.
   *
   * @param bot a SWTBot class that wraps a {@link Widget} which extends {@link Control}. E.g.
   *     {@link SWTBotTree}.
   * @param nodes the nodes of the context menu e.g New, Class
   * @return <tt>true</tt> if the context menu item exists, <tt>false</tt> otherwise
   */
  public static boolean existsContextMenu(
      final AbstractSWTBot<? extends Control> bot, final String... nodes) {

    final MenuItem menuItem = getMenuItemWithRegEx(bot, quote(nodes));

    // hide
    if (menuItem != null) {
      UIThreadRunnable.syncExec(
          new VoidResult() {
            @Override
            public void run() {
              hide(menuItem.getParent());
            }
          });
      return true;
    }
    return false;
  }

  /**
   * Checks if the context menu item matching the text nodes is enabled.
   *
   * @param bot a SWTBot class that wraps a {@link Widget} which extends {@link Control}. E.g.
   *     {@link SWTBotTree}.
   * @param nodes the nodes of the context menu e.g New, Class
   * @return <tt>true</tt> if the context menu item is enabled, <tt>false</tt> otherwise
   * @throws WidgetNotFoundException if the widget is not found.
   */
  public static boolean isContextMenuEnabled(
      final AbstractSWTBot<? extends Control> bot, final String... nodes) {

    final MenuItem menuItem = getMenuItemWithRegEx(bot, quote(nodes));
    // show
    if (menuItem == null) {
      throw new WidgetNotFoundException("could not find menu: " + Arrays.asList(nodes));
    }

    return UIThreadRunnable.syncExec(
        new BoolResult() {
          @Override
          public Boolean run() {
            boolean enabled = menuItem.isEnabled();
            hide(menuItem.getParent());
            return enabled;
          }
        });
  }

  private static MenuItem show(final Menu menu, final Matcher<?> matcher) {
    if (menu != null) {
      menu.notifyListeners(SWT.Show, new Event());
      MenuItem[] items = menu.getItems();
      if (log.isTraceEnabled()) {
        for (final MenuItem menuItem : items)
          log.trace(
              "found context menu item: "
                  + menuItem.getText()
                  + "[enabled="
                  + menuItem.isEnabled()
                  + "]");
      }

      for (final MenuItem menuItem : items) {
        if (matcher.matches(menuItem)) {
          return menuItem;
        }
      }
      menu.notifyListeners(SWT.Hide, new Event());
    }
    return null;
  }

  private static void click(final MenuItem menuItem) {
    final Event event = new Event();
    event.time = (int) System.currentTimeMillis();
    event.widget = menuItem;
    event.display = menuItem.getDisplay();
    event.type = SWT.Selection;

    UIThreadRunnable.asyncExec(
        menuItem.getDisplay(),
        new VoidResult() {
          @Override
          public void run() {
            menuItem.notifyListeners(SWT.Selection, event);
          }
        });
  }

  private static void hide(final Menu menu) {
    menu.notifyListeners(SWT.Hide, new Event());
    if (menu.getParentMenu() != null) {
      hide(menu.getParentMenu());
    }
  }

  private static MenuItem getMenuItemWithRegEx(
      final AbstractSWTBot<? extends Control> bot, final String... texts) {
    final MenuItem menuItem =
        UIThreadRunnable.syncExec(
            new WidgetResult<MenuItem>() {
              @Override
              public MenuItem run() {
                MenuItem menuItem = null;
                Control control = bot.widget;

                Event event = new Event();
                control.notifyListeners(SWT.MenuDetect, event);
                if (!event.doit) {
                  return null;
                }

                Menu menu = control.getMenu();
                for (String text : texts) {
                  @SuppressWarnings("unchecked")
                  Matcher<?> matcher = allOf(instanceOf(MenuItem.class), withMnemonicRegex(text));
                  menuItem = show(menu, matcher);

                  if (menuItem != null) menu = menuItem.getMenu();
                  else break;
                }

                return menuItem;
              }
            });
    return menuItem;
  }

  private static String[] quote(String[] text) {
    final String[] quotedText = new String[text.length];

    for (int i = 0; i < text.length; i++) quotedText[i] = Pattern.quote(text[i]);

    return quotedText;
  }
}
