package de.fu_berlin.inf.dpp.net.util;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.XMPPUtil;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Utility class for classic {@link Roster} operations
 * 
 * @author bkahlert
 */
public class RosterUtils {
	private static final Logger log = Logger.getLogger(RosterUtils.class);

	private RosterUtils() {
		// no instantiation allowed
	}

	protected static class DialogContent {

		public DialogContent(String dialogTitle, String dialogMessage,
				String invocationTargetExceptionMessage) {
			super();
			this.dialogTitle = dialogTitle;
			this.dialogMessage = dialogMessage;
			this.invocationTargetExceptionMessage = invocationTargetExceptionMessage;
		}

		/**
		 * Title displayed in the question dialog
		 */
		String dialogTitle;

		/**
		 * Message displayed in the question dialog
		 */
		String dialogMessage;

		/**
		 * Detailed message for the InvocationTargetMessage
		 */
		String invocationTargetExceptionMessage;
	}

	protected static DialogContent getDialogContent(XMPPException e) {
		if (e.getMessage().contains("item-not-found")) {
			return new DialogContent("Buddy Unknown",
					"The buddy is unknown to the XMPP/Jabber server.\n\n"
							+ "Do you want to add the buddy anyway?",
					"Buddy unknown to XMPP/Jabber server.");
		}

		if (e.getMessage().contains("remote-server-not-found")) {
			return new DialogContent("Server Not Found",
					"The responsible XMPP/Jabber server could not be found.\n\n"
							+ "Do you want to add the buddy anyway?",
					"Unable to find the responsible XMPP/Jabber server.");

		}

		if (e.getMessage().contains("501")) {
			return new DialogContent(
					"Unsupported Buddy Status Check",
					"The responsible XMPP/Jabber server does not support status requests.\n\n"
							+ "If the buddy exists you can still successfully add him.\n\n"
							+ "Do you want to try to add the buddy?",
					"Buddy status check unsupported by XMPP/Jabber server.");
		}

		if (e.getMessage().contains("503")) {
			return new DialogContent(
					"Unknown Buddy Status",
					"For privacy reasons the XMPP/Jabber server does not reply to status requests.\n\n"
							+ "If the buddy exists you can still successfully add him.\n\n"
							+ "Do you want to try to add the buddy?",
					"Unable to check the buddy status.");
		}

		if (e.getMessage().contains("No response from the server")) {
			return new DialogContent(
					"Server Not Responding",
					"The responsible XMPP/Jabber server is not connectable.\n"
							+ "The server is either inexistent or offline right now.\n\n"
							+ "Do you want to add the buddy anyway?",
					"The XMPP/Jabber server did not respond.");
		}

		return new DialogContent("Unknown Error",
				"An unknown error has occured:\n\n" + e.getMessage() + "\n\n"
						+ "Do you want to add the buddy anyway?",
				"Unknown error: " + e.getMessage());
	}

	/**
	 * @return The nickname associated with the given JID in the current roster
	 *         or null if the current roster is not available or the nickname
	 *         has not been set.
	 */
	public static String getNickname(Saros saros, JID jid) {

		if (saros == null)
			return null;

		XMPPConnection connection = saros.getConnection();
		if (connection == null)
			return null;

		Roster roster = connection.getRoster();
		if (roster == null)
			return null;

		RosterEntry entry = roster.getEntry(jid.getBase());
		if (entry == null)
			return null;

		String nickName = entry.getName();
		if (nickName != null && nickName.trim().length() > 0) {
			return nickName;
		}
		return null;
	}

	public static String getDisplayableName(RosterEntry entry) {
		String nickName = entry.getName();
		if (nickName != null && nickName.trim().length() > 0) {
			return nickName.trim();
		}
		return entry.getUser();
	}

	/**
	 * Creates the given account on the given XMPP server.
	 * 
	 * @blocking
	 * 
	 * @param server
	 *            the server on which to create the account.
	 * @param username
	 *            for the new account.
	 * @param password
	 *            for the new account.
	 * @param monitor
	 *            for the operation.
	 * @throws InvocationTargetException
	 *             exception that occurs while registering.
	 */
	public static void createAccount(String server, String username,
			String password, IProgressMonitor monitor)
			throws InvocationTargetException {

		if (monitor == null)
			monitor = SubMonitor.convert(monitor);

		monitor.beginTask("Registering account...", 3);

		try {
			XMPPConnection connection = new XMPPConnection(server);
			monitor.worked(1);

			connection.connect();
			monitor.worked(1);

			String errorMessage = isAccountCreationPossible(connection,
					username);
			if (errorMessage != null)
				throw new XMPPException(errorMessage);

			monitor.worked(1);

			AccountManager manager = connection.getAccountManager();
			manager.createAccount(username, password);
			monitor.worked(1);

			connection.disconnect();
		} catch (XMPPException e) {
			String message = e.getMessage();
			XMPPError error = e.getXMPPError();
			if (error != null) {
				message = error.getMessage();
				if (message == null) {
					if (error.getCode() == 409)
						message = "The XMPP/Jabber account already exists.";
					else
						message = "An unknown error occured. Please register on provider's website.";
				}
			}
			throw new InvocationTargetException(e, message);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Checks whether a {@link Roster} account with the given username on the
	 * given server can be created.
	 * <p>
	 * <b>IMPORTANT:</b> Returns null if the account creation is possible.
	 * 
	 * @param connection
	 *            to the server to check
	 * @param username
	 *            to be used for account creation
	 * @return null if account creation is possible; otherwise error message
	 *         which describes why the account creation can not be perfomed.
	 */
	public static String isAccountCreationPossible(XMPPConnection connection,
			String username) {
		String errorMessage = null;

		Registration registration = null;
		try {
			registration = XMPPUtil.getRegistrationInfo(username, connection);
		} catch (XMPPException e) {
			log.error("Server " + connection.getHost()
					+ " does not support XEP-0077"
					+ " (In-Band Registration) properly:", e);
		}
		if (registration != null && registration.getError() != null) {
			if (registration.getAttributes().containsKey("registered")) {
				errorMessage = "Account " + username
						+ " already exists on the server.";
			} else if (!registration.getAttributes().containsKey("username")) {
				if (registration.getInstructions() != null) {
					errorMessage = "Registration via Saros not possible.\n\n"
							+ "Please follow these instructions:\n"
							+ registration.getInstructions();
				} else {
					errorMessage = "Registration via Saros not possible.\n\n"
							+ "Please see the server's web site for\n"
							+ "informations for how to create an account.";
				}
			} else {
				errorMessage = "No in-band registration. Please create account on provider's website.";
				log.warn("Unknow registration error: "
						+ registration.getError().getMessage());
			}
		}

		return errorMessage;
	}

	/**
	 * Adds given buddy to the {@link Roster}.
	 * 
	 * @param connection
	 * @param jid
	 * @param nickname
	 * @param monitor
	 * @throws InvocationTargetException
	 */
	public static void addToRoster(Connection connection, final JID jid,
			String nickname, SubMonitor monitor)
			throws InvocationTargetException {

		if (monitor == null)
			monitor = SubMonitor.convert(monitor);
		if (connection == null)
			throw new InvocationTargetException(new NullPointerException(),
					"You need to be connected to an XMPP/Jabber server.");

		monitor.beginTask("Adding buddy " + jid + "...", 3);
		try {
			try {
				boolean jidOnServer = isJIDonServer(connection, jid,
						monitor.newChild(1));
				if (!jidOnServer) {
					boolean cancel = false;
					try {
						cancel = Utils.runSWTSync(new Callable<Boolean>() {
							public Boolean call() throws Exception {
								return !DialogUtils
										.openQuestionMessageDialog(
												null,
												"Buddy Unknown",
												"You entered a valid XMPP/Jabber server.\n\n"
														+ "Unfortunately your entered JID is unknown to the server.\n"
														+ "Please make sure you spelled the JID correctly.\n\n"
														+ "Do you want to add the buddy anyway?");
							}
						});
					} catch (Exception e) {
						log.debug("Error opening questionMessageDialog", e);
					}

					if (cancel) {
						throw new InvocationTargetException(
								new XMPPException(
										"Please make sure you spelled the JID correctly."));
					}
					log.debug("The buddy " + jid
							+ " couldn't be found on the server."
							+ " The user chose to add it anyway.");

				}
			} catch (XMPPException e) {
				final DialogContent dialogContent = getDialogContent(e);

				boolean cancel = false;

				try {
					cancel = Utils.runSWTSync(new Callable<Boolean>() {
						public Boolean call() throws Exception {
							return !DialogUtils.openQuestionMessageDialog(null,
									dialogContent.dialogTitle,
									dialogContent.dialogMessage);
						}
					});
				} catch (Exception e1) {
					log.debug("Error opening questionMessageDialog", e);
				}

				if (cancel)
					throw new InvocationTargetException(e,
							dialogContent.invocationTargetExceptionMessage);

				log.warn("Problem while adding a buddy. User decided to add buddy anyway. Problem:\n"
						+ e.getMessage());
			}

			try {
				monitor.worked(1);

				/*
				 * Add the buddy to the Roster
				 */
				connection.getRoster().createEntry(jid.toString(), nickname,
						null);
			} catch (XMPPException e) {
				throw new InvocationTargetException(e, "Unknown error: "
						+ e.getMessage());
			} finally {
				monitor.done();
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Removes given buddy from the {@link Roster}.
	 * 
	 * @blocking
	 * 
	 * @param rosterEntry
	 *            the buddy that is to be removed
	 * @throws XMPPException
	 *             is thrown if no connection is established.
	 */
	public static void removeFromRoster(Connection connection,
			RosterEntry rosterEntry) throws XMPPException {
		if (!connection.isConnected()) {
			throw new XMPPException("Not connected");
		}
		connection.getRoster().removeEntry(rosterEntry);
	}

	/**
	 * Returns whether the given JID can be found on the server.
	 * 
	 * @blocking
	 * @cancelable
	 * 
	 * @param connection
	 * @param monitor
	 *            a {@link SubMonitor} to report progress to; may be null
	 * @throws XMPPException
	 *             if the service discovery failed
	 */
	public static boolean isJIDonServer(Connection connection, JID jid,
			SubMonitor monitor) throws XMPPException {
		if (monitor != null)
			monitor.beginTask("Performing Service Discovery on JID " + jid, 2);

		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				.getInstanceFor(connection);

		if (monitor != null) {
			monitor.worked(1);
			if (monitor.isCanceled())
				throw new CancellationException();
		}

		try {
			boolean discovered = sdm.discoverInfo(jid.toString())
					.getIdentities().hasNext();
			/*
			 * discovery does not change any state, if the user wanted to cancel
			 * it, we can do that even after the execution finished
			 */
			if (monitor != null && monitor.isCanceled())
				throw new CancellationException();
			return discovered;
		} finally {
			if (monitor != null)
				monitor.done();
		}
	}

}