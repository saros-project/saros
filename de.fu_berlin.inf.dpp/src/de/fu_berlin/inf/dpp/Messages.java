package de.fu_berlin.inf.dpp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "de.fu_berlin.inf.dpp.messages";
    public static String User_comparing;
    public static String User_invitation;
    public static String User_you;
    public static String Saros_tutorial_title;
    public static String Saros_tutorial_url;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
