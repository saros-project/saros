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
package de.fu_berlin.inf.dpp.invitation.internal;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * @author rdjemili
 */
public abstract class InvitationProcess implements IInvitationProcess {

	private static Logger logger = Logger.getLogger(InvitationProcess.class);
	
	protected final ITransmitter transmitter;

	protected State state;

	private Exception exception;

	protected JID peer;

	protected IInvitationUI invitationUI=null;

	protected String description;

	public InvitationProcess(ITransmitter transmitter, JID peer, String description) {
		this.transmitter = transmitter;
		this.peer = peer;
		this.description = description;

		transmitter.addInvitationProcess(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IInvitationProcess
	 */
	public Exception getException() {
		return exception;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IInvitationProcess
	 */
	public State getState() {
		return state;
	}
	
	public void setState(State newstate) {
		state = newstate;
		
		if (invitationUI!=null)
			invitationUI.updateInvitationProgress(peer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IInvitationProcess
	 */
	public JID getPeer() {
		return peer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IInvitationProcess
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.invitation.IInvitationProcess
	 */
	public void cancel(final String errorMsg, final boolean replicated) {
		if (state == State.CANCELED)
			return;

		setState(State.CANCELED);

		logger.error("Invitation was canceled. " + errorMsg);

		if (!replicated) {
			transmitter.sendCancelInvitationMessage(peer, errorMsg);
		}

		invitationUI.cancel(errorMsg, replicated);

		transmitter.removeInvitationProcess(this);
	}

	@Override
	public String toString() {
		return "InvitationProcess(peer:" + peer + ", state:" + state + ")";
	}

	/**
	 * Should be called if an exception occured. This saves the exception and
	 * sets the invitation to cancelled.
	 */
	protected void failed(Exception e) {
		exception = e;
		e.printStackTrace(); // HACK
		cancel(e.getMessage(), false);
	}

	/**
	 * Asssert that the process is in given state or throw an exception
	 * otherwise.
	 * 
	 * @param expected
	 *            the state that the process should currently have.
	 */
	protected void assertState(State expected) {
		if (state != expected)
			cancel("Unexpected state(" + state + ")", false);
	}

	protected void failState() {
		throw new IllegalStateException("Bad input while in state " + state);
	}
	
	public void setInvitationUI(IInvitationUI inviteUI){
		this.invitationUI = inviteUI;
	}

}
