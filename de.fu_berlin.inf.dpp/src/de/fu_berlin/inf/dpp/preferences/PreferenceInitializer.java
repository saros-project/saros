/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import de.fu_berlin.inf.dpp.Saros;

/**
 * Class used to initialize default preference values.
 * 
 * @author rdjemili
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
     */
    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences prefs = new DefaultScope().getNode(Saros.SAROS);

        prefs.put(PreferenceConstants.SERVER, "jabber.org");
        prefs.put(PreferenceConstants.USERNAME, "");
        prefs.put(PreferenceConstants.PASSWORD, "");
        prefs.putBoolean(PreferenceConstants.AUTO_CONNECT, false);
        prefs.putBoolean(PreferenceConstants.AUTO_FOLLOW_MODE, false);
        prefs.put(PreferenceConstants.SKYPE_USERNAME, "");
        prefs.putBoolean(PreferenceConstants.DEBUG, false);
        prefs.putInt(PreferenceConstants.FILE_TRANSFER_PORT, 7777);
        prefs.putBoolean(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT, false);
        prefs.putInt(PreferenceConstants.CHATFILETRANSFER_CHUNKSIZE, 50000);
        prefs.put(PreferenceConstants.STUN, "stunserver.org");
        prefs.putInt(PreferenceConstants.STUN_PORT, 3478);
        prefs.putBoolean(PreferenceConstants.MULTI_DRIVER, false);
        prefs.putBoolean(PreferenceConstants.AUTO_ACCEPT_INVITATION, false);
        prefs.putBoolean(PreferenceConstants.FOLLOW_EXCLUSIVE_DRIVER, true);
    }
}
