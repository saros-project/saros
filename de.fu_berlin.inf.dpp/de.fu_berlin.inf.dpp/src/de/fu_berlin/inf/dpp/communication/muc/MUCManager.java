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
package de.fu_berlin.inf.dpp.communication.muc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ChatState;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.muc.events.IMUCManagerListener;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.communication.muc.session.MUCSession;

/**
 * This class manages the creation and destruction of {@link MUCSession}s.
 * 
 * @author rdjemili
 * @author ahaferburg
 * @author bkahlert
 */
@Component(module = "communication")
public class MUCManager {
    private static final Logger log = Logger.getLogger(MUCManager.class);

    protected Saros saros;
    protected Set<MUCSession> mucSessions = new HashSet<MUCSession>();
    protected List<IMUCManagerListener> mucManagerListeners = new ArrayList<IMUCManagerListener>();

    public MUCManager(Saros saros) {
        log.setLevel(Level.DEBUG);
        this.saros = saros;
    }

    /**
     * Connects to a {@link MUCSession}. Automatically (if necessary) created
     * and joins the {@link MUCSession} based on the
     * {@link MUCSessionPreferences}
     * 
     * @param preferences
     * @return TODO connectMUC should be split into create and join; bkahlert
     *         2010/11/23
     */
    public MUCSession connectMUC(MUCSessionPreferences preferences) {
        if (!saros.isConnected()) {
            log.error("Can't join chat: Not connected.");
            return null;
        }

        log.debug("Joining MUC...");

        boolean createdRoom = false;
        MUCSession mucSession = new MUCSession(saros.getConnection(),
            preferences);
        try {
            createdRoom = mucSession.connect();
        } catch (XMPPException e) {
            log.error("Couldn't join chat: " + preferences.getRoom(), e);
            return null;
        }
        this.mucSessions.add(mucSession);
        mucSession.setState(ChatState.active);

        /*
         * Notification
         */
        if (createdRoom) {
            notifyMUCSessionCreated(mucSession);
        }
        notifyMUCSessionJoined(mucSession);

        return mucSession;
    }

    /**
     * Disconnects from a {@link MUCSession}. Automatically destroys the
     * {@link MUCSession} if the participant created it.
     * 
     * @param mucSession
     *            TODO disconnectMUC should be split into create and join;
     *            bkahlert 2010/11/23
     */
    public void disconnectMUC(MUCSession mucSession) {
        boolean destroyedRoom = false;

        log.debug("Leaving chat.");
        assert mucSession != null;

        mucSession.setState(ChatState.gone);
        destroyedRoom = mucSession.disconnect();

        notifyMUCSessionLeft(mucSession);
        if (destroyedRoom) {
            notifyMUCSessionDestroyed(mucSession);
        }
    }

    /**
     * Adds a {@link IMUCManagerListener}
     * 
     * @param mucManagerListener
     */
    public void addMUCManagerListener(IMUCManagerListener mucManagerListener) {
        this.mucManagerListeners.add(mucManagerListener);
    }

    /**
     * Removes a {@link IMUCManagerListener}
     * 
     * @param mucManagerListener
     */
    public void removeMUCManagerListener(IMUCManagerListener mucManagerListener) {
        this.mucManagerListeners.remove(mucManagerListener);
    }

    /**
     * Notify all {@link IMUCManagerListener}s about a created
     * {@link MUCSession}
     */
    public void notifyMUCSessionCreated(MUCSession mucSession) {
        for (IMUCManagerListener mucManagerListener : this.mucManagerListeners) {
            mucManagerListener.mucSessionCreated(mucSession);
        }
    }

    /**
     * Notify all {@link IMUCManagerListener}s about a joined {@link MUCSession}
     */
    public void notifyMUCSessionJoined(MUCSession mucSession) {
        for (IMUCManagerListener mucManagerListener : this.mucManagerListeners) {
            mucManagerListener.mucSessionJoined(mucSession);
        }
    }

    /**
     * Notify all {@link IMUCManagerListener}s about a left {@link MUCSession}
     */
    public void notifyMUCSessionLeft(MUCSession mucSession) {
        for (IMUCManagerListener mucManagerListener : this.mucManagerListeners) {
            mucManagerListener.mucSessionLeft(mucSession);
        }
    }

    /**
     * Notify all {@link IMUCManagerListener}s about a destroyed
     * {@link MUCSession}
     */
    public void notifyMUCSessionDestroyed(MUCSession mucSession) {
        for (IMUCManagerListener mucManagerListener : this.mucManagerListeners) {
            mucManagerListener.mucSessionDestroyed(mucSession);
        }
    }
}
