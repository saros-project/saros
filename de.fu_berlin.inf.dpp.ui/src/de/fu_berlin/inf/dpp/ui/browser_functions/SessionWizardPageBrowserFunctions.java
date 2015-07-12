package de.fu_berlin.inf.dpp.ui.browser_functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.manager.ProjectListManager;
import de.fu_berlin.inf.dpp.ui.model.Contact;
import de.fu_berlin.inf.dpp.ui.model.ProjectTree;
import de.fu_berlin.inf.dpp.ui.util.ICollaborationUtils;
import de.fu_berlin.inf.dpp.ui.webpages.SessionWizardPage;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * This class implements the functions to be called by JavaScript code for the
 * session wizard page. These are so-called browsers functions to invoke Java
 * code from JavaScript.
 */

public class SessionWizardPageBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(SessionWizardPageBrowserFunctions.class);

    private final DialogManager dialogManager;
    private ProjectListManager projectListManager;
    private ICollaborationUtils collaborationUtils;

    private static final String ERROR_LOG_INVALID_CALL = "Error while starting session invitation: Invalid arguments.";

    public SessionWizardPageBrowserFunctions(DialogManager dialogManager,
        ProjectListManager projectListManager,
        ICollaborationUtils collaborationUtils) {
        this.dialogManager = dialogManager;
        this.projectListManager = projectListManager;
        this.collaborationUtils = collaborationUtils;
    }

    /**
     * Returns the list of browser functions encapsulated by this class. They
     * can be injected into a browser so that they can be called from
     * JavaScript.
     */
    public List<JavascriptFunction> getJavascriptFunctions() {
        return Arrays.asList(new JavascriptFunction("__java_sendInvitation") {
            private List<JID> usersToInvite;
            private List<IResource> resourcesToShare;
            private Gson gson = new Gson();

            @Override
            public Object function(Object[] arguments) {
                if (arguments.length != 2 || arguments[0] == null
                    || arguments[1] == null) {
                    showFrontendError(HTMLUIStrings.START_SESSION_CANCELD);
                    LOG.error(ERROR_LOG_INVALID_CALL);
                    return null;
                }

                try {
                    ProjectTree[] projectTrees = extractProjects(arguments[0]
                        .toString());
                    Contact[] contactList = extractContacts(arguments[1]
                        .toString());
                    this.usersToInvite = createUserInviteList(contactList);
                    this.resourcesToShare = projectListManager
                        .getAllResources(projectTrees);
                } catch (JsonSyntaxException e) {
                    LOG.error(
                        "Error while converting JSON to Java. Malformed json: ",
                        e);
                    showFrontendError(HTMLUIStrings.START_SESSION_CANCELD);
                    return null;
                }
                ThreadUtils.runSafeAsync(LOG, new Runnable() {
                    @Override
                    public void run() {
                        collaborationUtils.startSession(resourcesToShare,
                            usersToInvite);
                    }
                });
                return null;
            }

            /**
             * @param projectTreesJson
             *            a JSON containing a list of {@link ProjectTree}s
             * @return the {@link ProjectTree}s converted from the given gson as
             *         array.
             * @throws JsonSyntaxException
             *             This exception is raised when Gson attempts to read
             *             (or write) a malformed JSON element.
             */
            private ProjectTree[] extractProjects(String projectTreesJson)
                throws JsonSyntaxException {
                return gson.fromJson(projectTreesJson, ProjectTree[].class);
            }

            /**
             * @param contactListJson
             *            a JSON containing a list of {@link Contact}s
             * @return the {@link Contact}s converted from the given gson as
             *         array.
             * @throws JsonSyntaxException
             *             This exception is raised when Gson attempts to read
             *             (or write) a malformed JSON element.
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

            /**
             * This will trigger a JS call, that indicates an error.
             * 
             * @param errorMsg
             *            the message that is shown to the user
             */
            // TODO: This should be moved to a central place, since the
            // browser.run("SarosApi.trigger(‘showError’," + MSG + ")") is used
            // in multiple places in the UI project.
            private void showFrontendError(String errorMsg) {
                browser.run("SarosApi.trigger(‘showError’," + errorMsg + ")");
                return;

            }
        }, new JavascriptFunction("__java_closeStartSessionWizard") {
            @Override
            public Object function(Object[] arguments) {
                dialogManager.closeDialogWindow(SessionWizardPage.WEB_PAGE);
                return null;
            }
        });

    }
}
