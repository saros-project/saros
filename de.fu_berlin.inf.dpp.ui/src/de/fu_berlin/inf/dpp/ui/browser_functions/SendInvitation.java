package de.fu_berlin.inf.dpp.ui.browser_functions;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunction.Policy;
import de.fu_berlin.inf.dpp.ui.manager.ProjectListManager;
import de.fu_berlin.inf.dpp.ui.model.Contact;
import de.fu_berlin.inf.dpp.ui.model.ProjectTree;
import de.fu_berlin.inf.dpp.ui.util.ICollaborationUtils;

/**
 * Send an invitation to a number of {@link Contact}s.
 */
public class SendInvitation extends TypedJavascriptFunction {

    private static final String JS_NAME = "sendInvitation";

    private final ProjectListManager projectListManager;
    private final ICollaborationUtils collaborationUtils;

    /**
     * Created by PicoContainer
     * 
     * @param projectListManager
     * @param collaborationUtils
     * @see HTMLUIContextFactory
     */
    public SendInvitation(ProjectListManager projectListManager,
        ICollaborationUtils collaborationUtils) {

        super(JS_NAME);
        this.projectListManager = projectListManager;
        this.collaborationUtils = collaborationUtils;
    }

    /**
     * Send an invitation request with the given resources wrapped by
     * {@link ProjectTree}s to the given {@link Contact}s.
     * <p>
     * Note that this will fail if
     * {@link ProjectListManager#createProjectModels()} hasn't been called yet.
     * 
     * @param projectTrees
     *            The models containing the selected resources to start the
     *            session with
     * @param contactList
     *            The models representing the contacts to start the session with
     */
    @BrowserFunction(Policy.ASYNC)
    public void sendInvitation(final ProjectTree[] projectTrees,
        final Contact[] contactList) {

        List<JID> usersToInvite = new ArrayList<JID>();
        for (Contact contact : contactList)
            usersToInvite.add(new JID(contact.getJid()));

        List<IResource> resourcesToShare = projectListManager
            .getAllResources(projectTrees);

        collaborationUtils.startSession(resourcesToShare, usersToInvite);
    }
}
