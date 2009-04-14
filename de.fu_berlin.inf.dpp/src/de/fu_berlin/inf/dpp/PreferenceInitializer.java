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
package de.fu_berlin.inf.dpp;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

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
        IPreferenceStore store = Saros.getDefault().getPreferenceStore();
        store.setDefault(PreferenceConstants.SERVER, "jabber.org");
        store.setDefault(PreferenceConstants.USERNAME, "");
        store.setDefault(PreferenceConstants.PASSWORD, "");

        store.setDefault(PreferenceConstants.AUTO_CONNECT, false);
        store.setDefault(PreferenceConstants.AUTO_FOLLOW_MODE, false);
        store.setDefault(PreferenceConstants.SKYPE_USERNAME, "");
        store.setDefault(PreferenceConstants.DEBUG, false);
        store.setDefault(PreferenceConstants.FILE_TRANSFER_PORT, 7777);
        store.setDefault(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT, false);
        store.setDefault(PreferenceConstants.CHATFILETRANSFER_CHUNKSIZE, 50000);
        store.setDefault(PreferenceConstants.STUN, "stunserver.org");
        store.setDefault(PreferenceConstants.STUN_PORT, 3478);
        store.setDefault(PreferenceConstants.MULTI_DRIVER, false);
        store.setDefault(PreferenceConstants.AUTO_ACCEPT_INVITATION, false);
        store.setDefault(PreferenceConstants.FOLLOW_EXCLUSIVE_DRIVER, true);
    }
}
