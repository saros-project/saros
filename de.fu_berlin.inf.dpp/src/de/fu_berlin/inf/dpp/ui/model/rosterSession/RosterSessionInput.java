package de.fu_berlin.inf.dpp.ui.model.rosterSession;

import org.jivesoftware.smack.Roster;

import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.rosterSession.BuddySessionDisplayComposite;

/**
 * Instances of this class bundle a {@link Roster} and an {@link ISarosSession}
 * instance for use with {@link BuddySessionDisplayComposite}.
 */
public class RosterSessionInput {
    protected Roster roster;
    protected ISarosSession sarosSession;

    public RosterSessionInput(Roster roster, ISarosSession sarosSession) {
        super();
        this.roster = roster;
        this.sarosSession = sarosSession;
    }

    public Roster getRoster() {
        return roster;
    }

    public ISarosSession getSarosSession() {
        return sarosSession;
    }
}
