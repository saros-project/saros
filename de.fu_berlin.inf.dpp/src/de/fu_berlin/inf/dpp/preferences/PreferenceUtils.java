package de.fu_berlin.inf.dpp.preferences;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;

@Component(module = "prefs")
public class PreferenceUtils {

    Saros saros;

    public PreferenceUtils(Saros saros) {
        this.saros = saros;
    }

    public List<JID> getAutoInviteUsers() {

        String autoInvite = saros.getPreferenceStore().getString(
            PreferenceConstants.AUTO_INVITE);

        if (autoInvite == null || autoInvite.trim().length() == 0)
            return Collections.emptyList();

        List<JID> result = new LinkedList<JID>();

        for (String user : autoInvite.split(",")) {
            result.add(new JID(user.trim()));
        }
        return result;
    }

    public boolean isAutoReuseExisting() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_REUSE_PROJECT);
    }

    public boolean isAutoAcceptInvitation() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_ACCEPT_INVITATION);
    }

}
