package de.fu_berlin.inf.dpp.communication.muc.singleton;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.communication.muc.MUCManager;
import de.fu_berlin.inf.dpp.communication.muc.events.IMUCManagerListener;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.communication.muc.session.MUCSession;

/**
 * This abstract class handles the creation and destruction of a single
 * {@link MUCSession}.
 * <p>
 * For convenience reasons this class allows the registration of
 * {@link IMUCManagerListener}s. In difference to the {@link MUCManager} this
 * class only fires events affect the single {@link MUCSession} this class is
 * responsible for.
 * 
 * @author bkahlert
 */
public abstract class AbstractSingletonMUCManager {

    protected MUCManager mucManager;

    protected MUCSession mucSession;

    /**
     * This {@link IMUCManagerListener} forwards all events that have it's
     * origin in this {@link AbstractSingletonMUCManager}.
     * <p>
     * It uses the used {@link MUCSessionPreferences} to compare the equality.
     */
    protected IMUCManagerListener mucManagerListener = new IMUCManagerListener() {
        public void mucSessionCreated(MUCSession mucSession) {
            if (mucSession.getPreferences() == AbstractSingletonMUCManager.this
                .getPreferences()) {
                for (IMUCManagerListener mucManagerListener : AbstractSingletonMUCManager.this.mucManagerListeners) {
                    mucManagerListener.mucSessionCreated(mucSession);
                }
            }
        }

        public void mucSessionJoined(MUCSession mucSession) {
            if (mucSession.getPreferences() == AbstractSingletonMUCManager.this
                .getPreferences()) {
                for (IMUCManagerListener mucManagerListener : AbstractSingletonMUCManager.this.mucManagerListeners) {
                    mucManagerListener.mucSessionJoined(mucSession);
                }
            }
        }

        public void mucSessionLeft(MUCSession mucSession) {
            if (mucSession.getPreferences() == AbstractSingletonMUCManager.this
                .getPreferences()) {
                for (IMUCManagerListener mucManagerListener : AbstractSingletonMUCManager.this.mucManagerListeners) {
                    mucManagerListener.mucSessionLeft(mucSession);
                }
            }
        }

        public void mucSessionDestroyed(MUCSession mucSession) {
            if (mucSession.getPreferences() == AbstractSingletonMUCManager.this
                .getPreferences()) {
                for (IMUCManagerListener mucManagerListener : AbstractSingletonMUCManager.this.mucManagerListeners) {
                    mucManagerListener.mucSessionDestroyed(mucSession);
                }
            }
        }
    };

    protected List<IMUCManagerListener> mucManagerListeners = new ArrayList<IMUCManagerListener>();

    public AbstractSingletonMUCManager(MUCManager mucManager) {
        this.mucManager = mucManager;
        this.mucManager.addMUCManagerListener(mucManagerListener);
    }

    /**
     * Returns the created {@link MUCSession}
     * 
     * @return
     */
    public MUCSession getMUCSession() {
        return this.mucSession;
    }

    /**
     * Returns the {@link MUCSessionPreferences}
     */
    public abstract MUCSessionPreferences getPreferences();

    /**
     * Adds a {@link IMUCManagerListener}
     * 
     * @param mucManagerListener
     */
    public void addMUCManagerListener(IMUCManagerListener mucManagerListener) {
        this.mucManagerListeners.add(mucManagerListener);
    }

    /**
     * Removes a {@link IMUCManagerListener}
     * 
     * @param mucManagerListener
     */
    public void removeMUCManagerListener(IMUCManagerListener mucManagerListener) {
        this.mucManagerListeners.remove(mucManagerListener);
    }
}
