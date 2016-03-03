package de.fu_berlin.inf.dpp.ui.pages;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.ui.browser_functions.CloseSessionInvitationWizard;
import de.fu_berlin.inf.dpp.ui.browser_functions.GetValidJID;
import de.fu_berlin.inf.dpp.ui.browser_functions.SendInvitation;
import de.fu_berlin.inf.dpp.ui.renderer.ProjectListRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer;

/**
 * Represents the Saros Session invitation Wizard. This wizard is used to start
 * a Session by sharing Projects with multiple contacts.
 */
public class SessionWizardPage extends AbstractBrowserPage {

    public static final String HTML_DOC_NAME = "start-session-wizard.html";

    /**
     * Created by PicoContainer
     * 
     * @see HTMLUIContextFactory
     */
    public SessionWizardPage(

    StateRenderer stateRenderer,

    ProjectListRenderer projectListRenderer,

    CloseSessionInvitationWizard closeSessionInvitationWizard,

    SendInvitation sendInvitation,

    GetValidJID getValidJID) {

        super(HTML_DOC_NAME, HTMLUIStrings.START_SESSION_WIZARD_TITLE);

        this.addBrowserFunctions(closeSessionInvitationWizard, getValidJID,
            sendInvitation);
        this.addRenderer(stateRenderer, projectListRenderer);
    }
}
