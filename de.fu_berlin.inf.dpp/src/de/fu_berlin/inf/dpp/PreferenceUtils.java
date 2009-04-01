package de.fu_berlin.inf.dpp;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.fu_berlin.inf.dpp.net.JID;

public class PreferenceUtils {

    public static List<JID> getAutoInviteUsers() {

        String autoInvite = Saros.getDefault().getPreferenceStore().getString(
            PreferenceConstants.AUTO_INVITE);

        if (autoInvite == null || autoInvite.trim().length() == 0)
            return Collections.emptyList();

        List<JID> result = new LinkedList<JID>();

        for (String user : autoInvite.split(",")) {
            result.add(new JID(user.trim()));
        }
        return result;
    }

    public static boolean isAutoReuseExisting() {
        return Saros.getDefault().getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_REUSE_PROJECT);
    }

    public static boolean isAutoAcceptInvitation() {
        return Saros.getDefault().getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_ACCEPT_INVITATION);
    }

}
