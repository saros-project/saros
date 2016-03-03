package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.pages.SessionWizardPage;

/**
 * Offers a via Javascript invokable method to close an open
 * {@link SessionWizardPage} Dialog.
 * <p>
 * JS-signature: "void __java_closeStartSessionWizard();"
 */
public class CloseSessionInvitationWizard extends JavascriptFunction {
    private DialogManager dialogManager;
    public static final String JS_NAME = "closeStartSessionWizard";

    /**
     * Created by PicoContainer
     * 
     * @param dialogManager
     * @see HTMLUIContextFactory
     */
    public CloseSessionInvitationWizard(DialogManager dialogManager) {
        super(NameCreator.getConventionName(JS_NAME));
        this.dialogManager = dialogManager;
    }

    @Override
    public Object function(Object[] arguments) {
        this.dialogManager.closeDialogWindow(SessionWizardPage.HTML_DOC_NAME);
        return null;
    }

}
