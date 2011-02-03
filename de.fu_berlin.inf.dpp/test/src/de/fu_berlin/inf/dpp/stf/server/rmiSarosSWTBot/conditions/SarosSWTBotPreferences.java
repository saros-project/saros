package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;

public class SarosSWTBotPreferences extends SWTBotPreferences {

    public static long SAROS_TIMEOUT = toLong(
        System.getProperty(KEY_TIMEOUT, "10000"), 10000);

    public static long SAROS_LONG_TIMEOUT = 20000;

    public static long SAROS_SHORT_TIMEOUT = 3000;

    private static long toLong(String timeoutValue, long defaultValue) {
        try {
            Long timeout = Long.valueOf(timeoutValue);
            return timeout.longValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
