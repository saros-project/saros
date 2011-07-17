package de.fu_berlin.inf.dpp.stf.server.bot.widget;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.hamcrest.Matcher;

public class ContextMenuHelper {

    private static final Logger log = Logger.getLogger(ContextMenuHelper.class);

    /**
     * Clicks the context menu matching the text.
     * 
     * @throws WidgetNotFoundException
     *             if the widget is not found.
     */
    public static void clickContextMenu(
        final AbstractSWTBot<? extends Control> bot, final String... texts) {

        final MenuItem menuItem = getMenuItem(bot, texts);
        // show
        if (menuItem == null) {
            throw new WidgetNotFoundException("Could not find menu: "
                + Arrays.asList(texts));
        }

        // click
        click(menuItem);

        // hide
        UIThreadRunnable.syncExec(new VoidResult() {
            public void run() {
                hide(menuItem.getParent());
            }
        });
    }

    public static SWTBotMenu getContextMenu(
        final AbstractSWTBot<? extends Control> bot, final String... texts) {

        final MenuItem menuItem = getMenuItem(bot, texts);

        if (menuItem == null) {
            throw new WidgetNotFoundException("Could not find menu: "
                + Arrays.asList(texts));
        }

        // hide
        UIThreadRunnable.syncExec(new VoidResult() {
            public void run() {
                hide(menuItem.getParent());
            }
        });

        return new SWTBotMenu(menuItem);

    }

    /**
     * Exists the context menu matching the texts.
     * 
     * @param texts
     *            the text on the context menus.
     * @throws WidgetNotFoundException
     *             if the widget is not found.
     */
    public static boolean existsContextMenu(
        final AbstractSWTBot<? extends Control> bot, final String... texts) {

        final MenuItem menuItem = getMenuItem(bot, texts);

        // hide
        if (menuItem != null) {
            UIThreadRunnable.syncExec(new VoidResult() {
                public void run() {
                    hide(menuItem.getParent());
                }
            });
            return true;
        }
        return false;
    }

    public static boolean isContextMenuEnabled(
        final AbstractSWTBot<? extends Control> bot, final String... texts) {

        final MenuItem menuItem = getMenuItem(bot, texts);
        // show
        if (menuItem == null) {
            throw new WidgetNotFoundException("Could not find menu: "
                + Arrays.asList(texts));
        }

        return UIThreadRunnable.syncExec(new BoolResult() {
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
                    log.trace("found context menu item: " + menuItem.getText());
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

        UIThreadRunnable.asyncExec(menuItem.getDisplay(), new VoidResult() {
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

    private static MenuItem getMenuItem(
        final AbstractSWTBot<? extends Control> bot, final String... texts) {
        final MenuItem menuItem = UIThreadRunnable
            .syncExec(new WidgetResult<MenuItem>() {
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
                        Matcher<?> matcher = allOf(instanceOf(MenuItem.class),
                            withMnemonic(text));
                        menuItem = show(menu, matcher);
                        if (menuItem != null) {
                            menu = menuItem.getMenu();
                        } else {
                            hide(menu);
                            break;
                        }
                    }

                    return menuItem;
                }
            });
        return menuItem;
    }

}
