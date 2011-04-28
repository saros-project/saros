package de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import java.util.Arrays;

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

    /**
     * Clicks the context menu matching the text.
     * 
     * @throws WidgetNotFoundException
     *             if the widget is not found.
     */
    public static void clickContextMenu(
        final AbstractSWTBot<? extends Control> bot, final String... texts) {

        // show
        final MenuItem menuItem = UIThreadRunnable
            .syncExec(new WidgetResult<MenuItem>() {
                public MenuItem run() {
                    MenuItem menuItem = null;
                    Control control = bot.widget;
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

        // show
        final MenuItem menuItem = UIThreadRunnable
            .syncExec(new WidgetResult<MenuItem>() {
                public MenuItem run() {
                    MenuItem menuItem = null;
                    Control control = bot.widget;
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

        final boolean existContext = UIThreadRunnable
            .syncExec(new BoolResult() {
                MenuItem menuItem = null;

                public Boolean run() {
                    Control control = bot.widget;
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
                            return false;
                        }
                    }
                    return true;
                }

            });
        return existContext;

    }

    public static boolean isContextMenuEnabled(
        final AbstractSWTBot<? extends Control> bot, final String... texts) {

        final boolean istContextEnabled = UIThreadRunnable
            .syncExec(new BoolResult() {
                MenuItem menuItem = null;

                public Boolean run() {
                    Control control = bot.widget;
                    Menu menu = control.getMenu();

                    for (String text : texts) {
                        @SuppressWarnings("unchecked")
                        Matcher<?> matcher = allOf(instanceOf(MenuItem.class),
                            withMnemonic(text));
                        menuItem = show(menu, matcher);
                        if (menuItem != null) {
                            if (!menuItem.isEnabled()) {
                                hide(menu);
                                return false;
                            } else
                                menu = menuItem.getMenu();
                        } else {
                            hide(menu);
                            return false;
                        }
                    }
                    return true;
                }

            });
        return istContextEnabled;

    }

    public static MenuItem show(final Menu menu, final Matcher<?> matcher) {
        if (menu != null) {
            menu.notifyListeners(SWT.Show, new Event());
            MenuItem[] items = menu.getItems();
            for (final MenuItem menuItem : items) {
                if (matcher.matches(menuItem)) {
                    return menuItem;
                }
            }
            menu.notifyListeners(SWT.Hide, new Event());
        }
        return null;
    }

    public static void click(final MenuItem menuItem) {
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

    public static void hide(final Menu menu) {
        menu.notifyListeners(SWT.Hide, new Event());
        if (menu.getParentMenu() != null) {
            hide(menu.getParentMenu());
        }
    }
}
