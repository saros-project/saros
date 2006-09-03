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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.osgi.framework.BundleContext;

import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.PacketExtensions;
import de.fu_berlin.inf.dpp.project.ActivityRegistry;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;

/**
 * @author rdjemili
 */
public class Saros extends AbstractUIPlugin {
    public static enum ConnectionState{NOT_CONNECTED, CONNECTING, CONNECTED, DISCONNECTING, ERROR};
    
    // The shared instance.
    private static Saros              plugin;
    private static SarosUI            uiInstance;

    private XMPPConnection            connection;
    private ConnectionState           connectionState  = ConnectionState.NOT_CONNECTED;
    private String                    connectionError;

    private MessagingManager          messagingManager;
    private SessionManager            sessionManager;
    private ActivityRegistry          activityRegister;

//  TODO use ListenerList instead
    private List<IConnectionListener> listeners        = new CopyOnWriteArrayList<IConnectionListener>();
    
    static {
        PacketExtensions.hookExtensionProviders();
    }
    
    /**
     * Create the shared instance.
     */
    public Saros() {
        plugin = this;
    }
    
    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        XMPPConnection.DEBUG_ENABLED = 
            getPreferenceStore().getBoolean(PreferenceConstants.DEBUG);
        
        setupLoggers();
        
        messagingManager = new MessagingManager();
        sessionManager = new SessionManager();
        activityRegister = ActivityRegistry.getDefault();
        
        uiInstance = new SarosUI(sessionManager);
        addPreferencesListener();
        
        if (getPreferenceStore().getBoolean(PreferenceConstants.AUTO_CONNECT)) {
            asyncConnect();
        }
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        
        sessionManager.leaveSession();
        disconnect(null);
        
        plugin = null;
    }
    
    /**
     * Returns the shared instance.
     * 
     * @return the shared instance.
     */
    public static Saros getDefault() {
        return plugin;
    }
    
    public JID getMyJID() {
        return isConnected() ? new JID(connection.getUser()) : null;
    }

    public SarosUI getUI() { // HACK
        return uiInstance;
    }

    public Roster getRoster() {
        if (!isConnected())
            return null;
        
        return connection.getRoster();
    }
    
    /**
     * @return the MessagingManager which is responsible for handling instant
     * messaging. Is never <code>null</code>.
     */
    public MessagingManager getMessagingManager() {
        return messagingManager;
    }
    
    /**
     * @return the SessionManager. Is never <code>null</code>.
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    public void asyncConnect() {
        new Thread(new Runnable() {
            public void run() {
                connect();
            }
        }).start();
    }
    
    /**
     * Connects according to the preferences. This is a blocking method.
     * 
     * If there is already a established connection when calling this method, it
     * disconnects before connecting.
     */
    public void connect() {
        try {
            if (isConnected())
                disconnect(null);
            
            setConnectionState(ConnectionState.CONNECTING, null);
            
            IPreferenceStore preferenceStore = getPreferenceStore();
            String server   = preferenceStore.getString(PreferenceConstants.SERVER);
            String username = preferenceStore.getString(PreferenceConstants.USERNAME);
            String password = preferenceStore.getString(PreferenceConstants.PASSWORD);
            
            connection = new XMPPConnection(server);
            connection.login(username, password);
            
            connectionState = ConnectionState.CONNECTED;
            
            setConnectionState(ConnectionState.CONNECTED, null);
            
        } catch (Exception e) {
            e.printStackTrace();
            
            disconnect(e.getMessage());
        }        
    }
    
    /**
     * Disconnects.This is a blocking method.
     * 
     * @param reason the error why the connection was closed. If the connection
     * was not closed due to an error <code>null</code> is passed.
     */
    public void disconnect(String error) {
        setConnectionState(ConnectionState.DISCONNECTING, error);
        
        if (connection != null) {
            connection.close();
        }
        connection = null;
        
        connectionState = error == null ? ConnectionState.NOT_CONNECTED : ConnectionState.ERROR;
        
        setConnectionState(ConnectionState.NOT_CONNECTED, error);
    }
    
    /**
     * Creates the given account on the given Jabber server. This is a blocking
     * method.
     * 
     * @param server the server on which to create the account.
     * @param username the username for the new account.
     * @param password the password for the new account.
     * @param monitor the progressmonitor for the operation.
     * @throws XMPPException exception that occcurs while registering.
     */
    public void createAccount(String server, String username, String password, 
        IProgressMonitor monitor) throws XMPPException {
        
        monitor.beginTask("Registering account", 3);
        
        XMPPConnection connection = new XMPPConnection(server);
        monitor.worked(1);
        
        connection.getAccountManager().createAccount(username, password);
        monitor.worked(1);
        
        connection.close();
        monitor.done();
    }
    
    /**
     * Adds given contact to the roster. This is a blocking method.
     * 
     * @param jid the Jabber ID of the contact.
     * @param nickname the nickname under which the new contact should appear in
     * the roster.
     * @param groups the groups to which the new contact should belong to. This
     * information will be saved on the server.
     * @throws XMPPException is thrown if no connection is establised.
     */
    public void addContact(JID jid, String nickname, String[] groups) 
        throws XMPPException {
        
        if (!isConnected()) {
            throw new XMPPException("No connection");
        }
        
        connection.getRoster().createEntry(jid.toString(), nickname, groups);
    }
    
    /**
     * Removes given contact from the roster. This is a blocking method.
     * 
     * @param rosterEntry the contact that is to be removed
     * @throws XMPPException is thrown if no connection is establised.
     */
    public void removeContact(RosterEntry rosterEntry) throws XMPPException {
        if (!isConnected()) {
            throw new XMPPException("No connection");
        }
        
        connection.getRoster().removeEntry(rosterEntry);
    }
    
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }
    
    /**
     * @return the current state of the connection.
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }
    
    /**
     * @return an error string that contains the error message for the current
     * connection error if the state is {@link ConnectionState.ERROR} or
     * <code>null</code> if there is another state set.
     */
    public String getConnectionError() {
       return connectionError; 
    }
    
    /**
     * @return the currently established connection or <code>null</code> if
     * there is none.
     */
    public XMPPConnection getConnection() {
        return connection;
    }
    
    public void addListener(IConnectionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(IConnectionListener listener) {
        listeners.remove(listener);
    }

    private void addPreferencesListener() {
        IPreferenceStore store = getPreferenceStore();
        store.addPropertyChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                // reconnect if jabber values were changed reconnect
                if (event.getProperty() == PreferenceConstants.USERNAME ||
                    event.getProperty() == PreferenceConstants.PASSWORD ||
                    event.getProperty() == PreferenceConstants.SERVER) {
                    
                    asyncConnect();
                }
            }
        });
    }
    
    private void setConnectionState(ConnectionState state, String error) {
        connectionState = state;
        connectionError = error;
        
        for (IConnectionListener listener : listeners) {
            listener.connectionStateChanged(connection, state);
        }
    }
    
    private void setupLoggers() {
        try {
            Logger sarosRootLogger = Logger.getLogger("de.fu_berlin.inf.dpp");
            sarosRootLogger.setLevel(Level.ALL);
            
            Handler handler = new FileHandler("saros.log", 10 * 1024 * 1024, 1, true);
            handler.setFormatter(new SimpleFormatter());
            sarosRootLogger.addHandler(handler);
            
            handler = new ConsoleHandler();
            sarosRootLogger.addHandler(handler);
            
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
