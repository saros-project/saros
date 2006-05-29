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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.IActivityListener;
import de.fu_berlin.inf.dpp.IActivityProvider;
import de.fu_berlin.inf.dpp.IActivitySequencer;
import de.fu_berlin.inf.dpp.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.ISharedProject;
import de.fu_berlin.inf.dpp.ITransmitter;
import de.fu_berlin.inf.dpp.EditorManager;
import de.fu_berlin.inf.dpp.SharedResourcesManager;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.activities.TextLoadActivity;
import de.fu_berlin.inf.dpp.listeners.ISharedProjectListener;
import de.fu_berlin.inf.dpp.xmpp.JID;

public class SharedProject implements ISharedProject, IActivityProvider {
    protected static final int           MILLIS_UPDATE     = 1000;

    protected JID                        myID;
    protected List<User>                 participants      = new ArrayList<User>();

    private IProject                     project;

    private List<ISharedProjectListener> listeners = new ArrayList<ISharedProjectListener>();
    private List<IActivityListener>      activityListeners = new LinkedList<IActivityListener>();

    private User                         driver;
    private User                         host;
    
    private IPath                        driverPath;
    private ITextSelection               driverTextSelection;
    
    private EditorManager          editorManager;
    private SharedResourcesManager       sharedResourcesManager;

    private final ITransmitter           transmitter;
    private IActivitySequencer           activitySequencer = new ActivitySequencer();

    
    public SharedProject(ITransmitter transmitter, IProject project, JID myID) { // host
        this.transmitter = transmitter;
        
        this.myID = myID;
        driver = host = new User(myID);
        participants.add(driver);
        
        setupManagers(project);
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
        
        setupManagers(project);
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
    
    public EditorManager getEditorManager() { // HACK
        return editorManager;
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

        // HACK
        for (ISharedProjectListener listener : listeners) {
            listener.driverChanged(driver.getJid(), replicated);
        }
        
        IActivity activity = new RoleActivity(driver.getJid());
        for (IActivityListener listener : activityListeners) {
            listener.activityCreated(activity);
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
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public boolean isHost() {
        return host.getJid().equals(myID);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public void addUser(User user) {
        if (participants.contains(user))
            return;
        
        participants.add(user);
        
        for (ISharedProjectListener listener : listeners) {
            listener.userJoined(user.getJid());
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public void removeUser(User user) {
        participants.remove(user);
        
        for (ISharedProjectListener listener : listeners) {
            listener.userLeft(user.getJid());
        }
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public IOutgoingInvitationProcess invite(JID jid, String description) {
        return new OutgoingInvitationProcess(transmitter, jid, this, description);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public void addListener(ISharedProjectListener listener) {
        listeners.add(listener);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public void removeListener(ISharedProjectListener listener) {
        listeners.remove(listener);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public IProject getProject() {
        return project;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public IPath getDriverPath() {
        return driverPath;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public void setDriverPath(IPath path, boolean replicated) { // HACK
        driverPath = path;
        
        for (ISharedProjectListener listener : listeners) {
            listener.driverPathChanged(driverPath, replicated);
        }
        
        if (!replicated) {
            IActivity activity = new TextLoadActivity(path);
            for (IActivityListener listener : activityListeners) {
                listener.activityCreated(activity);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public ITextSelection getDriverTextSelection() {
        return driverTextSelection;
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void setDriverTextSelection(ITextSelection selection) {
        driverTextSelection = selection;
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void exec(IActivity activity) {
        if (activity instanceof RoleActivity) {
            RoleActivity roleActivity = (RoleActivity)activity;
            setDriver(getParticipant(roleActivity.getDriver()), true);
            
        } else if (activity instanceof TextLoadActivity) {
            TextLoadActivity textLoad = (TextLoadActivity)activity;
            setDriverPath(textLoad.getPath(), true);
        } 
    }

    public FileList getFileList() throws CoreException {
        return new FileList(project);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void addActivityListener(IActivityListener listener) {
        activityListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void removeActivityListener(IActivityListener listener) {
        activityListeners.remove(listener);
    }

    private void setupManagers(IProject project) {
        this.project = project;
        
        editorManager = new EditorManager(this, new HumbleEditorManager());
        sharedResourcesManager = new SharedResourcesManager(this);
        
        activitySequencer.addProvider(editorManager);
        activitySequencer.addProvider(sharedResourcesManager);
        activitySequencer.addProvider(this);
        
        startSendTimer();
    }

    private void startSendTimer() {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<IActivity> activities = activitySequencer.flush();
                if (activities == null) 
                    return;
                
                try {
                    int time = activitySequencer.incTime(activities.size()); // HACK
                    transmitter.sendActivities(SharedProject.this, activities, time);
                } catch (CoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, 0, MILLIS_UPDATE);
    }
    
    private User getParticipant(JID jid) {
        for (User participant : participants) {
            if (participant.getJid().equals(jid)) {
                return participant;
            }
        }
        
        return null;
    }
}
