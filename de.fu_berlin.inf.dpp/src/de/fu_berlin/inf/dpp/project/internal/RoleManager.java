package de.fu_berlin.inf.dpp.project.internal;

import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

/**
 * This manager is responsible for handling driver changes.
 * 
 * @author rdjemili
 */
public class RoleManager implements IActivityProvider, ISharedProjectListener {
	private List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

	private ISharedProject sharedProject;

	public RoleManager() {
		Saros.getDefault().getSessionManager().addSessionListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void sessionStarted(ISharedProject session) {
		sharedProject = session;
		sharedProject.addListener(this);
		sharedProject.getActivityManager().addProvider(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void sessionEnded(ISharedProject session) {
		sharedProject.removeListener(this);
		sharedProject.getActivityManager().removeProvider(this);
		sharedProject = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void invitationReceived(IIncomingInvitationProcess invitation) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IActivityProvider
	 */
	public void addActivityListener(IActivityListener listener) {
		activityListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IActivityProvider
	 */
	public void removeActivityListener(IActivityListener listener) {
		activityListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IActivityProvider
	 */
	public void exec(IActivity activity) {
		if (activity instanceof RoleActivity) {
			RoleActivity roleActivity = (RoleActivity) activity;
			User user = sharedProject.getParticipant(roleActivity.getDriver());
			sharedProject.setDriver(user, true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
	 */
	public void driverChanged(JID driver, boolean replicated) {
		if (!replicated) {
			IActivity activity = new RoleActivity(driver);
			for (IActivityListener listener : activityListeners) {
				listener.activityCreated(activity);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
	 */
	public void userJoined(JID user) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
	 */
	public void userLeft(JID user) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.IActivityProvider
	 */
	public IActivity fromXML(XmlPullParser parser) {
		if (parser.getName().equals("driver")) {
			JID user = new JID(parser.getAttributeValue(null, "id"));
			return new RoleActivity(user);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.IActivityProvider
	 */
	public String toXML(IActivity activity) {
		if (activity instanceof RoleActivity) {
			RoleActivity roleActivity = (RoleActivity) activity;
			return "<driver id=\"" + roleActivity.getDriver() + "\" />";
		}

		return null;
	}
}
