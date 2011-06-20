package de.fu_berlin.inf.dpp.stf.server.bot;

import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;

public class SarosSWTBotPreferences extends SWTBotPreferences {

    public static final long SAROS_TIMEOUT;

    public static final long SAROS_LONG_TIMEOUT;

    public static final long SAROS_SHORT_TIMEOUT;

    public static final long SAROS_WIDGET_TIMEOUT;

    static {
        SAROS_WIDGET_TIMEOUT = 10 * 1000;
        SAROS_SHORT_TIMEOUT = 10 * 1000;
        SAROS_LONG_TIMEOUT = 60 * 1000;
        SAROS_TIMEOUT = fromSystemProperty(KEY_TIMEOUT, 20 * 1000);
    }

    private static long fromSystemProperty(String property, long def) {
        long r;
        try {
            r = Long.valueOf(System.getProperty(property));
        } catch (Exception e) {
            r = def;
        }
        return r;
    }
}
