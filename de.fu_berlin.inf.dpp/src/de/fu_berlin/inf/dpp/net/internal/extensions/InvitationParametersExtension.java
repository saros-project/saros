/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.chat.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * Small class used by the invitation process as a first message
 */
@Component(module = "net")
public class InvitationParametersExtension extends InvitationExtension {
    public int colorID;
    public VersionInfo versionInfo;
    public DateTime sessionStart;
    public MUCSessionPreferences comPrefs;
    public String description;
    public JID host;
    public int inviterColorID;

    public InvitationParametersExtension(String sessionID, String invitationID,
        int colorID, String description, VersionInfo versionInfo,
        DateTime sessionStart, MUCSessionPreferences comPrefs, JID host,
        int inviterColorID) {
        super(sessionID, invitationID);
        this.colorID = colorID;
        this.versionInfo = versionInfo;
        this.sessionStart = sessionStart;
        this.comPrefs = comPrefs;
        this.description = description;
        this.host = host;
        this.inviterColorID = inviterColorID;
    }

    public static class Provider extends
        XStreamExtensionProvider<InvitationParametersExtension> {

        public Provider() {
            super("invitationParameters", InvitationParametersExtension.class);
        }
    }
}
