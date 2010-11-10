//package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages;
//
//import org.apache.log4j.Logger;
//import org.eclipse.swtbot.swt.finder.waits.ICondition;
//
//import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
//import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosSWTBotPreferences;
//import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;
//
//public class WaitUntilObject {
//    private static final transient Logger log = Logger
//        .getLogger(WaitUntilObject.class);
//    private RmiSWTWorkbenchBot rmiBot;
//    private SarosSWTBot bot;
//
//    public WaitUntilObject(RmiSWTWorkbenchBot rmiBot) {
//        this.rmiBot = rmiBot;
//        this.bot = RmiSWTWorkbenchBot.delegate;
//
//    }
//
//    public void waitUntil(ICondition condition) {
//        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
//    }
// }
