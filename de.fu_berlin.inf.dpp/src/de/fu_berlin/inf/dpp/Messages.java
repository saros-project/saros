package de.fu_berlin.inf.dpp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "de.fu_berlin.inf.dpp.messages";
    public static String User_comparing;
    public static String Saros_tutorial_title;
    public static String Saros_tutorial_url;
    public static String Saros_connecting_smack_sasl_bug;
    public static String Saros_connecting_invalid_username_password;
    public static String Saros_connecting_sasl_required;
    public static String Saros_connecting_failed;
    public static String Saros_connecting_modify_account;
    public static String Saros_connecting_unknown_host;
    public static String Saros_connecting_internal_error;
    public static String Saros_connecting_error_title;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
