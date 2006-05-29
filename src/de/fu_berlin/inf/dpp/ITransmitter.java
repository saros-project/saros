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
package de.fu_berlin.inf.dpp;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.xmpp.JID;

public interface ITransmitter {
    
    public void addInvitationProcess(IInvitationProcess process);
    public void removeInvitationProcess(IInvitationProcess process);

    // invitations related
    
    public void sendRequestForFileListMessage(JID host);
    public void sendInviteMessage(ISharedProject sharedProject, JID jid, String description);
    public void sendJoinMessage(ISharedProject sharedProject);
    public void sendLeaveMessage(ISharedProject sharedProject);
    public void sendCloseSessionMessage(ISharedProject sharedProject);
    
    public void sendFileList(JID jid, FileList fileList) throws XMPPException, IOException;
    
    // others
    
    // CHECK remove sharedProject param/give inputstream instead (consider testability) 
    public void sendResource(JID jid, IPath path) throws CoreException;
    public void sendResource(JID jid, IPath path, int time) throws CoreException;
    public void sendActivities(ISharedProject sharedProject, List<IActivity> activities, int time) throws CoreException;
}
