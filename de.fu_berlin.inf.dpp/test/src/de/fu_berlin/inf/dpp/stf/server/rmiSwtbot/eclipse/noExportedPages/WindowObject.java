package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.saros.finder.SarosSWTBot;

public class WindowObject {
    private static final transient Logger log = Logger
        .getLogger(WindowObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private WaitUntilObject wUntil;
    private TableObject tableObject;
    private static SarosSWTBot bot = new SarosSWTBot();

    public WindowObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.wUntil = rmiBot.wUntilObject;
        this.tableObject = rmiBot.tableObject;
    }

    public String getCurrentActiveShell() {
        final SWTBotShell activeShell = bot.activeShell();
        return activeShell == null ? null : activeShell.getText();
    }

    public boolean isTableItemInWindowExist(String title, String label) {
        activateShellWithText(title);
        return tableObject.isTableItemExist(bot.shell(title).bot().table(),
            label);
    }

    public boolean activateShellWithText(String title) {
        SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().equals(title)) {
                log.debug("shell found");
                if (!shell.isActive()) {
                    shell.activate();
                }
                return true;
            }
        }
        log.error("No shell found matching \"" + title + "\"!");
        return false;
    }

}
