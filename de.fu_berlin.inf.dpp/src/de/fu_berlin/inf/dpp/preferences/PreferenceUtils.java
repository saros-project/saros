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

    /**
     * Returns the user name from the PreferenceStore. Might be an empty string
     * but never null.
     * 
     * @return the user name
     */
    public String getUserName() {
        return saros.getPreferenceStore().getString(
            PreferenceConstants.USERNAME);
    }

    /**
     * Returns if the user has specified a user name for a Jabber account.
     * 
     * @return true if there is a user name that is not equal to the empty
     *         string
     */
    public boolean hasUserName() {
        return getUserName().length() > 0;
    }

    /**
     * Returns whether auto-connect is enabled or not.
     * 
     * @return true if auto-connect is enabled.
     */
    public boolean isAutoConnecting() {
        return saros.getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_CONNECT);
    }

    /**
     * Returns the Skype user name or an empty string if none was specified.
     * 
     * @return the user name.for Skype or an empty string
     */
    public String getSkypeUserName() {
        return saros.getPreferenceStore().getString(
            PreferenceConstants.SKYPE_USERNAME);
    }

}
