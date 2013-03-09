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

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.chat.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * Packet used for exchanging data during session negotiation. Can be used on
 * both sides.
 */
@Component(module = "net")
public class InvitationParameterExchangeExtension extends InvitationExtension {

    public static final Provider PROVIDER = new Provider();

    private String sessionID;
    private int localColorID;
    private int localFavoriteColorID;
    private int remoteColorID;
    private int remoteFavoriteColorID;
    private MUCSessionPreferences mucPreferences;
    private JID sessionHost;

    public int getLocalFavoriteColorID() {
        return localFavoriteColorID;
    }

    public void setLocalFavoriteColorID(int localFavoriteColorID) {
        this.localFavoriteColorID = localFavoriteColorID;
    }

    public int getRemoteFavoriteColorID() {
        return remoteFavoriteColorID;
    }

    public void setRemoteFavoriteColorID(int remoteFavoriteColorID) {
        this.remoteFavoriteColorID = remoteFavoriteColorID;
    }

    public int getLocalColorID() {
        return localColorID;
    }

    public void setLocalColorID(int localColorID) {
        this.localColorID = localColorID;
    }

    public int getRemoteColorID() {
        return remoteColorID;
    }

    public void setRemoteColorID(int remoteColorID) {
        this.remoteColorID = remoteColorID;
    }

    public MUCSessionPreferences getMUCPreferences() {
        return mucPreferences;
    }

    public void setMUCPreferences(MUCSessionPreferences mucPreferences) {
        this.mucPreferences = mucPreferences;
    }

    public JID getSessionHost() {
        return sessionHost;
    }

    public void setSessionHost(JID sessionHost) {
        this.sessionHost = sessionHost;
    }

    public InvitationParameterExchangeExtension(String invitationID) {
        super(invitationID);
    }

    public String getSessionID() {
        return sessionID;
    }

    public static class Provider extends
        InvitationExtension.Provider<InvitationParameterExchangeExtension> {

        private Provider() {
            super("invitationParameterExchange",
                InvitationParameterExchangeExtension.class);
        }
    }
}
