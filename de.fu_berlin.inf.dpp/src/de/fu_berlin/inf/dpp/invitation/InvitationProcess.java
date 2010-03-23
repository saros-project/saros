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
package de.fu_berlin.inf.dpp.invitation;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.exceptions.StreamException;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.StreamService;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.net.internal.StreamSession.StreamSessionListener;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;

/**
 * @author rdjemili
 * @author sotitas
 */
public abstract class InvitationProcess {

    private final static Logger log = Logger.getLogger(InvitationProcess.class);

    protected final ITransmitter transmitter;
    protected JID peer;
    protected String description;
    protected final int colorID;

    @Inject
    protected StreamServiceManager streamServiceManager;
    @Inject
    protected ArchiveStreamService archiveStreamService;
    protected StreamSession streamSession;

    protected InvitationProcessObservable invitationProcesses;
    protected boolean error = false;

    protected StreamSessionListener sessionListener = new StreamSessionListener() {

        public void sessionStopped() {
            if (streamSession != null) {

                streamSession.shutdownFinished();
                streamSession = null;
            }
        }

        public void errorOccured(StreamException e) {
            log.debug("Got error while streaming project archive: ", e);
            error = true;
        }
    };

    public InvitationProcess(ITransmitter transmitter, JID peer,
        String description, int colorID,
        InvitationProcessObservable invitationProcesses) {
        this.transmitter = transmitter;
        this.peer = peer;
        this.description = description;
        this.colorID = colorID;
        this.invitationProcesses = invitationProcesses;
        this.invitationProcesses.addInvitationProcess(this);

        Saros.reinject(this);
    }

    /**
     * @return the peer that is participating with us in this process. For an
     *         incoming invitation this is the inviter. For an outgoing
     *         invitation this is the invitee.
     */
    public JID getPeer() {
        return this.peer;
    }

    /**
     * @return the user-provided informal description that can be provided with
     *         an invitation.
     */
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return "InvitationProcess(peer:" + this.peer + ")";
    }

    /**
     * 
     * @return the name of the project that is shared by the peer.
     */
    public abstract String getProjectName();

    public enum CancelOption {
        /**
         * Use this option if the peer should be notified that the invitation
         * has been cancelled. He gets a message with the cancellation reason.
         */
        NOTIFY_PEER,
        /**
         * Use this option if the peer should not be notified that the
         * invitation has been cancelled.
         */
        DO_NOT_NOTIFY_PEER;
    }

    public enum CancelLocation {
        /**
         * Use this option if the invitation has been cancelled by the local
         * user.
         */
        LOCAL,
        /**
         * Use this option if the invitation has been cancelled by the remote
         * user.
         */
        REMOTE;
    }

    public abstract void remoteCancel(String errorMsg);

    public static class ArchiveStreamService extends StreamService {

        protected IProject sharedProject;
        protected int numOfFiles;

        protected StreamSession streamSession;

        protected Lock startLock = new ReentrantLock();
        protected Condition sessionReceived = startLock.newCondition();

        @Override
        public String getServiceName() {
            return "ArchiveProject";
        }

        @Override
        public int getStreamsPerSession() {
            return 1;
        }

        @Override
        public int[] getChunkSize() {
            return new int[] { 1024 * 1024 };
        }

        public void setProject(IProject sp) {
            this.sharedProject = sp;
        }

        public void setFileNumber(int num) {
            this.numOfFiles = num;
        }

        @Override
        public boolean sessionRequest(User from, Object initial) {
            log.info(from + " wants to stream the project archive.");

            return true;
        }

        @Override
        public void startSession(final StreamSession newSession) {

            this.startLock.lock();
            this.streamSession = newSession;
            this.sessionReceived.signal();
            this.startLock.unlock();

        }

    }
}
