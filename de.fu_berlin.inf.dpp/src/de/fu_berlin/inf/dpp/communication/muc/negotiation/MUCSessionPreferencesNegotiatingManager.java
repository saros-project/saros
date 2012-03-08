package de.fu_berlin.inf.dpp.communication.muc.negotiation;

import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/*
 * FIXME this class seems not to transmit anything anymore. This class is only
 * queried for the current configuration. It should be renamed.
 */

/**
 * The CommunicationNegotiatingManager is responsible for transmitting the
 * Communication config of the host to all other participants of the shared
 * project during the Invitation process
 * 
 * @author ologa
 * @author bkahlert
 */
public class MUCSessionPreferencesNegotiatingManager {

	private static final Logger log = Logger
		.getLogger(MUCSessionPreferencesNegotiatingManager.class);

	@Inject
	protected IPreferenceStore preferences;

	protected SessionIDObservable sessionID;

	protected MUCSessionPreferences localPreferences;
	protected MUCSessionPreferences sessionPreferences;

	protected String password;

	private Random random = new Random();

	public MUCSessionPreferencesNegotiatingManager(
		SessionIDObservable sessionID, IPreferenceStore preferences) {
		this.sessionID = sessionID;
		this.password = Integer.toString(random.nextInt());
	}

	/**
	 * Load communication settings from PreferenceStore and generate chat room
	 * and chat room password.
	 */
	public MUCSessionPreferences getOwnPreferences() {
		String service = preferences.getString(PreferenceConstants.CHATSERVER);
		String roomName = "SAROS" + sessionID.getValue();
		return new MUCSessionPreferences(service, roomName, this.password);
	}

	/**
	 * @return temporarily session preferences
	 */
	public MUCSessionPreferences getSessionPreferences() {
		return sessionPreferences;
	}

	/**
	 * Set temporarily communication shared project settings
	 * 
	 * @param remotePreferences
	 *            received communication settings
	 */
	public void setSessionPreferences(MUCSessionPreferences remotePreferences) {
		log.debug("Got hosts Communication Config: server "
				+ remotePreferences.getService() + " room "
				+ remotePreferences.getRoomName() + " pw "
				+ remotePreferences.getPassword());

		sessionPreferences = remotePreferences;
	}

}
