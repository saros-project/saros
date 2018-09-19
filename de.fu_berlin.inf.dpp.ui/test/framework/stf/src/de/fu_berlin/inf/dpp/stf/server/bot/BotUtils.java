package de.fu_berlin.inf.dpp.stf.server.bot;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;

public class BotUtils {

    public static List<String> getListItemsText(IJQueryBrowser browser,
        ISelector selector) {
        String code = "var strings = [];" + selector.getStatement()
            + ".each(function (i) { "
            + "strings[i] = $(this).text().trim(); }); return strings; ";

        return getListItems(browser, selector, code);
    }

    public static List<String> getListItemsValue(IJQueryBrowser browser,
        ISelector selector) {
        String code = "var strings = [];" + selector.getStatement()
            + ".each(function (i) { "
            + "strings[i] = $(this).val(); }); return strings; ";

        return getListItems(browser, selector, code);
    }

    private static List<String> getListItems(IJQueryBrowser browser,
        ISelector selector, String code) {

        Object[] objects = (Object[]) browser.syncRun(code);

        List<String> strings = new ArrayList<String>();
        for (Object o : objects) {
            strings.add(o.toString());
        }

        return strings;

    }

}
