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
package de.fu_berlin.inf.dpp.internal;

import de.fu_berlin.inf.dpp.IInvitationProcess;
import de.fu_berlin.inf.dpp.ITransmitter;
import de.fu_berlin.inf.dpp.xmpp.JID;

public abstract class InvitationProcess implements IInvitationProcess {
    
    public class InvitationException extends Exception {
        InvitationException(Exception cause) {
            super(cause);
        }
    }
    
    protected final ITransmitter transmitter;
    
    protected State              state;
    protected Exception          exception;
    
    protected JID                peer; 
    protected String             description;
    
    
    public InvitationProcess(ITransmitter transmitter, JID peer, String description) {
        this.transmitter = transmitter;
        this.peer = peer;
        this.description = description;
        
        transmitter.addInvitationProcess(this);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IInvitationProcess#getException()
     */
    public Exception getException() {
        return exception;
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IInvitationProcess#getState()
     */
    public State getState() {
        return state;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IInvitationProcess#getPeer()
     */
    public JID getPeer() {
        return peer;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IInvitationProcess#getDescription()
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Asssert that the process is in given state or throw an exception
     * otherwise.
     * 
     * @param expected the state that the process should currently have.
     */
    protected void assertState(State expected) {
        if (state != expected) {
            IllegalStateException exception = new IllegalStateException(
                "State should've been "+expected+" instead of "+state);
            
            setException(exception);
            throw exception;
        }
    }
    
    protected void failState() {
        throw new IllegalStateException("Bad input while in state "+state);
    }
    
    protected void setException(Exception e) {
        exception = e;
        e.printStackTrace();
        
        state = State.FAILED;
        transmitter.removeInvitationProcess(this); // HACK
    }
    
    protected void cancel() {
        System.out.println(this + " was canceled"); // HACK
        state = State.CANCELED;
        
        transmitter.removeInvitationProcess(this); // HACK
    }
}
