package de.fu_berlin.inf.dpp.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;

@Component(module = "prefs")
public class PreferenceUtils {

    private IPreferenceStore preferenceStore;

    public PreferenceUtils(IPreferenceStore preferenceStore) {
        this.preferenceStore = preferenceStore;
    }

    public boolean isDebugEnabled() {
        return preferenceStore.getBoolean(PreferenceConstants.DEBUG);
    }

    /**
     * Returns Saros's XMPP server DNS address.
     * 
     * @return
     */
    public String getSarosXMPPServer() {
        return "saros-con.imp.fu-berlin.de";
    }

    /**
     * Returns the default server.<br/>
     * Is never empty or null.
     * 
     * @return
     */
    public String getDefaultServer() {
        return getSarosXMPPServer();
    }

    /**
     * Returns whether auto-connect is enabled or not.
     * 
     * @return true if auto-connect is enabled.
     */
    public boolean isAutoConnecting() {
        return preferenceStore.getBoolean(PreferenceConstants.AUTO_CONNECT);
    }

    /**
     * Returns whether port mapping is enabled or not by evaluating the stored
     * deviceID to be empty or not.
     * 
     * @return true of port mapping is enabled, false otherwise
     */
    public boolean isAutoPortmappingEnabled() {
        return preferenceStore.getString(
            PreferenceConstants.AUTO_PORTMAPPING_DEVICEID).isEmpty() == false;
    }

    /**
     * Returns the Socks5 candidates for the Socks5 proxy.
     * 
     * @return
     */
    public List<String> getSocks5Candidates() {
        String addresses = preferenceStore
            .getString(PreferenceConstants.LOCAL_SOCKS5_PROXY_CANDIDATES);

        List<String> result = new ArrayList<String>();

        for (String address : addresses.split(",")) {
            address = address.trim();

            if (address.isEmpty())
                continue;

            result.add(address);
        }

        return result;
    }

    /**
     * Returns whether the external address of the gateway should be used as a
     * Socks5 candidate or not.
     * 
     * @return
     */
    public boolean useExternalGatewayAddress() {
        return preferenceStore
            .getBoolean(PreferenceConstants.LOCAL_SOCKS5_PROXY_USE_UPNP_EXTERNAL_ADDRESS);
    }

    /**
     * Returns the device ID of the gateway to perform port mapping on.
     * 
     * @return Device ID of the gateway or empty String if disabled.
     */
    public String getAutoPortmappingGatewayID() {
        return preferenceStore
            .getString(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID);
    }

    public int getAutoPortmappingLastPort() {
        return preferenceStore
            .getInt(PreferenceConstants.AUTO_PORTMAPPING_LASTMAPPEDPORT);
    }

    /**
     * Returns the Skype user name or an empty string if none was specified.
     * 
     * @return the user name.for Skype or an empty string
     */
    public String getSkypeUserName() {
        return preferenceStore.getString(PreferenceConstants.SKYPE_USERNAME);
    }

    /**
     * Returns the port for SOCKS5 file transfer. If
     * {@link PreferenceConstants#USE_NEXT_PORTS_FOR_FILE_TRANSFER} is set, a
     * negative number is returned (smacks will try next free ports above this
     * number)
     * 
     * @return port for smacks configuration (negative if to try out ports
     *         above)
     */
    public int getFileTransferPort() {
        int port = preferenceStore
            .getInt(PreferenceConstants.FILE_TRANSFER_PORT);

        if (preferenceStore
            .getBoolean(PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER))
            return -port;
        else
            return port;
    }

    public boolean forceFileTransferByChat() {
        return preferenceStore
            .getBoolean(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT);
    }

    public boolean isConcurrentUndoActivated() {
        return preferenceStore.getBoolean(PreferenceConstants.CONCURRENT_UNDO);
    }

    public boolean useVersionControl() {
        return !preferenceStore
            .getBoolean(PreferenceConstants.DISABLE_VERSION_CONTROL);
    }

    public void setUseVersionControl(boolean value) {
        preferenceStore.setValue(PreferenceConstants.DISABLE_VERSION_CONTROL,
            !value);
    }

    public boolean isLocalSOCKS5ProxyEnabled() {
        return !preferenceStore
            .getBoolean(PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED);
    }

    public String getStunIP() {
        return preferenceStore.getString(PreferenceConstants.STUN);
    }

    public int getStunPort() {
        return preferenceStore.getInt(PreferenceConstants.STUN_PORT);
    }

    /**
     * Returns the favorite color ID that should be used during a session.
     * 
     * @return the favorite color ID or {@value UserColorID#UNKNOWN} if no
     *         favorite color ID is available
     */
    public int getFavoriteColorID() {
        return preferenceStore
            .getInt(PreferenceConstants.FAVORITE_SESSION_COLOR_ID);
    }
}
