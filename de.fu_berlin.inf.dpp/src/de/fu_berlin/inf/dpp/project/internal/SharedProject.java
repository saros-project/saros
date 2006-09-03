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
package de.fu_berlin.inf.dpp.project.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.internal.OutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.net.IActivitySequencer;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.project.IActivityManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

public class SharedProject implements ISharedProject {
    private static Logger log = Logger.getLogger(SharedProject.class.getName());
    
    protected static final int           MILLIS_UPDATE     = 1000;

    protected JID                        myID;
    protected List<User>                 participants      = new ArrayList<User>();

    private IProject                     project;

    private List<ISharedProjectListener> listeners = new ArrayList<ISharedProjectListener>();

    private User                         driver;
    private User                         host;
    
    private final ITransmitter           transmitter;
    private ActivitySequencer            activitySequencer = new ActivitySequencer();

    
    public SharedProject(ITransmitter transmitter, IProject project, JID myID) { // host
        this.transmitter = transmitter;
        
        this.myID = myID;
        driver = host = new User(myID);
        participants.add(driver);
        
        this.project = project;
    }
    
    public SharedProject(ITransmitter transmitter, IProject project, JID myID, // guest 
            JID host, JID driver, List<JID> allParticipants) {
        
        this.transmitter = transmitter;
        
        this.myID = myID;
        
        this.host = new User(host);
        this.driver = new User(driver);
        
        for (JID jid : allParticipants) { // HACK
            participants.add(new User(jid));
        }
        
        this.project = project;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public List<User> getParticipants() {
        return participants;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public IActivitySequencer getSequencer() {
        return activitySequencer;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public IActivityManager getActivityManager() {
    	return activitySequencer;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public void setDriver(User driver, boolean replicated) {
        assert driver != null;
        
        // TODO if replicated=false check for privileges
        
        if (driver.equals(this.driver))
            return;
        
        this.driver = driver;

        for (ISharedProjectListener listener : listeners) {
            listener.driverChanged(driver.getJid(), replicated);
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public User getDriver() {
        return driver;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public boolean isDriver() {
        return driver.getJid().equals(myID); 
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public User getHost() {
        return host;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public boolean isHost() {
        return host.getJid().equals(myID);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void addUser(User user) {
        if (participants.contains(user)) return;
        
        participants.add(user);
        
        for (ISharedProjectListener listener : listeners) {
            listener.userJoined(user.getJid());
        }
        
        log.info("User "+user.getJid()+" joined session");
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void removeUser(User user) {
        participants.remove(user);
        
        if (driver.equals(user)) {
            // TODO make sure all users have the participants  
            // list in the same order
            setDriver(participants.get(0), true);
        }
        
        for (ISharedProjectListener listener : listeners) {
            listener.userLeft(user.getJid());
        }
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public IOutgoingInvitationProcess invite(JID jid, String description) {
        return new OutgoingInvitationProcess(transmitter, jid, this, description);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void addListener(ISharedProjectListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void removeListener(ISharedProjectListener listener) {
        listeners.remove(listener);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public IProject getProject() {
        return project;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public FileList getFileList() throws CoreException {
        return new FileList(project);
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void start() {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (participants.size() <= 1) {
                    activitySequencer.flush();
                    
                } else {
                    List<TimedActivity> activities = activitySequencer.flushTimed();
                    
                    if (activities != null)
                        transmitter.sendActivities(SharedProject.this, activities);
                }
            }
        }, 0, MILLIS_UPDATE);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public User getParticipant(JID jid) {
        for (User participant : participants) {
            if (participant.getJid().equals(jid)) {
                return participant;
            }
        }
        
        return null;
    }
}
