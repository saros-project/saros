package de.fu_berlin.inf.dpp.project.internal;

import java.util.List;

import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.ChangeColorActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * This manager is responsible for handling color changes.
 * 
 * @author cnk and tobi
 */
@Component(module = "core")
public class ChangeColorManager extends AbstractActivityProvider implements
    Startable {

    private static final Logger log = Logger
        .getLogger(ChangeColorManager.class);

    private final ISarosSession sarosSession;
    private final EditorManager editorManager;

    public ChangeColorManager(ISarosSession sarosSession,
        EditorManager editorManager) {
        this.sarosSession = sarosSession;
        this.editorManager = editorManager;
    }

    @Override
    public void exec(IActivity activity) {
        activity.dispatch(receiver);
    }

    private AbstractActivityReceiver receiver = new AbstractActivityReceiver() {

        @Override
        public void receive(ChangeColorActivity activity) {
            handleChangeColorActivity(activity);
        }
    };

    private void handleChangeColorActivity(ChangeColorActivity activity) {

        User source = activity.getSource();
        User affected = activity.getAffected();
        int colorID = activity.getColorID();

        int assignedColorID;

        if (affected == null) {
            log.warn("received color id change for a user that is no longer part of the session");
            return;
        }

        log.debug("received color id change fo user : " + affected + " ["
            + activity.getColorID() + "]");

        // host send us an update for a user
        if (source.isHost() && !sarosSession.isHost()) {
            sarosSession.returnColor(affected.getColorID());

            assignedColorID = sarosSession.getColor(colorID);

            if (assignedColorID != colorID) {
                log.error("received a color id change for an id that is already in use");
                /*
                 * We should stop the session here as something seriously had
                 * gone wrong
                 */

                // try to revert
                sarosSession.returnColor(assignedColorID);
                assignedColorID = sarosSession.getColor(affected.getColorID());

                // give up
                if (assignedColorID != affected.getColorID())
                    sarosSession.returnColor(assignedColorID);

                return;
            }
            // this fails if a new copy is returned !
            affected.setColorID(colorID);
        } else {

            // FIXME race condition as remove and add are not atomic !

            assert sarosSession.isHost() : "only the session host can assign a color id";

            assignedColorID = sarosSession.getColor(colorID);

            if (assignedColorID != colorID) {
                log.debug("could not assign color id '" + colorID
                    + "' to user " + affected + " because it is already in use");
                sarosSession.returnColor(assignedColorID);
                return;
            }

            log.debug("readding color id " + affected.getColorID()
                + " to the pool");

            sarosSession.returnColor(affected.getColorID());

            affected.setColorID(colorID);
            broadcastColorChange(affected, affected.getColorID());
        }

        editorManager.colorChanged();
        editorManager.refreshAnnotations();
    }

    @Override
    public void start() {
        sarosSession.addActivityProvider(this);
    }

    @Override
    public void stop() {
        sarosSession.removeActivityProvider(this);
    }

    /**
     * Changes the color id for the current local user. The change is done
     * asynchronously and may not be available immediately.
     * 
     * @param colorID
     *            the new color ID for the current session
     */
    public void changeColorID(int colorID) {
        fireActivity(new ChangeColorActivity(sarosSession.getLocalUser(),
            sarosSession.getHost(), sarosSession.getLocalUser(), colorID));
    }

    private void broadcastColorChange(User affected, int colorID) {

        assert sarosSession.isHost() : "only the session host can broadcast color id changes";

        List<User> currentRemoteSessionUsers = sarosSession.getRemoteUsers();

        for (User user : currentRemoteSessionUsers)
            fireActivity(new ChangeColorActivity(sarosSession.getLocalUser(),
                user, affected, colorID));
    }
}
