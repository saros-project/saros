package de.fu_berlin.inf.dpp.ui.wizards.pages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "de.fu_berlin.inf.dpp.ui.wizards.pages.messages"; //$NON-NLS-1$
    public static String jid_example;
    public static String jid_explanation;
    public static String jid_format_errorMessage;
    public static String jid_longform;
    public static String password_empty_errorMessage;
    public static String xmpp_saros_restriction;
    public static String xmpp_saros_restriction_short;
    public static String roster_addself_errorMessage;
    public static String roster_alreadyadded_errorMessage;
    public static String wizard_finish_noeffect;
    public static String server_unresolvable_errorMessage;
    public static String account_exists_errorMessage;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // do nothing
    }
}
