/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.store;

import de.fu_berlin.inf.dpp.preferences.IPreferences;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * IntelliJPreferences is implementation {@link de.fu_berlin.inf.dpp.preferences.IPreferences} by
 * using the {@link de.fu_berlin.inf.dpp.intellij.store.PreferenceStore}
 * to simplify the usage of {@link com.intellij.ide.util.PropertiesComponent}.
 * It is the IntelliJ specific Preference Store.
 */
public class IntelliJPreferences implements IPreferences {

    private PreferenceStore preferenceStore;

    /**
     * Creates a new IntelliJPreference object
     */
    public IntelliJPreferences() {
        this.preferenceStore = new PreferenceStore();
    }

    public boolean isDebugEnabled() {
        return preferenceStore.getBoolean(PreferenceConstants.DEBUG);
    }

    @Override
    public String getSarosXMPPServer() {
        return "saros-con.imp.fu-berlin.de";
    }

    @Override
    public String getDefaultServer() {
        return getSarosXMPPServer();
    }

    @Override
    public boolean isAutoConnecting() {
        return preferenceStore.getBoolean(PreferenceConstants.AUTO_CONNECT);
    }

    @Override
    public boolean isAutoPortmappingEnabled() {
        return !preferenceStore
            .getString(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID).isEmpty();
    }

    @Override
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

    @Override
    public boolean useExternalGatewayAddress() {
        return preferenceStore.getBoolean(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_USE_UPNP_EXTERNAL_ADDRESS);
    }

    @Override
    public String getAutoPortmappingGatewayID() {
        return preferenceStore
            .getString(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID);
    }

    public int getAutoPortmappingLastPort() {
        return preferenceStore
            .getInt(PreferenceConstants.AUTO_PORTMAPPING_LASTMAPPEDPORT);
    }

    @Override
    public String getSkypeUserName() {
        return preferenceStore.getString(PreferenceConstants.SKYPE_USERNAME);
    }

    @Override
    public int getFileTransferPort() {
        int port = preferenceStore
            .getInt(PreferenceConstants.FILE_TRANSFER_PORT);

        if (preferenceStore
            .getBoolean(PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER))
            return -port;
        else
            return port;
    }

    @Override
    public boolean forceIBBTransport() {
        return preferenceStore
            .getBoolean(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT);
    }

    @Override
    public boolean isConcurrentUndoActivated() {
        return preferenceStore.getBoolean(PreferenceConstants.CONCURRENT_UNDO);
    }

    @Override
    public boolean useVersionControl() {
        return !preferenceStore
            .getBoolean(PreferenceConstants.DISABLE_VERSION_CONTROL);
    }

    @Override
    public void setUseVersionControl(boolean value) {
        preferenceStore
            .setValue(PreferenceConstants.DISABLE_VERSION_CONTROL, !value);
    }

    @Override
    public boolean isLocalSOCKS5ProxyEnabled() {
        return !preferenceStore
            .getBoolean(PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED);
    }

    @Override
    public String getStunIP() {
        return preferenceStore.getString(PreferenceConstants.STUN);
    }

    @Override
    public int getStunPort() {
        return preferenceStore.getInt(PreferenceConstants.STUN_PORT);
    }

    @Override
    public int getFavoriteColorID() {
        return preferenceStore
            .getInt(PreferenceConstants.FAVORITE_SESSION_COLOR_ID);
    }

    @Override
    public String getSessionNickname() {
        return preferenceStore.getString(PreferenceConstants.SESSION_NICKNAME);
    }
}
