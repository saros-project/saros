package de.fu_berlin.inf.dpp.stf.server.bot;

import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;

public class SarosSWTBotPreferences extends SWTBotPreferences {

  public static final long SAROS_DEFAULT_TIMEOUT;

  public static final long SAROS_LONG_TIMEOUT;

  public static final long SAROS_SHORT_TIMEOUT;

  static {
    SAROS_SHORT_TIMEOUT =
        fromSystemProperty("de.fu_berlin.inf.dpp.stf.server.bot.short.timeout", 10 * 1000);
    SAROS_DEFAULT_TIMEOUT =
        fromSystemProperty("de.fu_berlin.inf.dpp.stf.server.bot.default.timeout", 30 * 1000);
    SAROS_LONG_TIMEOUT =
        fromSystemProperty("de.fu_berlin.inf.dpp.stf.server.bot.long.timeout", 60 * 1000);
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
