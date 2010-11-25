package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;

public class SarosSWTBotPreferences extends SWTBotPreferences {

    public static long SAROS_TIMEOUT = toLong(
        System.getProperty(KEY_TIMEOUT, "15000"), 15000);

    private static long toLong(String timeoutValue, long defaultValue) {
        try {
            Long timeout = Long.valueOf(timeoutValue);
            return timeout.longValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
