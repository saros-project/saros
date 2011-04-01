package de.fu_berlin.inf.dpp.ui.widgets.decoration;

import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Widget;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;

/**
 * Utility class for {@link Widget} manipulation
 */
public class JIDComboUtils {

    /**
     * Fill an editable {@link Combo} with domains that are retrieved from the
     * {@link XMPPAccountStore}.
     * <p>
     * <b>Example:</b><br/>
     * The following {@link JID}s are defined in {@link XMPPAccountStore}:
     * <ul>
     * <li>alice@alpha.com</li>
     * <li>alice@beta.com</li>
     * <li>alice@gamma.com</li>
     * </ul>
     * The {@link Combo}'s elements will be:
     * <ul>
     * <li>@alpha.com</li>
     * <li>@beta.com</li>
     * <li>@gamma.com</li>
     * </ul>
     * 
     * @param jidCombo
     *            to fill
     * @param preferenceUtils
     *            to get the current/default server from
     * @param xmppAccountStore
     *            to get a list of used domains from
     */
    public static void fillJIDCombo(Combo jidCombo,
        PreferenceUtils preferenceUtils, XMPPAccountStore xmppAccountStore) {
        String defaultServer = preferenceUtils.getServer();
        if (defaultServer.isEmpty())
            defaultServer = preferenceUtils.getDefaultServer();

        List<String> servers = xmppAccountStore.getDomains();
        if (servers.size() == 0)
            servers.add(defaultServer);
        jidCombo.removeAll();
        int selectIndex = 0;
        for (int i = 0, j = servers.size(); i < j; i++) {
            String server = servers.get(i);
            jidCombo.add("@" + server);
            if (defaultServer.equals(server))
                selectIndex = i;
        }
        jidCombo.select(selectIndex);
    }

    /**
     * Updates user portions of an editable {@link Combo}'s elements to reflect
     * the typed in user portion.
     * <p>
     * <b>Example:</b><br/>
     * {@link Combo}'s elements are:
     * <ul>
     * <li>alice@alpha.com</li>
     * <li>alice@beta.com</li>
     * <li>alice@gamma.com</li>
     * </ul>
     * If now the user enters "bob@xyz.com" the {@link Combo}'s elements change
     * to:
     * <ul>
     * <li>bob@alpha.com</li>
     * <li>bob@beta.com</li>
     * <li>bob@gamma.com</li>
     * </ul>
     * 
     * @param jidCombo
     *            to update
     */
    public static void updateJIDCombo(Combo jidCombo) {
        String jid = jidCombo.getText();
        String username = (jid.contains("@")) ? jid.split("@")[0] : jid;

        String[] items = jidCombo.getItems();
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            item = username + "@" + item.split("@")[1];
            jidCombo.setItem(i, item);
        }

        /*
         * The modification of the list items resets the text. We make sure the
         * initially set value remains.
         */
        Point selection = jidCombo.getSelection();
        if (!jidCombo.getText().equals(jid)) {
            jidCombo.setText(jid);
            jidCombo.setSelection(selection);
        }
    }
}
