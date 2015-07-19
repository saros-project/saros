package de.fu_berlin.inf.dpp.ui.browser_functions;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.manager.ProjectListManager;
import de.fu_berlin.inf.dpp.ui.model.Contact;
import de.fu_berlin.inf.dpp.ui.model.ProjectTree;
import de.fu_berlin.inf.dpp.ui.util.ICollaborationUtils;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * Offers a via Javascript invokable method to send an invitation request with
 * the given resources wrapped by {@link ProjectTree}s to the given
 * {@link Contact}s. Note that this will fail if
 * {@link ProjectListManager#createAndMapProjectModels()} hasn't been called
 * yet.
 * <p>
 * JS-signature:
 * "void __java_sendInvitation(String[] projectTreeJson, String[] usersToInviteJID);"
 */
public class SendInvitation extends JavascriptFunction {
    private static final Logger LOG = Logger.getLogger(SendInvitation.class);

    private ProjectListManager projectListManager;
    private ICollaborationUtils collaborationUtils;
    private List<JID> usersToInvite;
    private List<IResource> resourcesToShare;
    private Gson gson = new Gson();

    private static final String ERROR_LOG_INVALID_CALL = "Error while starting session invitation: Invalid arguments.";
    private static final String JS_NAME = "sendInvitation";

    public SendInvitation(ProjectListManager projectListManager,
        ICollaborationUtils collaborationUtils) {
        super(NameCreator.getConventionName(JS_NAME));
        this.projectListManager = projectListManager;
        this.collaborationUtils = collaborationUtils;
    }

    @Override
    public Object function(Object[] arguments) {
        if (arguments.length != 2 || arguments[0] == null
            || arguments[1] == null) {
            LOG.error(ERROR_LOG_INVALID_CALL);
            return null;
        }

        try {
            ProjectTree[] projectTrees = extractProjects(arguments[0]
                .toString());
            Contact[] contactList = extractContacts(arguments[1].toString());
            this.usersToInvite = createUserInviteList(contactList);
            this.resourcesToShare = projectListManager
                .getAllResources(projectTrees);
        } catch (JsonSyntaxException e) {
            LOG.error("Error while converting JSON to Java. Malformed json: ",
                e);
            return null;
        }
        ThreadUtils.runSafeAsync(LOG, new Runnable() {
            @Override
            public void run() {
                collaborationUtils
                    .startSession(resourcesToShare, usersToInvite);
            }
        });
        return null;
    }

    /**
     * @param projectTreesJson
     *            a JSON containing a list of {@link ProjectTree}s
     * @return the {@link ProjectTree}s converted from the given gson as array.
     * @throws JsonSyntaxException
     *             This exception is raised when Gson attempts to read (or
     *             write) a malformed JSON element.
     */
    private ProjectTree[] extractProjects(String projectTreesJson)
        throws JsonSyntaxException {
        return gson.fromJson(projectTreesJson, ProjectTree[].class);
    }

    /**
     * @param contactListJson
     *            a JSON containing a list of {@link Contact}s
     * @return the {@link Contact}s converted from the given gson as array.
     * @throws JsonSyntaxException
     *             This exception is raised when Gson attempts to read (or
     *             write) a malformed JSON element.
     */
    private Contact[] extractContacts(String contactListJson)
        throws JsonSyntaxException {
        return gson.fromJson(contactListJson, Contact[].class);
    }

    private List<JID> createUserInviteList(Contact[] contactList) {
        List<JID> contatcs = new ArrayList<JID>();

        for (Contact contact : contactList) {
            contatcs.add(new JID(contact.getJid()));
        }
        return contatcs;
    }
}
