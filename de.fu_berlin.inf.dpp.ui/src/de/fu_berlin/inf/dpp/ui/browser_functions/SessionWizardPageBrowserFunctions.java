package de.fu_berlin.inf.dpp.ui.browser_functions;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
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

    public SessionWizardPageBrowserFunctions(DialogManager dialogManager) {
        this.dialogManager = dialogManager;

    }

    /**
     * Returns the list of browser functions encapsulated by this class. They
     * can be injected into a browser so that they can be called from
     * JavaScript.
     */
    public List<JavascriptFunction> getJavascriptFunctions() {
        return Arrays.asList(new JavascriptFunction("__java_sendInvitation") {
            @Override
            public Object function(Object[] arguments) {
                if (arguments.length > 0 && arguments[0] != null) {
                    Gson gson = new Gson();
                    /**
                     * TODO: Valued if this is feasible: First Parameter is a
                     * List of ijds which should be invited Second Parameter is
                     * a List of Projects which should be shared
                     */

                    ThreadUtils.runSafeAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            // TODO start invitation process;

                        }
                    });
                } else {
                    LOG.error("Error while starting session invitation.");
                    browser.run("alert('Session Invtiation is aborted.');");
                }
                return null;
            }
        }, new JavascriptFunction("__java_closeStartSessionWizard") {
            @Override
            public Object function(Object[] arguments) {
                dialogManager.closeDialogWindow(SessionWizardPage.WEB_PAGE);
                return null;
            }

        }, new JavascriptFunction("__java_renderAllAvailableContacts") {
            @Override
            public Object function(Object[] arguments) {
                // TODO Render only online contacts
                return null;
            }
        });

    }
}
