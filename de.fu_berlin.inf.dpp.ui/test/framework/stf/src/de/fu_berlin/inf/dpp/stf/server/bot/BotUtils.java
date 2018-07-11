package de.fu_berlin.inf.dpp.stf.server.bot;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;

public class BotUtils {

    public static List<String> getListItemsWithSelector(IJQueryBrowser browser,
        ISelector selector) {
        Object[] objects = (Object[]) browser.syncRun("var strings = [];"
            + selector.getStatement() + ".each(function (i) { "
            + "strings[i] = $(this).text().trim(); }); return strings; ");

        List<String> strings = new ArrayList<String>();
        for (Object o : objects) {
            strings.add(o.toString());
        }

        return strings;
    }

}
