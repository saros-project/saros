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
package de.fu_berlin.inf.dpp;

import java.awt.Toolkit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.osgi.framework.BundleContext;

import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.PacketExtensions;
import de.fu_berlin.inf.dpp.project.ActivityRegistry;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;

/**
 * The main plug-in of Saros.
 * 
 * @author rdjemili
 * @author coezbek
 */
public class Saros extends AbstractUIPlugin {

    public static enum ConnectionState {
	NOT_CONNECTED, CONNECTING, CONNECTED, DISCONNECTING, ERROR
    };

    // The shared instance.
    private static Saros plugin;

    public static final String SAROS = "de.fu_berlin.inf.dpp"; //$NON-NLS-1$

    private static SarosUI uiInstance;

    private XMPPConnection connection;

    private JID myjid;

    private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

    private String connectionError;

    private MessagingManager messagingManager;

    private ISessionManager sessionManager;

    // TODO use ListenerList instead
    private final List<IConnectionListener> listeners = new CopyOnWriteArrayList<IConnectionListener>();

    // Smack (XMPP) connection listener
    private final ConnectionListener smackConnectionListener = new XMPPConnectionListener();

    static {
	PacketExtensions.hookExtensionProviders();
	Roster.setDefaultSubscriptionMode(SubscriptionMode.accept_all);
    }

    /**
     * Create the shared instance.
     */
    public Saros() {
	Saros.plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {
	super.start(context);
	XMPPConnection.DEBUG_ENABLED = getPreferenceStore().getBoolean(
		PreferenceConstants.DEBUG);

	setupLoggers();

	this.messagingManager = new MessagingManager();
	this.sessionManager = new SessionManager();

	ActivityRegistry.getDefault();
	SkypeManager.getDefault();

	Saros.uiInstance = new SarosUI(this.sessionManager);

	boolean hasUserName = getPreferenceStore().getString(
		PreferenceConstants.USERNAME).length() > 0;

	if (getPreferenceStore().getBoolean(PreferenceConstants.AUTO_CONNECT)
		&& hasUserName) {
	    asyncConnect();
	}

	// if (!hasUserName){
	//			
	// Display.getDefault().asyncExec(new Runnable() {
	// public void run() {
	// try {
	// Shell shell = Display.getDefault().getActiveShell();
	// new WizardDialog(shell, new ConfigurationWizard()).open();
	// } catch (Exception e) {
	// Saros.getDefault().getLog().log(
	// new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
	// "Error while running configuration wizard", e));
	// }
	// }
	// });
	// }
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
	super.stop(context);

	this.sessionManager.leaveSession();
	disconnect(null);

	Saros.plugin = null;
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance.
     */
    public static Saros getDefault() {
	return Saros.plugin;
    }

    public JID getMyJID() {
	return this.myjid;
    }

    public SarosUI getUI() { // HACK
	return Saros.uiInstance;
    }

    public Roster getRoster() {
	if (!isConnected()) {
	    return null;
	}

	return this.connection.getRoster();
    }

    /**
     * @return the MessagingManager which is responsible for handling instant
     *         messaging. Is never <code>null</code>.
     */
    public MessagingManager getMessagingManager() {
	return this.messagingManager;
    }

    /**
     * @return the SessionManager. Is never <code>null</code>.
     */
    public ISessionManager getSessionManager() {
	return this.sessionManager;
    }

    public void asyncConnect() {
	new Thread(new Runnable() {
	    public void run() {
		connect(false);
	    }
	}).start();
    }

    /**
     * Connects according to the preferences. This is a blocking method.
     * 
     * If there is already a established connection when calling this method, it
     * disconnects before connecting.
     */
    public void connect(boolean reconnect) {

	IPreferenceStore prefStore = getPreferenceStore();
	final String server = prefStore.getString(PreferenceConstants.SERVER);
	final String username = prefStore
		.getString(PreferenceConstants.USERNAME);
	String password = prefStore.getString(PreferenceConstants.PASSWORD);

	try {
	    if (!reconnect) {
		if (isConnected()) {
		    disconnect(null);

		    setConnectionState(ConnectionState.CONNECTING, null);
		}
	    } else if (isConnected()) {
		// connection.close();
		this.connection.disconnect();
		this.connection
			.removeConnectionListener(this.smackConnectionListener);
	    }

	    // TODO: for testing reconnection
	    ConnectionConfiguration conConfig = new ConnectionConfiguration(
		    server);
	    conConfig.setReconnectionAllowed(true);

	    // connection = new XMPPConnection(server);
	    this.connection = new XMPPConnection(conConfig);
	    this.connection.connect();
	    this.connection.login(username, password);

	    // TODO: ConnectionListener
	    this.connection.addConnectionListener(this.smackConnectionListener);
	    setConnectionState(ConnectionState.CONNECTED, null);

	    this.myjid = new JID(this.connection.getUser());

	} catch (final Exception e) {
	    // disconnect(e.getMessage());

	    if (!reconnect) {
		setConnectionState(ConnectionState.NOT_CONNECTED, null);
		Display.getDefault().syncExec(new Runnable() {
		    public void run() {
			MessageDialog.openError(Display.getDefault()
				.getActiveShell(), "Error Connecting",
				"Could not connect to server '" + server
					+ "' as user '" + username
					+ "'.\nErrorMessage was: "
					+ e.getMessage());
		    }
		});
	    }
	}
    }

    /**
     * Disconnects. This is a blocking method.
     * 
     * @param reason
     *            the error why the connection was closed. If the connection was
     *            not closed due to an error <code>null</code> should be passed.
     */
    public void disconnect(String error) {
	setConnectionState(ConnectionState.DISCONNECTING, error);

	if (this.connection != null) {
	    // leave running session before disconnecting
	    getSessionManager().leaveSession();

	    this.connection
		    .removeConnectionListener(this.smackConnectionListener);
	    // connection.close();
	    // TODO: Änderung für Smack 3
	    this.connection.disconnect();
	    this.connection = null;
	}

	setConnectionState(error == null ? ConnectionState.NOT_CONNECTED
		: ConnectionState.ERROR, error);

	this.myjid = null;

    }

    /**
     * Creates the given account on the given Jabber server. This is a blocking
     * method.
     * 
     * @param server
     *            the server on which to create the account.
     * @param username
     *            the username for the new account.
     * @param password
     *            the password for the new account.
     * @param monitor
     *            the progressmonitor for the operation.
     * @throws XMPPException
     *             exception that occcurs while registering.
     */
    public void createAccount(String server, String username, String password,
	    IProgressMonitor monitor) throws XMPPException {

	monitor.beginTask("Registering account", 3);

	XMPPConnection connection = new XMPPConnection(server);
	monitor.worked(1);

	connection.connect();
	monitor.worked(1);

	connection.getAccountManager().createAccount(username, password);
	monitor.worked(1);

	// connection.close();
	// TODO: Änderung für Smack 3
	connection.disconnect();
	monitor.done();
    }

    /**
     * Adds given contact to the roster. This is a blocking method.
     * 
     * @param jid
     *            the Jabber ID of the contact.
     * @param nickname
     *            the nickname under which the new contact should appear in the
     *            roster.
     * @param groups
     *            the groups to which the new contact should belong to. This
     *            information will be saved on the server.
     * @throws XMPPException
     *             is thrown if no connection is establised.
     */
    public void addContact(JID jid, String nickname, String[] groups)
	    throws XMPPException {
	assertConnection();
	this.connection.getRoster().createEntry(jid.toString(), nickname,
		groups);
    }

    /**
     * Removes given contact from the roster. This is a blocking method.
     * 
     * @param rosterEntry
     *            the contact that is to be removed
     * @throws XMPPException
     *             is thrown if no connection is establised.
     */
    public void removeContact(RosterEntry rosterEntry) throws XMPPException {
	assertConnection();
	this.connection.getRoster().removeEntry(rosterEntry);
    }

    public boolean isConnected() {
	//		
	// new Thread(new Runnable() {
	// public void run() {
	// int counter = 0;
	// while(counter < 30){
	// if(connection != null && connection.isConnected()){
	// System.out.println(".");
	// }
	// else{
	// System.out.println("-");
	// }
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// counter++;
	// }
	// }
	// }).start();
	//		
	// System.out.println("reconnect enable: "+connection.isConnected());
	// connection.disconnect();
	return (this.connection != null) && this.connection.isConnected();
    }

    /**
     * @return the current state of the connection.
     */
    public ConnectionState getConnectionState() {
	return this.connectionState;
    }

    /**
     * @return an error string that contains the error message for the current
     *         connection error if the state is {@link ConnectionState.ERROR} or
     *         <code>null</code> if there is another state set.
     */
    public String getConnectionError() {
	return this.connectionError;
    }

    /**
     * @return the currently established connection or <code>null</code> if
     *         there is none.
     */
    public XMPPConnection getConnection() {
	return this.connection;
    }

    public void addListener(IConnectionListener listener) {
	if (!this.listeners.contains(listener)) {
	    this.listeners.add(listener);
	}
    }

    public void removeListener(IConnectionListener listener) {
	this.listeners.remove(listener);
    }

    private void assertConnection() throws XMPPException {
	if (!isConnected()) {
	    throw new XMPPException("No connection");
	}
    }

    /**
     * Sets a new connection state and notifies all connection listeners.
     */
    private void setConnectionState(ConnectionState state, String error) {
	this.connectionState = state;
	this.connectionError = error;

	for (IConnectionListener listener : this.listeners) {
	    listener.connectionStateChanged(this.connection, state);
	}
    }

    private void setupLoggers() {
	try {

	    PropertyConfigurator.configureAndWatch("log4j.properties",
		    60 * 1000);
	    Logger.getLogger("de.fu_berlin.inf.dpp");

	    // Logger sarosRootLogger =
	    // Logger.getLogger("de.fu_berlin.inf.dpp");
	    // sarosRootLogger.setLevel(Level.ALL);

	    // Handler handler = new FileHandler("saros.log", 10 * 1024 * 1024,
	    // 1, true);
	    // Handler handler = new ConsoleHandler();
	    // handler.setFormatter(new SimpleFormatter());
	    // sarosRootLogger.addHandler(handler);

	    // handler = new ConsoleHandler();
	    // sarosRootLogger.addHandler(handler);

	} catch (SecurityException e) {
	    e.printStackTrace();
	}
	// catch (IOException e) {
	// e.printStackTrace();
	// }
    }

    /**
     * Log a message to the Eclipse ErrorLog. This method should be used to log
     * all errors that occur in the plugin that cannot be corrected by the user
     * and seem to be errors within the plug-in or the used libraries.
     * 
     * @param message
     *            A meaningful description of during which operation the error
     *            occurred
     * @param e
     *            The exception associated with the error (may be null)
     */
    public static void log(String message, Exception e) {
	Saros.getDefault().getLog().log(
		new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR, message,
			e));
    }

    private class XMPPConnectionListener implements ConnectionListener {

	// Variables to support listener notifications verification
	private boolean connectionClosed = false;
	private boolean connectionClosedOnError = false;
	private boolean reconnected = false;
	private boolean reconnectionFailed = false;
	private int remainingSeconds = 0;
	private int attemptsNotifications = 0;
	private final boolean reconnectionCanceled = false;

	public void connectionClosed() {

	    // self inflicted, controlled disconnect
	    this.connectionClosed = true;
	}

	public void connectionClosedOnError(Exception e) {

	    this.connectionClosedOnError = true;

	    Toolkit.getDefaultToolkit().beep();
	    System.out.println("XMPP Connection Error: " + e.toString());

	    if (Saros.this.connection != null) {
		Saros.this.connection
			.removeConnectionListener(Saros.this.smackConnectionListener);
		Saros.this.connection.disconnect();
		// TODO: Änderung
		// connection.close();
	    }

	    if (getConnectionState() != ConnectionState.ERROR) {
		setConnectionState(ConnectionState.ERROR,
			"XMPP Connection Error");

		new Thread(new Runnable() {

		    public void run() {

			int offlineAtTS = 0;
			if (Saros.this.sessionManager.getSharedProject() != null) {
			    offlineAtTS = Saros.this.sessionManager
				    .getSharedProject().getSequencer()
				    .getTimestamp();
			}

			try {
			    do {
				connect(true);

				if (!Saros.this.connection.isConnected()) {
				    Thread.sleep(5000);
				}

			    } while (!Saros.this.connection.isConnected());

			    if (Saros.this.connection.isConnected()) {
				Saros.this.sessionManager
					.OnReconnect(offlineAtTS);
				System.out.println("XMPP reconnected");
			    }

			} catch (InterruptedException e) {

			    e.printStackTrace();
			}
		    }
		}).start();

	    }

	}

	public void reconnectingIn(int seconds) {
	    // TODO Auto-generated method stub
	    System.out.println("saros reconnectingIn");
	    this.attemptsNotifications = this.attemptsNotifications + 1;
	    this.remainingSeconds = seconds;
	}

	public void reconnectionFailed(Exception e) {
	    // TODO Auto-generated method stub
	    System.out.println("saros reconnectionFailed");
	    this.reconnectionFailed = true;
	}

	public void reconnectionSuccessful() {
	    // TODO Auto-generated method stub
	    System.out.println("saros reconnectionSuccessful");
	    this.reconnected = true;
	}
    }

}
