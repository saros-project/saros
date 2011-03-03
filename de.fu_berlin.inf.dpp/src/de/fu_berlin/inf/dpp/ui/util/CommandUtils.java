package de.fu_berlin.inf.dpp.ui.util;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import de.fu_berlin.inf.dpp.util.Utils;

public class CommandUtils {

    private final static Logger log = Logger.getLogger(CommandUtils.class);

    /**
     * Refreshes all UI elements that display the given {@link Command}s.
     * 
     * @param commandIDs
     */
    public static void refreshUIElements(final String[] commandIDs) {
        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                ICommandService commandService = (ICommandService) PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow()
                    .getService(ICommandService.class);

                if (commandService != null) {
                    for (String commandID : commandIDs) {
                        commandService.refreshElements(commandID, null);
                    }
                }
            }
        });
    }

}
