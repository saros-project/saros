package de.fu_berlin.inf.dpp.stf.server.bot;

import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;

public class SarosSWTBotPreferences extends SWTBotPreferences {

    public static long SAROS_TIMEOUT;

    public static long SAROS_LONG_TIMEOUT;

    public static long SAROS_SHORT_TIMEOUT;

    public static long SAROS_WIDGET_TIMEOUT;

    static {
        SAROS_WIDGET_TIMEOUT = 10 * 1000;
        SAROS_SHORT_TIMEOUT = 10 * 1000;
        SAROS_TIMEOUT = 20 * 1000;
        SAROS_LONG_TIMEOUT = 60 * 1000;

        try {
            SAROS_TIMEOUT = Long.valueOf(System.getProperty(KEY_TIMEOUT));
        } catch (Exception ignore) {
            // ignore
        }
    }
}
